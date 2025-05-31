package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.EventAttendance; // Assuming model.EventAttendance exists
import model.Event;           // Assuming model.Event exists
import model.Member;          // Assuming model.Member exists
import service.EventAttendanceService; // Assuming service.EventAttendanceService interface exists
import java.util.List;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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


public class AttendancePanel extends JPanel {

    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JButton btnRefreshAttendance;
    private JButton btnExportAttendancePdf; // Added export button
    private EventAttendanceService attendanceService;

    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AttendancePanel() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000); // RMI server details
            this.attendanceService = (EventAttendanceService) registry.lookup("eventattendance"); // Service name
            System.out.println("EventAttendanceService RMI object found and bound.");
        } catch (Exception e) {
            e.printStackTrace();
            this.attendanceService = null;
        }

        setLayout(new BorderLayout());
        initComponents();

        if (this.attendanceService == null) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error connecting to Event Attendance Service: Could not establish RMI connection.\n" +
                        "Please ensure the RMI server is running and accessible on port 6000.\n" +
                        "Attendance records cannot be loaded.",
                        "Service ConnectionError", JOptionPane.ERROR_MESSAGE);
            });
            updateComponentStates(false); // Use new method name
        }
        loadAttendanceRecords();
    }

    // Renamed and updated to manage all relevant component states
    private void updateComponentStates(boolean enable) {
        if (btnRefreshAttendance != null) btnRefreshAttendance.setEnabled(enable);
        // Export button state depends on table content as well, handled after data load
        // Initially set based on 'enable' flag, but will be fine-tuned after data loads.
        if (btnExportAttendancePdf != null) btnExportAttendancePdf.setEnabled(enable && tableModel != null && tableModel.getRowCount() > 0);
    }

    private void initComponents() {
        // Table Setup
        String[] columnNames = {"Attendance ID", "Event Name", "Member Name", "Check-in Time"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        attendanceTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRefreshAttendance = new JButton("Refresh Attendance");
        btnExportAttendancePdf = new JButton("Export to PDF"); // Instantiate

        buttonPanel.add(btnRefreshAttendance);
        buttonPanel.add(btnExportAttendancePdf); // Add to panel
        add(buttonPanel, BorderLayout.SOUTH);

        // Add Action Listeners
        btnRefreshAttendance.addActionListener(e -> loadAttendanceRecords()); // This should call the SwingWorker version
        btnExportAttendancePdf.addActionListener(e -> exportAttendanceToPdf());
    }

    private void loadAttendanceRecords() { // This method will now use SwingWorker
        if (attendanceService == null) {
            tableModel.setRowCount(0);
            updateComponentStates(false);
            return;
        }
        updateComponentStates(false); // Disable controls during load

        SwingWorker<List<EventAttendance>, Void> worker = new SwingWorker<List<EventAttendance>, Void>() {
            private String errorMessage = null;

            @Override
            protected List<EventAttendance> doInBackground() throws Exception {
                try {
                    return attendanceService.retreiveAll();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error loading attendance records: " + e.getMessage();
                    return java.util.Collections.emptyList(); // Use Collections.emptyList()
                }
            }

            @Override
            protected void done() {
                updateComponentStates(true); // Re-enable general controls
                tableModel.setRowCount(0); // Clear table before populating

                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(AttendancePanel.this, errorMessage, "RemoteException", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        List<EventAttendance> attendanceRecords = get();
                        if (attendanceRecords != null) {
                            for (EventAttendance record : attendanceRecords) {
                                String eventName = "N/A";
                                if (record.getEvent() != null && record.getEvent().getEventName() != null) {
                                    eventName = record.getEvent().getEventName();
                                }
                                String memberName = "N/A";
                                if (record.getMember() != null && record.getMember().getFullName() != null) {
                                    memberName = record.getMember().getFullName();
                                }
                                String checkInTimeStr = "N/A";
                                if (record.getCheckInTime() != null) {
                                    checkInTimeStr = dateTimeFormatter.format(record.getCheckInTime());
                                }
                                tableModel.addRow(new Object[]{
                                    record.getAttendanceId(),
                                    eventName,
                                    memberName,
                                    checkInTimeStr
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(AttendancePanel.this, "Error processing attendance data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                // Final check for export button state based on actual table content
                if (btnExportAttendancePdf != null) {
                    btnExportAttendancePdf.setEnabled(tableModel.getRowCount() > 0 && attendanceService != null);
                }
            }
        };
        worker.execute();
    }

    // Removed the redundant loadAttendanceRecordsWithWorker() method

    private void exportAttendanceToPdf() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Attendance Records PDF");
        fileChooser.setSelectedFile(new File("AttendanceRecords.pdf"));
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

                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float tableTop = yStart - 20;
                float yPosition = tableTop;
                float bottomMargin = 70;
                float lineHeight = 15f;
                int rowsPerPage = (int) ((tableTop - bottomMargin) / lineHeight) -1;

                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yStart);
                contentStream.showText("Attendance Records");
                contentStream.endText();
                yPosition -= 30;

                PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                float headerFontSize = 10f;
                // "Attendance ID", "Event Name", "Member Name", "Check-in Time"
                float[] columnWidths = {80, 150, 150, 120};

                Runnable drawHeaders = () -> {
                    try {
                        contentStream.setFont(headerFont, headerFontSize);
                        float x = margin;
                        for (int i = 0; i < tableModel.getColumnCount(); i++) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(x, yPosition);
                            contentStream.showText(tableModel.getColumnName(i));
                            contentStream.endText();
                            x += columnWidths[i];
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                };

                drawHeaders.run();
                yPosition -= lineHeight * 1.5f;

                PDType1Font dataFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float dataFontSize = 9f;
                contentStream.setFont(dataFont, dataFontSize);
                int rowsWrittenOnPage = 0;

                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    if (rowsWrittenOnPage >= rowsPerPage) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin - 20;
                        drawHeaders.run();
                        yPosition -= lineHeight * 1.5f;
                        contentStream.setFont(dataFont, dataFontSize);
                        rowsWrittenOnPage = 0;
                    }

                    float currentX = margin;
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object cellValue = tableModel.getValueAt(row, col);
                        String text = (cellValue != null) ? cellValue.toString() : "";

                        float colWidth = columnWidths[col] - 2;
                        float textWidth = dataFont.getStringWidth(text) / 1000 * dataFontSize;
                        if (textWidth > colWidth) {
                            StringBuilder sb = new StringBuilder();
                            for (char c : text.toCharArray()) {
                                if (dataFont.getStringWidth(sb.toString() + c) / 1000 * dataFontSize < colWidth - (dataFont.getStringWidth("...")/1000 * dataFontSize) ) {
                                    sb.append(c);
                                } else { break; }
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

                contentStream.close();
                document.save(fileToSave);
                JOptionPane.showMessageDialog(this, "Attendance records exported to:\n" + fileToSave.getAbsolutePath(), "PDF Export Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
