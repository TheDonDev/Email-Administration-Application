package emailapp.ui;

import emailapp.model.*;
import emailapp.service.EmailService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmailAdminUI extends JFrame {
    private final EmailService service;
    private JTabbedPane tabbedPane;

    public EmailAdminUI() {
        this.service = new EmailService();
        setTitle("Email Administration Application - Enterprise Edition");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Employees", createEmployeesPanel());
        tabbedPane.addTab("Email Accounts", createEmailAccountsPanel());
        tabbedPane.addTab("Departments", createDepartmentsPanel());
        tabbedPane.addTab("Organizations", createOrganizationsPanel());
        tabbedPane.addTab("Groups", createGroupsPanel());
        tabbedPane.addTab("Audit Logs", createAuditLogsPanel());
        tabbedPane.addTab("Settings", createSettingsPanel());

        add(tabbedPane);

        refreshDashboard();
    }

    private void refreshDashboard() {
        // dashboard stats are refreshed via its internal timer
    }

    private void refreshDepartments(javax.swing.JComboBox<Department> cb) {
        cb.removeAllItems();
        for (Department d : service.getAllDepartments()) cb.addItem(d);
    }

    /* ==================== DASHBOARD ==================== */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblOrgs = statCard("Organizations", "0");
        JLabel lblDepts = statCard("Departments", "0");
        JLabel lblEmps = statCard("Employees", "0");
        JLabel lblEmails = statCard("Email Accounts", "0");
        JLabel lblGroups = statCard("Email Groups", "0");
        JLabel lblAudit = statCard("Audit Logs", "0");

        panel.add(lblOrgs); panel.add(lblDepts); panel.add(lblEmps); panel.add(lblEmails);
        panel.add(lblGroups); panel.add(lblAudit); panel.add(new JLabel()); panel.add(new JLabel());

        Timer timer = new Timer(3000, e -> {
            lblOrgs.setText("<html><b>Organizations</b><br><font size=+2>" + service.getAllOrganizations().size() + "</font></html>");
            lblDepts.setText("<html><b>Departments</b><br><font size=+2>" + service.getAllDepartments().size() + "</font></html>");
            lblEmps.setText("<html><b>Employees</b><br><font size=+2>" + service.getAllEmployees().size() + "</font></html>");
            lblEmails.setText("<html><b>Email Accounts</b><br><font size=+2>" + service.getAllEmailAccounts().size() + "</font></html>");
            lblGroups.setText("<html><b>Email Groups</b><br><font size=+2>" + service.getAllEmailGroups().size() + "</font></html>");
            lblAudit.setText("<html><b>Audit Logs</b><br><font size=+2>" + service.getAllAuditLogs().size() + "</font></html>");
        });
        timer.setRepeats(false);
        timer.start();

        return panel;
    }

    private JLabel statCard(String title, String value) {
        JLabel label = new JLabel("<html><b>" + title + "</b><br><font size=+2>" + value + "</font></html>", SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setOpaque(true);
        label.setBackground(new Color(245, 245, 250));
        label.setPreferredSize(new Dimension(200, 100));
        return label;
    }

    /* ==================== EMPLOYEES ==================== */
    private JPanel createEmployeesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Name", "Department", "Role", "Active"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField search = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnAdd = new JButton("Add Employee");
        JButton btnDelete = new JButton("Deactivate Selected");

        top.add(new JLabel("Search:")); top.add(search); top.add(btnSearch);
        top.add(btnRefresh); top.add(btnAdd); top.add(btnDelete);
        panel.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tfFirst = new JTextField(10);
        JTextField tfLast = new JTextField(10);
        JComboBox<Department> cbDept = new JComboBox<>();
        JComboBox<Role> cbRole = new JComboBox<>(Role.values());
        JButton btnCreate = new JButton("Create + Email");

        bottom.add(new JLabel("First Name:")); bottom.add(tfFirst);
        bottom.add(new JLabel("Last Name:")); bottom.add(tfLast);
        bottom.add(new JLabel("Dept:")); bottom.add(cbDept);
        bottom.add(new JLabel("Role:")); bottom.add(cbRole);
        bottom.add(btnCreate);
        panel.add(bottom, BorderLayout.SOUTH);

        refreshDepartments(cbDept);

        Runnable refresh = () -> {
            model.setRowCount(0);
            List<Employee> emps = service.getAllEmployees();
            for (Employee e : emps) {
                Department d = service.getDepartmentById(e.getDepartmentId());
                model.addRow(new Object[]{e.getId(), e.getFullName(), (d != null ? d.getName() : "?"), e.getRole(), e.isActive() ? "Yes" : "No"});
            }
        };
        refresh.run();

        btnRefresh.addActionListener(e -> refresh.run());
        btnSearch.addActionListener(e -> {
            model.setRowCount(0);
            for (Employee emp : service.searchEmployees(search.getText())) {
                Department d = service.getDepartmentById(emp.getDepartmentId());
                model.addRow(new Object[]{emp.getId(), emp.getFullName(), (d != null ? d.getName() : "?"), emp.getRole(), emp.isActive() ? "Yes" : "No"});
            }
        });
        btnAdd.addActionListener(e -> {
            String first = tfFirst.getText().trim();
            String last = tfLast.getText().trim();
            Department d = (Department) cbDept.getSelectedItem();
            Role r = (Role) cbRole.getSelectedItem();
            if (first.isEmpty() || last.isEmpty() || d == null) {
                JOptionPane.showMessageDialog(this, "Please fill all fields");
                return;
            }
            int acctId = service.createEmployeeWithEmail(first, last, d.getId(), 1, r);
            if (acctId > 0) {
                JOptionPane.showMessageDialog(this, "Created! Email account ID: " + acctId);
                tfFirst.setText(""); tfLast.setText("");
                refresh.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create employee");
            }
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row"); return; }
            int id = (int) model.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Deactivate employee #" + id + "?") == JOptionPane.YES_OPTION) {
                service.deleteEmployee(id);
                refresh.run();
            }
        });

        return panel;
    }

    /* ==================== EMAIL ACCOUNTS ==================== */
    private JPanel createEmailAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Email Address", "Password", "Capacity (MB)", "Alternate Email", "Active"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField search = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnPass = new JButton("Change Password");
        JButton btnCap = new JButton("Set Capacity");
        JButton btnAlt = new JButton("Set Alternate Email");
        JButton btnDel = new JButton("Deactivate Selected");

        top.add(new JLabel("Search:")); top.add(search); top.add(btnSearch);
        top.add(btnRefresh); top.add(btnPass); top.add(btnCap); top.add(btnAlt); top.add(btnDel);
        panel.add(top, BorderLayout.NORTH);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (EmailAccount a : service.getAllEmailAccounts()) {
                model.addRow(new Object[]{a.getId(), a.getEmailAddress(), a.getPassword(), a.getMailboxCapacity(),
                        a.getAlternateEmail() == null ? "-" : a.getAlternateEmail(), a.isActive() ? "Yes" : "No"});
            }
        };
        refresh.run();

        btnRefresh.addActionListener(e -> refresh.run());
        btnSearch.addActionListener(e -> {
            model.setRowCount(0);
            for (EmailAccount a : service.searchEmails(search.getText())) {
                model.addRow(new Object[]{a.getId(), a.getEmailAddress(), a.getPassword(), a.getMailboxCapacity(),
                        a.getAlternateEmail() == null ? "-" : a.getAlternateEmail(), a.isActive() ? "Yes" : "No"});
            }
        });
        btnPass.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row"); return; }
            int id = (int) model.getValueAt(row, 0);
            String newPass = JOptionPane.showInputDialog(this, "New password for #" + id + ":");
            if (newPass != null && !newPass.isEmpty()) {
                if (service.changePassword(id, newPass, 1)) {
                    JOptionPane.showMessageDialog(this, "Password updated");
                    refresh.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid password (check policy) or update failed");
                }
            }
        });
        btnCap.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row"); return; }
            int id = (int) model.getValueAt(row, 0);
            String capStr = JOptionPane.showInputDialog(this, "Mailbox capacity (MB) for #" + id + ":");
            if (capStr != null) {
                try {
                    int cap = Integer.parseInt(capStr);
                    if (service.setMailboxCapacity(id, cap)) {
                        JOptionPane.showMessageDialog(this, "Capacity updated");
                        refresh.run();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to set capacity");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number");
                }
            }
        });
        btnAlt.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row"); return; }
            int id = (int) model.getValueAt(row, 0);
            String alt = JOptionPane.showInputDialog(this, "Alternate email for #" + id + ":");
            if (alt != null) {
                if (service.setAlternateEmail(id, alt)) {
                    JOptionPane.showMessageDialog(this, "Alternate email set");
                    refresh.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to set alternate email");
                }
            }
        });
        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row"); return; }
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Deactivate email #" + id + "?") == JOptionPane.YES_OPTION) {
                service.deleteEmailAccount(id);
                refresh.run();
            }
        });

        return panel;
    }

    /* ==================== DEPARTMENTS ==================== */
    private JPanel createDepartmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Name", "Code", "Email Domain"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tfName = new JTextField(12);
        JTextField tfCode = new JTextField(10);
        JTextField tfDomain = new JTextField(20);
        JButton btnAdd = new JButton("Add Department");
        JButton btnRefresh = new JButton("Refresh");

        bottom.add(new JLabel("Name:")); bottom.add(tfName);
        bottom.add(new JLabel("Code:")); bottom.add(tfCode);
        bottom.add(new JLabel("Email Domain:")); bottom.add(tfDomain);
        bottom.add(btnAdd); bottom.add(btnRefresh);
        panel.add(bottom, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Department d : service.getAllDepartments()) {
                model.addRow(new Object[]{d.getId(), d.getName(), d.getCode(), d.getEmailDomain()});
            }
        };
        refresh.run();

        btnRefresh.addActionListener(e -> refresh.run());
        btnAdd.addActionListener(e -> {
            String name = tfName.getText().trim();
            String code = tfCode.getText().trim();
            String domain = tfDomain.getText().trim();
            if (name.isEmpty() || code.isEmpty() || domain.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields"); return;
            }
            service.addDepartment(new Department(name, code, domain));
            tfName.setText(""); tfCode.setText(""); tfDomain.setText("");
            refresh.run();
        });

        return panel;
    }

    /* ==================== ORGANIZATIONS ==================== */
    private JPanel createOrganizationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Name", "Code", "Country", "Email Suffix", "Active"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tfName = new JTextField(12);
        JTextField tfCode = new JTextField(10);
        JTextField tfCountry = new JTextField(10);
        JTextField tfSuffix = new JTextField(15);
        JButton btnAdd = new JButton("Add Organization");
        JButton btnRefresh = new JButton("Refresh");

        bottom.add(new JLabel("Name:")); bottom.add(tfName);
        bottom.add(new JLabel("Code:")); bottom.add(tfCode);
        bottom.add(new JLabel("Country:")); bottom.add(tfCountry);
        bottom.add(new JLabel("Email Suffix:")); bottom.add(tfSuffix);
        bottom.add(btnAdd); bottom.add(btnRefresh);
        panel.add(bottom, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Organization o : service.getAllOrganizations()) {
                model.addRow(new Object[]{o.getId(), o.getName(), o.getCode(), o.getCountry(), o.getEmailSuffix(), o.isActive() ? "Yes" : "No"});
            }
        };
        refresh.run();

        btnRefresh.addActionListener(e -> refresh.run());
        btnAdd.addActionListener(e -> {
            Organization o = new Organization(tfName.getText().trim(), tfCode.getText().trim(), tfCountry.getText().trim(), tfSuffix.getText().trim());
            service.addOrganization(o);
            tfName.setText(""); tfCode.setText(""); tfCountry.setText(""); tfSuffix.setText("");
            refresh.run();
        });

        return panel;
    }

    /* ==================== GROUPS ==================== */
    private JPanel createGroupsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Name", "Email Address", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField tfName = new JTextField(12);
        JTextField tfEmail = new JTextField(18);
        JTextField tfDesc = new JTextField(20);
        JComboBox<Employee> cbMember = new JComboBox<>();
        JButton btnAddGroup = new JButton("Add Group");
        JButton btnAddMember = new JButton("Add Member to Selected");
        JButton btnRefresh = new JButton("Refresh");

        bottom.add(new JLabel("Name:")); bottom.add(tfName);
        bottom.add(new JLabel("Email:")); bottom.add(tfEmail);
        bottom.add(new JLabel("Desc:")); bottom.add(tfDesc);
        bottom.add(new JLabel("Member:")); bottom.add(cbMember);
        bottom.add(btnAddGroup); bottom.add(btnAddMember); bottom.add(btnRefresh);
        panel.add(bottom, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (EmailGroup g : service.getAllEmailGroups()) {
                model.addRow(new Object[]{g.getId(), g.getName(), g.getEmailAddress(), g.getDescription()});
            }
            cbMember.removeAllItems();
            for (Employee e : service.getAllEmployees()) cbMember.addItem(e);
        };
        refresh.run();

        btnRefresh.addActionListener(e -> refresh.run());
        btnAddGroup.addActionListener(e -> {
            EmailGroup g = new EmailGroup(1, tfName.getText().trim(), tfEmail.getText().trim(), tfDesc.getText().trim());
            service.addEmailGroup(g);
            tfName.setText(""); tfEmail.setText(""); tfDesc.setText("");
            refresh.run();
        });
        btnAddMember.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a group"); return; }
            int gid = (int) model.getValueAt(row, 0);
            Employee emp = (Employee) cbMember.getSelectedItem();
            if (emp != null && service.addGroupMember(gid, emp.getId())) {
                JOptionPane.showMessageDialog(this, "Added " + emp.getFullName() + " to group");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add member");
            }
        });

        return panel;
    }

    /* ==================== AUDIT LOGS ==================== */
    private JPanel createAuditLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "User ID", "Action", "Entity Type", "Entity ID", "Details", "Created At"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        bottom.add(btnRefresh);
        panel.add(bottom, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (AuditLog a : service.getAllAuditLogs()) {
                model.addRow(new Object[]{a.getId(), a.getUserId(), a.getAction(), a.getEntityType(), a.getEntityId(), a.getDetails(), a.getCreatedAt()});
            }
        };
        refresh.run();
        btnRefresh.addActionListener(e -> refresh.run());

        return panel;
    }

    /* ==================== SETTINGS ==================== */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setText("""
                Email Administration Application - Enterprise Edition
                
                Architecture:
                  - Layered: UI (Swing) -> Service (Business Logic) -> Database (SQLite)
                
                Features:
                  - Employee & email account lifecycle management
                  - Department & Organization multi-tenancy
                  - Role-Based Access Control (RBAC)
                  - Password policy enforcement (length, complexity, expiration)
                  - Mailbox capacity & alternate email management
                  - Email groups with membership
                  - Full audit trail of all actions
                  - Search across employees and email accounts
                
                Database: emailapp.db (SQLite, file-based)
                """);
        panel.add(new JScrollPane(info), BorderLayout.CENTER);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmailAdminUI().setVisible(true));
    }
}
