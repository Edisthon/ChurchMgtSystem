package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Event;
import service.EventService; // Now used
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.List;
// ArrayList might still be needed if service returns null and we want an empty list to avoid NPEs, or for testing.
// For now, assume service returns empty list instead of null for "no events".
// import java.util.ArrayList;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import com.toedter.calendar.JDateChooser; // Assuming this library is available

public class EventsPanel extends JPanel {

    private JTable eventsTable;
    private DefaultTableModel tableModel;
    private JButton btnAddEvent, btnEditEvent, btnDeleteEvent, btnRefreshEvents;
    private EventService eventService; // Service instance

    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public EventsPanel() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000);
            this.eventService = (EventService) registry.lookup("event");
            System.out.println("EventService RMI object found and bound.");
        } catch (Exception e) {
            e.printStackTrace();
            this.eventService = null; // Ensure service is null if lookup fails
            // Message will be shown after components are initialized, or buttons disabled.
        }

        setLayout(new BorderLayout());
        initComponents(); // Initialize components first

        if (this.eventService == null) {
            // Show error message only once after panel is somewhat visible
            SwingUtilities.invokeLater(() -> { // Ensure it runs after constructor finishes and panel is potentially visible
                JOptionPane.showMessageDialog(this,
                        "Error connecting to Event Service: Could not establish RMI connection.\n" +
                        "Please ensure the RMI server is running and accessible on port 6000.\n" +
                        "CRUD operations will be disabled.",
                        "Service Connection Error", JOptionPane.ERROR_MESSAGE);
            });
            disableCrudButtons();
        }
        loadEvents(); // Then load data
    }

    private void disableCrudButtons() {
        btnAddEvent.setEnabled(false);
        btnEditEvent.setEnabled(false);
        btnDeleteEvent.setEnabled(false);
        // btnRefreshEvents could also be disabled, or try to reconnect. For now, let it be.
    }

    private void initComponents() {
        // Table Setup
        String[] columnNames = {"ID", "Name", "Date/Time", "Location", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        eventsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(eventsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center align buttons
        btnAddEvent = new JButton("Add Event");
        btnEditEvent = new JButton("Edit Event");
        btnDeleteEvent = new JButton("Delete Event");
        btnRefreshEvents = new JButton("Refresh");

        buttonPanel.add(btnAddEvent);
        buttonPanel.add(btnEditEvent);
        buttonPanel.add(btnDeleteEvent);
        buttonPanel.add(btnRefreshEvents);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add Action Listeners
        btnAddEvent.addActionListener(e -> openEventDialog(null)); // Listeners added irrespective of service status initially
        btnEditEvent.addActionListener(e -> {
            if (!btnEditEvent.isEnabled()) return; // Check if button is enabled
            Event selectedEvent = getSelectedEvent();
            if (selectedEvent != null) {
                openEventDialog(selectedEvent);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an event to edit.", "No Event Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnDeleteEvent.addActionListener(e -> {
            if (!btnDeleteEvent.isEnabled()) return;
            deleteSelectedEvent();
        });
        btnRefreshEvents.addActionListener(e -> loadEvents()); // Refresh might still work or show service error
    }

    private void loadEvents() {
        if (eventService == null) {
            // Optionally, show a recurring message or rely on the initial one.
            // For now, just clear the table if service is not available.
            tableModel.setRowCount(0);
            // Consider showing a specific panel state like "Service unavailable" in the table area
            // JOptionPane.showMessageDialog(this, "Event Service not available. Cannot load events.", "Service Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            List<Event> events = eventService.retreiveAll(); // Assuming this is the correct method
            tableModel.setRowCount(0); // Clear existing rows
            if (events != null) {
                for (Event event : events) {
                    tableModel.addRow(new Object[]{
                        event.getEventId(),
                        event.getEventName(),
                        dateTimeFormatter.format(event.getEventDateTime()), // Format Timestamp to String
                        event.getLocation(),
                        event.getDescription()
                    });
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            tableModel.setRowCount(0); // Clear table on error
            JOptionPane.showMessageDialog(this, "Error loading events: " + e.getMessage(), "RemoteException", JOptionPane.ERROR_MESSAGE);
            disableCrudButtons(); // Disable buttons again if an error occurs during operation
        }
    }

    private void openEventDialog(Event eventToEdit) {
        if (eventService == null && eventToEdit == null) { // Don't open add dialog if service is down
             JOptionPane.showMessageDialog(this, "Event Service not available. Cannot add event.", "Service Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        // Edit dialog might still be opened with service down to view data, but save will fail.

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Event Details", true);
        dialog.setLayout(new BorderLayout(10, 10)); // Add gaps
        dialog.setSize(500, 400); // Increased size for better layout

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5,5,5,5); // Padding

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; // Allow field to expand
        JTextField txtEventName = new JTextField(20);
        formPanel.add(txtEventName, gbc);
        gbc.weightx = 0; // Reset

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Date/Time:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd HH:mm:ss");
        formPanel.add(dateChooser, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField txtLocation = new JTextField(20);
        formPanel.add(txtLocation, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Align label to top
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH; // Allow textarea to expand
        gbc.weighty = 1.0; // Allow textarea to take vertical space
        JTextArea txtDescription = new JTextArea(5, 20);
        JScrollPane descScrollPane = new JScrollPane(txtDescription);
        formPanel.add(descScrollPane, gbc);
        gbc.weighty = 0; // Reset
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill

        if (eventToEdit != null) {
            txtEventName.setText(eventToEdit.getEventName());
            dateChooser.setDate(eventToEdit.getEventDateTime());
            txtLocation.setText(eventToEdit.getLocation());
            txtDescription.setText(eventToEdit.getDescription());
        }

        JPanel buttonPanelDialog = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        buttonPanelDialog.add(btnSave);
        buttonPanelDialog.add(btnCancel);

        btnSave.addActionListener(e -> {
            String name = txtEventName.getText();
            java.util.Date selectedDate = dateChooser.getDate();
            String location = txtLocation.getText();
            String description = txtDescription.getText();

            if (name.trim().isEmpty() || selectedDate == null || location.trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name, Date/Time, and Location are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Timestamp eventTimestamp = new Timestamp(selectedDate.getTime());

            Event newOrUpdatedEvent = (eventToEdit == null) ? new Event() : eventToEdit;
            // If eventToEdit is not null, its ID is already set.
            // For a new event, ID is usually set by the database/service.

            newOrUpdatedEvent.setEventName(name);
            newOrUpdatedEvent.setEventDateTime(eventTimestamp);
            newOrUpdatedEvent.setLocation(location);
            newOrUpdatedEvent.setDescription(description);

            try {
                if (eventService == null) { // Double check service availability
                    JOptionPane.showMessageDialog(dialog, "Event Service not available. Cannot save event.", "Service Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (eventToEdit == null) {
                    String result = eventService.registerEvent(newOrUpdatedEvent);
                    JOptionPane.showMessageDialog(dialog, "Event registered: " + result, "Event Saved", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String result = eventService.updateEvent(newOrUpdatedEvent);
                    JOptionPane.showMessageDialog(dialog, "Event updated: " + result, "Event Saved", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadEvents(); // Refresh table
            } catch (RemoteException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving event: " + ex.getMessage(), "RemoteException", JOptionPane.ERROR_MESSAGE);
            }
            // No specific ParseException catch here as JDateChooser.getDate() and new Timestamp() are used.
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanelDialog, BorderLayout.SOUTH);
        dialog.pack(); // Adjusts dialog size to fit components
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private Event getSelectedEvent() {
        int selectedRow = eventsTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                String name = (String) tableModel.getValueAt(selectedRow, 1);
                // Date/Time is stored as String in table model, parse it back
                Timestamp dateTime = new Timestamp(dateTimeFormatter.parse((String) tableModel.getValueAt(selectedRow, 2)).getTime());
                String location = (String) tableModel.getValueAt(selectedRow, 3);
                String description = (String) tableModel.getValueAt(selectedRow, 4);

                // Create and return Event object
                // Assumes Event constructor: Event(int id, String name, Timestamp dateTime, String location, String description)
                // Or use setters if a default constructor is used.
                Event event = new Event();
                event.setEventId(id);
                event.setEventName(name);
                event.setEventDateTime(dateTime);
                event.setLocation(location);
                event.setDescription(description);
                return event;

            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Error parsing date from table: " + e.getMessage(), "Date Error", JOptionPane.ERROR_MESSAGE);
                return null;
            } catch (ClassCastException cce) {
                 JOptionPane.showMessageDialog(this, "Error casting data from table: " + cce.getMessage(), "Data Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }

    private void deleteSelectedEvent() {
        Event selectedEvent = getSelectedEvent();
        if (selectedEvent != null) {
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete event: " + selectedEvent.getEventName() + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                 if (eventService == null) {
                    JOptionPane.showMessageDialog(this, "Event Service not available. Cannot delete event.", "Service Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    String result = eventService.deleteEvent(selectedEvent); // Assumes deleteEvent takes Event object or eventId
                    JOptionPane.showMessageDialog(this, "Event '" + selectedEvent.getEventName() + "' deleted: " + result, "Event Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadEvents();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting event: " + ex.getMessage(), "RemoteException", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.", "No Event Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Dummy Event model class should be removed as actual model.Event is expected to be used.
}
