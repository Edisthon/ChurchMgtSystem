package service;

import java.rmi.Remote; // Added for RMI interface
import java.rmi.RemoteException;
import model.Member; // Single import is sufficient
import java.util.List;
// Date and Timestamp imports can be kept if Member model uses them, or removed if not directly used by this interface.
// For now, keeping them.
import java.sql.Date;
import java.sql.Timestamp;


public interface MemberService extends Remote { // Ensure it extends Remote
   public String  registerMember(Member member) throws RemoteException;
    public  String  updateMember(Member member) throws RemoteException;
    public  String  deleteMember(int memberID) throws RemoteException; // Consider taking int memberId instead of Member object
    public List<Member> retreiveAll() throws RemoteException;
    public Member retrieveById(int memberID) throws RemoteException; // This seems like an older/alternative version
    public Member getMemberById(int memberId) throws RemoteException; // Standard way by ID

    List<Member> searchMembersByName(String name) throws RemoteException; // Added method
}

