package service;

import java.rmi.RemoteException;
import model.Event;
import java.util.List;
import java.sql.Date; // For Member birthdate
import java.sql.Timestamp; // For Event eventDateTime & EventAttendance checkInTime
import model.Event;
import model.Member;

public interface EventService {
    public String  registerEvent(Event events) throws RemoteException;
    public  String  updateEvent(Event events) throws RemoteException;
    public  String  deleteEvent(Event events) throws RemoteException;
    public List<Event> retreiveAll() throws RemoteException;
    public Event retrieveById(Event accounts) throws RemoteException;
    Member getMemberById(int userId) throws RemoteException;
    
}
