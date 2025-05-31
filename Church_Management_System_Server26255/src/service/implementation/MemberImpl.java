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
    public String deleteMember(Member members) throws RemoteException {
        return memberDao.deleteMember(members);
    }

    @Override
    public List<Member> retreiveAll() throws RemoteException {
        return memberDao.retreiveAll();
    }

    @Override
    public Member retrieveById(Member member) throws RemoteException {
        return memberDao.retrieveById(member);
    }

    @Override
    public Member getMemberById(int userId) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
