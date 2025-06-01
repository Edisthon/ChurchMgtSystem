package service;

import java.rmi.Remote; // Added for RMI
import java.rmi.RemoteException;
import model.Accounts; // Ensure client-side model.Accounts is used
import java.util.List;
// Assuming java.sql.Date and java.sql.Timestamp might be used by Accounts model.
import java.sql.Date;
import java.sql.Timestamp;
// model.Member import removed

public interface AccountsService extends Remote { // Extended Remote
    public String  registerAccounts(Accounts accountss) throws RemoteException;
    public  String  updateAccounts(Accounts accountss) throws RemoteException;
    public  String  deleteAccounts(Accounts accountss) throws RemoteException; // Consider taking int accountId
    public List<Accounts> retreiveAll() throws RemoteException;
    public Accounts retrieveById(Accounts accounts) throws RemoteException; // Consider taking int accountId
    // Member getMemberById(int userId) throws RemoteException; // Removed

    String requestOtp(String username) throws RemoteException;
    model.Accounts verifyOtpAndLogin(String username, String otp) throws RemoteException;
}
