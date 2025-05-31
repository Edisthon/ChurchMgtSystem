package service;

import java.rmi.Remote; // Added for RMI interface
import java.rmi.RemoteException;
import model.Member;
import java.util.List;
// java.sql.Date and java.sql.Timestamp are not directly used in this interface's method signatures
// but might be used by the Member model, so keeping them is fine if Member model uses them.
// For clarity, if not directly used by THIS interface's parameters/return types, they could be removed here.
// Let's keep them for now as Member model might need them.
import java.sql.Date;
import java.sql.Timestamp;


public interface MemberService extends Remote { // Ensure it extends Remote
    public String  registerMember(Member member) throws RemoteException;
    public  String  updateMember(Member member) throws RemoteException;
    public  String  deleteMember(Member member) throws RemoteException;
    public List<Member> retreiveAll() throws RemoteException;
    public Member retrieveById(Member accounts) throws RemoteException; // This seems like an older/alternative version
    public Member getMemberById(int memberId) throws RemoteException; // Standard way by ID

    List<Member> searchMembersByName(String name) throws RemoteException; // Added method
}

