package dao;

import dao.HibernateUtil;
import model.Member;
import java.util.List; // Example for custom methods
import org.hibernate.Session;
import org.hibernate.Transaction;

public class MemberDao{
   
    public String registerMember(Member member){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(member);
            tr.commit();
            ss.close();
            return "Data saved succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String updateMember(Member member){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(member);
            tr.commit();
            ss.close();
            return "Data updated succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     public String deleteMember(Member member){
        try{
            //1. Create a Session
            Session ss= HibernateUtil.getSessionFactory().openSession();
            //2.Create a transaction
            Transaction tr= ss.beginTransaction();
            ss.save(member);
            tr.commit();
            ss.close();
            return "Data deleted succesfully";
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
     
      public List<Member> retreiveAll(){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        List<Member> memberList=ss.createQuery("select mem from"
                + "Member mem").list();
        ss.close();
        return memberList;
    }
    public Member retrieveById(Member member){
        Session ss= HibernateUtil.getSessionFactory().openSession();
        Member members=(Member)ss.get(Member.class,member.getMemberId());
        ss.close();
        return members;
    }
}
