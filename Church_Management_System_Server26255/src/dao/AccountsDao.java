package dao;

import dao.HibernateUtil;
import model.Accounts;
import java.util.List; // Example for custom methods
import org.hibernate.Session;
import org.hibernate.Transaction;

public class AccountsDao {
    
    public String registerAccounts(Accounts accounts){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(accounts);
            tr.commit();
            ss.close();
            return "Data saved succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String updateAccounts(Accounts accounts){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(accounts);
            tr.commit();
            ss.close();
            return "Data updated succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String deleteAccounts(Accounts accounts){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(accounts);
            tr.commit();
            ss.close();
            return "Data deleted succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     
      public List<Accounts> retreiveAll(){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        List<Accounts> accountsList=ss.createQuery("select acc from"
                + "Accounts acc").list();
        ss.close();
        return accountsList;
    }
    public Accounts retrieveById(Accounts accounts){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        Accounts accountss=(Accounts)ss.get(Accounts.class,accounts.getAccountId());
        ss.close();
        return accountss;
    }
}
