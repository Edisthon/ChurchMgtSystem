package service.implementation;

import model.Groups;
import service.GroupsService;
import dao.GroupsDao; // Using Impl directly for now
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
                                                       // In a real app, use dependency injection

import java.util.List;
import java.sql.Date; // For Member birthdate
import java.sql.Timestamp; // For Groups groupsDateTime & GroupsAttendance checkInTime
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import model.Member;


public class GroupsImpl extends UnicastRemoteObject implements GroupsService {

    private GroupsDao groupsDao;

    public GroupsImpl() throws RemoteException{
        super();
    } 

    @Override
    public String registerGroups(Groups groupss) throws RemoteException {
            return groupsDao.registerGroups(groupss);
        }

    @Override
    public String updateGroups(Groups groupss) throws RemoteException {
        return groupsDao.updateGroups(groupss);
    }

    @Override
    public String deleteGroups(Groups groupss) throws RemoteException {
        return groupsDao.deleteGroups(groupss);
    }

    @Override
    public List<Groups> retreiveAll() throws RemoteException {
        return groupsDao.retreiveAll();
    }

    @Override
    public Groups retrieveById(Groups groups) throws RemoteException {
        return groupsDao.retrieveById(groups);
    }

    @Override
    public Member getMemberById(int userId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
