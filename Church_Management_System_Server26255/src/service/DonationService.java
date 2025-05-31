package service;

import model.Donation;
import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.List;

public interface DonationService extends Remote {

    String recordDonation(Donation donation) throws RemoteException;

    Donation getDonationById(int donationId) throws RemoteException;

    List<Donation> getAllDonations() throws RemoteException;

    List<Donation> getDonationsByMemberId(int memberId) throws RemoteException;

    List<Donation> getDonationsByMemberName(String memberName) throws RemoteException; // Added for searching by name

    List<Donation> getDonationsByDateRange(Timestamp startDate, Timestamp endDate) throws RemoteException;

    String updateDonation(Donation donation) throws RemoteException;

    String deleteDonation(int donationId) throws RemoteException;

    BigDecimal getTotalDonations() throws RemoteException;

    BigDecimal getTotalDonationsByMemberId(int memberId) throws RemoteException;

    BigDecimal getTotalDonationsForDateRange(Timestamp startDate, Timestamp endDate) throws RemoteException;

}
