package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Donation; // Client-side model
import service.DonationService; // Client-side RMI interface
import util.UserSession;
import java.util.List;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.Locale; // For currency formatting
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class UserDonationHistoryPanel extends JPanel {

    private JTable donationsHistoryTable;
    private DefaultTableModel historyTableModel;
    private JButton btnRefreshHistory;
    private JLabel lblUserTotalDonations;
    private DonationService donationService;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US); // Or your preferred Locale

    public UserDonationHistoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        boolean serviceInitialized = initializeServices();
        initComponents();

        if (serviceInitialized) {
            loadUserDonationHistory();
        } else {
            disablePanelFunctionality("Could not connect to Donation Service.\nPlease ensure RMI server is running.");
        }
    }

    private boolean initializeServices() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000);
            donationService = (DonationService) registry.lookup("donation");
            System.out.println("DonationService RMI object found for UserDonationHistoryPanel.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            donationService = null;
            return false;
        }
    }

    private void disablePanelFunctionality(String message) {
        if (btnRefreshHistory != null) btnRefreshHistory.setEnabled(false);
        if (lblUserTotalDonations != null) lblUserTotalDonations.setText("Your Total Donations: Service Unavailable");
        if (historyTableModel != null) historyTableModel.setRowCount(0);

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Service Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void initComponents() {
        // Table Setup
        String[] columnNames = {"ID", "Amount", "Date", "Event ID", "Notes"};
        historyTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        donationsHistoryTable = new JTable(historyTableModel);
        donationsHistoryTable.setFillsViewportHeight(true);
        donationsHistoryTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(donationsHistoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // Summary Panel (South)
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

        lblUserTotalDonations = new JLabel("Your Total Donations: Fetching...");
        lblUserTotalDonations.setFont(new Font("Arial", Font.BOLD, 14));

        btnRefreshHistory = new JButton("Refresh History");
        btnRefreshHistory.setFont(new Font("Arial", Font.PLAIN, 12));

        summaryPanel.add(lblUserTotalDonations);
        summaryPanel.add(Box.createHorizontalStrut(20));
        summaryPanel.add(btnRefreshHistory);
        add(summaryPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnRefreshHistory.addActionListener(e -> loadUserDonationHistory());
    }

    private void loadUserDonationHistory() {
        int memberId = UserSession.getInstance().getAccountId();

        if (donationService == null) {
            disablePanelFunctionality("Donation Service is not available.");
            return;
        }
        if (memberId <= 0) {
            // This case might happen if admin logs in (accountId 0) or session error
            lblUserTotalDonations.setText("Your Total Donations: N/A (Invalid User)");
            historyTableModel.setRowCount(0);
            btnRefreshHistory.setEnabled(false); // No point refreshing if user ID is invalid
            JOptionPane.showMessageDialog(this, "Valid user session not found. Cannot load donation history.", "User Session Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (lblUserTotalDonations != null) {
            lblUserTotalDonations.setText("Your Total Donations: Loading...");
        }
        historyTableModel.setRowCount(0);
        btnRefreshHistory.setEnabled(false); // Disable during load

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Donation> userDonations;
            private BigDecimal userTotal;
            private String errorMessage = null;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    userDonations = donationService.getDonationsByMemberId(memberId);
                    userTotal = donationService.getTotalDonationsByMemberId(memberId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error communicating with the server: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                btnRefreshHistory.setEnabled(true); // Re-enable after load attempt

                if (errorMessage != null) {
                    if (lblUserTotalDonations != null) lblUserTotalDonations.setText("Your Total Donations: Error");
                    JOptionPane.showMessageDialog(UserDonationHistoryPanel.this, errorMessage, "RemoteException", JOptionPane.ERROR_MESSAGE);
                    // Keep refresh enabled to allow retry by user
                    return;
                }

                if (userDonations != null) {
                    for (Donation donation : userDonations) {
                        String amountStr = (donation.getAmount() != null)
                                           ? currencyFormatter.format(donation.getAmount()) : "N/A";
                        String dateStr = (donation.getDonationDate() != null)
                                         ? dateFormatter.format(donation.getDonationDate()) : "N/A";
                        String eventIdStr = (donation.getEventId() != null)
                                            ? String.valueOf(donation.getEventId()) : "N/A";

                        historyTableModel.addRow(new Object[]{
                            donation.getDonationId(),
                            amountStr,
                            dateStr,
                            eventIdStr,
                            donation.getNotes() != null ? donation.getNotes() : ""
                        });
                    }
                }
                if (lblUserTotalDonations != null) {
                     lblUserTotalDonations.setText("Your Total Donations: " + currencyFormatter.format(userTotal != null ? userTotal : BigDecimal.ZERO));
                }
            }
        };
        worker.execute();
    }
}
