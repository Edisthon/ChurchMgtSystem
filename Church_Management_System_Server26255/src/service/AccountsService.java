package service;

import java.rmi.Remote; // Added for RMI
import java.rmi.RemoteException;
import model.Accounts;
import java.util.List;
// Assuming java.sql.Date and java.sql.Timestamp might be used by Accounts model.
import java.sql.Date;
import java.sql.Timestamp;
// model.Member import removed as getMemberById is removed.

public interface AccountsService extends Remote { // Extended Remote
    public String  registerAccounts(Accounts accountss) throws RemoteException;
    public  String  updateAccounts(Accounts accountss) throws RemoteException;
    public  String  deleteAccounts(Accounts accountss) throws RemoteException; // Consider taking int accountId
    public List<Accounts> retreiveAll() throws RemoteException;
    public Accounts retrieveById(Accounts accounts) throws RemoteException; // Consider taking int accountId
    // Member getMemberById(int userId) throws RemoteException; // Removed, seems out of place

    String requestOtp(String username) throws RemoteException; // Added method

    // Renamed and signature updated to include password for verification
    Accounts verifyOtpAndLogin(String username, String password, String otp) throws RemoteException;
}
