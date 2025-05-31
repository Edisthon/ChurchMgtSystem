package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Member; // Assuming model.Member exists
import service.MemberService; // Assuming service.MemberService interface exists
import java.util.List;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import com.toedter.calendar.JDateChooser; // Assuming this library is available
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class MembersPanel extends JPanel {

    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JButton btnAddMember, btnEditMember, btnDeleteMember, btnRefreshMembers;
    private MemberService memberService;

    // Search components
    private JTextField txtSearchMemberName;
    private JButton btnSearchMember;
    private JButton btnShowAllMembers;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public MembersPanel() {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 6000); // Assuming RMI server is on localhost, port 6000
            this.memberService = (MemberService) registry.lookup("member"); // Service name "member"
            System.out.println("MemberService RMI object found and bound.");
        } catch (Exception e) {
            e.printStackTrace();
            this.memberService = null;
        }

        setLayout(new BorderLayout());
        initComponents();

        if (this.memberService == null) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Error connecting to Member Service: Could not establish RMI connection.\n" +
                        "Please ensure the RMI server is running and accessible on port 6000.\n" +
                        "CRUD operations will be disabled.",
                        "Service Connection Error", JOptionPane.ERROR_MESSAGE);
            });
            disableCrudButtons();
        }
        loadMembers();
    }

    private void disableCrudButtons() {
        btnAddMember.setEnabled(false);
        btnEditMember.setEnabled(false);
        btnDeleteMember.setEnabled(false);
        // btnRefreshMembers can also be considered part of CRUD enablement
        if(btnRefreshMembers != null) btnRefreshMembers.setEnabled(false);
        if(btnSearchMember != null) btnSearchMember.setEnabled(false);
        if(btnShowAllMembers != null) btnShowAllMembers.setEnabled(false);
    }

    private void initComponents() {
        // Search Panel (North)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        txtSearchMemberName = new JTextField(25); // Increased size
        btnSearchMember = new JButton("Search by Name");
        btnShowAllMembers = new JButton("Show All Members");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearchMemberName);
        searchPanel.add(btnSearchMember);
        searchPanel.add(btnShowAllMembers);
        add(searchPanel, BorderLayout.NORTH);

        // Table Setup
        String[] columnNames = {"ID", "Full Name", "Gender", "Phone", "Birthdate", "Group ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        membersTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(membersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAddMember = new JButton("Add Member");
        btnEditMember = new JButton("Edit Member");
        btnDeleteMember = new JButton("Delete Member");
        btnRefreshMembers = new JButton("Refresh");

        buttonPanel.add(btnAddMember);
        buttonPanel.add(btnEditMember);
        buttonPanel.add(btnDeleteMember);
        buttonPanel.add(btnRefreshMembers);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add Action Listeners
        btnAddMember.addActionListener(e -> openMemberDialog(null));
        btnEditMember.addActionListener(e -> {
            if (!btnEditMember.isEnabled()) return;
            Member selectedMember = getSelectedMember();
            if (selectedMember != null) {
                openMemberDialog(selectedMember);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a member to edit.", "No Member Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnDeleteMember.addActionListener(e -> {
            if (!btnDeleteMember.isEnabled()) return;
            deleteSelectedMember();
        });
        btnRefreshMembers.addActionListener(e -> loadMembers()); // Refresh still loads all
        btnSearchMember.addActionListener(e -> searchMembersAction());
        btnShowAllMembers.addActionListener(e -> loadMembers()); // Show All also loads all
    }

    private void populateTableWithMembers(List<Member> members) {
        tableModel.setRowCount(0);
        if (members != null) {
            for (Member member : members) {
                tableModel.addRow(new Object[]{
                    member.getMemberId(),
                    member.getFullName(),
                    member.getGender(),
                    member.getPhoneNumber(),
                    (member.getBirthdate() != null) ? dateFormatter.format(member.getBirthdate()) : null,
                    member.getGroupId()
                });
            }
        }
    }

    private void loadMembers() {
        if (memberService == null) {
            tableModel.setRowCount(0);
            disableCrudButtons(); // Ensure all relevant buttons are disabled
            // Initial error message shown in constructor
            return;
        }

        btnRefreshMembers.setEnabled(false); // Disable during load
        btnSearchMember.setEnabled(false);
        btnShowAllMembers.setEnabled(false);

        SwingWorker<List<Member>, Void> worker = new SwingWorker<List<Member>, Void>() {
            private String errorMessage = null;
            @Override
            protected List<Member> doInBackground() throws Exception {
                try {
                    return memberService.retreiveAll();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error loading members: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                btnRefreshMembers.setEnabled(true);
                btnSearchMember.setEnabled(true);
                btnShowAllMembers.setEnabled(true);
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(MembersPanel.this, errorMessage, "RemoteException", JOptionPane.ERROR_MESSAGE);
                    disableCrudButtons(); // Re-disable if error
                } else {
                    try {
                        List<Member> members = get();
                        populateTableWithMembers(members);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(MembersPanel.this, "Error processing members list: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        worker.execute();
    }

    private void searchMembersAction() {
        if (memberService == null) {
            JOptionPane.showMessageDialog(this, "Member Service not available.", "Service Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String searchTerm = txtSearchMemberName.getText().trim();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a name to search.", "Search Error", JOptionPane.WARNING_MESSAGE);
            loadMembers(); // Or just return / clear table
            return;
        }

        btnSearchMember.setEnabled(false);
        btnRefreshMembers.setEnabled(false); // Disable other buttons during search
        btnShowAllMembers.setEnabled(false);


        SwingWorker<List<Member>, Void> worker = new SwingWorker<List<Member>, Void>() {
            private String errorMessage = null;
            @Override
            protected List<Member> doInBackground() throws Exception {
                try {
                    return memberService.searchMembersByName(searchTerm);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    errorMessage = "Error searching members: " + e.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                btnSearchMember.setEnabled(true);
                btnRefreshMembers.setEnabled(true);
                btnShowAllMembers.setEnabled(true);
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(MembersPanel.this, errorMessage, "Search Exception", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        List<Member> members = get();
                        populateTableWithMembers(members);
                        if (members == null || members.isEmpty()) {
                            JOptionPane.showMessageDialog(MembersPanel.this, "No members found matching '" + searchTerm + "'.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(MembersPanel.this, "Error processing search results: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        worker.execute();
    }

    private void openMemberDialog(Member memberToEdit) {
        if (memberService == null && memberToEdit == null) {
             JOptionPane.showMessageDialog(this, "Member Service not available. Cannot add member.", "Service Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Member Details", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 450);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5,5,5,5);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        JTextField txtFullName = new JTextField(20);
        formPanel.add(txtFullName, gbc);
        gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JComboBox<String> comboGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        formPanel.add(comboGender, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JTextField txtPhoneNumber = new JTextField(20);
        formPanel.add(txtPhoneNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Birthdate:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        JDateChooser dateChooserBirthdate = new JDateChooser();
        dateChooserBirthdate.setDateFormatString("yyyy-MM-dd");
        formPanel.add(dateChooserBirthdate, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Group ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        JTextField txtGroupId = new JTextField(20);
        formPanel.add(txtGroupId, gbc);


        if (memberToEdit != null) {
            txtFullName.setText(memberToEdit.getFullName());
            comboGender.setSelectedItem(memberToEdit.getGender());
            txtPhoneNumber.setText(memberToEdit.getPhoneNumber());
            dateChooserBirthdate.setDate(memberToEdit.getBirthdate());
            txtGroupId.setText(memberToEdit.getGroupId() != null ? String.valueOf(memberToEdit.getGroupId()) : "");
        }

        JPanel buttonPanelDialog = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        buttonPanelDialog.add(btnSave);
        buttonPanelDialog.add(btnCancel);

        btnSave.addActionListener(e -> {
            String fullName = txtFullName.getText();
            String gender = (String) comboGender.getSelectedItem();
            String phoneNumber = txtPhoneNumber.getText();
            java.util.Date selectedBirthDate = dateChooserBirthdate.getDate();
            String groupIdStr = txtGroupId.getText();

            if (fullName.trim().isEmpty() || gender == null || selectedBirthDate == null) {
                JOptionPane.showMessageDialog(dialog, "Full Name, Gender, and Birthdate are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date birthDateSql = new Date(selectedBirthDate.getTime());
            Integer groupId = null;
            if (!groupIdStr.trim().isEmpty()) {
                try {
                    groupId = Integer.parseInt(groupIdStr);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(dialog, "Invalid Group ID. Must be a number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Member member = (memberToEdit == null) ? new Member() : memberToEdit;
            member.setFullName(fullName);
            member.setGender(gender);
            member.setPhoneNumber(phoneNumber);
            member.setBirthdate(birthDateSql);
            member.setGroupId(groupId); // Assuming setter accepts Integer or int

            try {
                if (memberService == null) {
                    JOptionPane.showMessageDialog(dialog, "Member Service not available. Cannot save member.", "Service Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (memberToEdit == null) {
                    String result = memberService.registerMember(member);
                    JOptionPane.showMessageDialog(dialog, "Member registered: " + result, "Member Saved", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String result = memberService.updateMember(member);
                    JOptionPane.showMessageDialog(dialog, "Member updated: " + result, "Member Saved", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadMembers();
            } catch (RemoteException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving member: " + ex.getMessage(), "RemoteException", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanelDialog, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private Member getSelectedMember() {
        int selectedRow = membersTable.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                String fullName = (String) tableModel.getValueAt(selectedRow, 1);
                String gender = (String) tableModel.getValueAt(selectedRow, 2);
                String phoneNumber = (String) tableModel.getValueAt(selectedRow, 3);

                Date birthdate = null;
                String birthdateStr = (String) tableModel.getValueAt(selectedRow, 4);
                if (birthdateStr != null && !birthdateStr.trim().isEmpty()) {
                     birthdate = new Date(dateFormatter.parse(birthdateStr).getTime());
                }

                Integer groupId = null;
                Object groupIdObj = tableModel.getValueAt(selectedRow, 5);
                if (groupIdObj != null) {
                    if (groupIdObj instanceof Integer) {
                        groupId = (Integer) groupIdObj;
                    } else if (groupIdObj instanceof String) {
                        String groupIdStr = (String) groupIdObj;
                        if (!groupIdStr.trim().isEmpty()) {
                             groupId = Integer.parseInt(groupIdStr);
                        }
                    } else { // Could be Long from database, etc.
                        groupId = ((Number) groupIdObj).intValue();
                    }
                }

                Member member = new Member();
                member.setMemberId(id);
                member.setFullName(fullName);
                member.setGender(gender);
                member.setPhoneNumber(phoneNumber);
                member.setBirthdate(birthdate);
                member.setGroupId(groupId);
                return member;

            } catch (ParseException | NumberFormatException | ClassCastException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error parsing data from table: " + e.getMessage(), "Data Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }

    private void deleteSelectedMember() {
        Member selectedMember = getSelectedMember();
        if (selectedMember != null) {
            int confirmation = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete member: " + selectedMember.getFullName() + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                 if (memberService == null) {
                    JOptionPane.showMessageDialog(this, "Member Service not available. Cannot delete member.", "Service Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    String result = memberService.deleteMember(selectedMember);
                    JOptionPane.showMessageDialog(this, "Member '" + selectedMember.getFullName() + "' deleted: " + result, "Member Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadMembers();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting member: " + ex.getMessage(), "RemoteException", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a member to delete.", "No Member Selected", JOptionPane.WARNING_MESSAGE);
        }
    }
}
