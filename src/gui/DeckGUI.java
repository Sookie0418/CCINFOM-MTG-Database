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

/**
 * A Java Swing GUI for managing Decks and Deck Transactions.
 * STYLED with a Dark MTG theme.
 */
public class DeckGUI extends JFrame {

    // --- Colors & Fonts ---
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color FG_LIGHT = new Color(240, 240, 240);
    private static final Color ACCENT_BLUE = new Color(0, 100, 255);
    private static final Color INPUT_BG = new Color(45, 45, 45);
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final String TASKBAR_ICON_FILE = "taskbar_icon.png";

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
    private JButton refreshButton;
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
            File iconFile = new File(TASKBAR_ICON_FILE);
            if (iconFile.exists()) {
                Image iconImage = new ImageIcon(iconFile.getAbsolutePath()).getImage();
                this.setIconImage(iconImage);
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
        String[] columnNames = {"Deck ID", "Deck Name", "Player ID", "Commander ID", "Bracket Info", "Mana Base", "Salt Score", "Validity", "Description"};
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

        // Configure column widths
        deckTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        deckTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        deckTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        deckTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        deckTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        deckTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        deckTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        deckTable.getColumnModel().getColumn(7).setPreferredWidth(70);
        deckTable.getColumnModel().getColumn(8).setPreferredWidth(150);

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
        deckPanel.setPreferredSize(new Dimension(350, 600));

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
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Style buttons
        styleButton(createDeckButton, new Color(0, 150, 0)); // Green for create
        styleButton(validateDeckButton, new Color(255, 140, 0)); // Orange for validate
        styleButton(addCardButton, new Color(70, 130, 180)); // Steel blue for add card
        styleButton(refreshButton, new Color(100, 100, 100)); // Gray for refresh
        styleButton(clearButton, new Color(90, 90, 90)); // Dark gray for clear

        buttonPanel.add(createDeckButton);
        buttonPanel.add(validateDeckButton);
        buttonPanel.add(addCardButton);
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
        refreshButton = new JButton("Refresh Table");
        clearButton = new JButton("Clear Form");

        // Action Listeners
        createDeckButton.addActionListener(this::handleCreateDeck);
        validateDeckButton.addActionListener(this::handleValidateDeck);
        addCardButton.addActionListener(this::handleAddCardToDeck);
        refreshButton.addActionListener(e -> refreshTable());
        clearButton.addActionListener(e -> clearForm());
    }

    /**
     * Loads the selected deck record into the form.
     */
    private void loadDeckRecordIntoForm(int selectedRow) {
        if (selectedRow >= 0) {
            try {
                int deckId = (int) tableModel.getValueAt(selectedRow, 0);
                String deckName = (String) tableModel.getValueAt(selectedRow, 1);
                int playerId = (int) tableModel.getValueAt(selectedRow, 2);
                String bracketInfo = (String) tableModel.getValueAt(selectedRow, 4);
                String validity = (String) tableModel.getValueAt(selectedRow, 7);
                String description = (String) tableModel.getValueAt(selectedRow, 8);

                deckIdField.setText(String.valueOf(deckId));
                deckNameField.setText(deckName);
                playerIdField.setText(String.valueOf(playerId));
                bracketInfoField.setText(bracketInfo);
                descriptionArea.setText(description != null ? description : "");

                statusArea.setText("Selected Deck ID: " + deckId + "\n" +
                        "Deck Name: " + deckName + "\n" +
                        "Player ID: " + playerId + "\n" +
                        "Validity: " + validity);

                statusLabel.setText("Editing Deck ID: " + deckId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading deck record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    private void refreshTable() {
        tableModel.setRowCount(0);

        try {
            // Note: You'll need to add getAllDecks() method to your controller
            List<Deck> decks = controller.getAllDecks();

            for (Deck deck : decks) {
                Object[] rowData = {
                        deck.getDeckId(),
                        deck.getDeckName(),
                        deck.getPlayerId(),
                        deck.getCommanderCardId(),
                        deck.getBracketNum(),
                        deck.getManaBase(),
                        deck.getValidity(),
                        deck.getDescription()
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

            // Note: You'll need to add createDeck() method to your controller
            int newDeckId = controller.createDeck(deckName, playerId, bracketInfo, description);

            if (newDeckId != -1) {
                statusArea.setText("Deck created successfully!\n" +
                        "Deck ID: " + newDeckId + "\n" +
                        "Deck Name: " + deckName + "\n" +
                        "Player ID: " + playerId + "\n" +
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
        // This would typically open a dialog for card selection
        // For now, we'll show a message about required parameters
        JOptionPane.showMessageDialog(this,
                "To add a card to a deck, you need:\n" +
                        "- Deck ID\n" +
                        "- Card ID\n" +
                        "- Quantity\n" +
                        "- Is Commander (true/false)\n\n" +
                        "This feature would typically open a card selection dialog.",
                "Add Card to Deck",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        // For testing purposes
        SwingUtilities.invokeLater(() -> {
            try {
                MTGDatabaseController controller = new MTGDatabaseController();
                new DeckGUI(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}