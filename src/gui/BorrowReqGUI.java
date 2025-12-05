package gui;
import controller.*;
import entity.BorrowRequest;
import entity.Player;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;

/**
 * A Java Swing GUI for managing Borrow Requests and Transactions.
 * STYLED with a Dark MTG theme.
 */
public class BorrowReqGUI extends JFrame {

    // --- Colors & Fonts ---
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color FG_LIGHT = new Color(240, 240, 240);
    private static final Color ACCENT_RED = new Color(255, 60, 0);
    private static final Color INPUT_BG = new Color(45, 45, 45);
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Color MENU_BAR_COLOR = new Color(40, 40, 40);
    private static final String TASKBAR_ICON_FILE = "/images/taskbar_icon.png";

    // --- UI Components ---
    private JTable borrowTable;
    private DefaultTableModel tableModel;

    // Input Fields for Borrow Transactions
    private JComboBox<String> playerComboBox;
    private Map<String, Integer> playerNameToIdMap = new HashMap<>();
    private JTextField deckIdField;
    private JTextField borrowCodeField;
    private JTextArea statusArea;

    // Buttons
    private JButton requestBorrowButton;
    private JButton returnDeckButton;
    private JButton checkAvailabilityButton;
    private JButton refreshButton;
    private JButton clearButton;
    private JLabel statusLabel;

    private MTGDatabaseController controller;
    private String loggedInUsername;

    public BorrowReqGUI(MTGDatabaseController controller, String username) {
        this.controller = controller;
        this.loggedInUsername = username;

        // 1. Frame Setup
        setTitle("MTG Commander Database System (Borrow Request Management)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1300, 700);
        setLayout(new BorderLayout(15, 15));

        getContentPane().setBackground(BG_DARK);

        // 1.5 Add Menu Bar for Navigation
        setJMenuBar(createMenuBar());

        // 2. Initialize Components
        initializeForm();
        initializeTable();

        // 2.5 Make sure players are loaded
        loadPlayers();

        // 3. Status Bar (Themed)
        statusLabel = new JLabel("Borrow Request Management Ready.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        statusLabel.setBackground(ACCENT_RED);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);

        // 4. Layout Assembly
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        try {
            URL iconUrl = getClass().getResource(TASKBAR_ICON_FILE);
            if (iconUrl != null) {
                Image iconImage = new ImageIcon(iconUrl).getImage();
                this.setIconImage(iconImage); // Set the taskbar and window icon
            }
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }


        // Add Title
        JLabel mainTitle = new JLabel("BORROW REQUEST MANAGEMENT", SwingConstants.CENTER);
        mainTitle.setFont(TITLE_FONT);
        mainTitle.setForeground(ACCENT_RED);
        contentPanel.add(mainTitle, BorderLayout.NORTH);

        contentPanel.add(new JScrollPane(borrowTable), BorderLayout.CENTER);
        contentPanel.add(createTransactionPanel(), BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Set initial visibility and center the window
        setLocationRelativeTo(null);
        setVisible(true);

        // Initial render of data
        refreshTable();
    }

    /**
     * Creates a themed Menu Bar for navigation.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        // Set menu bar background
        menuBar.setBackground(MENU_BAR_COLOR);
        menuBar.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));

        JMenu navMenu = new JMenu("Navigate");
        navMenu.setForeground(FG_LIGHT);
        navMenu.setFont(BOLD_FONT);
        navMenu.setBackground(MENU_BAR_COLOR);

        JMenu appMenu = new JMenu("App");
        appMenu.setForeground(FG_LIGHT);
        appMenu.setFont(BOLD_FONT);
        appMenu.setBackground(MENU_BAR_COLOR);

        // Core Navigation Items
        JMenuItem dashboardItem = new JMenuItem("Dashboard");
        dashboardItem.addActionListener(e -> launchGUI(new DashboardGUI(controller, loggedInUsername)));

        JMenuItem cardItem = new JMenuItem("Card Management");
        cardItem.addActionListener(e -> launchGUI(new CardGUI(controller, loggedInUsername)));

        JMenuItem playerItem = new JMenuItem("Player Management");
        playerItem.addActionListener(e -> launchGUI(new PlayerGUI(controller, loggedInUsername)));

        JMenuItem deckItem = new JMenuItem("Deck Management");
        deckItem.addActionListener(e -> launchGUI(new DeckGUI(controller, loggedInUsername)));

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        // Set styling for all menu items
        JMenuItem[] items = {dashboardItem, cardItem, playerItem, deckItem, exitItem};
        for (JMenuItem item : items) {
            item.setBackground(INPUT_BG);
            item.setForeground(FG_LIGHT);
        }
        exitItem.setForeground(ACCENT_RED); // Highlight exit

        navMenu.add(dashboardItem);
        navMenu.addSeparator();
        navMenu.add(cardItem);
        navMenu.add(playerItem);
        navMenu.add(deckItem);

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
     * Creates and configures the JTable component for borrow requests.
     */
    private void initializeTable() {
        String[] columnNames = {"Borrow Code", "Player ID", "Deck ID", "Borrow Type", "Request Date", "Return Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowTable = new JTable(tableModel);

        // --- Table Styling ---
        borrowTable.setBackground(BG_DARK.brighter());
        borrowTable.setForeground(FG_LIGHT);
        borrowTable.setFont(new Font("Arial", Font.PLAIN, 12));
        borrowTable.setSelectionBackground(ACCENT_RED.darker());
        borrowTable.setSelectionForeground(Color.WHITE);
        borrowTable.setRowHeight(25);

        // Header Styling
        JTableHeader header = borrowTable.getTableHeader();
        header.setBackground(new Color(60, 60, 60));
        header.setForeground(FG_LIGHT);
        header.setFont(BOLD_FONT);
        header.setReorderingAllowed(false);

        borrowTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configure column widths
        borrowTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        borrowTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        borrowTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        borrowTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        borrowTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        borrowTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        borrowTable.getColumnModel().getColumn(6).setPreferredWidth(80);

        borrowTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && borrowTable.getSelectedRow() != -1) {
                loadBorrowRecordIntoForm(borrowTable.getSelectedRow());
            }
        });
    }

    /**
     * Creates the transaction panel with input fields and action buttons.
     */
    private JPanel createTransactionPanel() {
        JPanel transactionPanel = new JPanel();
        transactionPanel.setLayout(new GridBagLayout());
        transactionPanel.setBackground(BG_DARK.brighter());
        transactionPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_RED, 2),
                "Borrow Transactions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                BOLD_FONT.deriveFont(Font.ITALIC, 14),
                FG_LIGHT));
        transactionPanel.setPreferredSize(new Dimension(350, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel inputGrid = new JPanel(new GridBagLayout());
        inputGrid.setOpaque(false);

        // --- Input Fields Layout ---
        int y = 0;

        // Player ComboBox
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        JLabel playerLabel = new JLabel("Player:");
        playerLabel.setForeground(FG_LIGHT);
        inputGrid.add(playerLabel, gbc);

        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        inputGrid.add(playerComboBox, gbc);

        // Deck ID
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        JLabel deckLabel = new JLabel("Deck ID:");
        deckLabel.setForeground(FG_LIGHT);
        inputGrid.add(deckLabel, gbc);

        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        inputGrid.add(deckIdField, gbc);

        // Borrow Code (for returns)
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        JLabel codeLabel = new JLabel("Borrow Code:");
        codeLabel.setForeground(FG_LIGHT);
        inputGrid.add(codeLabel, gbc);

        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        inputGrid.add(borrowCodeField, gbc);

        // Status Area
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.weightx = 1.0;
        JLabel statusAreaLabel = new JLabel("Transaction Status:");
        statusAreaLabel.setForeground(FG_LIGHT);
        inputGrid.add(statusAreaLabel, gbc);

        gbc.gridx = 0; gbc.gridy = y + 1; gbc.weighty = 0.3;
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setPreferredSize(new Dimension(300, 80));
        statusScrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        statusArea.setBackground(INPUT_BG);
        statusArea.setForeground(FG_LIGHT);
        statusArea.setEditable(false);
        inputGrid.add(statusScrollPane, gbc);
        y += 2; gbc.weighty = 0; gbc.gridwidth = 1;

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Style buttons
        styleButton(requestBorrowButton, new Color(0, 150, 0)); // Green for borrow
        styleButton(returnDeckButton, new Color(255, 140, 0)); // Orange for return
        styleButton(checkAvailabilityButton, new Color(70, 130, 180)); // Steel blue for check
        styleButton(refreshButton, new Color(100, 100, 100)); // Gray for refresh
        styleButton(clearButton, new Color(90, 90, 90)); // Dark gray for clear

        buttonPanel.add(requestBorrowButton);
        buttonPanel.add(returnDeckButton);
        buttonPanel.add(checkAvailabilityButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);

        // --- Final Assembly of Transaction Panel ---
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(10, 10, 10, 10);
        gbcForm.fill = GridBagConstraints.HORIZONTAL;

        // Add input grid
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.weightx = 1.0;
        gbcForm.weighty = 0;
        transactionPanel.add(inputGrid, gbcForm);

        // Add button panel
        gbcForm.gridy = 1;
        gbcForm.weighty = 0;
        transactionPanel.add(buttonPanel, gbcForm);

        // Filler component
        gbcForm.gridy = 2;
        gbcForm.weighty = 1.0;
        transactionPanel.add(new JLabel(""), gbcForm);

        return transactionPanel;
    }

    /**
     * Loads player names and IDs into the combo box
     */
    private void loadPlayers() {
        playerNameToIdMap.clear();
        playerComboBox.removeAllItems();

        try {
            List<entity.Player> players = controller.getAllPlayers();

            if (players == null || players.isEmpty()) {
                playerComboBox.addItem("No players found");
                return;
            }

            for (entity.Player player : players) {
                // Get player ID and combine first and last name
                int playerId = player.getPlayerId();
                String fullName = player.getFirstName() + " " + player.getLastName();
                String displayName = String.format("%s (ID: %d)", fullName, playerId);

                playerComboBox.addItem(displayName);
                playerNameToIdMap.put(displayName, playerId);
            }

        } catch (SQLException e) {
            System.err.println("Error loading players: " + e.getMessage());
            playerComboBox.addItem("Error loading players");
            JOptionPane.showMessageDialog(this, "Failed to load player list: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
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
     * Initializes all input fields for borrow transactions.
     */
    private void initializeForm() {
        playerComboBox = new JComboBox<>();
        playerComboBox.setBackground(INPUT_BG);
        playerComboBox.setForeground(FG_LIGHT);
        playerComboBox.setFont(BOLD_FONT);
        playerComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        playerComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT_RED.darker() : INPUT_BG);
                setForeground(FG_LIGHT);
                return this;
            }
        });

        deckIdField = createThemedField(10);
        borrowCodeField = createThemedField(10);
        statusArea = new JTextArea(4, 25);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);

        // Buttons
        requestBorrowButton = new JButton("Request Borrow");
        returnDeckButton = new JButton("Return Deck");
        checkAvailabilityButton = new JButton("Check Availability");
        refreshButton = new JButton("Refresh Table");
        clearButton = new JButton("Clear Form");

        // Action Listeners
        requestBorrowButton.addActionListener(this::handleBorrowRequest);
        returnDeckButton.addActionListener(this::handleReturnDeck);
        checkAvailabilityButton.addActionListener(this::handleCheckAvailability);
        refreshButton.addActionListener(e -> {
            loadPlayers(); // Reload players when refreshing
            refreshTable();
        });
        clearButton.addActionListener(e -> clearForm());
    }

    /**
     * Loads the selected borrow record into the form.
     */
    private void loadBorrowRecordIntoForm(int selectedRow) {
        if (selectedRow >= 0) {
            try {
                int borrowCode = (int) tableModel.getValueAt(selectedRow, 0);
                int playerId = (int) tableModel.getValueAt(selectedRow, 1);
                int deckId = (int) tableModel.getValueAt(selectedRow, 2);
                String borrowType = (String) tableModel.getValueAt(selectedRow, 3);
                String status = (String) tableModel.getValueAt(selectedRow, 6);

                borrowCodeField.setText(String.valueOf(borrowCode));
                deckIdField.setText(String.valueOf(deckId));

                // Find and select the player in combo box
                String playerToSelect = null;
                for (Map.Entry<String, Integer> entry : playerNameToIdMap.entrySet()) {
                    if (entry.getValue() == playerId) {
                        playerToSelect = entry.getKey();
                        break;
                    }
                }

                if (playerToSelect != null) {
                    playerComboBox.setSelectedItem(playerToSelect);
                }

                statusArea.setText("Selected Borrow Code: " + borrowCode + "\n" +
                        "Player ID: " + playerId + "\n" +
                        "Deck ID: " + deckId + "\n" +
                        "Borrow Type: " + borrowType + "\n" +
                        "Status: " + status);

                statusLabel.setText("Editing Borrow Code: " + borrowCode);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading borrow record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clears the input fields and resets the form.
     */
    private void clearForm() {
        playerComboBox.setSelectedIndex(0);
        deckIdField.setText("");
        borrowCodeField.setText("");
        statusArea.setText("");
        borrowTable.clearSelection();
        statusLabel.setText("Form cleared. Ready for new transactions.");
    }

    /**
     * Retrieves the borrow request data and updates the JTable model.
     */
    private void refreshTable() {
        tableModel.setRowCount(0);

        try {
            List<BorrowRequest> requests = controller.getAllBorrowRequests();

            for (BorrowRequest request : requests) {
                Object[] rowData = {
                        request.getBorrowCode(),
                        request.getPlayerId(),
                        request.getDeckId(),
                        request.getBorrowType(),
                        request.getRequestDate(),
                        request.getReturnDate(),
                        request.getStatus()
                };
                tableModel.addRow(rowData);
            }

            statusArea.setText("Loaded " + requests.size() + " borrow requests from database.");
            statusLabel.setText("Table refreshed successfully. Total requests: " + requests.size());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load borrow requests: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("ERROR: Failed to load data.");
            statusArea.setText("Database error: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading borrow requests: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("ERROR: Failed to load data.");
        }
    }

    // --- Event Handlers for Borrow Transactions ---

    private void handleBorrowRequest(ActionEvent e) {
        try {
            // Get selected player from combo box
            String selectedPlayer = (String) playerComboBox.getSelectedItem();

            if (selectedPlayer == null || selectedPlayer.contains("No players") ||
                    selectedPlayer.contains("Error")) {
                JOptionPane.showMessageDialog(this, "Please select a valid player",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int playerId = playerNameToIdMap.get(selectedPlayer);
            int deckId = Integer.parseInt(deckIdField.getText().trim());

            if (playerId <= 0 || deckId <= 0) {
                JOptionPane.showMessageDialog(this, "Player ID and Deck ID must be positive numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = controller.requestBorrow(playerId, deckId);

            if (success) {
                statusArea.setText("Borrow request submitted successfully!\n" +
                        "Player: " + selectedPlayer + "\n" +
                        "Deck ID: " + deckId + "\n" +
                        "Status: Pending\n\n" +
                        "Note: Borrow type is automatically determined based on deck availability.");
                statusLabel.setText("Borrow request created successfully.");
                clearForm();
                refreshTable();
            } else {
                statusArea.setText("Borrow request failed!\n" +
                        "Deck may be unavailable or database error occurred.");
                statusLabel.setText("Borrow request failed - deck may be unavailable.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Deck ID must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error processing borrow request: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error processing borrow request: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReturnDeck(ActionEvent e) {
        try {
            int borrowCode = Integer.parseInt(borrowCodeField.getText().trim());

            if (borrowCode <= 0) {
                JOptionPane.showMessageDialog(this, "Borrow Code must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = controller.returnDeck(borrowCode);

            if (success) {
                statusArea.setText("Deck returned successfully!\n" +
                        "Borrow Code: " + borrowCode + "\n" +
                        "Return Date: " + java.time.LocalDate.now());
                statusLabel.setText("Deck returned successfully.");
                clearForm();
                refreshTable();
            } else {
                statusArea.setText("Deck return failed!\n" +
                        "Borrow Code may not exist or database error occurred.");
                statusLabel.setText("Deck return failed.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Borrow Code must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error returning deck: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error returning deck: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCheckAvailability(ActionEvent e) {
        try {
            int deckId = Integer.parseInt(deckIdField.getText().trim());

            if (deckId <= 0) {
                JOptionPane.showMessageDialog(this, "Deck ID must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean available = controller.isDeckAvailable(deckId);

            if (available) {
                statusArea.setText("Deck Availability Check:\n" +
                        "Deck ID: " + deckId + "\n" +
                        "Status: AVAILABLE ✓\n" +
                        "This deck can be borrowed immediately.\n" +
                        "Borrow type will be: 'Available Borrow'");
                statusLabel.setText("Deck " + deckId + " is available.");
            } else {
                statusArea.setText("Deck Availability Check:\n" +
                        "Deck ID: " + deckId + "\n" +
                        "Status: UNAVAILABLE ✗\n" +
                        "This deck is currently borrowed.\n" +
                        "Borrow type will be: 'Pending Borrow' (waiting list)");
                statusLabel.setText("Deck " + deckId + " is unavailable.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Deck ID must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error checking availability: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error checking availability: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}