package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Donation; // Client-side model
import model.Member;  // Client-side model
import service.DonationService; // Client-side RMI interface
import java.util.List;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.util.Locale; // For currency formatting
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class DonationsPanel extends JPanel {

    private JTable donationsTable;
    private DefaultTableModel tableModel;
    private JButton btnRefreshDonations;
    private JLabel lblTotalDonations;
    private DonationService donationService;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US); // Or your preferred Locale

    public DonationsPanel() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000);
            this.donationService = (DonationService) registry.lookup("donation");
            System.out.println("DonationService RMI object found and bound for DonationsPanel.");
        } catch (Exception e) {
            e.printStackTrace();
            this.donationService = null;
        }

        setLayout(new BorderLayout(10, 10)); // Add some gaps
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Add padding to the panel
        initComponents();

        if (this.donationService == null) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error connecting to Donation Service: Could not establish RMI connection.\n" +
                        "Please ensure the RMI server is running and accessible on port 6000.\n" +
                        "Donation records cannot be loaded.",
                        "Service Connection Error", JOptionPane.ERROR_MESSAGE);
            });
            disableRefreshButton();
            if(lblTotalDonations != null) lblTotalDonations.setText("Total Donations: Service Unavailable");
        }
        loadDonationRecords();
    }

    private void disableRefreshButton() {
        if (btnRefreshDonations != null) {
            btnRefreshDonations.setEnabled(false);
        }
    }

    private void initComponents() {
        // Table Setup
        String[] columnNames = {"ID", "Donor Name", "Amount", "Date", "Event ID", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        donationsTable = new JTable(tableModel);
        donationsTable.setFillsViewportHeight(true); // Ensure table fills viewport
        donationsTable.setRowHeight(25); // Increase row height for better readability
        JScrollPane scrollPane = new JScrollPane(donationsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Summary Panel (South)
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5)); // Align left with gaps
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0)); // Top padding for summary

        lblTotalDonations = new JLabel("Total Donations: Fetching...");
        lblTotalDonations.setFont(new Font("Arial", Font.BOLD, 14));

        btnRefreshDonations = new JButton("Refresh List");
        btnRefreshDonations.setFont(new Font("Arial", Font.PLAIN, 12));

        summaryPanel.add(lblTotalDonations);
        summaryPanel.add(Box.createHorizontalStrut(20)); // Space between label and button
        summaryPanel.add(btnRefreshDonations);
        add(summaryPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnRefreshDonations.addActionListener(e -> loadDonationRecords());
    }

    private void loadDonationRecords() {
        if (donationService == null) {
            if (lblTotalDonations != null) { // Check if label is initialized
                 lblTotalDonations.setText("Total Donations: Service Unavailable");
            }
            tableModel.setRowCount(0);
            disableRefreshButton(); // Ensure button is disabled
            return;
        }

        if (lblTotalDonations != null) {
            lblTotalDonations.setText("Total Donations: Loading...");
        }
        tableModel.setRowCount(0); // Clear table before loading

        // Perform service calls in a background thread to keep UI responsive
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Donation> donations;
            private BigDecimal total;
            private String errorMessage = null;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    donations = donationService.getAllDonations();
                    total = donationService.getTotalDonations();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error communicating with the server: " + e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMessage != null) {
                    if (lblTotalDonations != null) lblTotalDonations.setText("Total Donations: Error");
                    JOptionPane.showMessageDialog(DonationsPanel.this, errorMessage, "RemoteException", JOptionPane.ERROR_MESSAGE);
                    disableRefreshButton();
                    return;
                }

                if (donations != null) {
                    for (Donation donation : donations) {
                        String donorName = (donation.getMember() != null && donation.getMember().getFullName() != null)
                                           ? donation.getMember().getFullName() : "N/A";
                        String amountStr = (donation.getAmount() != null)
                                           ? currencyFormatter.format(donation.getAmount()) : "N/A";
                        String dateStr = (donation.getDonationDate() != null)
                                         ? dateFormatter.format(donation.getDonationDate()) : "N/A";
                        String eventIdStr = (donation.getEventId() != null)
                                            ? String.valueOf(donation.getEventId()) : "N/A";

                        tableModel.addRow(new Object[]{
                            donation.getDonationId(),
                            donorName,
                            amountStr,
                            dateStr,
                            eventIdStr,
                            donation.getNotes() != null ? donation.getNotes() : ""
                        });
                    }
                }
                if (lblTotalDonations != null) {
                     lblTotalDonations.setText("Total Donations: " + currencyFormatter.format(total != null ? total : BigDecimal.ZERO));
                }
                 if (btnRefreshDonations != null) btnRefreshDonations.setEnabled(true); // Re-enable after successful load
            }
        };
        worker.execute();
    }
}
