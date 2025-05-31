package service;

import java.rmi.RemoteException;
import model.Member;
import java.util.List;
import java.sql.Date; // For Member birthdate
import java.sql.Timestamp; // For Event eventDateTime & EventAttendance checkInTime
import model.Member;

public interface MemberService {
   public String  registerMember(Member member) throws RemoteException;
    public  String  updateMember(Member member) throws RemoteException;
    public  String  deleteMember(int memberID) throws RemoteException;
    public List<Member> retreiveAll() throws RemoteException;
    public Member retrieveById(Member accounts) throws RemoteException;
    Member getMemberById(int userId) throws RemoteException;

}

