package service.implementation;

import model.Event;
import service.EventService;
import dao.EventDao; // Using Impl directly for now
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


public class EventImpl extends  UnicastRemoteObject implements EventService {

    private EventDao eventDao;

    public EventImpl() throws RemoteException{
        super();
    }

 

    @Override
    public String registerEvent(Event events) throws RemoteException {
            return eventDao.registerEvent(events);
        }

    @Override
    public String updateEvent(Event events) throws RemoteException {
        return eventDao.updateEvent(events);
    }

    @Override
    public String deleteEvent(Event events) throws RemoteException {
        return eventDao.deleteEvent(events);
    }

    @Override
    public List<Event> retreiveAll() throws RemoteException {
        return eventDao.retreiveAll();
    }

    @Override
    public Event retrieveById(Event event) throws RemoteException {
        return eventDao.retrieveById(event);
    }

    @Override
    public Member getMemberById(int userId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
