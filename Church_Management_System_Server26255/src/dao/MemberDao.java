package dao;

import dao.HibernateUtil;
import model.Member;
import java.util.List;
import java.util.ArrayList; // Added for searchMembersByName
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query; // Added for searchMembersByName
import util.HibernateUtil; // Corrected import assuming HibernateUtil is in util

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
        // Corrected HQL query
        List<Member> memberList=ss.createQuery("FROM Member mem", Member.class).list();
        ss.close();
        return memberList;
    }
    public Member retrieveById(Member member){ // Consider changing parameter to int memberId
        Session ss= HibernateUtil.getSessionFactory().openSession();
        Member members=(Member)ss.get(Member.class,member.getMemberId());
        ss.close();
        return members;
    }

    public List<Member> searchMembersByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Member> query = session.createQuery(
                "FROM Member m WHERE lower(m.fullName) LIKE lower(:name)", Member.class);
            query.setParameter("name", "%" + name + "%"); // Wildcards for contains search
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }
}
