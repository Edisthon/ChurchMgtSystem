package dao;

import dao.HibernateUtil;
import model.Event;
import java.util.List; // Example for custom methods
import org.hibernate.Session;
import org.hibernate.Transaction;

public class EventDao{
    
    public String registerEvent(Event event){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(event);
            tr.commit();
            ss.close();
            return "Data saved succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String updateEvent(Event event){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(event);
            tr.commit();
            ss.close();
            return "Data updated succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String deleteEvent(Event event){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(event);
            tr.commit();
            ss.close();
            return "Data deleted succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     
      public List<Event> retreiveAll(){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        List<Event> eventList=ss.createQuery("select evt from"
                + "Event evt").list();
        ss.close();
        return eventList;
    }
    public Event retrieveById(Event event){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        Event events=(Event)ss.get(Event.class,event.getEventId());
        ss.close();
        return events;
    }
}
