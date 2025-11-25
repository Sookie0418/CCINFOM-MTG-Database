package gui;

import controller.MTGDatabaseController;
import connection.*;
import reports.ReportGenerator;
import reports.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * A Java Swing GUI for generating and viewing reports.
 * STYLED with a Dark MTG theme.
 */
public class ReportGUI extends JFrame {

    // --- Colors & Fonts ---
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color FG_LIGHT = new Color(240, 240, 240);
    private static final Color ACCENT_PURPLE = new Color(128, 0, 128);
    private static final Color INPUT_BG = new Color(45, 45, 45);
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final String TASKBAR_ICON_FILE = "taskbar_icon.png";

    // --- UI Components ---
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JTextArea reportSummaryArea;
    private JLabel statusLabel;

    // Report Controls
    private JComboBox<String> reportTypeComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton generateReportButton;
    private JButton exportButton;
    private JButton clearButton;

    private MTGDatabaseController controller;
    private ReportGenerator reportGenerator;
    private String loggedInUsername;

    private static final String[] REPORT_TYPES = {
        "Player Borrowing Statistics",
        "Card Usage Frequency", 
        "Borrow Activity",
        "Deck Usage Frequency",
        "System Summary"
    };

    public ReportGUI(MTGDatabaseController controller, String username) {
        this.controller = controller;
        this.loggedInUsername = username;
        this.reportGenerator = new ReportGenerator();

        // 1. Frame Setup
        setTitle("MTG Commander Database System (Reports)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1300, 700);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(BG_DARK);

        // 1.5 Add Menu Bar for Navigation
        setJMenuBar(createMenuBar());

        // 2. Initialize Components
        initializeForm();
        initializeTable();

        // 3. Status Bar (Themed)
        statusLabel = new JLabel("Report Generator Ready. Select a report type to begin.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        statusLabel.setBackground(ACCENT_PURPLE);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);

        // 4. Layout Assembly
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        try {
            File iconFile = new File(TASKBAR_ICON_FILE);
            if (iconFile.exists()) {
                Image iconImage = new ImageIcon(iconFile.getAbsolutePath()).getImage();
                this.setIconImage(iconImage);
            }
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }

        // Add Title
        JLabel mainTitle = new JLabel("REPORT GENERATOR", SwingConstants.CENTER);
        mainTitle.setFont(TITLE_FONT);
        mainTitle.setForeground(ACCENT_PURPLE);
        contentPanel.add(mainTitle, BorderLayout.NORTH);

        contentPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        contentPanel.add(createControlPanel(), BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Set initial visibility and center the window
        setLocationRelativeTo(null);
        setVisible(true);

        // Set default dates (current month)
        setDefaultDateRange();
    }

    /**
     * Creates a themed Menu Bar for navigation.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(BG_DARK.brighter());
        menuBar.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));

        JMenu navMenu = new JMenu("Navigate");
        navMenu.setForeground(FG_LIGHT);
        navMenu.setFont(BOLD_FONT);

        JMenu appMenu = new JMenu("App");
        appMenu.setForeground(FG_LIGHT);
        appMenu.setFont(BOLD_FONT);

        // Dashboard
        JMenuItem dashboardItem = new JMenuItem("Dashboard");
        dashboardItem.addActionListener(e -> launchGUI(new DashboardGUI(controller, loggedInUsername)));
        dashboardItem.setBackground(INPUT_BG);
        dashboardItem.setForeground(FG_LIGHT);

        // Card Management
        JMenuItem cardItem = new JMenuItem("Card Management");
        cardItem.addActionListener(e -> launchGUI(new CardGUI(controller, loggedInUsername)));
        cardItem.setBackground(INPUT_BG);
        cardItem.setForeground(FG_LIGHT);

        // Player Management
        JMenuItem playerItem = new JMenuItem("Player Management");
        playerItem.addActionListener(e -> launchGUI(new PlayerGUI(controller, loggedInUsername)));
        playerItem.setBackground(INPUT_BG);
        playerItem.setForeground(FG_LIGHT);

        // Deck Management
        JMenuItem deckItem = new JMenuItem("Deck Management");
        deckItem.addActionListener(e -> launchGUI(new DeckGUI(controller, loggedInUsername)));
        deckItem.setBackground(INPUT_BG);
        deckItem.setForeground(FG_LIGHT);

        // Borrow Management
        JMenuItem borrowItem = new JMenuItem("Borrow Management");
        borrowItem.addActionListener(e -> launchGUI(new BorrowReqGUI(controller, loggedInUsername)));
        borrowItem.setBackground(INPUT_BG);
        borrowItem.setForeground(FG_LIGHT);

        // Exit
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        exitItem.setBackground(INPUT_BG);
        exitItem.setForeground(Color.RED);

        navMenu.add(dashboardItem);
        navMenu.add(cardItem);
        navMenu.add(playerItem);
        navMenu.add(deckItem);
        navMenu.add(borrowItem);
        // Note: Reports is not included since we're already in it
        appMenu.add(exitItem);

        menuBar.add(appMenu);
        menuBar.add(navMenu);

        return menuBar;
    }

    /**
     * Helper method to launch a new frame instance.
     */
    private void launchGUI(JFrame frame) {
        frame.setVisible(true);
        if (frame instanceof DashboardGUI) {
            this.dispose();
        }
    }

    /**
     * Creates a themed JTextField.
     */
    private JTextField createThemedField(int columns) {
        JTextField field = new JTextField(columns);
        field.setBackground(INPUT_BG);
        field.setForeground(FG_LIGHT);
        field.setCaretColor(FG_LIGHT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(BOLD_FONT);
        return field;
    }

    /**
     * Creates and configures the JTable component for reports.
     */
    private void initializeTable() {
        String[] columnNames = {"Select a report type to view data"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(tableModel);

        // --- Table Styling ---
        reportTable.setBackground(BG_DARK.brighter());
        reportTable.setForeground(FG_LIGHT);
        reportTable.setFont(new Font("Arial", Font.PLAIN, 12));
        reportTable.setSelectionBackground(ACCENT_PURPLE.darker());
        reportTable.setSelectionForeground(Color.WHITE);
        reportTable.setRowHeight(25);

        // Header Styling
        JTableHeader header = reportTable.getTableHeader();
        header.setBackground(new Color(60, 60, 60));
        header.setForeground(FG_LIGHT);
        header.setFont(BOLD_FONT);

        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Creates the control panel with report options and action buttons.
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        controlPanel.setBackground(BG_DARK.brighter());
        controlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_PURPLE, 2),
                "Report Controls",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                BOLD_FONT.deriveFont(Font.ITALIC, 14),
                FG_LIGHT));
        controlPanel.setPreferredSize(new Dimension(350, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel inputGrid = new JPanel(new GridBagLayout());
        inputGrid.setOpaque(false);

        // --- Input Fields Layout ---
        int y = 0;

        // Report Type
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; 
        JLabel reportTypeLabel = new JLabel("Report Type:"); 
        reportTypeLabel.setForeground(FG_LIGHT); 
        inputGrid.add(reportTypeLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; 
        inputGrid.add(reportTypeComboBox, gbc);
        reportTypeComboBox.setBackground(INPUT_BG);
        reportTypeComboBox.setForeground(FG_LIGHT);
        reportTypeComboBox.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

        // Start Date
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; 
        JLabel startDateLabel = new JLabel("Start Date:"); 
        startDateLabel.setForeground(FG_LIGHT); 
        inputGrid.add(startDateLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; 
        inputGrid.add(startDateField, gbc);

        // End Date
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; 
        JLabel endDateLabel = new JLabel("End Date:"); 
        endDateLabel.setForeground(FG_LIGHT); 
        inputGrid.add(endDateLabel, gbc);
        
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; 
        inputGrid.add(endDateField, gbc);

        // Report Summary Area
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.weightx = 1.0;
        JLabel summaryLabel = new JLabel("Report Summary:"); 
        summaryLabel.setForeground(FG_LIGHT); 
        inputGrid.add(summaryLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = y + 1; gbc.weighty = 0.3;
        JScrollPane summaryScrollPane = new JScrollPane(reportSummaryArea);
        summaryScrollPane.setPreferredSize(new Dimension(300, 120));
        summaryScrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        reportSummaryArea.setBackground(INPUT_BG);
        reportSummaryArea.setForeground(FG_LIGHT);
        reportSummaryArea.setEditable(false);
        inputGrid.add(summaryScrollPane, gbc);
        y += 2; gbc.weighty = 0; gbc.gridwidth = 1;

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Style buttons
        styleButton(generateReportButton, new Color(0, 150, 0)); // Green for generate
        styleButton(exportButton, new Color(70, 130, 180)); // Steel blue for export
        styleButton(clearButton, new Color(90, 90, 90)); // Dark gray for clear

        buttonPanel.add(generateReportButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(clearButton);

        // --- Final Assembly of Control Panel ---
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(10, 10, 10, 10);
        gbcForm.fill = GridBagConstraints.HORIZONTAL;

        // Add input grid
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.weightx = 1.0;
        gbcForm.weighty = 0;
        controlPanel.add(inputGrid, gbcForm);

        // Add button panel
        gbcForm.gridy = 1;
        gbcForm.weighty = 0;
        controlPanel.add(buttonPanel, gbcForm);

        // Filler component
        gbcForm.gridy = 2;
        gbcForm.weighty = 1.0;
        controlPanel.add(new JLabel(""), gbcForm);

        return controlPanel;
    }

    /**
     * Helper method to style buttons uniformly.
     */
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(BOLD_FONT.deriveFont(14f));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /**
     * Initializes all input fields for report generation.
     */
    private void initializeForm() {
        reportTypeComboBox = new JComboBox<>(REPORT_TYPES);
        startDateField = createThemedField(10);
        endDateField = createThemedField(10);
        reportSummaryArea = new JTextArea(6, 25);
        reportSummaryArea.setLineWrap(true);
        reportSummaryArea.setWrapStyleWord(true);

        // Buttons
        generateReportButton = new JButton("Generate Report");
        exportButton = new JButton("Export to CSV");
        clearButton = new JButton("Clear Report");

        // Action Listeners
        generateReportButton.addActionListener(this::handleGenerateReport);
        exportButton.addActionListener(this::handleExportReport);
        clearButton.addActionListener(e -> clearReport());
    }

    /**
     * Sets default date range to current month
     */
    private void setDefaultDateRange() {
        LocalDate now = LocalDate.now();
        LocalDate firstOfMonth = now.withDayOfMonth(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        startDateField.setText(firstOfMonth.format(formatter));
        endDateField.setText(now.format(formatter));
    }

    /**
     * Handles report generation based on selected type
     */
    private void handleGenerateReport(ActionEvent e) {
        try {
            String reportType = (String) reportTypeComboBox.getSelectedItem();
            LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
            LocalDate endDate = LocalDate.parse(endDateField.getText().trim());

            if (endDate.isBefore(startDate)) {
                JOptionPane.showMessageDialog(this, "End date cannot be before start date.", "Date Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            switch (reportType) {
                case "Player Borrowing Statistics":
                    generatePlayerBorrowingStats(startDate, endDate);
                    break;
                case "Card Usage Frequency":
                    generateCardUsageFrequency();
                    break;
                case "Borrow Activity":
                    generateBorrowActivity(startDate, endDate);
                    break;
                case "Deck Usage Frequency":
                    generateDeckUsageFrequency(startDate, endDate);
                    break;
                case "System Summary":
                    generateSystemSummary();
                    break;
            }

            statusLabel.setText(reportType + " generated successfully.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage(), "Report Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error generating report.");
        }
    }

    /**
     * Generates Player Borrowing Statistics report
     */
    private void generatePlayerBorrowingStats(LocalDate startDate, LocalDate endDate) {
        List<PlayerBorrowingStats> stats = reportGenerator.generatePlayerBorrowingStats(startDate, endDate);
        
        String[] columnNames = {"Player ID", "First Name", "Last Name", "Total Borrows", "Avg Duration (Days)", "Overdue Count"};
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0);

        for (PlayerBorrowingStats stat : stats) {
            Object[] rowData = {
                stat.getPlayerId(),
                stat.getFirstName(),
                stat.getLastName(),
                stat.getTotalBorrows(),
                String.format("%.1f", stat.getAvgDuration()),
                stat.getOverdueCount()
            };
            tableModel.addRow(rowData);
        }

        // Update summary
        String summary = String.format(
            "Player Borrowing Statistics Report\n" +
            "Period: %s to %s\n" +
            "Total Players: %d\n" +
            "Total Borrows: %d\n" +
            "Players with Overdue Items: %d",
            startDate, endDate,
            stats.size(),
            stats.stream().mapToInt(PlayerBorrowingStats::getTotalBorrows).sum(),
            stats.stream().filter(s -> s.getOverdueCount() > 0).count()
        );
        reportSummaryArea.setText(summary);
    }

    /**
     * Generates Card Usage Frequency report
     */
    private void generateCardUsageFrequency() {
        List<CardUsageStats> stats = reportGenerator.generateCardUsageFrequency();
        
        String[] columnNames = {"Card ID", "Card Name", "Type", "Mana Cost", "Decks Used In", "Total Copies"};
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0);

        for (CardUsageStats stat : stats) {
            Object[] rowData = {
                stat.getCardId(),
                stat.getCardName(),
                stat.getCardType(),
                stat.getManaCost(),
                stat.getDeckCount(),
                stat.getTotalCopies()
            };
            tableModel.addRow(rowData);
        }

        // Update summary
        String summary = String.format(
            "Card Usage Frequency Report\n" +
            "Top %d Most Used Cards\n" +
            "Average Usage per Card: %.1f decks\n" +
            "Most Used Card: %s (%d decks)",
            stats.size(),
            stats.stream().mapToInt(CardUsageStats::getDeckCount).average().orElse(0),
            stats.isEmpty() ? "N/A" : stats.get(0).getCardName(),
            stats.isEmpty() ? 0 : stats.get(0).getDeckCount()
        );
        reportSummaryArea.setText(summary);
    }

    /**
     * Generates Borrow Activity report
     */
    private void generateBorrowActivity(LocalDate startDate, LocalDate endDate) {
        List<BorrowActivity> activities = reportGenerator.generateBorrowActivity(startDate, endDate);
        
        String[] columnNames = {"Borrow Code", "Player", "Deck", "Request Date", "Return Date", "Status", "Duration (Days)"};
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0);

        for (BorrowActivity activity : activities) {
            Object[] rowData = {
                activity.getBorrowCode(),
                activity.getPlayerFirstName() + " " + activity.getPlayerLastName(),
                activity.getDeckName(),
                activity.getRequestDate(),
                activity.getReturnDate(),
                activity.getStatus(),
                activity.getDurationDays()
            };
            tableModel.addRow(rowData);
        }

        // Update summary
        long returned = activities.stream().filter(a -> "Returned".equals(a.getStatus())).count();
        long overdue = activities.stream().filter(a -> "Overdue".equals(a.getStatus())).count();
        long pending = activities.stream().filter(a -> "Pending".equals(a.getStatus())).count();

        String summary = String.format(
            "Borrow Activity Report\n" +
            "Period: %s to %s\n" +
            "Total Transactions: %d\n" +
            "Returned: %d | Overdue: %d | Pending: %d\n" +
            "Average Duration: %.1f days",
            startDate, endDate,
            activities.size(),
            returned, overdue, pending,
            activities.stream().mapToInt(BorrowActivity::getDurationDays).average().orElse(0)
        );
        reportSummaryArea.setText(summary);
    }

    /**
     * Generates Deck Usage Frequency report
     */
    private void generateDeckUsageFrequency(LocalDate startDate, LocalDate endDate) {
        List<DeckUsageStats> stats = reportGenerator.generateDeckUsageFrequency(startDate, endDate);
        
        String[] columnNames = {"Deck ID", "Deck Name", "Owner", "Bracket", "Validity", "Borrow Count", "Avg Duration", "Last Borrowed"};
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0);

        for (DeckUsageStats stat : stats) {
            Object[] rowData = {
                stat.getDeckId(),
                stat.getDeckName(),
                stat.getOwnerFirstName() + " " + stat.getOwnerLastName(),
                stat.getBracketInfo(),
                stat.getValidity(),
                stat.getBorrowCount(),
                String.format("%.1f", stat.getAvgBorrowDuration()),
                stat.getLastBorrowed()
            };
            tableModel.addRow(rowData);
        }

        // Update summary
        String summary = String.format(
            "Deck Usage Frequency Report\n" +
            "Period: %s to %s\n" +
            "Total Decks: %d\n" +
            "Total Borrows: %d\n" +
            "Most Borrowed Deck: %s (%d times)",
            startDate, endDate,
            stats.size(),
            stats.stream().mapToInt(DeckUsageStats::getBorrowCount).sum(),
            stats.isEmpty() ? "N/A" : stats.get(0).getDeckName(),
            stats.isEmpty() ? 0 : stats.get(0).getBorrowCount()
        );
        reportSummaryArea.setText(summary);
    }

    /**
     * Generates System Summary report
     */
    private void generateSystemSummary() {
        Map<String, Object> summary = reportGenerator.generateSummaryStats();
        
        String[] columnNames = {"Metric", "Value"};
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0);

        for (Map.Entry<String, Object> entry : summary.entrySet()) {
            Object[] rowData = {
                formatMetricName(entry.getKey()),
                entry.getValue()
            };
            tableModel.addRow(rowData);
        }

        // Update summary area
        String summaryText = String.format(
            "System Summary Report\n" +
            "Generated: %s\n" +
            "Database Overview:\n" +
            "- Total Players: %d\n" +
            "- Total Decks: %d\n" +
            "- Total Cards: %d\n" +
            "- Monthly Borrows: %d\n" +
            "- Overdue Items: %d",
            LocalDate.now(),
            summary.getOrDefault("totalPlayers", 0),
            summary.getOrDefault("totalDecks", 0),
            summary.getOrDefault("totalCards", 0),
            summary.getOrDefault("monthlyBorrows", 0),
            summary.getOrDefault("overdueBorrows", 0)
        );
        reportSummaryArea.setText(summaryText);
    }

    /**
     * Formats metric names for display
     */
    private String formatMetricName(String key) {
        switch (key) {
            case "totalPlayers": return "Total Players";
            case "totalDecks": return "Total Decks";
            case "totalCards": return "Total Cards";
            case "monthlyBorrows": return "Borrows This Month";
            case "overdueBorrows": return "Overdue Borrows";
            default: return key;
        }
    }

    /**
     * Handles report export to CSV
     */
    private void handleExportReport(ActionEvent e) {
        // Basic implementation - in a real application, you'd write to a CSV file
        JOptionPane.showMessageDialog(this, 
            "Export feature would save the current report to a CSV file.\n" +
            "This functionality can be implemented to export data for external analysis.",
            "Export Feature", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Clears the current report
     */
    private void clearReport() {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"Select a report type to view data"});
        reportSummaryArea.setText("");
        statusLabel.setText("Report cleared. Ready to generate new report.");
    }

}
