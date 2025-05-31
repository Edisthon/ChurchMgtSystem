package dao;

import util.HibernateUtil; // Assuming this is the correct location
import model.Accounts;
import java.util.List;
import java.util.ArrayList; // For returning empty list on error
import java.sql.Timestamp; // For OTP expiry
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query; // For typed queries

public class AccountsDao {
    
    public String registerAccounts(Accounts accounts) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(accounts);
            transaction.commit();
            return "Account registered successfully.";
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "Error registering account: " + e.getMessage();
        }
    }

    public String updateAccounts(Accounts accounts) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(accounts); // Corrected to update
            transaction.commit();
            return "Account updated successfully.";
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "Error updating account: " + e.getMessage();
        }
    }

    public String deleteAccounts(int accountId) { // Changed to take ID
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Accounts account = session.get(Accounts.class, accountId);
            if (account != null) {
                session.delete(account); // Corrected to delete
                transaction.commit();
                return "Account deleted successfully.";
            } else {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                return "Account not found with ID: " + accountId;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "Error deleting account: " + e.getMessage();
        }
    }
     
    public List<Accounts> retreiveAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Corrected HQL and using typed query
            Query<Accounts> query = session.createQuery("FROM Accounts acc", Accounts.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    public Accounts retrieveById(int accountId) { // Changed parameter to int
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Accounts.class, accountId);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null on error or if not found
        }
    }

    public Accounts getAccountByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Accounts> query = session.createQuery(
                "FROM Accounts WHERE username = :username", Accounts.class);
            query.setParameter("username", username);
            return query.uniqueResultOptional().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String updateOtpForAccount(int accountId, String otpCode, Timestamp otpExpiryTime) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Accounts account = session.get(Accounts.class, accountId);
            if (account != null) {
                account.setOtpCode(otpCode);
                account.setOtpExpiryTime(otpExpiryTime);
                session.update(account);
                transaction.commit();
                return "OTP details updated successfully for account ID: " + accountId;
            } else {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                return "Account not found with ID: " + accountId;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "Error updating OTP details: " + e.getMessage();
        }
    }
}
