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
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;


public class MembersPanel extends JPanel {

    private JTable membersTable;
    private DefaultTableModel tableModel;
    private JButton btnAddMember, btnEditMember, btnDeleteMember, btnRefreshMembers, btnExportMembersPdf; // Added export button
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
            updateComponentStates(false); // Use updated method
        }
        loadMembers();
    }

    // Renamed and updated to manage all relevant component states
    private void updateComponentStates(boolean enable) {
        btnAddMember.setEnabled(enable);
        btnEditMember.setEnabled(enable);
        btnDeleteMember.setEnabled(enable);
        if(btnRefreshMembers != null) btnRefreshMembers.setEnabled(enable);
        if(btnSearchMember != null) btnSearchMember.setEnabled(enable);
        if(btnShowAllMembers != null) btnShowAllMembers.setEnabled(enable);
        // Export button state depends on table content as well, handled after data load
        // Initially set based on 'enable' flag, but will be fine-tuned after data loads.
        if(btnExportMembersPdf != null) btnExportMembersPdf.setEnabled(enable && tableModel != null && tableModel.getRowCount() > 0);
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
        btnRefreshMembers = new JButton("Refresh"); // This button is now less prominent, btnShowAllMembers is clearer
        btnExportMembersPdf = new JButton("Export to PDF");

        buttonPanel.add(btnAddMember);
        buttonPanel.add(btnEditMember);
        buttonPanel.add(btnDeleteMember);
        buttonPanel.add(btnRefreshMembers); // Keeping this for now, acts like show all
        buttonPanel.add(btnExportMembersPdf);
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
        btnRefreshMembers.addActionListener(e -> loadMembers());
        btnSearchMember.addActionListener(e -> searchMembersAction());
        btnShowAllMembers.addActionListener(e -> loadMembers());
        btnExportMembersPdf.addActionListener(e -> exportMembersToPdf());
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
            // disableCrudButtons(); // This was the old name, updateComponentStates is now used.
            updateComponentStates(false);
            // Initial error message shown in constructor
            return;
        }

        // updateComponentStates(false); // Call this instead of individual setEnabled(false)
        // btnRefreshMembers.setEnabled(false);
        // btnSearchMember.setEnabled(false);
        // btnShowAllMembers.setEnabled(false);
        updateComponentStates(false); // This will also handle btnExportMembersPdf initially
        if(btnExportMembersPdf != null) btnExportMembersPdf.setEnabled(false); // Explicitly ensure export is off during load

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
                // btnRefreshMembers.setEnabled(true); // Handled by updateComponentStates
                // btnSearchMember.setEnabled(true);   // Handled by updateComponentStates
                // btnShowAllMembers.setEnabled(true); // Handled by updateComponentStates
                updateComponentStates(true); // Re-enable general controls first

                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(MembersPanel.this, errorMessage, "RemoteException", JOptionPane.ERROR_MESSAGE);
                    // disableCrudButtons(); // Old name. updateComponentStates(false) would be too broad here.
                                        // updateComponentStates(true) was called, so specific disabling for export is needed on error.
                    if(btnExportMembersPdf != null) btnExportMembersPdf.setEnabled(false);
                } else {
                    try {
                        List<Member> members = get();
                        populateTableWithMembers(members);
                        // Set export button state based on table content
                        if(btnExportMembersPdf != null) btnExportMembersPdf.setEnabled(tableModel.getRowCount() > 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(MembersPanel.this, "Error processing members list: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        if(btnExportMembersPdf != null) btnExportMembersPdf.setEnabled(false);
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
            loadMembers(); // Or just return / clear table
            return;
        }

        // updateComponentStates(false); // Call this instead of individual setEnabled(false)
        // btnSearchMember.setEnabled(false);
        // btnRefreshMembers.setEnabled(false);
        // btnShowAllMembers.setEnabled(false);
        updateComponentStates(false); // This will also handle btnExportMembersPdf initially
        if(btnExportMembersPdf != null) btnExportMembersPdf.setEnabled(false); // Explicitly ensure export is off during search


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
                // btnSearchMember.setEnabled(true); // Handled by updateComponentStates
                // btnRefreshMembers.setEnabled(true); // Handled by updateComponentStates
                // btnShowAllMembers.setEnabled(true); // Handled by updateComponentStates
                updateComponentStates(true); // Re-enable general controls first

                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(MembersPanel.this, errorMessage, "Search Exception", JOptionPane.ERROR_MESSAGE);
                    if(btnExportMembersPdf != null) btnExportMembersPdf.setEnabled(false);
                } else {
                    try {
                        List<Member> members = get();
                        populateTableWithMembers(members);
                        // Set export button state based on table content
                        if(btnExportMembersPdf != null) btnExportMembersPdf.setEnabled(tableModel.getRowCount() > 0);
                        if (members == null || members.isEmpty()) {
                            JOptionPane.showMessageDialog(MembersPanel.this, "No members found matching '" + searchTerm + "'.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(MembersPanel.this, "Error processing search results: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        if(btnExportMembersPdf != null) btnExportMembersPdf.setEnabled(false);
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
                    int memberId=0;
                    String result = memberService.deleteMember(memberId);
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

    private void exportMembersToPdf() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Members PDF");
        fileChooser.setSelectedFile(new File("MembersList.pdf"));
        // Add PDF file filter
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure .pdf extension
            if (!fileToSave.getAbsolutePath().toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }

            // Confirm overwrite
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
                float tableTop = yStart - 20; // Space for title
                float yPosition = tableTop;
                // float tableWidth = page.getMediaBox().getWidth() - 2 * margin; // Not directly used for now
                float bottomMargin = 70;
                float lineHeight = 15f;
                int rowsPerPage = (int) ((tableTop - bottomMargin) / lineHeight) -1; // -1 for header row on new pages

                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                // Title
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yStart);
                contentStream.showText("Members List");
                contentStream.endText();
                yPosition -= 30;

                // Table Headers
                PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                float headerFontSize = 10f;

                // Define column widths (approximate, adjust as needed)
                // "ID", "Full Name", "Gender", "Phone", "Birthdate", "Group ID"
                float[] columnWidths = {40, 160, 70, 100, 80, 60};
                float currentX = margin;

                // Function to draw headers (to reuse for new pages)
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
                    } catch (IOException e) {
                        // This lambda is within a try-catch block for the outer method,
                        // but direct IOExceptions from contentStream here need handling or rethrowing.
                        // For simplicity, we assume it won't throw if the stream is valid.
                         e.printStackTrace(); // Or handle more gracefully
                    }
                };

                drawHeaders.run();
                yPosition -= lineHeight * 1.5f; // Extra space after headers

                // Table Data
                PDType1Font dataFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                float dataFontSize = 9f;
                contentStream.setFont(dataFont, dataFontSize);
                int rowsWrittenOnPage = 0;

                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    if (rowsWrittenOnPage >= rowsPerPage ) { // New page check
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = page.getMediaBox().getHeight() - margin - 20; // Reset Y for new page
                        drawHeaders.run(); // Draw headers on new page
                        yPosition -= lineHeight * 1.5f;
                        contentStream.setFont(dataFont, dataFontSize); // Reset to data font
                        rowsWrittenOnPage = 0;
                    }

                    currentX = margin;
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object cellValue = tableModel.getValueAt(row, col);
                        String text = (cellValue != null) ? cellValue.toString() : "";

                        // Simple text truncation if too long for the column width
                        // A more sophisticated approach might involve text wrapping or font size reduction.
                        float colWidth = columnWidths[col] - 2; // Small padding
                        float textWidth = dataFont.getStringWidth(text) / 1000 * dataFontSize;
                        if (textWidth > colWidth) {
                            // Truncate text
                            StringBuilder sb = new StringBuilder();
                            for (char c : text.toCharArray()) {
                                if (dataFont.getStringWidth(sb.toString() + c) / 1000 * dataFontSize < colWidth - (dataFont.getStringWidth("...")/1000 * dataFontSize) ) {
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

                contentStream.close();
                document.save(fileToSave);
                JOptionPane.showMessageDialog(this, "Members list exported successfully to:\n" + fileToSave.getAbsolutePath(), "PDF Export Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting to PDF: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
