package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Event; // Client-side model
import service.EventService; // Client-side RMI interface
import java.util.List;
import java.util.Date; // For current time comparison
import java.util.stream.Collectors;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class UserEventsPanel extends JPanel {

    private JTable eventsTable;
    private DefaultTableModel tableModel;
    private JButton btnRefreshEvents;
    private EventService eventService;

    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public UserEventsPanel() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000);
            this.eventService = (EventService) registry.lookup("event");
            System.out.println("EventService RMI object found and bound for UserEventsPanel.");
        } catch (Exception e) {
            e.printStackTrace();
            this.eventService = null;
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();

        if (this.eventService == null) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error connecting to Event Service: Could not establish RMI connection.\n" +
                        "Please ensure the RMI server is running and accessible on port 6000.\n" +
                        "Event records cannot be loaded.",
                        "Service Connection Error", JOptionPane.ERROR_MESSAGE);
            });
            disableRefreshButton();
        }
        loadUpcomingEvents();
    }

    private void disableRefreshButton() {
        if (btnRefreshEvents != null) {
            btnRefreshEvents.setEnabled(false);
        }
    }

    private void initComponents() {
        // Table Setup
        String[] columnNames = {"Name", "Date/Time", "Location", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        eventsTable = new JTable(tableModel);
        eventsTable.setFillsViewportHeight(true);
        eventsTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(eventsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel (South)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
        btnRefreshEvents = new JButton("Refresh Upcoming Events");
        btnRefreshEvents.setFont(new Font("Arial", Font.PLAIN, 12));
        buttonPanel.add(btnRefreshEvents);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnRefreshEvents.addActionListener(e -> loadUpcomingEvents());
    }

    private void loadUpcomingEvents() {
        if (eventService == null) {
            tableModel.setRowCount(0);
            disableRefreshButton();
            // Optionally, show a message in the table area or a status bar
            return;
        }
        if (btnRefreshEvents != null) btnRefreshEvents.setEnabled(false); // Disable while loading
        tableModel.setRowCount(0); // Clear table

        SwingWorker<List<Event>, Void> worker = new SwingWorker<List<Event>, Void>() {
            private String errorMessage = null;

            @Override
            protected List<Event> doInBackground() throws Exception {
                try {
                    List<Event> allEvents = eventService.retreiveAll();
                    if (allEvents == null) {
                        return List.of(); // Return empty list if service returns null
                    }
                    // Filter for upcoming events
                    Date currentTime = new Date(); // Current time
                    return allEvents.stream()
                        .filter(event -> event.getEventDateTime() != null && event.getEventDateTime().after(currentTime))
                        .collect(Collectors.toList());
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error communicating with the server: " + e.getMessage();
                    return List.of(); // Return empty list on error
                }
            }

            @Override
            protected void done() {
                if (btnRefreshEvents != null) btnRefreshEvents.setEnabled(true); // Re-enable after loading attempt

                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(UserEventsPanel.this, errorMessage, "RemoteException", JOptionPane.ERROR_MESSAGE);
                    disableRefreshButton(); // Keep it disabled if error occurred
                    return;
                }

                try {
                    List<Event> upcomingEvents = get(); // Get result from doInBackground
                    if (upcomingEvents != null) {
                        for (Event event : upcomingEvents) {
                            tableModel.addRow(new Object[]{
                                event.getEventName(),
                                (event.getEventDateTime() != null) ? dateTimeFormatter.format(event.getEventDateTime()) : "N/A",
                                event.getLocation() != null ? event.getLocation() : "N/A",
                                event.getDescription() != null ? event.getDescription() : ""
                            });
                        }
                    }
                } catch (Exception e) { // Catch exceptions from get() like InterruptedException, ExecutionException
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UserEventsPanel.this, "Error processing events: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
