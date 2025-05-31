package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import util.UserSession;
import view.EventsPanel; // Added for EventsPanel integration
// No need to import view.LoginForm explicitly if it's in the same package 'view'
// import view.LoginForm;

public class AdminDashboard extends JFrame {

    private JPanel mainPanel;
    private JPanel navigationPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private EventsPanel eventsPanel; // Declare EventsPanel instance

    private JButton btnManageEvents;
    private JButton btnViewMembers;
    private JButton btnViewAttendance;
    private JButton btnDonationReports;
    private JButton btnLogout;

    public AdminDashboard() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Admin Dashboard - Church Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 600));

        // Main Panel
        mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel);

        // Navigation Panel (West)
        navigationPanel = new JPanel();
        // Using BoxLayout for vertical arrangement
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add some padding
        navigationPanel.setBackground(Color.LIGHT_GRAY); // Basic styling

        btnManageEvents = new JButton("Manage Events");
        btnViewMembers = new JButton("View Members");
        btnViewAttendance = new JButton("View Attendance");
        btnDonationReports = new JButton("Donation Reports");
        btnLogout = new JButton("Logout");

        // Style buttons
        Dimension buttonSize = new Dimension(150, 40); // Define a common size
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);

        JButton[] navButtons = {btnManageEvents, btnViewMembers, btnViewAttendance, btnDonationReports, btnLogout};
        for (JButton button : navButtons) {
            button.setPreferredSize(buttonSize);
            button.setMaximumSize(buttonSize); // For BoxLayout to respect preferred size
            button.setFont(buttonFont);
            button.setAlignmentX(Component.LEFT_ALIGNMENT); // Align buttons to the left
            navigationPanel.add(button);
            navigationPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing between buttons
        }

        mainPanel.add(navigationPanel, BorderLayout.WEST);

        // Content Panel (Center) with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add a default welcome panel
        JPanel welcomePanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for centering
        JLabel welcomeLabel = new JLabel("Welcome, Admin!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomePanel.add(welcomeLabel);
        contentPanel.add(welcomePanel, "WelcomePanel"); // "WelcomePanel" is the key

        // Instantiate and add EventsPanel
        eventsPanel = new EventsPanel();
        contentPanel.add(eventsPanel, "EventsPanel"); // Key for CardLayout

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "WelcomePanel"); // Show welcome panel initially

        // Add Action Listeners
        btnManageEvents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, "EventsPanel");
            }
        });

        btnViewMembers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "View Members clicked!");
                // cardLayout.show(contentPanel, "MembersPanel");
            }
        });

        btnViewAttendance.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "View Attendance clicked!");
                // cardLayout.show(contentPanel, "AttendancePanel");
            }
        });

        btnDonationReports.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(AdminDashboard.this, "Donation Reports clicked!");
                // cardLayout.show(contentPanel, "ReportsPanel");
            }
        });

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserSession.getInstance().clearSession();
                new LoginForm().setVisible(true);
                AdminDashboard.this.dispose();
            }
        });

        pack();
        setLocationRelativeTo(null); // Center the window
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
            java.util.logging.Logger.getLogger(AdminDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminDashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDashboard().setVisible(true);
            }
        });
    }
}
