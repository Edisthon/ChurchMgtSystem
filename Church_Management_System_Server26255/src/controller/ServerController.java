package controller;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import service.implementation.AccountsServiceImpl;
import service.implementation.EventAttendanceImpl;
import service.implementation.EventImpl;
import service.implementation.GroupsImpl;
import service.implementation.MemberImpl;
import service.implementation.DonationServiceImpl; // Added import
// sun.security.acl.GroupImpl seems unused and potentially problematic, consider removing if not needed.
// For now, I will leave it as it's in the original code.
import sun.security.acl.GroupImpl;



public class ServerController {
    
       
        private AccountsServiceImpl accountsServiceImpl;
        private EventAttendanceImpl attendanceImpl;                
        private EventImpl eventImpl;
        private MemberImpl memberImpl;
        private GroupsImpl groupImpl;
        private DonationServiceImpl donationServiceImpl; // Added declaration
        
        public ServerController() throws RemoteException{
            
            this.accountsServiceImpl= new AccountsServiceImpl();
            this.eventImpl= new EventImpl();
            this.attendanceImpl= new EventAttendanceImpl();
            this.memberImpl= new MemberImpl();
            this.groupImpl= new GroupsImpl();
            this.donationServiceImpl = new DonationServiceImpl(); // Added instantiation
        }
        
        public static void main(String[] args) {
            
            try{
                
                System.setProperty("java.rmi.server.hostname", "127.0.0.1");
                Registry registry= LocateRegistry.createRegistry(6000);                                
                registry.rebind("account", new ServerController().accountsServiceImpl);
                registry.rebind("event", new ServerController().eventImpl);
                registry.rebind("eventattendance", new ServerController().attendanceImpl);
                registry.rebind("member", new ServerController().memberImpl);
                registry.rebind("groups", new ServerController().groupImpl);
                registry.rebind("donation", new ServerController().donationServiceImpl); // Added binding
                System.out.println("Server is running on port 6000"); // Corrected port message to match registry
            }catch(Exception ex){
                ex.printStackTrace();
        
            }
            
    }
}
