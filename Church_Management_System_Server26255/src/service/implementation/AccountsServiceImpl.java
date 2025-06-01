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


public class AccountsServiceImpl extends UnicastRemoteObject implements AccountsService {

    private AccountsDao accountsDao;

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
            return "Error: Username cannot be empty.";
        }
        if (this.accountsDao == null) {
            System.err.println("AccountsServiceImpl.requestOtp: accountsDao is null!");
            throw new RemoteException("Account service is not properly initialized.");
        }

        Accounts account = this.accountsDao.getAccountByUsername(username);

        if (account == null) {
            return "Error: User not found.";
        }

        // Generate OTP
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000); // 6-digit OTP
        String otpCode = String.valueOf(otpValue);

        // Set OTP expiry time (e.g., 5 minutes from now)
        Timestamp otpExpiryTime = Timestamp.from(Instant.now().plus(5, ChronoUnit.MINUTES));

        // Update OTP in database
        // This assumes Accounts model has setOtpCode and setOtpExpiryTime methods
        String updateStatus = this.accountsDao.updateOtpForAccount(account.getAccountId(), otpCode, otpExpiryTime);

        if (!updateStatus.startsWith("OTP details updated successfully")) {
            return "Error: Could not store OTP details. " + updateStatus;
        }

        // Send email with OTP
        // The username field is assumed to be the email address for the account
        boolean emailSent = EmailUtil.sendOtpEmail(account.getUsername(), otpCode);

        if (emailSent) {
            return "OTP has been sent to your registered email address.";
        } else {
            // Even if email fails, OTP is stored. For critical systems, might rollback or have retry.
            // For now, inform user of email failure but OTP is technically set.
            // Consider logging this failure more robustly on the server.
            return "OTP generated and stored, but failed to send email. Please contact support or check your email server configuration.";
        }
    }

    @Override
    public Accounts verifyOtpAndLogin(String username, String otp) throws RemoteException {
        if (username == null || username.trim().isEmpty() ||
            otp == null || otp.trim().isEmpty()) {
            throw new RemoteException("Username and OTP cannot be empty.");
        }
        if (this.accountsDao == null) {
            System.err.println("AccountsServiceImpl.verifyOtpAndLogin: accountsDao is null!");
            throw new RemoteException("Account service is not properly initialized.");
        }

        Accounts account = this.accountsDao.getAccountByUsername(username);

        if (account == null) {
            return null; // User not found
        }

        // Step 2: Verify OTP (existing logic)
        // Assumes Accounts model has getOtpCode() and getOtpExpiryTime()
        String storedOtp = account.getOtpCode();
        Timestamp otpExpiryTime = account.getOtpExpiryTime();

        if (storedOtp == null || storedOtp.trim().isEmpty() || !storedOtp.equals(otp)) {
            return null; // OTP does not match or not found
        }

        if (otpExpiryTime == null || Timestamp.from(Instant.now()).after(otpExpiryTime)) {
            // OTP has expired
            this.accountsDao.updateOtpForAccount(account.getAccountId(), null, null); // Clear expired OTP
            return null;
        }

        // OTP is valid and not expired
        this.accountsDao.updateOtpForAccount(account.getAccountId(), null, null); // Clear used OTP
        return account; // Return the full account object
    }
}
