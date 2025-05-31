package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Donation;
import model.Member;
import model.Event; // Client-side model
import service.DonationService;
import service.MemberService;
import service.EventService;
import util.UserSession;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Collections; // Added import
import java.util.Vector; // For JComboBox model, though not strictly necessary with EventWrapper
import java.util.stream.Collectors;
import java.util.Date; // For filtering upcoming events
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MakeDonationPanel extends JPanel {

    private JTextField txtAmount;
    private JTextArea txtNotes; // Changed to JTextArea for potentially longer notes
    private JComboBox<EventWrapper> comboEvents;
    private JButton btnSubmitDonation;
    private JButton btnRefreshData; // For reloading events/member info

    private DonationService donationService;
    private MemberService memberService;
    private EventService eventService;

    private Member currentMember;
    private List<Event> upcomingEventsList;

    // Inner class for JComboBox items
    private static class EventWrapper {
        Event event;
        public EventWrapper(Event event) { this.event = event; }
        @Override public String toString() { return event != null ? event.getEventName() + " (" + new SimpleDateFormat("yyyy-MM-dd").format(event.getEventDateTime()) + ")" : "None (General Donation)"; }
        public Event getEvent() { return event; }
    }

    public MakeDonationPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        boolean servicesInitialized = initializeServices();
        initComponents();

        if (servicesInitialized) {
            loadInitialData();
        } else {
            disablePanelFunctionality("Could not connect to one or more services.\nPlease ensure RMI server is running.");
        }
    }

    private boolean initializeServices() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000);
            donationService = (DonationService) registry.lookup("donation");
            memberService = (MemberService) registry.lookup("member");
            eventService = (EventService) registry.lookup("event");
            System.out.println("All services RMI objects found for MakeDonationPanel.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            donationService = null; memberService = null; eventService = null;
            return false;
        }
    }

    private void disablePanelFunctionality(String message) {
        if (btnSubmitDonation != null) btnSubmitDonation.setEnabled(false);
        if (txtAmount != null) txtAmount.setEnabled(false);
        if (txtNotes != null) txtNotes.setEnabled(false);
        if (comboEvents != null) comboEvents.setEnabled(false);
        if (btnRefreshData != null) btnRefreshData.setEnabled(false);

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Service Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Amount
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Amount (USD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        txtAmount = new JTextField(15);
        add(txtAmount, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        // For Event (Optional)
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("For Event (Optional):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        comboEvents = new JComboBox<>(); // Will be populated by loadInitialData
        add(comboEvents, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Notes
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        txtNotes = new JTextArea(4, 20);
        JScrollPane notesScrollPane = new JScrollPane(txtNotes);
        add(notesScrollPane, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weighty = 0; gbc.anchor = GridBagConstraints.WEST;


        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnSubmitDonation = new JButton("Submit Donation");
        btnRefreshData = new JButton("Refresh Data"); // To reload member and events if needed
        buttonsPanel.add(btnSubmitDonation);
        buttonsPanel.add(btnRefreshData);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonsPanel, gbc);

        // Action Listeners
        btnSubmitDonation.addActionListener(e -> submitDonationAction());
        btnRefreshData.addActionListener(e -> loadInitialData());
    }

    private void loadInitialData() {
        if (memberService == null || eventService == null) {
            disablePanelFunctionality("Member or Event service is not available.");
            return;
        }

        btnSubmitDonation.setEnabled(false); // Disable while loading
        btnRefreshData.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String errorMessage = null;
            private List<Event> fetchedEvents;

            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Fetch current member
                    int accountId = UserSession.getInstance().getAccountId();
                    if (accountId <= 0) {
                        errorMessage = "Invalid user session. Please log in again.";
                        return false;
                    }
                    currentMember = memberService.getMemberById(accountId);
                    if (currentMember == null) {
                        errorMessage = "Could not retrieve your member details for ID: " + accountId;
                        return false;
                    }

                    // Fetch upcoming events
                    List<Event> allEvents = eventService.retreiveAll();
                    if (allEvents != null) {
                        Date currentTime = new Date();
                        fetchedEvents = allEvents.stream()
                            .filter(event -> event.getEventDateTime() != null && event.getEventDateTime().after(currentTime))
                            .collect(Collectors.toList());
                    } else {
                        fetchedEvents = java.util.Collections.emptyList(); // Empty list
                    }
                    return true; // Success
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error fetching initial data: " + e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                btnRefreshData.setEnabled(true);
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(MakeDonationPanel.this, errorMessage, "Data Load Error", JOptionPane.ERROR_MESSAGE);
                    disablePanelFunctionality(errorMessage); // Keep panel disabled
                    return;
                }
                try {
                    boolean success = get();
                    if (success) {
                        // Populate comboEvents
                        comboEvents.removeAllItems();
                        comboEvents.addItem(new EventWrapper(null)); // "None" option
                        if (fetchedEvents != null) {
                            for (Event event : fetchedEvents) {
                                comboEvents.addItem(new EventWrapper(event));
                            }
                        }
                        upcomingEventsList = fetchedEvents; // Store for reference if needed
                        btnSubmitDonation.setEnabled(true); // Enable submit if member and events loaded
                    } else {
                         // This case should ideally be covered by errorMessage != null
                        JOptionPane.showMessageDialog(MakeDonationPanel.this, "Failed to load necessary data.", "Error", JOptionPane.ERROR_MESSAGE);
                        disablePanelFunctionality("Failed to load necessary data.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MakeDonationPanel.this, "Error processing initial data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    disablePanelFunctionality("Error processing initial data.");
                }
            }
        };
        worker.execute();
    }

    private void submitDonationAction() {
        if (currentMember == null) {
            JOptionPane.showMessageDialog(this, "Your member details could not be loaded. Cannot make a donation.", "Member Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (donationService == null) {
            JOptionPane.showMessageDialog(this, "Donation Service is not available.", "Service Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amountStr = txtAmount.getText().trim();
        String notesStr = txtNotes.getText().trim();

        if (amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a donation amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Donation amount must be positive.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format. Please enter a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Donation donation = new Donation();
        donation.setMember(currentMember);
        donation.setAmount(amount);
        donation.setDonationDate(new Timestamp(System.currentTimeMillis()));
        donation.setNotes(notesStr);

        EventWrapper selectedEventWrapper = (EventWrapper) comboEvents.getSelectedItem();
        if (selectedEventWrapper != null && selectedEventWrapper.getEvent() != null) {
            donation.setEventId(selectedEventWrapper.getEvent().getEventId());
        } else {
            donation.setEventId(null); // Explicitly null for general donation
        }

        btnSubmitDonation.setEnabled(false); // Disable during operation

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            private String errorMessage = null;

            @Override
            protected String doInBackground() throws Exception {
                try {
                    return donationService.recordDonation(donation);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error submitting donation: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                btnSubmitDonation.setEnabled(true); // Re-enable button
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(MakeDonationPanel.this, errorMessage, "Donation Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(MakeDonationPanel.this, result, "Donation Submitted", JOptionPane.INFORMATION_MESSAGE);
                        // Clear fields on success
                        txtAmount.setText("");
                        txtNotes.setText("");
                        comboEvents.setSelectedIndex(0); // Reset to "None"
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(MakeDonationPanel.this, "Failed to get donation result: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        worker.execute();
    }
}
