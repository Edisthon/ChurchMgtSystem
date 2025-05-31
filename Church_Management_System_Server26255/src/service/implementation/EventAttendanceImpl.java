package service.implementation;

import model.EventAttendance;
import service.EventAttendanceService;
import dao.EventAttendanceDao; // Using Impl directly for now
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


public class EventAttendanceImpl extends UnicastRemoteObject implements EventAttendanceService {

    private EventAttendanceDao eventAttendanceDao;

    public EventAttendanceImpl() throws RemoteException{
        super();
    }   

    @Override
    public String registerEventAttendance(EventAttendance eventAttendances) throws RemoteException {
            return eventAttendanceDao.registerEventAttendance(eventAttendances);
        }

    @Override
    public String updateEventAttendance(EventAttendance eventAttendances) throws RemoteException {
        return eventAttendanceDao.updateEventAttendance(eventAttendances);
    }

    @Override
    public String deleteEventAttendance(EventAttendance eventAttendances) throws RemoteException {
        return eventAttendanceDao.deleteEventAttendance(eventAttendances);
    }

    @Override
    public List<EventAttendance> retreiveAll() throws RemoteException {
        return eventAttendanceDao.retreiveAll();
    }

    @Override
    public EventAttendance retrieveById(EventAttendance eventAttendance) throws RemoteException {
        return eventAttendanceDao.retrieveById(eventAttendance);
    }

    @Override
    public Member getMemberById(int userId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
