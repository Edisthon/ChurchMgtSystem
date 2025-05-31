package service;

import java.rmi.RemoteException;
import model.EventAttendance;
import java.util.List;
import java.sql.Date; // For Member birthdate
import java.sql.Timestamp; // For Event eventDateTime & EventAttendance checkInTime
import model.Member;

public interface EventAttendanceService {
   
    public String  registerEventAttendance(EventAttendance products) throws RemoteException;
    public  String  updateEventAttendance(EventAttendance products) throws RemoteException;
    public  String  deleteEventAttendance(EventAttendance products) throws RemoteException;
    public List<EventAttendance> retreiveAll() throws RemoteException;
    public EventAttendance retrieveById(EventAttendance product) throws RemoteException;
    Member getMemberById(int userId) throws RemoteException;
}
