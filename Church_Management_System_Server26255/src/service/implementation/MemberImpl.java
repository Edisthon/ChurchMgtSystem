package service.implementation;

import model.Member;
import service.MemberService;
import dao.MemberDao; // Using Impl directly for now
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
                                                       // In a real app, use dependency injection

import java.util.List;
import java.sql.Date; // For Member birthdate
import java.sql.Timestamp; // For Member memberDateTime & MemberAttendance checkInTime
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import model.Member;


public class MemberImpl extends UnicastRemoteObject implements MemberService {

    private MemberDao memberDao;

    public MemberImpl() throws RemoteException{
         super();
         this.memberDao = new MemberDao(); // Initialize memberDao
    }

    @Override
    public String registerMember(Member members) throws RemoteException {
            return memberDao.registerMember(members);
        }

    @Override
    public String updateMember(Member members) throws RemoteException {
        return memberDao.updateMember(members);
    }

    @Override
    public String deleteMember(int memberID) throws RemoteException {
        return memberDao.deleteMember(memberID);
    }

    @Override
    public List<Member> retreiveAll() throws RemoteException {
        return memberDao.retreiveAll();
    }

    @Override
    public Member retrieveById(int memberID) throws RemoteException {
        return memberDao.retrieveById(memberID);
    }

    @Override
    public Member getMemberById(int userId) throws RemoteException {
        if (this.memberDao == null) {
            System.err.println("MemberImpl.getMemberById: memberDao is null!");
            throw new RemoteException("Member service is not properly initialized.");
        }
        // Assuming MemberDao.retrieveById was correctly refactored to take an int ID.
        return this.memberDao.retrieveById(userId);
    }

    // Assuming searchMembersByName will be added to MemberService interface
    @Override
    public List<Member> searchMembersByName(String name) throws RemoteException {
        if (this.memberDao == null) {
            System.err.println("MemberImpl.searchMembersByName: memberDao is null!");
            throw new RemoteException("Member service is not properly initialized.");
        }
        return this.memberDao.searchMembersByName(name);
    }
}
