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
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
public class DonationsPanel extends JPanel {

    private JTable donationsTable;
    private DefaultTableModel tableModel;
    private JButton btnRefreshDonations; // Will act as "Show All / Refresh"
    private JLabel lblTotalDonations;
    private DonationService donationService;
    private JButton btnExportDonationsPdf; // Added

    // Search components
    private JTextField txtSearchMemberNameForDonation;
    private com.toedter.calendar.JDateChooser dateChooserStartDate;
    private com.toedter.calendar.JDateChooser dateChooserEndDate;
    private JButton btnSearchDonations;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat dateOnlyFormatter = new SimpleDateFormat("yyyy-MM-dd"); // For JDateChooser display
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

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();

        if (this.donationService == null) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                    "Error connecting to Donation Service. Please ensure server is running.",
                    "Service Connection Error", JOptionPane.ERROR_MESSAGE));
            updateComponentStates(false); // Disable all controls
            if (lblTotalDonations != null) lblTotalDonations.setText("Total Donations: Service Unavailable");
        } else {
            loadAllDonationsAndTotal(true); // Initial load
        }
    }

    private void updateComponentStates(boolean enable) {
        if (btnRefreshDonations != null) btnRefreshDonations.setEnabled(enable);
        if (btnSearchDonations != null) btnSearchDonations.setEnabled(enable);
        if (txtSearchMemberNameForDonation != null) txtSearchMemberNameForDonation.setEnabled(enable);
        if (dateChooserStartDate != null) dateChooserStartDate.setEnabled(enable);
        if (dateChooserEndDate != null) dateChooserEndDate.setEnabled(enable);
        if (btnExportDonationsPdf != null) btnExportDonationsPdf.setEnabled(enable && tableModel != null && tableModel.getRowCount() > 0);
    }

    private void initComponents() {
        // --- Search Panel (NORTH) ---
        JPanel searchCriteriaPanel = new JPanel(new GridBagLayout());
        searchCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Filter Donations"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Member Name Search
        gbc.gridx = 0; gbc.gridy = 0;
        searchCriteriaPanel.add(new JLabel("Member Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        txtSearchMemberNameForDonation = new JTextField(20);
        searchCriteriaPanel.add(txtSearchMemberNameForDonation, gbc);
        gbc.weightx = 0; // Reset

        // Start Date
        gbc.gridx = 0; gbc.gridy = 1;
        searchCriteriaPanel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        dateChooserStartDate = new com.toedter.calendar.JDateChooser();
        dateChooserStartDate.setDateFormatString(dateOnlyFormatter.toPattern());
        searchCriteriaPanel.add(dateChooserStartDate, gbc);
        gbc.weightx = 0;

        // End Date
        gbc.gridx = 2; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        searchCriteriaPanel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5;
        dateChooserEndDate = new com.toedter.calendar.JDateChooser();
        dateChooserEndDate.setDateFormatString(dateOnlyFormatter.toPattern());
        searchCriteriaPanel.add(dateChooserEndDate, gbc);
        gbc.weightx = 0;

        // Search Button
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        btnSearchDonations = new JButton("Search Donations");
        searchCriteriaPanel.add(btnSearchDonations, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; // Reset

        add(searchCriteriaPanel, BorderLayout.NORTH);

        // --- Table Setup (CENTER) ---
        String[] columnNames = {"ID", "Donor Name", "Amount", "Date", "Event ID", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        donationsTable = new JTable(tableModel);
        donationsTable.setFillsViewportHeight(true);
        donationsTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(donationsTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Summary Panel (SOUTH) ---
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

        lblTotalDonations = new JLabel("Total Donations: Fetching...");
        lblTotalDonations.setFont(new Font("Arial", Font.BOLD, 14));

        btnRefreshDonations = new JButton("Show All / Refresh");
        btnRefreshDonations.setFont(new Font("Arial", Font.PLAIN, 12));

        btnExportDonationsPdf = new JButton("Export to PDF"); // Instantiate
        btnExportDonationsPdf.setFont(new Font("Arial", Font.PLAIN, 12));

        summaryPanel.add(lblTotalDonations);
        summaryPanel.add(Box.createHorizontalStrut(20));
        summaryPanel.add(btnRefreshDonations);
        summaryPanel.add(btnExportDonationsPdf); // Add to panel
        add(summaryPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnSearchDonations.addActionListener(e -> searchDonationsAction());
        btnRefreshDonations.addActionListener(e -> loadAllDonationsAndTotal(true));
        btnExportDonationsPdf.addActionListener(e -> exportDonationsToPdf()); // Add listener
    }

    private void updateDonationsDisplay(List<Donation> donations, BigDecimal totalAmount, String totalLabelPrefix) {
        tableModel.setRowCount(0); // Clear existing data
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
            lblTotalDonations.setText(totalLabelPrefix + currencyFormatter.format(totalAmount != null ? totalAmount : BigDecimal.ZERO));
        }
    }

    private void loadAllDonationsAndTotal(boolean clearSearchFields) {
        if (donationService == null) {
            updateComponentStates(false);
            if (lblTotalDonations != null) lblTotalDonations.setText("Overall Total: Service Unavailable");
            return;
        }

        if (clearSearchFields) {
            txtSearchMemberNameForDonation.setText("");
            dateChooserStartDate.setDate(null);
            dateChooserEndDate.setDate(null);
        }

        updateComponentStates(false); // Disable controls during load
        if (lblTotalDonations != null) lblTotalDonations.setText("Overall Total: Loading...");


        SwingWorker<List<Donation>, Void> worker = new SwingWorker<List<Donation>, Void>() {
            private BigDecimal overallTotal;
            private String errorMessage = null;

            @Override
            protected List<Donation> doInBackground() throws Exception {
                try {
                    overallTotal = donationService.getTotalDonations();
                    return donationService.getAllDonations();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error loading all donations: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                updateComponentStates(true); // Re-enable controls
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(DonationsPanel.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                    updateDonationsDisplay(null, BigDecimal.ZERO, "Overall Total: Error");
                } else {
                    try {
                        List<Donation> allDonations = get();
                        updateDonationsDisplay(allDonations, overallTotal, "Overall Total: ");
                    } catch (Exception e) {
                         e.printStackTrace();
                         JOptionPane.showMessageDialog(DonationsPanel.this, "Error processing donation list: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                         updateDonationsDisplay(null, BigDecimal.ZERO, "Overall Total: Error");
                    }
                }
            }
        };
        worker.execute();
    }

    private void searchDonationsAction() {
        if (donationService == null) {
            JOptionPane.showMessageDialog(this, "Donation Service not available.", "Service Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String memberName = txtSearchMemberNameForDonation.getText().trim();
        java.util.Date utilStartDate = dateChooserStartDate.getDate();
        java.util.Date utilEndDate = dateChooserEndDate.getDate();
        Timestamp startDate = (utilStartDate != null) ? new Timestamp(utilStartDate.getTime()) : null;
        Timestamp endDate = (utilEndDate != null) ? new Timestamp(utilEndDate.getTime()) : null;

        if (memberName.isEmpty() && startDate == null && endDate == null) {
            JOptionPane.showMessageDialog(this, "Please enter at least one search criterion.", "Search Error", JOptionPane.WARNING_MESSAGE);
            loadAllDonationsAndTotal(false); // Show all if no criteria
            return;
        }
        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            JOptionPane.showMessageDialog(this, "Please provide both start and end dates for a date range search.", "Date Range Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date.", "Date Range Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        updateComponentStates(false); // Disable controls during search
        if (lblTotalDonations != null) lblTotalDonations.setText("Search Total: Loading...");


        SwingWorker<List<Donation>, Void> worker = new SwingWorker<List<Donation>, Void>() {
            private BigDecimal searchTotal;
            private String errorMessage = null;

            @Override
            protected List<Donation> doInBackground() throws Exception {
                try {
                    List<Donation> results;
                    if (!memberName.isEmpty() && startDate == null) { // Name only
                        results = donationService.getDonationsByMemberName(memberName);
                        searchTotal = results.stream().map(Donation::getAmount).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                    } else if (memberName.isEmpty() && startDate != null) { // Date range only
                        results = donationService.getDonationsByDateRange(startDate, endDate);
                        searchTotal = donationService.getTotalDonationsForDateRange(startDate, endDate);
                    } else { // Name and Date range
                        List<Donation> nameResults = donationService.getDonationsByMemberName(memberName);
                        if (nameResults != null) {
                            results = nameResults.stream()
                                .filter(d -> d.getDonationDate() != null &&
                                             !d.getDonationDate().before(startDate) &&
                                             !d.getDonationDate().after(endDate))
                                .collect(java.util.stream.Collectors.toList());
                            searchTotal = results.stream().map(Donation::getAmount).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                        } else {
                            results = new java.util.ArrayList<>();
                            searchTotal = BigDecimal.ZERO;
                        }
                    }
                    return results;
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error during search: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                updateComponentStates(true); // Re-enable controls
                 if (errorMessage != null) {
                    JOptionPane.showMessageDialog(DonationsPanel.this, errorMessage, "Search Error", JOptionPane.ERROR_MESSAGE);
                    updateDonationsDisplay(null, BigDecimal.ZERO, "Search Total: Error");
                } else {
                    try {
                        List<Donation> searchResults = get();
                        updateDonationsDisplay(searchResults, searchTotal, "Search Total: ");
                        if (searchResults == null || searchResults.isEmpty()) {
                             JOptionPane.showMessageDialog(DonationsPanel.this, "No donations found matching your criteria.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                         e.printStackTrace();
                         JOptionPane.showMessageDialog(DonationsPanel.this, "Error processing search results: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                         updateDonationsDisplay(null, BigDecimal.ZERO, "Search Total: Error");
                    }
                }
            }
        };
        worker.execute();
    }
    private void exportDonationsToPdf() {
    if (tableModel.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "No data to export.", "Export Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save Donation Report PDF");
    fileChooser.setSelectedFile(new File("DonationReport.pdf"));
    javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents", "pdf");
    fileChooser.setFileFilter(filter);

    int userSelection = fileChooser.showSaveDialog(this);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
        }

        if (fileToSave.exists()) {
            int response = JOptionPane.showConfirmDialog(this,
                            "The file already exists. Do you want to overwrite it?",
                            "Confirm Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            float margin = 40; // Slightly smaller margin
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableTop = yStart - 25;
            float yPosition = tableTop;
            float bottomMargin = 60;
            float lineHeight = 14f;
            // "ID", "Donor Name", "Amount", "Date", "Event ID", "Notes"
            float[] columnWidths = {30, 130, 80, 110, 60, 120};
            int rowsPerPage = (int) ((tableTop - bottomMargin - (lineHeight * 2)) / lineHeight); // Reserve space for headers and total

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Title
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText("Donation Report");
            contentStream.endText();
            yPosition -= 30;

            // Draw headers
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            float x = margin;
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                contentStream.beginText();
                contentStream.newLineAtOffset(x, yPosition);
                contentStream.showText(tableModel.getColumnName(i));
                contentStream.endText();
                x += columnWidths[i];
            }
            yPosition -= lineHeight * 1.5f;

            // Table Data
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            int rowsWrittenOnPage = 0;

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                if (rowsWrittenOnPage >= rowsPerPage && row < tableModel.getRowCount() - 1) { // Check if there's more data for new page
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - margin - 20; // Reset Y for new page

                    // Re-draw headers for the new page
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                    x = margin;
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(x, yPosition);
                        contentStream.showText(tableModel.getColumnName(i));
                        contentStream.endText();
                        x += columnWidths[i];
                    }
                    yPosition -= lineHeight * 1.5f;
                    contentStream.setFont(PDType1Font.HELVETICA, 9);
                    rowsWrittenOnPage = 0;
                }

                float currentX = margin;
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object cellValue = tableModel.getValueAt(row, col);
                    String text = (cellValue != null) ? cellValue.toString() : "";

                    float colWidth = columnWidths[col] - 2;
                    float textWidth = PDType1Font.HELVETICA.getStringWidth(text) / 1000 * 9f;
                    if (textWidth > colWidth) {
                        StringBuilder sb = new StringBuilder();
                        for (char c : text.toCharArray()) {
                            if (PDType1Font.HELVETICA.getStringWidth(sb.toString() + c) / 1000 * 9f < colWidth - (PDType1Font.HELVETICA.getStringWidth("...")/1000 * 9f)) {
                                sb.append(c);
                            } else {
                                break;
                            }
                        }
                        text = sb.toString() + "...";
                    }
                    contentStream.beginText();
                    contentStream.newLineAtOffset(currentX, yPosition);
                    contentStream.showText(text);
                    contentStream.endText();
                    currentX += columnWidths[col];
                }
                yPosition -= lineHeight;
                rowsWrittenOnPage++;
            }

            // Write Summary Total
            yPosition -= lineHeight * 2; // Extra space before total
            if (yPosition < bottomMargin) { // Check if new page needed for total
                contentStream.close();
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                yPosition = page.getMediaBox().getHeight() - margin - 20;
            }
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(lblTotalDonations.getText()); // Get text from the label
            contentStream.endText();

            contentStream.close();
            document.save(fileToSave);
            JOptionPane.showMessageDialog(this, "Donation report exported successfully to:\n" + fileToSave.getAbsolutePath(), "PDF Export Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}





}
