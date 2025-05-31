package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import util.UserSession;
import view.UserEventsPanel; // Added for UserEventsPanel integration
// view.LoginForm is in the same package, so direct import is not strictly necessary
// import view.LoginForm;

public class UserDashboard extends JFrame {

    private JPanel mainPanel;
    private JPanel navigationPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private UserEventsPanel userEventsPanel; // Declare UserEventsPanel instance

    private JButton btnUpcomingEvents;
    private JButton btnMarkAttendance;
    private JButton btnMyProfile;
    private JButton btnMakeDonation;
    private JButton btnMyDonations;
    private JButton btnLogout;

    public UserDashboard() {
        initComponents();
    }

    private void initComponents() {
        setTitle("User Dashboard - Church Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        // Main Panel
        mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        // Navigation Panel (West)
        navigationPanel = new JPanel();
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        navigationPanel.setBackground(new Color(220, 220, 220)); // A slightly different shade for user

        btnUpcomingEvents = new JButton("Upcoming Events");
        btnMarkAttendance = new JButton("Mark Attendance");
        btnMyProfile = new JButton("My Profile");
        btnMakeDonation = new JButton("Make a Donation");
        btnMyDonations = new JButton("My Donations");
        btnLogout = new JButton("Logout");

        // Style buttons
        Dimension buttonSize = new Dimension(160, 40); // Slightly wider for longer text
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);

        JButton[] navButtons = {btnUpcomingEvents, btnMarkAttendance, btnMyProfile, btnMakeDonation, btnMyDonations, btnLogout};
        for (JButton button : navButtons) {
            button.setPreferredSize(buttonSize);
            button.setMaximumSize(buttonSize);
            button.setFont(buttonFont);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            navigationPanel.add(button);
            navigationPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing
        }

        mainPanel.add(navigationPanel, BorderLayout.WEST);

        // Content Panel (Center) with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add a default welcome panel
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        String username = UserSession.getInstance().getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = "User"; // Default if session somehow doesn't have a name
        }
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomePanel.add(welcomeLabel);
        contentPanel.add(welcomePanel, "WelcomePanel");

        // Instantiate and add UserEventsPanel
        userEventsPanel = new UserEventsPanel();
        contentPanel.add(userEventsPanel, "UserEventsPanel"); // Key for CardLayout

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "WelcomePanel");

        // Add Action Listeners
        btnUpcomingEvents.addActionListener(e ->
            cardLayout.show(contentPanel, "UserEventsPanel")
        );

        btnMarkAttendance.addActionListener(e ->
            JOptionPane.showMessageDialog(UserDashboard.this, "Mark Attendance clicked!")
            // cardLayout.show(contentPanel, "MarkAttendancePanel");
        );

        btnMyProfile.addActionListener(e ->
            JOptionPane.showMessageDialog(UserDashboard.this, "My Profile clicked!")
            // cardLayout.show(contentPanel, "MyProfilePanel");
        );

        btnMakeDonation.addActionListener(e ->
            JOptionPane.showMessageDialog(UserDashboard.this, "Make a Donation clicked!")
            // cardLayout.show(contentPanel, "MakeDonationPanel");
        );

        btnMyDonations.addActionListener(e ->
            JOptionPane.showMessageDialog(UserDashboard.this, "My Donations clicked!")
            // cardLayout.show(contentPanel, "MyDonationsPanel");
        );

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserSession.getInstance().clearSession();
                new LoginForm().setVisible(true);
                UserDashboard.this.dispose();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UserDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UserDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UserDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // For testing, manually set a user session if running directly
                // UserSession.getInstance().setUser("TestUser", "user");
                new UserDashboard().setVisible(true);
            }
        });
    }
}
