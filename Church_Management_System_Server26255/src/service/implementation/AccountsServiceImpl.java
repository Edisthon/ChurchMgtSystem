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
import java.util.stream.Collectors;
import model.Member;


public class AccountsServiceImpl extends UnicastRemoteObject implements AccountsService {

    private AccountsDao accountsDao;

    public AccountsServiceImpl() throws RemoteException{
        super();
    }
    

    @Override
    public String registerAccounts(Accounts accountss) throws RemoteException {
            return accountsDao.registerAccounts(accountss);
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
    public Member getMemberById(int userId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
