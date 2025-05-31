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
   
    public String registerMember(Member member) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Member memberToDelete = session.get(Member.class, memberId);
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Corrected HQL query and using try-with-resources
            Query<Member> query = session.createQuery("FROM Member mem", Member.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    public Member retrieveById(int memberId) { // Changed parameter to int
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Member.class, memberId);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null on error or if not found
        }
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
