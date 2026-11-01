package ui.admin;

import model.TechnicalOfficer;
import service.TechnicalOfficerService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import ui.util.UIAnimationHelper;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modern Technical Officer Management CRUD Panel for FAMS Admin Dashboard.
 * Integrates directly with technical_officers and users tables.
 */
public class TechnicalOfficerManagementPanel extends JPanel {

    // Harmonized Palette matching AdminDashboard
    private static final Color PRIMARY_NAVY    = new Color(16, 40, 74);    // #10284A
    private static final Color SECONDARY_BLUE  = new Color(29, 78, 216);   // #1D4ED8
    private static final Color WARNING_COLOR   = new Color(217, 119, 6);   // #D97706
    private static final Color LIGHT_AMBER     = new Color(254, 243, 199); // #FEF3C7
    private static final Color MAIN_BG         = new Color(245, 247, 251); // #F5F7FB
    private static final Color CARD_BG         = Color.WHITE;
    private static final Color TEXT_PRIMARY    = new Color(23, 43, 77);    // #172B4D
    private static final Color TEXT_SECONDARY  = new Color(100, 116, 139); // #64748B
    private static final Color BORDER_COLOR    = new Color(226, 232, 240); // #E2E8F0
    private static final Color SUCCESS_COLOR   = new Color(22, 163, 74);   // #16A34A
    private static final Color DANGER_COLOR    = new Color(220, 38, 38);   // #DC2626

    // Table Components
    private JTable officerTable;
    private TechnicalOfficerTableModel tableModel;
    private TableRowSorter<TechnicalOfficerTableModel> sorter;

    // Filter & Search Controls
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterDepartment;
    private JComboBox<String> cmbFilterAccountStatus;
    private JLabel lblTableStatus;

    // Action Buttons
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnToggleStatus;
    private JButton btnRefresh;

    // Summary Counter Labels
    private JLabel lblTotalOfficers;
    private JLabel lblActiveOfficers;
    private JLabel lblIctOfficers;
    private JLabel lblInactiveOfficers;

    // Standardized Blue Header Banner
    private PageHeaderBanner headerBanner;

    // Callback on data changes
    private Runnable dataChangedCallback;

    public TechnicalOfficerManagementPanel() {
        this(null);
    }

    public TechnicalOfficerManagementPanel(Runnable dataChangedCallback) {
        this.dataChangedCallback = dataChangedCallback;
        initUI();
        loadOfficersAsync();
    }

    public void setDataChangedCallback(Runnable callback) {
        this.dataChangedCallback = callback;
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(MAIN_BG);
        setBorder(new EmptyBorder(20, 28, 24, 28));

        // 1. Top Section: Standardized Header Banner & Summary Cards
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        headerBanner = new PageHeaderBanner(
                "Technical Officer Management",
                "Manage technical staff profiles, laboratory assignments, and system access.",
                null,
                "↻ Refresh",
                () -> loadOfficersAsync()
        );
        topContainer.add(headerBanner);
        topContainer.add(Box.createVerticalStrut(14));
        topContainer.add(createSummaryCards());

        add(topContainer, BorderLayout.NORTH);

        // 2. Main Content Card (Toolbar + Table + Pagination Footer)
        JPanel mainCard = createMainCard();
        add(mainCard, BorderLayout.CENTER);
    }

    private JPanel createSummaryCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        row.setPreferredSize(new Dimension(1000, 86));

        lblTotalOfficers    = new JLabel("--");
        lblActiveOfficers   = new JLabel("--");
        lblIctOfficers      = new JLabel("--");
        lblInactiveOfficers = new JLabel("--");

        row.add(createMiniCard("Total Technical Staff", lblTotalOfficers, WARNING_COLOR, LIGHT_AMBER));
        row.add(createMiniCard("Active Accounts", lblActiveOfficers, SUCCESS_COLOR, new Color(220, 252, 231)));
        row.add(createMiniCard("ICT Laboratory Staff", lblIctOfficers, SECONDARY_BLUE, new Color(234, 242, 255)));
        row.add(createMiniCard("Inactive / Suspended", lblInactiveOfficers, new Color(148, 163, 184), new Color(241, 245, 249)));

        return row;
    }

    private JPanel createMiniCard(String title, JLabel valLabel, Color accent, Color bgPill) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        tLbl.setForeground(TEXT_SECONDARY);

        valLabel.setFont(AdminDashboard.getModernFont(Font.BOLD, 22));
        valLabel.setForeground(TEXT_PRIMARY);

        text.add(tLbl);
        text.add(Box.createVerticalStrut(2));
        text.add(valLabel);

        JPanel pill = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgPill);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                // Technical officer cog icon
                g2.drawOval(12, 12, 12, 12);
                g2.drawOval(16, 16, 4, 4);
                g2.drawLine(18, 9, 18, 12);
                g2.drawLine(18, 24, 18, 27);
                g2.drawLine(9, 18, 12, 18);
                g2.drawLine(24, 18, 27, 18);
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.setPreferredSize(new Dimension(36, 36));

        card.add(text, BorderLayout.CENTER);
        card.add(pill, BorderLayout.EAST);
        return card;
    }

    private JPanel createMainCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 20, 16, 20));

        // 1. Toolbar (Search, Filters, Action Buttons)
        JPanel toolbar = createToolbar();
        card.add(toolbar, BorderLayout.NORTH);

        // 2. Table in ScrollPane
        tableModel = new TechnicalOfficerTableModel();
        sorter = new TableRowSorter<>(tableModel);

        officerTable = new JTable(tableModel);
        officerTable.setRowSorter(sorter);
        officerTable.setRowHeight(40);
        officerTable.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        officerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        officerTable.setGridColor(new Color(241, 245, 249));
        officerTable.setShowVerticalLines(false);
        officerTable.setShowHorizontalLines(true);
        officerTable.setSelectionBackground(new Color(254, 243, 199));
        officerTable.setSelectionForeground(TEXT_PRIMARY);

        // Header Styling
        JTableHeader header = officerTable.getTableHeader();
        header.setFont(AdminDashboard.getModernFont(Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Setup Column Renderers and Widths
        setupTableColumns();

        // Selection Listener
        officerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtonsState();
            }
        });

        // Double Click to Edit
        officerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && officerTable.getSelectedRow() != -1) {
                    onEditOfficer();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(officerTable);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        card.add(scrollPane, BorderLayout.CENTER);

        // 3. Footer Bar
        JPanel footerBar = createFooterBar();
        card.add(footerBar, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setOpaque(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));

        // ROW 1: Search Field (Left) & Action Buttons (Right)
        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Search Input with animated focus glow and pill shape
        txtSearch = new JTextField();
        txtSearch.setToolTipText("Search by ID, staff no, name, email, department...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterOfficers(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterOfficers(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterOfficers(); }
        });
        JPanel searchBox = UIAnimationHelper.createPillSearchBox(txtSearch, 260, 36, 20);
        row1.add(searchBox, BorderLayout.WEST);

        // Right Controls: Action Buttons (Add, Edit, Delete, Status Toggle, Refresh)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        btnRefresh = createIconButton("↻", "Refresh Data", e -> loadOfficersAsync());
        rightPanel.add(btnRefresh);

        btnToggleStatus = createActionButton("Deactivate", WARNING_COLOR, e -> onToggleStatus());
        btnToggleStatus.setEnabled(false);
        rightPanel.add(btnToggleStatus);

        btnDelete = createActionButton("Delete", DANGER_COLOR, e -> onDeleteOfficer());
        btnDelete.setEnabled(false);
        rightPanel.add(btnDelete);

        btnEdit = createActionButton("Edit", SECONDARY_BLUE, e -> onEditOfficer());
        btnEdit.setEnabled(false);
        rightPanel.add(btnEdit);

        btnAdd = createPrimaryButton("+ Add Officer", new Color(180, 83, 9), e -> onAddOfficer());
        rightPanel.add(btnAdd);

        row1.add(rightPanel, BorderLayout.EAST);
        toolbar.add(row1);
        toolbar.add(Box.createVerticalStrut(10));

        // ROW 2: Filter Dropdowns & Reset
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel filterLbl = new JLabel("Filter by:");
        filterLbl.setFont(AdminDashboard.getModernFont(Font.BOLD, 11));
        filterLbl.setForeground(TEXT_SECONDARY);
        row2.add(filterLbl);

        // Department Filter
        String[] deptOptions = {"All Departments", "Information and Communication Technology", "Engineering Technology", "Biosystems Technology"};
        cmbFilterDepartment = new JComboBox<>(deptOptions);
        styleFilterCombo(cmbFilterDepartment, 210);
        cmbFilterDepartment.addActionListener(e -> filterOfficers());
        row2.add(cmbFilterDepartment);

        // Account Status Filter
        String[] accOptions = {"All Status", "ACTIVE", "INACTIVE"};
        cmbFilterAccountStatus = new JComboBox<>(accOptions);
        styleFilterCombo(cmbFilterAccountStatus, 120);
        cmbFilterAccountStatus.addActionListener(e -> filterOfficers());
        row2.add(cmbFilterAccountStatus);

        // Clear Filters Button
        JButton btnClear = UIAnimationHelper.createAnimatedSecondaryButton("Reset Filters", TEXT_SECONDARY, BORDER_COLOR, 18, e -> resetFilters());
        row2.add(btnClear);

        toolbar.add(row2);
        return toolbar;
    }

    private JPanel createFooterBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(4, 4, 0, 4));

        lblTableStatus = new JLabel("Loading technical staff records...");
        lblTableStatus.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        lblTableStatus.setForeground(TEXT_SECONDARY);
        footer.add(lblTableStatus, BorderLayout.WEST);

        JLabel tip = new JLabel("Tip: Double-click any officer record to edit.");
        tip.setFont(AdminDashboard.getModernFont(Font.PLAIN, 11));
        tip.setForeground(new Color(148, 163, 184));
        footer.add(tip, BorderLayout.EAST);

        return footer;
    }

    private void styleFilterCombo(JComboBox<String> cmb, int width) {
        cmb.setFont(AdminDashboard.getModernFont(Font.PLAIN, 12));
        cmb.setBackground(Color.WHITE);
        cmb.setPreferredSize(new Dimension(width, 36));
    }

    private JButton createPrimaryButton(String text, Color bg, java.awt.event.ActionListener al) {
        return UIAnimationHelper.createAnimatedPillButton(text, bg, 20, al);
    }

    private JButton createActionButton(String text, Color color, java.awt.event.ActionListener al) {
        return UIAnimationHelper.createAnimatedSecondaryButton(text, color, BORDER_COLOR, 20, al);
    }

    private JButton createIconButton(String icon, String tooltip, java.awt.event.ActionListener al) {
        return UIAnimationHelper.createAnimatedIconButton(icon, tooltip, TEXT_PRIMARY, new Color(241, 245, 249), 20, al);
    }

    private void setupTableColumns() {
        int[] columnWidths = {95, 115, 175, 220, 190, 120, 95};
        for (int i = 0; i < columnWidths.length && i < officerTable.getColumnCount(); i++) {
            officerTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        // Custom Cell Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        officerTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        officerTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Status Pill Renderer
        officerTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());
    }

    private void updateActionButtonsState() {
        int selectedRow = officerTable.getSelectedRow();
        boolean hasSelection = (selectedRow != -1);
        btnEdit.setEnabled(hasSelection);
        btnDelete.setEnabled(hasSelection);
        btnToggleStatus.setEnabled(hasSelection);

        if (hasSelection) {
            int modelIdx = officerTable.convertRowIndexToModel(selectedRow);
            TechnicalOfficer to = tableModel.getOfficerAt(modelIdx);
            if (to != null) {
                if ("ACTIVE".equalsIgnoreCase(to.getAccountStatus())) {
                    btnToggleStatus.setText("Deactivate");
                    btnToggleStatus.setForeground(WARNING_COLOR);
                } else {
                    btnToggleStatus.setText("Activate");
                    btnToggleStatus.setForeground(SUCCESS_COLOR);
                }
            }
        }
    }

    // CRUD OPERATIONS


    public void loadOfficersAsync() {
        lblTableStatus.setText("Loading technical staff records from database...");
        SwingWorker<List<TechnicalOfficer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TechnicalOfficer> doInBackground() {
                return TechnicalOfficerService.getInstance().getAllTechnicalOfficers();
            }

            @Override
            protected void done() {
                try {
                    List<TechnicalOfficer> list = get();
                    tableModel.setOfficers(list);
                    updateSummaryCards(list);
                    lblTableStatus.setText("Showing " + list.size() + " technical staff members.");
                    updateActionButtonsState();
                } catch (Exception e) {
                    System.err.println("[TechnicalOfficerManagementPanel] Error loading officers: " + e.getMessage());
                    lblTableStatus.setText("Failed to load records. Database may be offline.");
                }
            }
        };
        worker.execute();
    }

    private void updateSummaryCards(List<TechnicalOfficer> list) {
        if (list == null) return;
        int total = list.size();
        int active = 0;
        int ict = 0;
        int inactive = 0;

        for (TechnicalOfficer to : list) {
            if ("ACTIVE".equalsIgnoreCase(to.getAccountStatus())) {
                active++;
            } else {
                inactive++;
            }
            if (to.getDepartment() != null && to.getDepartment().toLowerCase().contains("information")) {
                ict++;
            }
        }

        lblTotalOfficers.setText(String.valueOf(total));
        lblActiveOfficers.setText(String.valueOf(active));
        lblIctOfficers.setText(String.valueOf(ict));
        lblInactiveOfficers.setText(String.valueOf(inactive));

        if (headerBanner != null) {
            headerBanner.setBadgeText(total + " Technical Staff");
        }
    }

    private void onAddOfficer() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        TechnicalOfficerDialog dialog = new TechnicalOfficerDialog(parentWindow, null);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadOfficersAsync();
            if (dataChangedCallback != null) {
                dataChangedCallback.run();
            }
        }
    }

    private void onEditOfficer() {
        int selectedRow = officerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an officer to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelIdx = officerTable.convertRowIndexToModel(selectedRow);
        TechnicalOfficer selectedOfficer = tableModel.getOfficerAt(modelIdx);
        if (selectedOfficer == null) return;

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        TechnicalOfficerDialog dialog = new TechnicalOfficerDialog(parentWindow, selectedOfficer);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            loadOfficersAsync();
            if (dataChangedCallback != null) {
                dataChangedCallback.run();
            }
        }
    }

    private void onDeleteOfficer() {
        int selectedRow = officerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an officer to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelIdx = officerTable.convertRowIndexToModel(selectedRow);
        TechnicalOfficer selectedOfficer = tableModel.getOfficerAt(modelIdx);
        if (selectedOfficer == null) return;

        String id = selectedOfficer.getTechnicalOfficerId();
        String name = selectedOfficer.getFullName();

        // 1. Check for dependent historical records first
        Map<String, Integer> related = TechnicalOfficerService.getInstance().getTechnicalOfficerRelatedRecords(id);
        if (!related.isEmpty()) {
            StringBuilder desc = new StringBuilder("<html><body style='font-family: sans-serif; width: 340px;'>");
            desc.append("<h4 style='color: #DC2626; margin-bottom: 6px;'>Cannot Delete Technical Officer ").append(id).append("</h4>");
            desc.append("<p style='color: #4A5568; line-height: 1.4;'>");
            desc.append("Technical Officer <b>").append(name).append("</b> (").append(id).append(") has dependent records:<br><br>");
            for (Map.Entry<String, Integer> entry : related.entrySet()) {
                desc.append("• <b>").append(entry.getValue()).append("</b> in <i>").append(entry.getKey()).append("</i><br>");
            }
            desc.append("<br>To maintain audit history and database constraints, related records cannot be deleted automatically.<br><br>");
            desc.append("<b>Would you like to deactivate this account instead?</b><br>");
            desc.append("<small style='color: #718096;'>Deactivating revokes portal login without deleting session records.</small>");
            desc.append("</p></body></html>");

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    desc.toString(),
                    "Foreign Key Constraint - Deactivate Instead?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                TechnicalOfficerService.OperationResult res = TechnicalOfficerService.getInstance().deactivateTechnicalOfficer(id);
                if (res.isSuccess()) {
                    JOptionPane.showMessageDialog(this, res.getMessage(), "Account Deactivated", JOptionPane.INFORMATION_MESSAGE);
                    loadOfficersAsync();
                    if (dataChangedCallback != null) dataChangedCallback.run();
                } else {
                    JOptionPane.showMessageDialog(this, res.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }

        // 2. No foreign key constraints exist: confirm clean deletion
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html>Are you sure you want to delete technical officer <b>" + name + "</b> (" + id + ")?<br>" +
                "This operation cannot be undone.</html>",
                "Confirm Permanent Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            TechnicalOfficerService.OperationResult res = TechnicalOfficerService.getInstance().deleteTechnicalOfficer(id);
            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Deleted Successfully", JOptionPane.INFORMATION_MESSAGE);
                loadOfficersAsync();
                if (dataChangedCallback != null) dataChangedCallback.run();
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onToggleStatus() {
        int selectedRow = officerTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelIdx = officerTable.convertRowIndexToModel(selectedRow);
        TechnicalOfficer to = tableModel.getOfficerAt(modelIdx);
        if (to == null) return;

        boolean isCurrentlyActive = "ACTIVE".equalsIgnoreCase(to.getAccountStatus());
        String action = isCurrentlyActive ? "deactivate" : "activate";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + action + " technical officer account for " + to.getFullName() + " (" + to.getTechnicalOfficerId() + ")?",
                "Confirm Account Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            TechnicalOfficerService.OperationResult res = isCurrentlyActive
                    ? TechnicalOfficerService.getInstance().deactivateTechnicalOfficer(to.getTechnicalOfficerId())
                    : TechnicalOfficerService.getInstance().activateTechnicalOfficer(to.getTechnicalOfficerId());

            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadOfficersAsync();
                if (dataChangedCallback != null) dataChangedCallback.run();
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void filterOfficers() {
        String query = txtSearch.getText().trim();
        String dept = (String) cmbFilterDepartment.getSelectedItem();
        String status = (String) cmbFilterAccountStatus.getSelectedItem();

        String deptParam = (dept == null || dept.startsWith("All")) ? null : dept;
        String statusParam = (status == null || status.startsWith("All")) ? null : status;

        SwingWorker<List<TechnicalOfficer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TechnicalOfficer> doInBackground() {
                return TechnicalOfficerService.getInstance().searchTechnicalOfficers(query, deptParam, statusParam);
            }

            @Override
            protected void done() {
                try {
                    List<TechnicalOfficer> filtered = get();
                    tableModel.setOfficers(filtered);
                    lblTableStatus.setText("Showing " + filtered.size() + " matches.");
                    updateActionButtonsState();
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void resetFilters() {
        txtSearch.setText("");
        cmbFilterDepartment.setSelectedIndex(0);
        cmbFilterAccountStatus.setSelectedIndex(0);
        loadOfficersAsync();
    }

    // TABLE MODEL & RENDERERS


    public static class TechnicalOfficerTableModel extends AbstractTableModel {
        private final String[] columns = {
                "Officer ID", "Staff No", "Full Name", "Department",
                "Email", "Phone", "Status"
        };
        private List<TechnicalOfficer> officers = new ArrayList<>();

        public void setOfficers(List<TechnicalOfficer> list) {
            this.officers = (list != null) ? list : new ArrayList<>();
            fireTableDataChanged();
        }

        public TechnicalOfficer getOfficerAt(int row) {
            if (row >= 0 && row < officers.size()) {
                return officers.get(row);
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return officers.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TechnicalOfficer to = officers.get(rowIndex);
            switch (columnIndex) {
                case 0: return to.getTechnicalOfficerId();
                case 1: return to.getStaffNo();
                case 2: return to.getFullName();
                case 3: return to.getDepartment();
                case 4: return to.getEmail() != null ? to.getEmail() : "-";
                case 5: return to.getPhone() != null ? to.getPhone() : "-";
                case 6: return to.getAccountStatus();
                default: return "";
            }
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            String status = (value != null) ? value.toString() : "";

            if (!isSelected) {
                if ("ACTIVE".equalsIgnoreCase(status)) {
                    lbl.setForeground(SUCCESS_COLOR);
                    lbl.setBackground(new Color(220, 252, 231));
                } else {
                    lbl.setForeground(DANGER_COLOR);
                    lbl.setBackground(new Color(254, 242, 242));
                }
                lbl.setOpaque(true);
            }
            return lbl;
        }
    }
}
