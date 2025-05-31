package view;

import javax.swing.*;
import com.toedter.calendar.JDateChooser; // For birthdate
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Member; // Client-side model
import service.MemberService; // Client-side RMI interface
import util.UserSession;
import java.sql.Date; // For birthdate
import java.text.SimpleDateFormat;
// ParseException might not be needed if JDateChooser handles date well internally for display
// import java.text.ParseException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class UserProfilePanel extends JPanel {

    private JTextField txtFullName, txtGender, txtPhoneNumber, txtGroupId;
    private JDateChooser dateChooserBirthdate;
    private JButton btnEditProfile, btnSaveChanges;

    private MemberService memberService;
    private Member currentMember;
    private boolean editMode = false;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public UserProfilePanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding

        boolean serviceInitialized = initializeServices();
        initComponents();

        if (serviceInitialized) {
            loadUserProfile();
        } else {
            disablePanelFunctionality("Could not connect to Member Service.\nPlease ensure RMI server is running.");
        }
        toggleEditMode(false); // Set initial state
    }

    private boolean initializeServices() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000);
            memberService = (MemberService) registry.lookup("member");
            System.out.println("MemberService RMI object found for UserProfilePanel.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            memberService = null;
            return false;
        }
    }

    private void disablePanelFunctionality(String message) {
        if (btnEditProfile != null) btnEditProfile.setEnabled(false);
        if (btnSaveChanges != null) btnSaveChanges.setEnabled(false);
        // Clear fields or show placeholder text
        if(txtFullName != null) txtFullName.setText("Service Unavailable");
        if(txtGender != null) txtGender.setText("");
        if(txtPhoneNumber != null) txtPhoneNumber.setText("");
        if(dateChooserBirthdate != null) dateChooserBirthdate.setDate(null);
        if(txtGroupId != null) txtGroupId.setText("");

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Service Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }


    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding between components
        gbc.anchor = GridBagConstraints.WEST;

        // Full Name
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtFullName = new JTextField(20);
        add(txtFullName, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;


        // Gender
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtGender = new JTextField(20);
        add(txtGender, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Phone Number
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtPhoneNumber = new JTextField(20);
        add(txtPhoneNumber, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Birthdate
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Birthdate:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        dateChooserBirthdate = new JDateChooser();
        dateChooserBirthdate.setDateFormatString("yyyy-MM-dd");
        add(dateChooserBirthdate, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Group ID
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Group ID:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtGroupId = new JTextField(20);
        add(txtGroupId, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnEditProfile = new JButton("Edit Profile");
        btnSaveChanges = new JButton("Save Changes");
        buttonsPanel.add(btnEditProfile);
        buttonsPanel.add(btnSaveChanges);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2; // Span two columns
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonsPanel, gbc);

        // Action Listeners
        btnEditProfile.addActionListener(e -> toggleEditMode(true));
        btnSaveChanges.addActionListener(e -> saveProfileChanges());
    }

    private void toggleEditMode(boolean enable) {
        editMode = enable;
        txtPhoneNumber.setEditable(enable);
        // txtFullName, txtGender, dateChooserBirthdate, txtGroupId are display-only for user
        txtFullName.setEditable(false);
        txtGender.setEditable(false);
        dateChooserBirthdate.setEnabled(false); // JDateChooser enable state controls its editability.
        txtGroupId.setEditable(false);

        btnSaveChanges.setEnabled(enable);
        btnEditProfile.setEnabled(!enable);
    }

    private void loadUserProfile() {
        if (memberService == null) {
            JOptionPane.showMessageDialog(this, "Member Service not available.", "Service Error", JOptionPane.ERROR_MESSAGE);
            disablePanelFunctionality("Member Service not available.");
            return;
        }

        // Disable buttons during load
        btnEditProfile.setEnabled(false);
        btnSaveChanges.setEnabled(false);

        SwingWorker<Member, Void> worker = new SwingWorker<Member, Void>() {
            private String errorMessage = null;

            @Override
            protected Member doInBackground() throws Exception {
                try {
                    int accountId = UserSession.getInstance().getAccountId();
                    if (accountId <= 0) {
                        errorMessage = "Invalid user session or account ID. Please log in again.";
                        return null;
                    }
                    return memberService.getMemberById(accountId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error fetching profile: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    currentMember = get(); // Get result from doInBackground
                    if (errorMessage != null) {
                        JOptionPane.showMessageDialog(UserProfilePanel.this, errorMessage, "Profile Load Error", JOptionPane.ERROR_MESSAGE);
                        disablePanelFunctionality(errorMessage); // Disable all fields if error
                    } else if (currentMember != null) {
                        txtFullName.setText(currentMember.getFullName() != null ? currentMember.getFullName() : "");
                        txtGender.setText(currentMember.getGender() != null ? currentMember.getGender() : "");
                        txtPhoneNumber.setText(currentMember.getPhoneNumber() != null ? currentMember.getPhoneNumber() : "");
                        dateChooserBirthdate.setDate(currentMember.getBirthdate()); // JDateChooser handles null date
                        txtGroupId.setText(currentMember.getGroupId() != null ? String.valueOf(currentMember.getGroupId()) : "");
                        toggleEditMode(false); // Set initial non-edit state, enables Edit button
                    } else {
                        JOptionPane.showMessageDialog(UserProfilePanel.this, "Could not load user profile.", "Profile Load Error", JOptionPane.ERROR_MESSAGE);
                        disablePanelFunctionality("Could not load user profile.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UserProfilePanel.this, "Error processing profile data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    disablePanelFunctionality("Error processing profile data.");
                }
            }
        };
        worker.execute();
    }

    private void saveProfileChanges() {
        if (currentMember == null || memberService == null) {
            JOptionPane.showMessageDialog(this, "Profile data or service not available. Cannot save.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newPhoneNumber = txtPhoneNumber.getText().trim();
        // Basic validation for phone number (e.g., not empty, specific format if required)
        if (newPhoneNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone number cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Add more specific phone number validation regex if needed

        currentMember.setPhoneNumber(newPhoneNumber);
        // Only phone number is editable by user in this setup.
        // If other fields (like birthdate via JDateChooser) were made editable, update them here:
        // if (dateChooserBirthdate.getDate() != null) {
        // currentMember.setBirthdate(new Date(dateChooserBirthdate.getDate().getTime()));
        // }

        btnSaveChanges.setEnabled(false); // Disable during save

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            private String errorMessage = null;

            @Override
            protected String doInBackground() throws Exception {
                try {
                    return memberService.updateMember(currentMember);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error saving profile: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (errorMessage != null) {
                        JOptionPane.showMessageDialog(UserProfilePanel.this, errorMessage, "Save Error", JOptionPane.ERROR_MESSAGE);
                    } else if (result != null) {
                        JOptionPane.showMessageDialog(UserProfilePanel.this, "Profile updated successfully: " + result, "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadUserProfile(); // Reload to confirm changes and reset edit mode
                    } else {
                         JOptionPane.showMessageDialog(UserProfilePanel.this, "Failed to update profile. Unknown error.", "Save Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UserProfilePanel.this, "Error processing save result: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // toggleEditMode(false) is called by loadUserProfile if successful
                    // if loadUserProfile is not called on error, ensure buttons are reset appropriately
                     if (errorMessage != null) { // If there was an error, re-enable save to allow retry
                        btnSaveChanges.setEnabled(true);
                     }
                }
            }
        };
        worker.execute();
    }
}
