package service.implementation;

import dao.DonationDao;
import dao.MemberDao; // For getDonationsByMemberName
import model.Donation;
import model.Member; // For getDonationsByMemberName
import service.DonationService;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class DonationServiceImpl extends UnicastRemoteObject implements DonationService {

    private final DonationDao donationDao;
    private final MemberDao memberDao; // For searching members by name

    public DonationServiceImpl() throws RemoteException {
        super();
        this.donationDao = new DonationDao();
        this.memberDao = new MemberDao(); // Initialize MemberDao
    }

    @Override
    public String recordDonation(Donation donation) throws RemoteException {
        if (donation == null || donation.getMember() == null || donation.getAmount() == null || donation.getDonationDate() == null) {
            return "Donation details are incomplete.";
        }
        if (donation.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Donation amount must be positive.";
        }
        return donationDao.saveDonation(donation);
    }

    @Override
    public Donation getDonationById(int donationId) throws RemoteException {
        return donationDao.getDonationById(donationId);
    }

    @Override
    public List<Donation> getAllDonations() throws RemoteException {
        return donationDao.getAllDonations();
    }

    @Override
    public List<Donation> getDonationsByMemberId(int memberId) throws RemoteException {
        return donationDao.getDonationsByMemberId(memberId);
    }

    @Override
    public List<Donation> getDonationsByMemberName(String memberName) throws RemoteException {
        // This requires searching members by name first, then their donations.
        // This could be inefficient if many members match the name.
        // A more optimized approach might involve a direct SQL query if performance is critical.
        List<Member> members = memberDao.searchMembersByName(memberName); // Assuming MemberDao has this method
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        List<Donation> allMemberDonations = new ArrayList<>();
        for (Member member : members) {
            List<Donation> donations = donationDao.getDonationsByMemberId(member.getMemberId());
            if (donations != null) {
                allMemberDonations.addAll(donations);
            }
        }
        // Ensure donations are distinct if a member could be part of multiple groups that might lead to overlap
        // or if the searchMembersByName could return duplicates that are then processed.
        // Using a stream for distinct based on the default equals/hashCode of Donation (or override them).
        // If Donation's equals/hashCode are not overridden based on ID, this might not work as expected for object distinctness.
        // For now, assume default or appropriate equals/hashCode.
        return allMemberDonations.stream().distinct().collect(Collectors.toList());
    }


    @Override
    public List<Donation> getDonationsByDateRange(Timestamp startDate, Timestamp endDate) throws RemoteException {
        if (startDate == null || endDate == null || startDate.after(endDate)) {
            // Consider throwing an IllegalArgumentException or returning empty list with a log
            return new ArrayList<>();
        }
        return donationDao.getDonationsByDateRange(startDate, endDate);
    }

    @Override
    public String updateDonation(Donation donation) throws RemoteException {
        if (donation == null || donation.getDonationId() == 0 || donation.getMember() == null || donation.getAmount() == null || donation.getDonationDate() == null) {
            return "Donation details for update are incomplete.";
        }
        if (donation.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Donation amount must be positive.";
        }
        // Ensure the member object is not null and has an ID if it's critical for the update
        if (donation.getMember().getMemberId() == 0 && donationDao.getDonationById(donation.getDonationId()).getMember() == null) {
             return "Member ID is missing for the donation update.";
        }
        return donationDao.updateDonation(donation);
    }

    @Override
    public String deleteDonation(int donationId) throws RemoteException {
        return donationDao.deleteDonation(donationId);
    }

    @Override
    public BigDecimal getTotalDonations() throws RemoteException {
        List<Donation> allDonations = donationDao.getAllDonations();
        if (allDonations == null) {
            return BigDecimal.ZERO;
        }
        return allDonations.stream()
                           .map(Donation::getAmount)
                           .filter(amount -> amount != null) // Ensure amount is not null
                           .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalDonationsByMemberId(int memberId) throws RemoteException {
        List<Donation> memberDonations = donationDao.getDonationsByMemberId(memberId);
        if (memberDonations == null) {
            return BigDecimal.ZERO;
        }
        return memberDonations.stream()
                              .map(Donation::getAmount)
                              .filter(amount -> amount != null) // Ensure amount is not null
                              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalDonationsForDateRange(Timestamp startDate, Timestamp endDate) throws RemoteException {
        if (startDate == null || endDate == null || startDate.after(endDate)) {
            return BigDecimal.ZERO;
        }
        List<Donation> donationsInRange = donationDao.getDonationsByDateRange(startDate, endDate);
        if (donationsInRange == null) {
            return BigDecimal.ZERO;
        }
        return donationsInRange.stream()
                               .map(Donation::getAmount)
                               .filter(amount -> amount != null) // Ensure amount is not null
                               .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
