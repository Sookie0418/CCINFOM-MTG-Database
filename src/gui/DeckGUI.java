package gui;
import controller.*;
import entity.Deck;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.sql.SQLException;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JCheckBox;
import java.net.URL;

/**
 * A Java Swing GUI for managing Decks and Deck Transactions.
 * STYLED with a Dark MTG theme.S
 */
public class DeckGUI extends JFrame {

    // --- Colors & Fonts ---
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color FG_LIGHT = new Color(240, 240, 240);
    private static final Color ACCENT_BLUE = new Color(0, 100, 255);
    private static final Color INPUT_BG = new Color(45, 45, 45);
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final String TASKBAR_ICON_FILE = "/images/taskbar_icon.png";

    // --- UI Components ---
    private JTable deckTable;
    private DefaultTableModel tableModel;

    // Input Fields for Deck Management
    private JTextField deckIdField;
    private JTextField deckNameField;
    private JTextField playerIdField;
    private JTextField bracketInfoField;
    private JTextArea descriptionArea;
    private JTextArea statusArea;

    // Buttons
    private JButton createDeckButton;
    private JButton validateDeckButton;
    private JButton addCardButton;
    private JButton viewCardsButton;
    private JButton refreshButton;
    private JButton deleteDeckButton;
    private JButton clearButton;
    private JLabel statusLabel;

    private MTGDatabaseController controller;

    public DeckGUI(MTGDatabaseController controller) {
        this.controller = controller;

        // 1. Frame Setup
        setTitle("MTG Commander Database System (Deck Management)");
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
        statusLabel = new JLabel("Deck Management Ready.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        statusLabel.setBackground(ACCENT_BLUE);
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
        JLabel mainTitle = new JLabel("DECK MANAGEMENT", SwingConstants.CENTER);
        mainTitle.setFont(TITLE_FONT);
        mainTitle.setForeground(ACCENT_BLUE);
        contentPanel.add(mainTitle, BorderLayout.NORTH);

        contentPanel.add(new JScrollPane(deckTable), BorderLayout.CENTER);
        contentPanel.add(createDeckPanel(), BorderLayout.EAST);

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
        menuBar.setBackground(BG_DARK.brighter());
        menuBar.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));

        JMenu navMenu = new JMenu("Navigate");
        navMenu.setForeground(FG_LIGHT);
        navMenu.setFont(BOLD_FONT);

        JMenu appMenu = new JMenu("App");
        appMenu.setForeground(FG_LIGHT);
        appMenu.setFont(BOLD_FONT);

        JMenuItem dashboardItem = new JMenuItem("Dashboard");
        dashboardItem.addActionListener(e -> launchGUI(new DashboardGUI(controller)));
        dashboardItem.setBackground(INPUT_BG);
        dashboardItem.setForeground(FG_LIGHT);

        JMenuItem cardItem = new JMenuItem("Card Management");
        cardItem.addActionListener(e -> launchGUI(new CardGUI(controller)));
        cardItem.setBackground(INPUT_BG);
        cardItem.setForeground(FG_LIGHT);

        JMenuItem playerItem = new JMenuItem("Player Management");
        playerItem.addActionListener(e -> launchGUI(new PlayerGUI(controller)));
        playerItem.setBackground(INPUT_BG);
        playerItem.setForeground(FG_LIGHT);

        JMenuItem borrowItem = new JMenuItem("Borrow Management");
        borrowItem.addActionListener(e -> launchGUI(new BorrowReqGUI(controller)));
        borrowItem.setBackground(INPUT_BG);
        borrowItem.setForeground(FG_LIGHT);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        exitItem.setBackground(INPUT_BG);
        exitItem.setForeground(Color.RED);

        navMenu.add(dashboardItem);
        navMenu.add(cardItem);
        navMenu.add(playerItem);
        navMenu.add(borrowItem);
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
     * Creates and configures the JTable component for decks.
     */
    private void initializeTable() {
        String[] columnNames = {"Deck ID", "Deck Name", "Player ID", "Commander ID", "Bracket Info", "Validity", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deckTable = new JTable(tableModel);

        // --- Table Styling ---
        deckTable.setBackground(BG_DARK.brighter());
        deckTable.setForeground(FG_LIGHT);
        deckTable.setFont(new Font("Arial", Font.PLAIN, 12));
        deckTable.setSelectionBackground(ACCENT_BLUE.darker());
        deckTable.setSelectionForeground(Color.WHITE);
        deckTable.setRowHeight(25);

        // Header Styling
        JTableHeader header = deckTable.getTableHeader();
        header.setBackground(new Color(60, 60, 60));
        header.setForeground(FG_LIGHT);
        header.setFont(BOLD_FONT);

        deckTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configure column widths (adjusted for removed columns)
        deckTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        deckTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        deckTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        deckTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        deckTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        deckTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        deckTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        deckTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && deckTable.getSelectedRow() != -1) {
                loadDeckRecordIntoForm(deckTable.getSelectedRow());
            }
        });
    }

    /**
     * Creates the deck management panel with input fields and action buttons.
     */
    private JPanel createDeckPanel() {
        JPanel deckPanel = new JPanel();
        deckPanel.setLayout(new GridBagLayout());
        deckPanel.setBackground(BG_DARK.brighter());
        deckPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                "Deck Operations",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                BOLD_FONT.deriveFont(Font.ITALIC, 14),
                FG_LIGHT));
        deckPanel.setPreferredSize(new Dimension(350, 700));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel inputGrid = new JPanel(new GridBagLayout());
        inputGrid.setOpaque(false);

        // --- Input Fields Layout ---
        int y = 0;

        // Deck ID
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        JLabel deckIdLabel = new JLabel("Deck ID:");
        deckIdLabel.setForeground(FG_LIGHT);
        inputGrid.add(deckIdLabel, gbc);

        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        deckIdField.setEditable(true);
        deckIdField.setBackground(new Color(60, 60, 60));
        inputGrid.add(deckIdField, gbc);

        // Deck Name
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        JLabel deckNameLabel = new JLabel("Deck Name:");
        deckNameLabel.setForeground(FG_LIGHT);
        inputGrid.add(deckNameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        inputGrid.add(deckNameField, gbc);

        // Player ID
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        JLabel playerIdLabel = new JLabel("Player ID:");
        playerIdLabel.setForeground(FG_LIGHT);
        inputGrid.add(playerIdLabel, gbc);

        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        inputGrid.add(playerIdField, gbc);

        // Bracket Info
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        JLabel bracketLabel = new JLabel("Bracket Info:");
        bracketLabel.setForeground(FG_LIGHT);
        inputGrid.add(bracketLabel, gbc);

        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0;
        inputGrid.add(bracketInfoField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.weightx = 1.0;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setForeground(FG_LIGHT);
        inputGrid.add(descLabel, gbc);

        gbc.gridx = 0; gbc.gridy = y + 1; gbc.weighty = 0.2;
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(300, 60));
        descScrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        descriptionArea.setBackground(INPUT_BG);
        descriptionArea.setForeground(FG_LIGHT);
        inputGrid.add(descScrollPane, gbc);
        y += 2; gbc.weighty = 0; gbc.gridwidth = 1;

        // Status Area
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.weightx = 1.0;
        JLabel statusAreaLabel = new JLabel("Operation Status:");
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
        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Style buttons
        styleButton(createDeckButton, new Color(0, 150, 0)); // Green for create
        styleButton(validateDeckButton, new Color(255, 140, 0)); // Orange for validate
        styleButton(addCardButton, new Color(70, 130, 180)); // Steel blue for add card
        styleButton(viewCardsButton, new Color(100, 100, 200)); // Purple for view cards
        styleButton(deleteDeckButton, new Color(200, 0, 0)); // RED for delete
        styleButton(refreshButton, new Color(100, 100, 100)); // Gray for refresh
        styleButton(clearButton, new Color(90, 90, 90)); // Dark gray for clear

        buttonPanel.add(createDeckButton);
        buttonPanel.add(validateDeckButton);
        buttonPanel.add(addCardButton);
        buttonPanel.add(viewCardsButton);
        buttonPanel.add(deleteDeckButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);

        // --- Final Assembly of Deck Panel ---
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(10, 10, 10, 10);
        gbcForm.fill = GridBagConstraints.HORIZONTAL;

        // Add input grid
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.weightx = 1.0;
        gbcForm.weighty = 0;
        deckPanel.add(inputGrid, gbcForm);

        // Add button panel
        gbcForm.gridy = 1;
        gbcForm.weighty = 0;
        deckPanel.add(buttonPanel, gbcForm);

        // Filler component
        gbcForm.gridy = 2;
        gbcForm.weighty = 1.0;
        deckPanel.add(new JLabel(""), gbcForm);

        return deckPanel;
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
     * Initializes all input fields for deck management.
     */
    private void initializeForm() {
        deckIdField = createThemedField(10);
        deckNameField = createThemedField(15);
        playerIdField = createThemedField(10);
        bracketInfoField = createThemedField(15);
        descriptionArea = new JTextArea(3, 25);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        statusArea = new JTextArea(4, 25);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);

        // Buttons
        createDeckButton = new JButton("Create New Deck");
        validateDeckButton = new JButton("Validate Deck");
        addCardButton = new JButton("Add Card to Deck");
        viewCardsButton = new JButton("View Deck Cards");
        refreshButton = new JButton("Refresh Table");
        clearButton = new JButton("Clear Form");
        deleteDeckButton = new JButton("Delete Deck");

        // Action Listeners
        createDeckButton.addActionListener(this::handleCreateDeck);
        validateDeckButton.addActionListener(this::handleValidateDeck);
        addCardButton.addActionListener(this::handleAddCardToDeck);
        viewCardsButton.addActionListener(this::handleViewDeckCards);
        refreshButton.addActionListener(e -> refreshTable());
        clearButton.addActionListener(e -> clearForm());
        deleteDeckButton.addActionListener(this::handleDeleteDeck);
    }

    /**
     * Loads the selected deck record into the form.
     */
    /**
     * Loads the selected deck record into the form.
     */
    /**
     * Loads the selected deck record into the form.
     */
    /**
     * Loads the selected deck record into the form.
     */
    /**
     * Loads the selected deck record into the form.
     */
    private void loadDeckRecordIntoForm(int selectedRow) {
        if (selectedRow >= 0) {
            try {
                // Get all values from the selected row with proper type handling
                Object deckIdObj = tableModel.getValueAt(selectedRow, 0);
                Object deckNameObj = tableModel.getValueAt(selectedRow, 1);
                Object playerIdObj = tableModel.getValueAt(selectedRow, 2);
                Object commanderIdObj = tableModel.getValueAt(selectedRow, 3);
                Object bracketInfoObj = tableModel.getValueAt(selectedRow, 4);
                Object validityObj = tableModel.getValueAt(selectedRow, 5);
                Object descriptionObj = tableModel.getValueAt(selectedRow, 6);

                // Convert to appropriate types
                int deckId = deckIdObj != null ? Integer.parseInt(deckIdObj.toString()) : -1;
                String deckName = deckNameObj != null ? deckNameObj.toString() : "";
                int playerId = playerIdObj != null ? Integer.parseInt(playerIdObj.toString()) : -1;
                Integer commanderId = null;
                if (commanderIdObj != null && !commanderIdObj.toString().isEmpty()) {
                    commanderId = Integer.parseInt(commanderIdObj.toString());
                }
                String bracketInfo = bracketInfoObj != null ? bracketInfoObj.toString() : "";
                String validity = validityObj != null ? validityObj.toString() : "";
                String description = descriptionObj != null ? descriptionObj.toString() : "";

                // Populate fields
                deckIdField.setText(deckId != -1 ? String.valueOf(deckId) : "");
                deckNameField.setText(deckName);
                playerIdField.setText(playerId != -1 ? String.valueOf(playerId) : "");
                bracketInfoField.setText(bracketInfo);
                descriptionArea.setText(description);

                // Enable operations for existing deck
                validateDeckButton.setEnabled(true);
                addCardButton.setEnabled(true);
                viewCardsButton.setEnabled(true);

                statusArea.setText("📋 DECK SELECTED\n\n" +
                        "Deck ID: " + deckId + "\n" +
                        "Deck Name: " + deckName + "\n" +
                        "Player ID: " + playerId + "\n" +
                        "Commander ID: " + (commanderId != null ? commanderId : "Not set") + "\n" +
                        "Current Validity: " + validity + "\n\n" +
                        "Available actions:\n" +
                        "• Add more cards\n" +
                        "• Validate deck\n" +
                        "• View current cards");

                statusLabel.setText("Editing Deck: " + deckName + " (ID: " + deckId + ")");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading deck record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace(); // Add this for debugging
            }
        }
    }

    /**
     * Clears the input fields and resets the form.
     */
    private void clearForm() {
        deckIdField.setText("");
        deckNameField.setText("");
        playerIdField.setText("");
        bracketInfoField.setText("");
        descriptionArea.setText("");
        statusArea.setText("");
        deckTable.clearSelection();
        statusLabel.setText("Form cleared. Ready for new operations.");
    }

    /**
     * Retrieves the deck data and updates the JTable model.
     */
    /**
     * Retrieves the deck data and updates the JTable model.
     */
    private void refreshTable() {
        tableModel.setRowCount(0);

        try {
            List<Deck> decks = controller.getAllDecks();

            for (Deck deck : decks) {
                Object[] rowData = {
                        deck.getDeckId(),
                        deck.getDeckName(),
                        deck.getPlayerId(),
                        deck.getCommanderCardId(),                 // This should be column 3
                        deck.getBracketInfo(),                     // Use the getter method instead of getBracketNum()
                        deck.getValidity() ? "Valid" : "Invalid",  // This should be column 5 (was column 7 before)
                        deck.getDescription()                      // This should be column 6 (was column 8 before)
                };
                tableModel.addRow(rowData);
            }

            statusArea.setText("Loaded " + decks.size() + " decks from database.");
            statusLabel.setText("Table refreshed successfully. Total decks: " + decks.size());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load decks: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("ERROR: Failed to load data.");
            statusArea.setText("Database error: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading decks: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("ERROR: Failed to load data.");
        }
    }

    // --- Event Handlers for Deck Operations ---

    private void handleCreateDeck(ActionEvent e) {
        try {
            String deckName = deckNameField.getText().trim();
            int playerId = Integer.parseInt(playerIdField.getText().trim());
            String bracketInfo = bracketInfoField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (deckName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Deck name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (playerId <= 0) {
                JOptionPane.showMessageDialog(this, "Player ID must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validate bracket info
            if (!bracketInfo.isEmpty()) {
                try {
                    // Try to parse bracket info
                    if (bracketInfo.startsWith("Bracket ")) {
                        int bracketNum = Integer.parseInt(bracketInfo.substring(8).trim());
                        if (bracketNum < 1 || bracketNum > 5) {
                            JOptionPane.showMessageDialog(this, "Bracket must be between 1 and 5.", "Input Error", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    } else {
                        int bracketNum = Integer.parseInt(bracketInfo.trim());
                        if (bracketNum < 1 || bracketNum > 5) {
                            JOptionPane.showMessageDialog(this, "Bracket must be between 1 and 5.", "Input Error", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        // Convert to proper format
                        bracketInfo = "Bracket " + bracketNum;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Bracket info must be a number (1-5) or 'Bracket X' format.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            int newDeckId = controller.createDeck(deckName, playerId, bracketInfo, description);

            if (newDeckId != -1) {
                deckIdField.setText(String.valueOf(newDeckId));

                validateDeckButton.setEnabled(true);
                addCardButton.setEnabled(true);
                viewCardsButton.setEnabled(true);
                deleteDeckButton.setEnabled(true); // Enable delete for new deck

                statusArea.setText("Deck created successfully!\n" +
                        "Deck ID: " + newDeckId + "\n" +
                        "Deck Name: " + deckName + "\n" +
                        "Player ID: " + playerId + "\n" +
                        "Bracket: " + (bracketInfo.isEmpty() ? "Not set" : bracketInfo) + "\n" +
                        "Status: Created (Invalid - needs validation)");
                statusLabel.setText("Deck created successfully with ID: " + newDeckId);
                clearForm();
                refreshTable();
            } else {
                statusArea.setText("Deck creation failed!\n" +
                        "Database error occurred.");
                statusLabel.setText("Deck creation failed.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Player ID must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error creating deck: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error creating deck: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleValidateDeck(ActionEvent e) {
        try {
            int deckId = Integer.parseInt(deckIdField.getText().trim());

            if (deckId <= 0) {
                JOptionPane.showMessageDialog(this, "Deck ID must be a positive number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Note: You'll need to add validateDeck() method to your controller
            String validationResult = controller.validateDeck(deckId);

            statusArea.setText("Deck Validation Result:\n" +
                    "Deck ID: " + deckId + "\n" +
                    "Result: " + validationResult);
            statusLabel.setText("Deck validation completed.");
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Deck ID must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error validating deck: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error validating deck: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddCardToDeck(ActionEvent e) {
        try {
            String deckIdText = deckIdField.getText().trim();
            if (deckIdText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a deck first.", "No Deck Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int deckId = Integer.parseInt(deckIdText);

            // Create dialog for adding cards
            JDialog addCardDialog = new JDialog(this, "Add Card to Deck " + deckId, true);
            addCardDialog.setLayout(new BorderLayout());
            addCardDialog.setSize(500, 350);
            addCardDialog.setLocationRelativeTo(this);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(BG_DARK);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Input fields
            JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
            inputPanel.setBackground(BG_DARK);
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel cardIdLabel = new JLabel("Card ID:");
            cardIdLabel.setForeground(FG_LIGHT);
            JTextField cardIdField = createThemedField(10);

            JLabel cardInfoLabel = new JLabel("Card Info:");
            cardInfoLabel.setForeground(FG_LIGHT);
            JTextArea cardInfoArea = new JTextArea(3, 20);
            cardInfoArea.setEditable(false);
            cardInfoArea.setBackground(INPUT_BG);
            cardInfoArea.setForeground(FG_LIGHT);
            cardInfoArea.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
            JScrollPane cardInfoScroll = new JScrollPane(cardInfoArea);

            JLabel quantityLabel = new JLabel("Quantity:");
            quantityLabel.setForeground(FG_LIGHT);
            JTextField quantityField = createThemedField(5);
            quantityField.setText("1");

            JLabel isCommanderLabel = new JLabel("Is Commander:");
            isCommanderLabel.setForeground(FG_LIGHT);
            JCheckBox isCommanderCheckbox = new JCheckBox();
            isCommanderCheckbox.setBackground(BG_DARK);
            isCommanderCheckbox.setForeground(FG_LIGHT);

            // Add card lookup functionality
            JButton lookupButton = new JButton("Lookup Card");
            styleButton(lookupButton, new Color(70, 130, 180));
            lookupButton.addActionListener(lookupEvt -> {
                try {
                    int cardId = Integer.parseInt(cardIdField.getText().trim());
                    // Get card info from controller
                    entity.Card card = controller.getCardById(cardId);
                    if (card != null) {
                        cardInfoArea.setText("Name: " + card.getCardName() + "\n" +
                                "Type: " + card.getCardType() + "\n" +
                                "Mana Cost: " + card.getManaCost() + "\n" +
                                "Status: " + card.getCardStatus());

                        // Auto-check commander for legendary creatures
                        if (card.getCardType().toLowerCase().contains("legendary") &&
                                card.getCardType().toLowerCase().contains("creature")) {
                            isCommanderCheckbox.setSelected(true);
                            cardInfoArea.append("\n⚜️ Legendary Creature - Can be Commander");
                        }
                    } else {
                        cardInfoArea.setText("Card not found with ID: " + cardId);
                        isCommanderCheckbox.setSelected(false);
                    }
                } catch (NumberFormatException ex) {
                    cardInfoArea.setText("Please enter a valid Card ID");
                } catch (SQLException ex) {
                    cardInfoArea.setText("Database error: " + ex.getMessage());
                }
            });

            inputPanel.add(cardIdLabel);
            inputPanel.add(cardIdField);
            inputPanel.add(new JLabel()); // spacer
            inputPanel.add(lookupButton);
            inputPanel.add(cardInfoLabel);
            inputPanel.add(new JLabel()); // spacer for layout
            inputPanel.add(new JLabel()); // spacer
            inputPanel.add(cardInfoScroll);
            inputPanel.add(quantityLabel);
            inputPanel.add(quantityField);
            inputPanel.add(isCommanderLabel);
            inputPanel.add(isCommanderCheckbox);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(BG_DARK);

            JButton addButton = new JButton("Add Card");
            styleButton(addButton, new Color(0, 150, 0));

            JButton cancelButton = new JButton("Cancel");
            styleButton(cancelButton, new Color(100, 100, 100));

            buttonPanel.add(addButton);
            buttonPanel.add(cancelButton);

            // Add action listeners
            addButton.addActionListener(evt -> {
                try {
                    int cardId = Integer.parseInt(cardIdField.getText().trim());
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    boolean isCommander = isCommanderCheckbox.isSelected();

                    if (cardId <= 0 || quantity <= 0) {
                        JOptionPane.showMessageDialog(addCardDialog, "Card ID and Quantity must be positive numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (quantity > 4 && !isCommander) {
                        int confirm = JOptionPane.showConfirmDialog(addCardDialog,
                                "Normal cards are typically limited to 4 copies. Are you sure you want to add " + quantity + " copies?",
                                "Quantity Warning",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }

                    // Validate commander rules
                    if (isCommander) {
                        try {
                            entity.Card card = controller.getCardById(cardId);
                            if (card != null && (!card.getCardType().toLowerCase().contains("legendary") ||
                                    !card.getCardType().toLowerCase().contains("creature"))) {
                                int confirm = JOptionPane.showConfirmDialog(addCardDialog,
                                        "This card doesn't appear to be a Legendary Creature. " +
                                                "Only Legendary Creatures can be commanders. Continue anyway?",
                                        "Commander Warning",
                                        JOptionPane.YES_NO_OPTION);
                                if (confirm != JOptionPane.YES_OPTION) {
                                    return;
                                }
                            }
                        } catch (SQLException ex) {
                            // Continue anyway if we can't validate
                        }
                    }

                    // Call controller to add card to deck
                    boolean success = controller.addCardToDeck(deckId, cardId, quantity, isCommander);

                    if (success) {
                        JOptionPane.showMessageDialog(addCardDialog,
                                "Card successfully added to deck!\n" +
                                        "Card ID: " + cardId + "\n" +
                                        "Quantity: " + quantity + "\n" +
                                        "Is Commander: " + (isCommander ? "Yes" : "No"),
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        addCardDialog.dispose();

                        // Update status
                        statusArea.setText("Card added to deck!\n\n" +
                                "Deck ID: " + deckId + "\n" +
                                "Card ID: " + cardId + "\n" +
                                "Quantity: " + quantity + "\n" +
                                "Commander: " + (isCommander ? "Yes" : "No"));
                        statusLabel.setText("Card added to deck " + deckId);

                        // Auto-validate deck after adding cards
                        refreshTable();
                    } else {
                        JOptionPane.showMessageDialog(addCardDialog,
                                "Failed to add card to deck.\n" +
                                        "Possible reasons:\n" +
                                        "- Card doesn't exist\n" +
                                        "- Card is banned\n" +
                                        "- Database error",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(addCardDialog, "Card ID and Quantity must be valid numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(addCardDialog, "Database error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(addCardDialog, "Error adding card: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(evt -> addCardDialog.dispose());

            mainPanel.add(new JLabel("Add Card to Deck " + deckId, SwingConstants.CENTER), BorderLayout.NORTH);
            mainPanel.add(inputPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            addCardDialog.add(mainPanel);
            addCardDialog.setVisible(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid deck ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    /**
     * Shows the cards currently in the selected deck
     */
    /**
     * Shows the cards currently in the selected deck
     */
    /**
     * Shows the cards currently in the selected deck
     */
    private void handleViewDeckCards(ActionEvent e) {
        try {
            String deckIdText = deckIdField.getText().trim();
            if (deckIdText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a deck first.", "No Deck Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int deckId = Integer.parseInt(deckIdText);

            // Create dialog to show cards
            JDialog cardsDialog = new JDialog(this, "Cards in Deck " + deckId, true);
            cardsDialog.setLayout(new BorderLayout());
            cardsDialog.setSize(800, 500);
            cardsDialog.setLocationRelativeTo(this);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(BG_DARK);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Create table for cards
            String[] columnNames = {"Card ID", "Card Name", "Type", "Mana Cost", "Power/Tough", "Quantity", "Commander"};
            DefaultTableModel cardsModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int column) {
                    return switch (column) {
                        case 0, 5 -> Integer.class; // Card ID and Quantity are integers
                        case 6 -> Boolean.class; // Commander is boolean
                        default -> String.class;
                    };
                }
            };

            JTable cardsTable = new JTable(cardsModel);

            // Style the table
            cardsTable.setBackground(BG_DARK.brighter());
            cardsTable.setForeground(FG_LIGHT);
            cardsTable.setSelectionBackground(ACCENT_BLUE.darker());
            cardsTable.setSelectionForeground(Color.WHITE);
            cardsTable.setRowHeight(25);
            cardsTable.setFont(new Font("Arial", Font.PLAIN, 12));
            cardsTable.getColumnModel().getColumn(6).setPreferredWidth(80);

            // Header Styling
            JTableHeader header = cardsTable.getTableHeader();
            header.setBackground(new Color(60, 60, 60));
            header.setForeground(FG_LIGHT);
            header.setFont(BOLD_FONT);

            JScrollPane scrollPane = new JScrollPane(cardsTable);

            try {
                // Get cards from the deck using controller
                List<Map<String, Object>> deckCards = controller.getCardsInDeck(deckId);

                if (deckCards.isEmpty()) {
                    cardsModel.addRow(new Object[]{"-", "No cards in deck", "-", "-", "-", 0, false});
                } else {
                    int totalCards = 0;
                    boolean hasCommander = false;

                    for (Map<String, Object> card : deckCards) {
                        Integer power = null;
                        Integer toughness = null;

                        // Try to get power/toughness for creatures
                        try {
                            entity.Card cardDetails = controller.getCardById((Integer) card.get("card_id"));
                            if (cardDetails != null) {
                                power = cardDetails.getPower();
                                toughness = cardDetails.getToughness();
                            }
                        } catch (Exception ex) {
                            // Ignore if we can't get details
                        }

                        String powerTough = (power != null && power > 0 && toughness != null && toughness > 0) ?
                                power + "/" + toughness : "-";

                        Object[] rowData = {
                                card.get("card_id"),
                                card.get("card_name"),
                                card.get("type"),
                                card.get("mana_cost"),
                                powerTough,
                                card.get("quantity"),
                                card.get("is_commander")
                        };
                        cardsModel.addRow(rowData);

                        totalCards += (Integer) card.get("quantity");
                        if ((Boolean) card.get("is_commander")) {
                            hasCommander = true;
                        }
                    }

                    // Update title with stats
                    String title = "Cards in Deck " + deckId + " - " + deckCards.size() + " unique cards, " + totalCards + " total";
                    if (hasCommander) {
                        title += " ⚜️";
                    }
                }
            } catch (Exception ex) {
                cardsModel.addRow(new Object[]{"Error", "loading cards", ex.getMessage(), "", "", 0, false});
            }

            JLabel titleLabel = new JLabel("Cards in Deck " + deckId, SwingConstants.CENTER);
            titleLabel.setForeground(FG_LIGHT);
            titleLabel.setFont(BOLD_FONT);

            JButton closeButton = new JButton("Close");
            styleButton(closeButton, new Color(100, 100, 100));
            closeButton.addActionListener(evt -> cardsDialog.dispose());

            // Add remove card functionality
            JButton removeCardButton = new JButton("Remove Selected Card");
            styleButton(removeCardButton, new Color(200, 0, 0));
            removeCardButton.addActionListener(evt -> {
                int selectedRow = cardsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int cardId = (int) cardsModel.getValueAt(selectedRow, 0);
                    String cardName = (String) cardsModel.getValueAt(selectedRow, 1);
                    boolean isCommander = (Boolean) cardsModel.getValueAt(selectedRow, 6);

                    if (isCommander) {
                        JOptionPane.showMessageDialog(cardsDialog,
                                "Cannot remove commander card '" + cardName + "'.\n" +
                                        "You must assign a new commander first.",
                                "Commander Protection",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(cardsDialog,
                            "Remove card '" + cardName + "' (ID: " + cardId + ") from deck?",
                            "Confirm Removal",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            boolean success = controller.removeCardFromDeck(deckId, cardId);
                            if (success) {
                                cardsModel.removeRow(selectedRow);
                                JOptionPane.showMessageDialog(cardsDialog, "Card removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                                // Refresh if no cards left
                                if (cardsModel.getRowCount() == 1 && cardsModel.getValueAt(0, 0).equals("-")) {
                                    cardsDialog.dispose();
                                }
                            } else {
                                JOptionPane.showMessageDialog(cardsDialog, "Failed to remove card.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(cardsDialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(cardsDialog, "Please select a card to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(BG_DARK);
            buttonPanel.add(removeCardButton);
            buttonPanel.add(closeButton);

            mainPanel.add(titleLabel, BorderLayout.NORTH);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            cardsDialog.add(mainPanel);
            cardsDialog.setVisible(true);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid deck ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading deck cards: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteDeck(ActionEvent e) {
        try {
            String deckIdText = deckIdField.getText().trim();
            if (deckIdText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a deck to delete first.", "No Deck Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int deckId = Integer.parseInt(deckIdText);
            String deckName = deckNameField.getText().trim();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this deck?\n" +
                            "Deck ID: " + deckId + "\n" +
                            "Deck Name: " + (deckName.isEmpty() ? "Unnamed Deck" : deckName) + "\n\n" +
                            "This will also remove all cards from the deck!",
                    "Confirm Deck Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Call controller to delete deck
                boolean success = controller.deleteDeck(deckId);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Deck deleted successfully!\n" +
                                    "Deck ID: " + deckId + "\n" +
                                    "Deck Name: " + deckName,
                            "Deck Deleted",
                            JOptionPane.INFORMATION_MESSAGE);

                    statusArea.setText("Deck deleted successfully!\n\n" +
                            "Deleted Deck ID: " + deckId + "\n" +
                            "Deleted Deck Name: " + deckName);
                    statusLabel.setText("Deck deleted: " + deckName);

                    clearForm();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete deck.\n" +
                                    "The deck may not exist or there might be active borrow requests.",
                            "Deletion Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid deck ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error deleting deck: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting deck: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}