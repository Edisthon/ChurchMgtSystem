package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Event;
import model.Member;
import model.EventAttendance;
import service.EventService;
import service.EventAttendanceService;
import service.MemberService;
import util.UserSession; // To get current user's details
import java.util.List;
import java.util.Collections; // Added import
import java.util.Date;
import java.util.stream.Collectors;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MarkAttendancePanel extends JPanel {

    private JTable upcomingEventsTable;
    private DefaultTableModel eventsTableModel;
    private JButton btnMarkAttendance, btnRefreshEvents;

    private EventService eventService;
    private EventAttendanceService eventAttendanceService;
    private MemberService memberService; // To fetch current member details

    private Member currentMember; // Store the logged-in member's details

    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MarkAttendancePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        boolean servicesInitialized = initializeServices();
        initComponents(); // Initialize UI components first

        if (servicesInitialized) {
            fetchCurrentMemberAndLoadEvents();
        } else {
            disablePanelFunctionality("Could not connect to one or more services.\nPlease ensure RMI server is running.");
        }
    }

    private boolean initializeServices() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000);
            eventService = (EventService) registry.lookup("event");
            eventAttendanceService = (EventAttendanceService) registry.lookup("eventattendance");
            memberService = (MemberService) registry.lookup("member"); // Assuming "member" is the RMI name
            System.out.println("All services RMI objects found for MarkAttendancePanel.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            eventService = null;
            eventAttendanceService = null;
            memberService = null;
            return false;
        }
    }

    private void disablePanelFunctionality(String message) {
        if (btnMarkAttendance != null) btnMarkAttendance.setEnabled(false);
        if (btnRefreshEvents != null) btnRefreshEvents.setEnabled(false);
        // Display a message on the panel itself or a status bar
        // For simplicity, a JOptionPane will be shown by the calling context if needed.
        // Here, we ensure that if components are already initialized, they get disabled.
        if (upcomingEventsTable != null) eventsTableModel.setRowCount(0); // Clear table
         SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Service Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }


    private void initComponents() {
        // Table Setup
        String[] columnNames = {"Event ID", "Name", "Date/Time", "Location"};
        eventsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        upcomingEventsTable = new JTable(eventsTableModel);
        upcomingEventsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        upcomingEventsTable.setFillsViewportHeight(true);
        upcomingEventsTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(upcomingEventsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel (South)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
        btnMarkAttendance = new JButton("Mark My Attendance");
        btnRefreshEvents = new JButton("Refresh Event List");

        buttonPanel.add(btnMarkAttendance);
        buttonPanel.add(btnRefreshEvents);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnMarkAttendance.addActionListener(e -> markAttendanceAction());
        btnRefreshEvents.addActionListener(e -> fetchCurrentMemberAndLoadEvents());
    }

    private void fetchCurrentMemberAndLoadEvents() {
        if (memberService == null || eventService == null) {
            disablePanelFunctionality("Member or Event service is not available.");
            return;
        }

        btnMarkAttendance.setEnabled(false); // Disable while loading
        btnRefreshEvents.setEnabled(false);
        eventsTableModel.setRowCount(0);

        SwingWorker<List<Event>, Void> worker = new SwingWorker<List<Event>, Void>() {
            private String errorMessage = null;
            private boolean memberFetchSuccess = false;

            @Override
            protected List<Event> doInBackground() throws Exception {
                memberFetchSuccess = false; // Reset flag
                currentMember = null; // Reset current member
                try {
                    // Step 1: Fetch current member using accountId from UserSession
                    int currentUserId = UserSession.getInstance().getAccountId();

                    if (currentUserId <= 0) { // Assuming 0 or negative is not a valid member ID for regular users
                        errorMessage = "Invalid user session. Please log in again.";
                        // System.err.println("MarkAttendancePanel: Invalid accountId for current user: " + currentUserId);
                        return java.util.Collections.emptyList();
                    }

                    // This assumes memberService.getMemberById(int id) exists and is defined in the RMI interface
                    currentMember = memberService.getMemberById(currentUserId);

                    if (currentMember == null) {
                        errorMessage = "Could not retrieve your member details for ID: " + currentUserId;
                        return java.util.Collections.emptyList();
                    }
                    memberFetchSuccess = true;

                    // Step 2: Fetch and filter upcoming events
                    List<Event> allEvents = eventService.retreiveAll();
                    if (allEvents == null) { // Service might return null
                         return java.util.Collections.emptyList();
                    }

                    Date currentTime = new Date(); // Current time, not Timestamp for 'after' comparison with event's Timestamp
                    return allEvents.stream()
                        .filter(event -> event.getEventDateTime() != null && event.getEventDateTime().after(currentTime))
                        .collect(Collectors.toList());

                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error communicating with the server: " + e.getMessage();
                    currentMember = null; // Ensure member is null on error
                    memberFetchSuccess = false;
                    return java.util.Collections.emptyList(); // Return empty list on RMI error
                } catch (Exception e) { // Catch any other unexpected errors during fetch
                    e.printStackTrace();
                    errorMessage = "An unexpected error occurred: " + e.getMessage();
                    currentMember = null;
                    memberFetchSuccess = false;
                    return java.util.Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                btnRefreshEvents.setEnabled(true);
                // Check memberFetchSuccess specifically for enabling btnMarkAttendance
                if (memberFetchSuccess && currentMember != null) {
                    btnMarkAttendance.setEnabled(true);
                } else {
                    btnMarkAttendance.setEnabled(false);
                    if (errorMessage == null) { // If no specific error message was set, but member is null
                        errorMessage = "Could not load your member details. Attendance marking is disabled.";
                    }
                }

                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(MarkAttendancePanel.this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                    // eventsTableModel might already be empty or contain old data; ensure it's clear on error.
                    eventsTableModel.setRowCount(0);
                    return; // Stop further processing in done() if there was an error
                }

                try {
                    List<Event> upcomingEvents = get(); // Get result from doInBackground
                    eventsTableModel.setRowCount(0); // Clear table before populating
                    if (upcomingEvents != null) {
                        for (Event event : upcomingEvents) {
                            eventsTableModel.addRow(new Object[]{
                                event.getEventId(),
                                event.getEventName(),
                                (event.getEventDateTime() != null) ? dateTimeFormatter.format(event.getEventDateTime()) : "N/A",
                                event.getLocation() != null ? event.getLocation() : "N/A"
                            });
                        }
                    }
                } catch (Exception e) { // Catch exceptions from get() like InterruptedException, ExecutionException
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MarkAttendancePanel.this, "Error processing events after fetch: " + e.getMessage(), "Processing Error", JOptionPane.ERROR_MESSAGE);
                    eventsTableModel.setRowCount(0); // Clear table on processing error
                    btnMarkAttendance.setEnabled(false); // Disable if there's an issue displaying events
                }
            }
        };
        worker.execute();
    }

    private void markAttendanceAction() {
        if (currentMember == null) {
            JOptionPane.showMessageDialog(this, "Your member details could not be loaded. Cannot mark attendance.", "Member Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = upcomingEventsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event from the list to mark your attendance.", "No Event Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (eventAttendanceService == null) {
            JOptionPane.showMessageDialog(this, "Attendance Service is not available.", "Service Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        btnMarkAttendance.setEnabled(false); // Disable during operation

        Integer eventId = (Integer) eventsTableModel.getValueAt(selectedRow, 0);
        String eventName = (String) eventsTableModel.getValueAt(selectedRow, 1);


        EventAttendance attendance = new EventAttendance();
        attendance.setMember(currentMember); // Set the fetched member

        Event selectedEvent = new Event(); // Create a shell Event object
        selectedEvent.setEventId(eventId);
        // Optionally set other event details if needed by server, but usually ID is enough for foreign key
        // selectedEvent.setEventName(eventName);
        attendance.setEvent(selectedEvent);

        attendance.setCheckInTime(new Timestamp(System.currentTimeMillis()));

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            private String errorMessage = null;

            @Override
            protected String doInBackground() throws Exception {
                try {
                    return eventAttendanceService.registerEventAttendance(attendance);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error marking attendance: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                btnMarkAttendance.setEnabled(true); // Re-enable button
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(MarkAttendancePanel.this, errorMessage, "Attendance Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(MarkAttendancePanel.this, result, "Attendance Marked", JOptionPane.INFORMATION_MESSAGE);
                        // Optionally, refresh the list or visually indicate marked attendance for the event
                        // loadUpcomingEvents(); // Could refresh, or just update UI if possible
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(MarkAttendancePanel.this, "Failed to get attendance result: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        worker.execute();
    }
}
