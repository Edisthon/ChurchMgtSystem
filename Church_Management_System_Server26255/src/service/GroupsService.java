package service;

import java.rmi.RemoteException;
import model.Groups;
import java.util.List;
import java.sql.Date; // For Member birthdate
import java.sql.Timestamp; // For Event eventDateTime & EventAttendance checkInTime
import model.Groups;
import model.Member;

public interface GroupsService {
    public String  registerGroups(Groups groupss) throws RemoteException;
    public  String  updateGroups(Groups groupss) throws RemoteException;
    public  String  deleteGroups(Groups groupss) throws RemoteException;
    public List<Groups> retreiveAll() throws RemoteException;
    public Groups retrieveById(Groups groups) throws RemoteException;
    Member getMemberById(int userId) throws RemoteException;   
}