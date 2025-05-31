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

public class AttendancePanel extends JPanel {

    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JButton btnRefreshAttendance;
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
                        "Service Connection Error", JOptionPane.ERROR_MESSAGE);
            });
            disableRefreshButton();
        }
        loadAttendanceRecords();
    }

    private void disableRefreshButton() {
        if (btnRefreshAttendance != null) { // Check if button is initialized
            btnRefreshAttendance.setEnabled(false);
        }
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
        buttonPanel.add(btnRefreshAttendance);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add Action Listeners
        btnRefreshAttendance.addActionListener(e -> loadAttendanceRecords());
    }

    private void loadAttendanceRecords() {
        if (attendanceService == null) {
            tableModel.setRowCount(0); // Clear table if service is not available
            // Initial error message is shown in constructor, subsequent attempts will just show empty table.
            // Or, show a specific message here too.
            // JOptionPane.showMessageDialog(this, "Attendance Service not available. Cannot load records.", "Service Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<EventAttendance> attendanceRecords = attendanceService.retreiveAll(); // Assuming this method exists
            tableModel.setRowCount(0); // Clear existing rows

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
        } catch (RemoteException e) {
            e.printStackTrace();
            tableModel.setRowCount(0); // Clear table on error
            JOptionPane.showMessageDialog(this, "Error loading attendance records: " + e.getMessage(), "RemoteException", JOptionPane.ERROR_MESSAGE);
            disableRefreshButton(); // Disable button if an error occurs during operation
        }
    }
}
