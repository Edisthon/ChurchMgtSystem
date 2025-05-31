package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardForm extends JFrame {

    private User currentUser;
    private JButton btnManageProducts, btnManageUsers, btnViewProductLogs, btnLogNewProductActivity, btnLogout;
    private JLabel lblTitle, lblWelcomeUser;

    public DashboardForm(User user) {
        this.currentUser = user;
        initComponents();
        setTitle("Dashboard - Food Traceability System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack(); // Pack after components are added
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 240, 240)); // Light gray background

        // Top Panel (Welcome Message)
        JPanel topPanel = new JPanel(new BorderLayout(5,5));
        topPanel.setOpaque(false); // Transparent to show mainPanel background

        lblTitle = new JLabel("Main Dashboard", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 22));
        lblTitle.setForeground(new Color(50, 50, 50)); // Dark gray text
        topPanel.add(lblTitle, BorderLayout.CENTER);

        if (currentUser != null) {
            lblWelcomeUser = new JLabel("Welcome, " + currentUser.getUsername() + "!", SwingConstants.RIGHT);
            lblWelcomeUser.setFont(new Font("Tahoma", Font.ITALIC, 14));
            lblWelcomeUser.setForeground(new Color(80, 80, 80));
            lblWelcomeUser.setBorder(new EmptyBorder(0,0,5,5)); // Padding for welcome message
            topPanel.add(lblWelcomeUser, BorderLayout.SOUTH);
        }
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Button Panel (Center)
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 15, 15)); // 5 rows, 1 col
        buttonPanel.setBorder(new EmptyBorder(20, 50, 20, 50)); // Padding around buttons
        buttonPanel.setOpaque(false);

        Font buttonFont = new Font("Tahoma", Font.BOLD, 14);
        Dimension buttonSize = new Dimension(250, 45); // Standard button size

        btnManageProducts = new JButton("Manage Products");
        styleButton(btnManageProducts, buttonFont, buttonSize, "View and manage existing products.");

        btnManageUsers = new JButton("Manage Users");
        styleButton(btnManageUsers, buttonFont, buttonSize, "Manage user accounts (Admin only).");

        btnViewProductLogs = new JButton("View Product Activity Logs");
        styleButton(btnViewProductLogs, buttonFont, buttonSize, "View detailed activity logs for products.");

        btnLogNewProductActivity = new JButton("Log New Product Activity");
        styleButton(btnLogNewProductActivity, buttonFont, buttonSize, "Manually log new activity or status for a product.");

        btnLogout = new JButton("Logout");
        styleButton(btnLogout, buttonFont, buttonSize, "Logout from the application.");
        btnLogout.setBackground(new Color(220, 53, 69)); // Reddish color for logout
        btnLogout.setForeground(Color.WHITE);


        // Add buttons to panel
        buttonPanel.add(btnManageProducts);
        buttonPanel.add(btnViewProductLogs);
        buttonPanel.add(btnLogNewProductActivity);
        buttonPanel.add(btnManageUsers); // Add here, visibility/enabled state managed below
        buttonPanel.add(btnLogout);

        // Role-Based Visibility/Enablement for "Manage Users"
        if (currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole())) {
            btnManageUsers.setEnabled(true);
        } else {
            btnManageUsers.setEnabled(false);
            btnManageUsers.setToolTipText("Manage user accounts (Admin access required).");
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Action Listeners
        btnManageProducts.addActionListener(e -> openProductListForm());
        btnManageUsers.addActionListener(e -> openUserListForm());
        btnViewProductLogs.addActionListener(e -> openProductUserLogListForm());
        btnLogNewProductActivity.addActionListener(e -> openProductUserLogForm());
        btnLogout.addActionListener(e -> logout());
    }

    private void styleButton(JButton button, Font font, Dimension size, String tooltip) {
        button.setFont(font);
        button.setPreferredSize(size);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false); // Improve look
        button.setBackground(new Color(70, 130, 180)); // Steel Blue
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Navigation Methods
    private void openProductListForm() {
        JOptionPane.showMessageDialog(this, "ProductListForm not yet implemented.", "TODO", JOptionPane.INFORMATION_MESSAGE);
        // new ProductListForm(currentUser).setVisible(true);
        // dispose();
    }

    private void openUserListForm() {
         JOptionPane.showMessageDialog(this, "UserListForm not yet implemented.", "TODO", JOptionPane.INFORMATION_MESSAGE);
        // new UserListForm(currentUser).setVisible(true);
        // dispose();
    }

    private void openProductUserLogListForm() {
        JOptionPane.showMessageDialog(this, "ProductUserLogListForm not yet implemented.", "TODO", JOptionPane.INFORMATION_MESSAGE);
        // new ProductUserLogListForm(currentUser).setVisible(true);
        // dispose();
    }

    private void openProductUserLogForm() {
         JOptionPane.showMessageDialog(this, "ProductUserLogForm not yet implemented.", "TODO", JOptionPane.INFORMATION_MESSAGE);
        // new ProductUserLogForm(currentUser).setVisible(true);
        // dispose();
    }

    private void logout() {
        new LoginForm().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Dummy user for testing
            User testUserAdmin = new User();
            testUserAdmin.setUserId(1);
            testUserAdmin.setUsername("TestAdmin");
            testUserAdmin.setRole("admin");

            User testUserNonAdmin = new User();
            testUserNonAdmin.setUserId(2);
            testUserNonAdmin.setUsername("TestUser");
            testUserNonAdmin.setRole("user");

            // Test with admin user
            // new DashboardForm(testUserAdmin).setVisible(true);

            // Test with non-admin user
             new DashboardForm(testUserNonAdmin).setVisible(true);
        });
    }
}
