/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton; // Keep explicit imports if used by GEN-BEGIN:initComponents if not using wildcard
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import util.UserSession;
import java.rmi.RemoteException; // Added for the try-catch block
import java.util.Random;

/**
 *
 * @author User
 */
public class LoginForm extends javax.swing.JFrame {

    // OTP and logic-related fields
    private String generatedOtp;

    // IMPORTANT: For NetBeans GUI Builder Integration:
    // The following JComponent fields (otpLabel, otpField, sendOtpButton, loginButton)
    // are intended to be linked to components you create in the NetBeans GUI Designer.
    // For this linkage to work correctly:
    // 1. In the NetBeans Design view, add the necessary visual components for OTP:
    //    - A JLabel to prompt for the OTP (e.g., text "OTP Code:").
    //    - A JTextField for the user to enter the OTP.
    //    - A JButton for the user to request an OTP (e.g., text "Send OTP").
    //    - A JButton for the user to submit their credentials and OTP (e.g., text "Login").
    //      (If you have an existing main login button, you can repurpose it or rename its variable).
    // 2. For each of these components, select it in the Design view, go to the
    //    Properties window, find the "Code" tab (or look for "Variable Name" directly),
    //    and set the "Variable Name" property EXACTLY as follows:
    //      - JLabel for OTP prompt:      otpLabel
    //      - JTextField for OTP input:   otpField
    //      - JButton for sending OTP:    sendOtpButton
    //      - JButton for final login:    loginButton
    //
    // After doing this, NetBeans will automatically declare these variables in the
    // special `//GEN-BEGIN:variables` block later in this file.
    // These explicit private declarations here are for code clarity and to ensure
    // these fields are recognized and accessible throughout the class logic.
    // The GUI builder-generated code in `initComponents()` will initialize these fields
    // if (and only if) they are correctly named in the Design view.
    private javax.swing.JLabel otpLabel;
    private javax.swing.JTextField otpField;
    private javax.swing.JButton sendOtpButton;
    private javax.swing.JButton loginButton;


    public LoginForm() {
        initComponents(); // This initializes all components designed in the .form file.

        // --- OTP Component Setup (Post-initComponents) ---
        // The following code configures the OTP components by setting their text (if not set in designer)
        // and adding action listeners. It relies on the components being correctly added and named
        // in the NetBeans GUI Designer as per the instructions above.
        //
        // If "CRITICAL ERROR" messages appear in your console when running, it means the
        // corresponding component was NOT found (i.e., it was 'null' when this code executed)
        // likely because its "Variable Name" in the Design view does not match the expected name.

        if (this.sendOtpButton != null) {
            this.sendOtpButton.setText("Send OTP"); // Text can also be set in the designer's properties
            this.sendOtpButton.addActionListener(evt -> sendOtpButtonActionPerformed(evt));
            this.sendOtpButton.setEnabled(true); // Send OTP button is initially enabled
        } else {
            System.err.println("CRITICAL ERROR: LoginForm - 'sendOtpButton' was not initialized by NetBeans. " +
                               "Please ensure a JButton with the variable name 'sendOtpButton' exists in your form design.");
        }

        if (this.loginButton != null) {
            this.loginButton.setText("Login"); // Text can also be set in the designer's properties
            this.loginButton.addActionListener(evt -> signInActionPerformed(evt));
            this.loginButton.setEnabled(false); // Login button is initially disabled until OTP is sent
        } else {
            System.err.println("CRITICAL ERROR: LoginForm - 'loginButton' was not initialized by NetBeans. " +
                               "Ensure your main login JButton has the variable name 'loginButton' in your form design.");
        }

        if (this.otpLabel != null) {
             this.otpLabel.setText("OTP Code:"); // Text can also be set in the designer's properties
        } else {
            System.err.println("CRITICAL ERROR: LoginForm - 'otpLabel' was not initialized by NetBeans. " +
                               "Ensure a JLabel with the variable name 'otpLabel' exists in your form design.");
        }

        if (this.otpField != null) {
            this.otpField.setEnabled(false); // OTP input field is initially disabled
        } else {
            System.err.println("CRITICAL ERROR: LoginForm - 'otpField' was not initialized by NetBeans. " +
                               "Ensure a JTextField with the variable name 'otpField' exists in your form design.");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Body = new javax.swing.JPanel();
        left = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        username = new javax.swing.JTextField();
        lms_icon = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        passwordTxt = new javax.swing.JPasswordField();
        // Note: The declarations for otpLabel, otpField, sendOtpButton, loginButton
        // will be automatically generated here by NetBeans if they are added to the form
        // in the Design view and their "Variable Name" properties are set to match the
        // private fields declared at the top of this class.

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        Body.setBackground(new java.awt.Color(255, 255, 255));

        left.setBackground(new java.awt.Color(102, 102, 102));
        left.setLayout(null);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Login");

        username.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        username.setForeground(new java.awt.Color(169, 169, 169));
        username.setText("Username");
        username.setToolTipText("Email or Username");
        username.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 1, 0, new java.awt.Color(204, 204, 204)));
        username.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                usernameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                usernameFocusLost(evt);
            }
        });
        username.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameActionPerformed(evt);
            }
        });

        lms_icon.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 0, new java.awt.Color(204, 204, 204)));

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/pass.png"))); // NOI18N
        jLabel2.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 1, 1, new java.awt.Color(204, 204, 204)));
        jLabel2.setOpaque(true);

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/user.png"))); // NOI18N
        jLabel3.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 1, 1, new java.awt.Color(204, 204, 204)));
        jLabel3.setOpaque(true);

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 30)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("-");
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 17)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("X");
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        passwordTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordTxtActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout BodyLayout = new javax.swing.GroupLayout(Body);
        Body.setLayout(BodyLayout);
         BodyLayout.setHorizontalGroup(
            BodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BodyLayout.createSequentialGroup()
                .addComponent(left, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                .addGroup(BodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BodyLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(91, 91, 91))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BodyLayout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BodyLayout.createSequentialGroup()
                        .addGroup(BodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(BodyLayout.createSequentialGroup()
                                .addComponent(passwordTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(BodyLayout.createSequentialGroup()
                                .addComponent(lms_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(74, 74, 74))
                    // DEVELOPER NOTE: Add OTP components (JLabel, JTextField, 2 JButtons)
                    // to this GroupLayout using the NetBeans GUI Builder.
                    // Ensure their "Variable Name" properties are set to:
                    // otpLabel, otpField, sendOtpButton, loginButton
                )
        );
        BodyLayout.setVerticalGroup(
            BodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(left, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(BodyLayout.createSequentialGroup()
                .addGroup(BodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(112, 112, 112)
                .addGroup(BodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lms_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(BodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwordTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                // DEVELOPER NOTE: Adjust layout to include OTP components here.
                .addContainerGap(205, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(837, 480));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void usernameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_usernameFocusGained
        if(username.getText().equals("Username")) {
            username.setText("");
        }
    }//GEN-LAST:event_usernameFocusGained

    private void usernameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_usernameFocusLost
        if(username.getText().equals("")) {
            username.setText("Username");
        }
    }//GEN-LAST:event_usernameFocusLost

    private void usernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameActionPerformed
        // Pressing enter in username field might try to send OTP or do nothing.
        // For this flow, it's better to rely on explicit button clicks.
    }//GEN-LAST:event_usernameActionPerformed

    private void passwordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordFocusGained
        if (String.valueOf(passwordTxt.getPassword()).equals("Password")){
            passwordTxt.setText("");
            passwordTxt.setEchoChar('•');
        }
    }//GEN-LAST:event_passwordFocusGained

    private void passwordFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordFocusLost
        if (String.valueOf(passwordTxt.getPassword()).equals("") ){
            passwordTxt.setText("Password");
            passwordTxt.setEchoChar((char)0);
        }
    }//GEN-LAST:event_passwordFocusLost

    private void passwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordActionPerformed
        // Pressing enter in password field should not trigger login before OTP.
    }//GEN-LAST:event_passwordActionPerformed

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        this.setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_jLabel8MouseClicked

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        System.exit(0);
    }//GEN-LAST:event_jLabel7MouseClicked

    private void sendOtpButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String user = username.getText();
        if (user.isEmpty() || user.equals("Username")) {
            JOptionPane.showMessageDialog(this, "Please enter username to receive OTP.", "OTP Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        generatedOtp = String.format("%06d", new Random().nextInt(999999));
        JOptionPane.showMessageDialog(this, "OTP sent to your registered email (simulated): " + generatedOtp, "OTP Sent", JOptionPane.INFORMATION_MESSAGE);

        if (otpField != null) otpField.setEnabled(true);
        if (loginButton != null) loginButton.setEnabled(true);
        if (sendOtpButton != null) sendOtpButton.setEnabled(false);
    }

    private void signInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signInActionPerformed
        String user = username.getText();
        String enteredOtp = (otpField != null) ? otpField.getText() : "";

        if (user.isEmpty() || user.equals("Username")) {
            JOptionPane.showMessageDialog(this, "Username is required.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (generatedOtp == null || generatedOtp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please send OTP first.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (enteredOtp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the OTP.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (enteredOtp.equals(generatedOtp)) {
            try {
                String role;
                if (user.equals("admin")) {
                    role = "admin";
                } else {
                    role = "user";
                }

                int accountIdToSet = role.equals("admin") ? 0 : 1; // Dummy IDs
                UserSession.getInstance().setUser(user, role, accountIdToSet);

                if (role.equals("admin")) {
                    new AdminDashboard().setVisible(true);
                } else {
                    new UserDashboard().setVisible(true);
                }
                this.dispose();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Login failed after OTP: " + e.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid OTP. Please try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_signInActionPerformed

    private void passwordTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordTxtActionPerformed
        // Pressing enter in password field should not trigger login before OTP.
    }//GEN-LAST:event_passwordTxtActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Body;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel left;
    private javax.swing.JLabel lms_icon;
    // IMPORTANT FOR NETBEANS GUI BUILDER:
    // If you add the OTP components (JLabel, JTextField, two JButtons) to your form
    // using the Design view, and set their "Variable Name" properties in the designer to:
    //   otpLabel, otpField, sendOtpButton, loginButton
    // then NetBeans will automatically declare them here. For example:
    //    private javax.swing.JLabel otpLabel;
    //    private javax.swing.JTextField otpField;
    //    private javax.swing.JButton sendOtpButton;
    //    private javax.swing.JButton loginButton;
    // The explicit private member declarations at the top of this class are for code clarity
    // and ensure these variables are accessible. The GUI builder initializes them.
    private javax.swing.JPasswordField passwordTxt;
    private javax.swing.JTextField username;
    // End of variables declaration//GEN-END:variables
}
