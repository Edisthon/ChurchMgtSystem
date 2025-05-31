package dao;

import dao.HibernateUtil;
import model.EventAttendance;
import java.util.List; // Example for custom methods
import org.hibernate.Session;
import org.hibernate.Transaction;

public class EventAttendanceDao {
    
    public String registerEventAttendance(EventAttendance eventAttendance){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(eventAttendance);
            tr.commit();
            ss.close();
            return "Data saved succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String updateEventAttendance(EventAttendance eventAttendance){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(eventAttendance);
            tr.commit();
            ss.close();
            return "Data updated succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String deleteEventAttendance(EventAttendance eventAttendance){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(eventAttendance);
            tr.commit();
            ss.close();
            return "Data deleted succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     
      public List<EventAttendance> retreiveAll(){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        List<EventAttendance> eventAttendanceList=ss.createQuery("select evt from"
                + "EventAttendance evt").list();
        ss.close();
        return eventAttendanceList;
    }
    public EventAttendance retrieveById(EventAttendance eventAttendance){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        EventAttendance eventAttendances=(EventAttendance)ss.get(EventAttendance.class,eventAttendance.getAttendanceId());
        ss.close();
        return eventAttendances;
    }
}
