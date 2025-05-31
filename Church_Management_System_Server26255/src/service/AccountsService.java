package service;

import java.rmi.RemoteException;
import model.Accounts;
import java.util.List;
import java.sql.Date; // For Member birthdate
import java.sql.Timestamp; // For Event eventDateTime & EventAttendance checkInTime
import model.Member;

public interface AccountsService {
    public String  registerAccounts(Accounts accountss) throws RemoteException;
    public  String  updateAccounts(Accounts accountss) throws RemoteException;
    public  String  deleteAccounts(Accounts accountss) throws RemoteException;
    public List<Accounts> retreiveAll() throws RemoteException;
    public Accounts retrieveById(Accounts accounts) throws RemoteException;
    Member getMemberById(int userId) throws RemoteException;
}
