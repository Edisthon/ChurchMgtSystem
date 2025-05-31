package dao;

import dao.HibernateUtil;
import model.Groups;
import java.util.List; // Example for custom methods
import org.hibernate.Session;
import org.hibernate.Transaction;

public class GroupsDao {
   
    public String registerGroups(Groups groups){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(groups);
            tr.commit();
            ss.close();
            return "Data saved succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String updateGroups(Groups groups){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(groups);
            tr.commit();
            ss.close();
            return "Data updated succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String deleteGroups(Groups groups){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(groups);
            tr.commit();
            ss.close();
            return "Data deleted succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     
      public List<Groups> retreiveAll(){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        List<Groups> groupsList=ss.createQuery("select grp from"
                + "Groups grp").list();
        ss.close();
        return groupsList;
    }
    public Groups retrieveById(Groups groups){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        Groups groupss=(Groups)ss.get(Groups.class,groups.getGroupId());
        ss.close();
        return groupss;
    }
}
