package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import service.AccountsService; // Client-side RMI interface
import model.Accounts;      // Client-side model
import util.UserSession;

public class NewLoginForm extends JFrame {

    private JTextField usernameField;
    private JTextField otpField;
    private JButton sendOtpButton;
    private JButton loginButton;
    private JLabel statusLabel; // For brief messages or errors

    private AccountsService accountsService;

    public NewLoginForm() {
        super("User Login"); // Frame title

        // Initialize RMI Service
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000);
            this.accountsService = (AccountsService) registry.lookup("account");
            System.out.println("AccountsService RMI object found for NewLoginForm.");
        } catch (Exception e) {
            e.printStackTrace();
            this.accountsService = null;
            // Error will be handled after initComponents
        }

        initComponents();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setSize(400, 350); // Adjusted size, pack() might be better
        pack(); // Pack components to their preferred sizes
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);

        if (this.accountsService == null) {
            statusLabel.setText("Error: Login service unavailable.");
            sendOtpButton.setEnabled(false);
            loginButton.setEnabled(false);
            usernameField.setEnabled(false);
            passwordField.setEnabled(false);
            otpField.setEnabled(false);
            // Show this after the frame is visible
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                    "Login service is currently unavailable. Please ensure the server is running and accessible.",
                    "Service Connection Error", JOptionPane.ERROR_MESSAGE)
            );
        }
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gbc.insets = new Insets(8, 8, 8, 8); // Increased insets
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Username (Email):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        // Send OTP Button
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE; // Don't make button fill horizontally
        gbc.anchor = GridBagConstraints.LINE_START; // Align to where text starts in its cell
        sendOtpButton = new JButton("Send OTP");
        mainPanel.add(sendOtpButton, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor

        // OTP Field
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("OTP Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        otpField = new JTextField(20);
        otpField.setEnabled(false); // Initially disabled
        mainPanel.add(otpField, gbc);

        // Login Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // Don't make button fill horizontally
        loginButton = new JButton("Login");
        loginButton.setEnabled(false); // Initially disabled
        mainPanel.add(loginButton, gbc);

        // Status Label
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        statusLabel = new JLabel(" ", SwingConstants.CENTER); // Placeholder for messages, centered
        statusLabel.setForeground(Color.RED);
        mainPanel.add(statusLabel, gbc);


        // Action Listeners
        sendOtpButton.addActionListener(this::sendOtpButtonActionPerformed);
        loginButton.addActionListener(this::loginButtonActionPerformed);

        // Placeholder text behavior
        addPlaceholderBehavior(usernameField, "Enter your email address");
        addPlaceholderBehavior(otpField, "Enter OTP from email");


        // Add mainPanel to JFrame
        setContentPane(mainPanel);
    }

    private void addPlaceholderBehavior(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        // Special handling for JPasswordField to show/hide placeholder
        if (field instanceof JPasswordField) {
            JPasswordField pField = (JPasswordField) field;
            pField.setEchoChar((char) 0); // Show placeholder text initially
            pField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    char[] pass = pField.getPassword();
                    String currentText = new String(pass);
                    if (currentText.equals(placeholder)) {
                        pField.setText("");
                        pField.setEchoChar('•');
                        pField.setForeground(Color.BLACK);
                    }
                }
                @Override
                public void focusLost(FocusEvent e) {
                    if (pField.getPassword().length == 0) {
                        pField.setForeground(Color.GRAY);
                        pField.setText(placeholder);
                        pField.setEchoChar((char) 0); // Show placeholder text
                    }
                }
            });
        } else {
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (field.getText().equals(placeholder)) {
                        field.setText("");
                        field.setForeground(Color.BLACK);
                    }
                }
                @Override
                public void focusLost(FocusEvent e) {
                    if (field.getText().isEmpty()) {
                        field.setForeground(Color.GRAY);
                        field.setText(placeholder);
                    }
                }
            });
        }
    }


    private void sendOtpButtonActionPerformed(ActionEvent evt) {
        if (accountsService == null) {
            JOptionPane.showMessageDialog(this, "Account service not available.", "Service Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String username = usernameField.getText();
        if (username.isEmpty() || username.equals("Enter your email address")) {
            statusLabel.setText("Username (Email) is required to send OTP.");
            JOptionPane.showMessageDialog(this, "Username (Email) is required to send OTP.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        statusLabel.setText("Sending OTP...");
        sendOtpButton.setEnabled(false);
        loginButton.setEnabled(false); // Also disable login button during OTP request

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return accountsService.requestOtp(username);
            }

            @Override
            protected void done() {
                try {
                    String serverResponse = get();
                    JOptionPane.showMessageDialog(NewLoginForm.this, serverResponse, "OTP Status", JOptionPane.INFORMATION_MESSAGE);
                    if (serverResponse != null && serverResponse.toLowerCase().startsWith("otp has been sent")) {
                        otpField.setEnabled(true);
                        loginButton.setEnabled(true); // Enable login button
                        statusLabel.setText("OTP sent. Please check your email.");
                        // sendOtpButton remains disabled
                    } else {
                        sendOtpButton.setEnabled(true); // Allow retry if server indicated failure
                        statusLabel.setText(serverResponse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendOtpButton.setEnabled(true); // Allow retry on communication error
                    statusLabel.setText("Error requesting OTP. Please try again.");
                    JOptionPane.showMessageDialog(NewLoginForm.this, "Error requesting OTP: " + e.getMessage(), "OTP Request Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void loginButtonActionPerformed(ActionEvent evt) {
        if (accountsService == null) {
            JOptionPane.showMessageDialog(this, "Account service not available.", "Service Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = usernameField.getText();
        String otp = otpField.getText();

        if (username.isEmpty() || username.equals("Enter your email address") ||
            otp.isEmpty() || otp.equals("Enter OTP from email")) {
            statusLabel.setText("Username and OTP are required for login.");
            JOptionPane.showMessageDialog(this, "Username (Email) and OTP are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        statusLabel.setText("Verifying...");
        loginButton.setEnabled(false);
        // sendOtpButton.setEnabled(false); // Keep sendOtpButton disabled during login attempt

        SwingWorker<Accounts, Void> worker = new SwingWorker<Accounts, Void>() {
            @Override
            protected Accounts doInBackground() throws Exception {
                return accountsService.verifyOtpAndLogin(username, otp);
            }

            @Override
            protected void done() {
                try {
                    Accounts loggedInAccount = get();
                    if (loggedInAccount != null) {
                        UserSession.getInstance().setUser(
                            loggedInAccount.getUsername(),
                            loggedInAccount.getRole(),
                            loggedInAccount.getAccountId()
                        );
                        statusLabel.setText("Login successful!");

                        if ("admin".equalsIgnoreCase(loggedInAccount.getRole())) {
                            new AdminDashboard().setVisible(true);
                        } else {
                            new UserDashboard().setVisible(true);
                        }
                        NewLoginForm.this.dispose();
                    } else {
                        statusLabel.setText("Login failed: Invalid username or OTP.");
                        JOptionPane.showMessageDialog(NewLoginForm.this, "Login failed. Please check username or OTP.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        loginButton.setEnabled(true); // Re-enable login button
                        otpField.setText(""); // Clear OTP field
                        sendOtpButton.setEnabled(true); // Re-enable Send OTP button for another full attempt
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error during login. Please try again.");
                    JOptionPane.showMessageDialog(NewLoginForm.this, "Login error: " + e.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true); // Re-enable login button
                    sendOtpButton.setEnabled(true); // Re-enable Send OTP on error
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new NewLoginForm().setVisible(true));
    }
}
