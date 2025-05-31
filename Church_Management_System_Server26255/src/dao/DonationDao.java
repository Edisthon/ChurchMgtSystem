package dao;

import model.Donation;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query; // Import for Hibernate 5+ Query


import java.sql.Timestamp;
import java.util.List;

public class DonationDao {

    public String saveDonation(Donation donation) {
        Transaction transaction = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(donation);
            transaction.commit();
            return "Donation saved successfully.";
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "Error saving donation: " + e.getMessage();
        }
    }

    public Donation getDonationById(int donationId) {
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            // Eagerly fetch member to avoid LazyInitializationException if member is accessed after session closes
                Donation donation = (Donation) session.createQuery(
    "FROM Donation d JOIN FETCH d.member WHERE d.donationId = :id"
)
.setParameter("id", donationId)
.uniqueResult();
return donation;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Donation> getAllDonations() {
        try{
            Session session = HibernateUtil.getSessionFactory().openSession();
            // Use HQL (Hibernate Query Language)
            List<Donation> donations = session.createQuery(
    "FROM Donation d JOIN FETCH d.member ORDER BY d.donationDate DESC"
).list();

return donations;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Donation> getDonationsByMemberId(int memberId) {
        try{
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
    "FROM Donation d JOIN FETCH d.member WHERE d.member.memberId = :memberId ORDER BY d.donationDate DESC"
);
query.setParameter("memberId", memberId);

List<Donation> donations = query.list(); // cast if needed
return donations;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Donation> getDonationsByDateRange(Timestamp startDate, Timestamp endDate) {
        try{
            Session session = HibernateUtil.getSessionFactory().openSession();
Query query = session.createQuery(
    "FROM Donation d JOIN FETCH d.member WHERE d.donationDate BETWEEN :startDate AND :endDate ORDER BY d.donationDate DESC"
);
query.setParameter("startDate", startDate);
query.setParameter("endDate", endDate);

@SuppressWarnings("unchecked")
List<Donation> donations = (List<Donation>) query.list();
return donations;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String updateDonation(Donation donation) {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.update(donation); // Use update for existing entities
            transaction.commit();
            return "Donation updated successfully.";
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "Error updating donation: " + e.getMessage();
        }
    }

    public String deleteDonation(int donationId) {
        Transaction transaction = null;
        Donation donationToDelete = null;
        try  {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            donationToDelete = (Donation) session.get(Donation.class, donationId); // Fetch first
            if (donationToDelete != null) {
                session.delete(donationToDelete);
                transaction.commit();
                return "Donation deleted successfully.";
            } else {
                if(transaction != null && transaction.isActive()) transaction.rollback(); // Rollback if not found
                return "Donation not found with ID: " + donationId;
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return "Error deleting donation: " + e.getMessage();
        }
    }
}
