package service.implementation;

import model.Accounts;
import service.AccountsService;
import dao.AccountsDao; // Using Impl directly for now
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
                                                       // In a real app, use dependency injection

import java.util.List;
import java.sql.Date; // For Member birthdate
import java.sql.Timestamp; // For Event eventDateTime & EventAttendance checkInTime
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
// import java.util.stream.Collectors; // Not directly used in this version
// import model.Member; // No longer needed after removing getMemberById
import util.EmailUtil; // Added for email sending
import util.PasswordUtil; // Added for password hashing
import java.util.Map;
import java.util.HashMap;


public class AccountsServiceImpl extends UnicastRemoteObject implements AccountsService {

    private AccountsDao accountsDao;

    // Inner class for storing OTP data
    private static class PendingOtpData {
        String otpCode;
        Timestamp expiryTime;

        PendingOtpData(String otpCode, Timestamp expiryTime) {
            this.otpCode = otpCode;
            this.expiryTime = expiryTime;
        }
    }

    // Static map for temporary OTP storage
    private static final Map<String, PendingOtpData> pendingOtps = new HashMap<>();

    public AccountsServiceImpl() throws RemoteException{
        super();
        this.accountsDao = new AccountsDao(); // Initialize accountsDao
    }
    

    @Override
    public String registerAccounts(Accounts account) throws RemoteException { // Renamed param for clarity
        if (account == null || account.getUsername() == null || account.getPasswordHash() == null || account.getPasswordHash().isEmpty()) {
            return "Error: Account details (username, password) are incomplete.";
        }
        // Assuming account.getPasswordHash() currently holds the plain password from client for registration
        String plainPassword = account.getPasswordHash();
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        if (hashedPassword == null) {
            return "Error: Password hashing failed."; // Should not happen with valid input to hashPassword
        }
        account.setPasswordHash(hashedPassword); // Set the hashed password before saving

        return accountsDao.registerAccounts(account);
    }

    @Override
    public String updateAccounts(Accounts accountss) throws RemoteException {
        return accountsDao.updateAccounts(accountss);
    }

    @Override
    public String deleteAccounts(Accounts accountss) throws RemoteException {
        return accountsDao.deleteAccounts(accountss);
    }

    @Override
    public List<Accounts> retreiveAll() throws RemoteException {
        return accountsDao.retreiveAll();
    }

    @Override
    public Accounts retrieveById(Accounts accounts) throws RemoteException {
        return accountsDao.retrieveById(accounts);
    }

    @Override
    // public Member getMemberById(int userId) throws RemoteException {
    //    throw new UnsupportedOperationException("Not supported yet."); // Removed as it's not in the interface
    // }

    @Override
    public String requestOtp(String username) throws RemoteException {
        if (username == null || username.trim().isEmpty()) {
            return "Error: Email address cannot be empty.";
        }
        // The 'username' parameter is treated as the email address.
        String email = username.trim();

        // Generate OTP
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000); // 6-digit OTP
        String otpCode = String.valueOf(otpValue);

        // Set OTP expiry time (e.g., 5 minutes from now)
        Timestamp otpExpiryTime = Timestamp.from(Instant.now().plus(5, ChronoUnit.MINUTES));

        // Store OTP temporarily
        synchronized (pendingOtps) { // Synchronize access to the map
            pendingOtps.put(email, new PendingOtpData(otpCode, otpExpiryTime));
        }

        // Send email with OTP
        boolean emailSent = EmailUtil.sendOtpEmail(email, otpCode);

        if (emailSent) {
            return "OTP has been sent to " + email + ".";
        } else {
            // If email sending fails, we might want to remove the OTP from pendingOtps
            // or let it expire naturally to avoid confusion. For now, let it expire.
            // Log this failure robustly on the server.
            System.err.println("Failed to send OTP email to " + email + " for OTP " + otpCode);
            return "OTP generated, but failed to send email. Please check server logs or contact support.";
        }
    }

    @Override
    public Accounts verifyOtpAndLogin(String username, String otp) throws RemoteException {
        if (username == null || username.trim().isEmpty() || otp == null || otp.trim().isEmpty()) {
            // Consider throwing an IllegalArgumentException or returning a specific error DTO
            // For now, returning null or throwing RemoteException for simplicity with RMI.
            System.err.println("verifyOtpAndLogin: Username or OTP is empty.");
            return null; // Or throw new RemoteException("Username and OTP cannot be empty.");
        }
        String email = username.trim();
        String providedOtp = otp.trim();

        // 1. Verify OTP from temporary storage
        PendingOtpData storedOtpData;
        synchronized (pendingOtps) { // Synchronize access
            storedOtpData = pendingOtps.get(email);
        }

        if (storedOtpData == null) {
            System.err.println("verifyOtpAndLogin: No OTP found for email: " + email);
            return null; // OTP not found for this email (maybe never requested or already used/expired from map)
        }

        if (!storedOtpData.otpCode.equals(providedOtp)) {
            System.err.println("verifyOtpAndLogin: Provided OTP does not match stored OTP for email: " + email);
            // Optional: Implement attempt counter / OTP invalidation after too many failed attempts for an email
            return null; // OTP mismatch
        }

        if (Timestamp.from(Instant.now()).after(storedOtpData.expiryTime)) {
            System.err.println("verifyOtpAndLogin: OTP expired for email: " + email);
            synchronized (pendingOtps) { // Synchronize access
                pendingOtps.remove(email); // Remove expired OTP
            }
            return null; // OTP expired
        }

        // OTP is valid, remove it from temporary storage to prevent reuse
        synchronized (pendingOtps) { // Synchronize access
            pendingOtps.remove(email);
        }

        // 2. Account Handling: Check if account exists or create a new one
        if (this.accountsDao == null) {
            System.err.println("AccountsServiceImpl.verifyOtpAndLogin: accountsDao is null!");
            throw new RemoteException("Account service is not properly initialized for DAO access.");
        }

        Accounts account = this.accountsDao.getAccountByUsername(email);

        if (account != null) {
            // Account exists, login successful
            System.out.println("Existing user logged in: " + email);
            return account;
        } else {
            // Account does not exist, create a new one
            System.out.println("New user registration via OTP: " + email);
            Accounts newAccount = new Accounts();
            newAccount.setUsername(email);
            newAccount.setRole("USER"); // Set a default role

            // Generate a strong, random password for the password_hash field
            // as the schema requires it to be not null. This password won't be used by the user.
            SecureRandom sr = new SecureRandom();
            byte[] randomBytes = new byte[32]; // Generate 32 random bytes
            sr.nextBytes(randomBytes);
            String randomPassword = new java.math.BigInteger(1, randomBytes).toString(16); // Convert to hex string

            String hashedPassword = PasswordUtil.hashPassword(randomPassword);
            if (hashedPassword == null) {
                // This should ideally not happen if PasswordUtil is robust
                System.err.println("verifyOtpAndLogin: Failed to hash generated password for new user: " + email);
                throw new RemoteException("Failed to create new user account due to password hashing error.");
            }
            newAccount.setPasswordHash(hashedPassword);

            // Set OTP fields to null for new accounts as OTP has been consumed
            newAccount.setOtpCode(null);
            newAccount.setOtpExpiryTime(null);

            String registrationStatus = this.accountsDao.registerAccounts(newAccount);
            if (registrationStatus.toLowerCase().startsWith("error")) {
                 System.err.println("verifyOtpAndLogin: Failed to register new account for " + email + ": " + registrationStatus);
                 // If registration fails, the user is not logged in.
                 // Depending on the error, you might throw an exception or return null.
                 // For instance, if the error is due to a unique constraint violation (shouldn't happen if getAccountByUsername was accurate)
                 // or other DB issues.
                 return null;
            }

            // After successful registration, refetch the account to get the accountId generated by the DB
            // or ensure registerAccounts populates it (JPA usually does).
            // For simplicity, assuming registerAccounts populates the ID or we can retrieve it.
            // If AccountsDao.registerAccounts returns the saved entity or its ID, that would be better.
            // For now, let's re-fetch. This is not ideal for performance.
            Accounts registeredAccount = this.accountsDao.getAccountByUsername(email);
            if(registeredAccount == null){
                 System.err.println("verifyOtpAndLogin: Critical error. Account for " + email + " registered but could not be retrieved immediately.");
                 // This case should be rare but indicates a potential issue in DAO or transaction handling.
                 return null;
            }
            System.out.println("New user registered and logged in: " + email + " with Account ID: " + registeredAccount.getAccountId());
            return registeredAccount;
        }
    }
}
