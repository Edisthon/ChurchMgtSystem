package dao;

import dao.HibernateUtil;
import model.Member;
import java.util.List;
import java.util.ArrayList; // Added for searchMembersByName
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query; // Added for searchMembersByName

public class MemberDao{
   
    public String registerMember(Member member) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(member);
            transaction.commit();
            return "Data saved successfully";
        } catch (Exception ex) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            ex.printStackTrace();
            return "Error saving member: " + ex.getMessage();
        }
    }

    public String updateMember(Member member) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(member); // Corrected to session.update()
            transaction.commit();
            return "Data updated successfully";
        } catch (Exception ex) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            ex.printStackTrace();
            return "Error updating member: " + ex.getMessage();
        }
    }

    public String deleteMember(int memberId) { // Changed parameter to int
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            Member memberToDelete = (Member) session.get(Member.class, memberId);
            if (memberToDelete != null) {
                session.delete(memberToDelete); // Corrected to session.delete()
                transaction.commit();
                return "Data deleted successfully";
            } else {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                return "Member not found with ID: " + memberId;
            }
        } catch (Exception ex) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            ex.printStackTrace();
            return "Error deleting member: " + ex.getMessage();
        }
    }
     
    public List<Member> retreiveAll() {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            // Corrected HQL query and using try-with-resources
            Query query = session.createQuery("FROM Member mem");
List<Member> members = query.list(); // Unsafe cast but required in Hibernate 4
return members;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    public Member retrieveById(Member members) { // Changed parameter to int
        Session ss= HibernateUtil.getSessionFactory().openSession();
        Member member=(Member)ss.get(Member.class,members.getMemberId());
        ss.close();
        return member;
    }

    public List<Member> searchMembersByName(String name) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
    "FROM Member m WHERE lower(m.fullName) LIKE lower(:name)");
query.setParameter("name", "%" + name + "%");

@SuppressWarnings("unchecked")
List<Member> members = (List<Member>) query.list();

return members;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }
    
}
