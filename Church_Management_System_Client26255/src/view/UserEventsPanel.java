package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Event; // Client-side model
import service.EventService; // Client-side RMI interface
import java.util.List;
import java.util.Collections; // Added import
import java.util.Date; // For current time comparison
import java.util.stream.Collectors;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import javax.swing.table.TableRowSorter;

public class UserEventsPanel extends JPanel {

    private JTable eventsTable;
    private DefaultTableModel tableModel;
    private JButton btnRefreshEvents;
    private EventService eventService;

    // Search components
    private JTextField txtSearchEventName;
    private JButton btnSearchUserEvents;
    private JButton btnClearUserEventSearch;
    private TableRowSorter<DefaultTableModel> sorter;

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
            updateComponentStates(false); // Disable all controls if service fails
        }
        loadUpcomingEvents(); // Initial load
    }

    private void updateComponentStates(boolean enable) {
        if (btnRefreshEvents != null) btnRefreshEvents.setEnabled(enable);
        if (txtSearchEventName != null) txtSearchEventName.setEnabled(enable);
        if (btnSearchUserEvents != null) btnSearchUserEvents.setEnabled(enable);
        if (btnClearUserEventSearch != null) btnClearUserEventSearch.setEnabled(enable);
    }

    private void initComponents() {
        // --- Search Panel (NORTH) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0,0,5,0)); // Bottom padding for search panel

        txtSearchEventName = new JTextField(25);
        btnSearchUserEvents = new JButton("Search Events");
        btnClearUserEventSearch = new JButton("Clear Search / Show All");

        searchPanel.add(new JLabel("Search by Name:"));
        searchPanel.add(txtSearchEventName);
        searchPanel.add(btnSearchUserEvents);
        searchPanel.add(btnClearUserEventSearch);
        add(searchPanel, BorderLayout.NORTH);

        // --- Table Setup (CENTER) ---
        String[] columnNames = {"Name", "Date/Time", "Location", "Description"}; // Name is at index 0
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        eventsTable = new JTable(tableModel);
        eventsTable.setFillsViewportHeight(true);
        eventsTable.setRowHeight(25);

        sorter = new TableRowSorter<>(tableModel);
        eventsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(eventsTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Button Panel (SOUTH) ---
        // Re-using btnRefreshEvents. Its text could be "Refresh / Show All"
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
        btnRefreshEvents = new JButton("Refresh Events from Server");
        btnRefreshEvents.setFont(new Font("Arial", Font.PLAIN, 12));
        buttonPanel.add(btnRefreshEvents);
        add(buttonPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnSearchUserEvents.addActionListener(e -> filterUserEventsAction());
        btnClearUserEventSearch.addActionListener(e -> clearUserEventFilterAction());
        btnRefreshEvents.addActionListener(e -> {
            clearUserEventFilterAction(); // Clear filter before loading all
            loadUpcomingEvents();
        });
    }

    private void filterUserEventsAction() {
        String searchTerm = txtSearchEventName.getText().trim();
        if (searchTerm.isEmpty()) {
            clearUserEventFilterAction();
        } else {
            // (?i) for case-insensitive. Pattern.quote to treat searchTerm literally.
            // Assuming "Name" is the first column (index 0)
            RowFilter<DefaultTableModel, Object> eventNameFilter = RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(searchTerm), 0);
            sorter.setRowFilter(eventNameFilter);
        }
    }

    private void clearUserEventFilterAction() {
        txtSearchEventName.setText("");
        if (sorter != null) {
            sorter.setRowFilter(null);
        }
    }

    private void loadUpcomingEvents() {
        if (eventService == null) {
            tableModel.setRowCount(0);
            updateComponentStates(false);
            return;
        }
        updateComponentStates(false); // Disable controls during load
        // Ensure filter is cleared when refreshing from server
        if (sorter != null) sorter.setRowFilter(null);
        if (txtSearchEventName != null) txtSearchEventName.setText("");


        SwingWorker<List<Event>, Void> worker = new SwingWorker<List<Event>, Void>() {
            private String errorMessage = null;

            @Override
            protected List<Event> doInBackground() throws Exception {
                try {
                    List<Event> allEvents = eventService.retreiveAll();
                    if (allEvents == null) {
                        return java.util.Collections.emptyList();
                    }
                    Date currentTime = new Date();
                    return allEvents.stream()
                        .filter(event -> event.getEventDateTime() != null && event.getEventDateTime().after(currentTime))
                        .collect(Collectors.toList());
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error communicating with the server: " + e.getMessage();
                    return java.util.Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                updateComponentStates(true); // Re-enable controls
                tableModel.setRowCount(0); // Clear table before populating with fresh data

                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(UserEventsPanel.this, errorMessage, "RemoteException", JOptionPane.ERROR_MESSAGE);
                    // updateComponentStates(false) was already called before worker.execute()
                    // if an error occurs, we might want to keep controls disabled, or enable refresh.
                    // For now, updateComponentStates(true) is called, allowing retry.
                    return;
                }

                try {
                    List<Event> upcomingEvents = get();
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
                    // No need to re-apply filter here, as loadUpcomingEvents implies showing all (upcoming)
                    // and clearing the filter.
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UserEventsPanel.this, "Error processing events: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
