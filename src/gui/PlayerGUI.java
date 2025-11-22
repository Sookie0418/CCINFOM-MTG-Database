package gui;
import controller.*;
import entity.Player;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader; // Needed for table header styling
import javax.swing.border.TitledBorder; // Needed for form border styling
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.net.URL;

/**
 * A dedicated Swing GUI for managing Player records (CRUD).
 * STYLED with a Dark MTG theme.
 */
public class PlayerGUI extends JFrame {

    // --- Colors & Fonts (Synchronized with CardGUI) ---
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color FG_LIGHT = new Color(240, 240, 240);
    private static final Color ACCENT_RED = new Color(255, 60, 0);
    private static final Color INPUT_BG = new Color(45, 45, 45);
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Color BUTTON_BLUE = new Color(50, 150, 255); // Update Button Color
    private static final Color MENU_BAR_COLOR = new Color(40, 40, 40);
    private static final String TASKBAR_ICON_FILE = "/images/taskbar_icon.png";

    // Reference to the controller
    private MTGDatabaseController controller;

    // --- UI Components ---
    private JTable dataTable;
    private DefaultTableModel tableModel;

    // Input Fields
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField cityAddressField;
    private JTextField ageField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JLabel statusLabel;

    // Tracks the ID of the player currently being edited
    private int editingPlayerId = -1;

    public PlayerGUI(MTGDatabaseController controller) {
        this.controller = controller;

        // 1. Frame Setup
        setTitle("MTG Commander Database System (Player Management)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout(15, 15));

        // Apply dark background to the frame
        getContentPane().setBackground(BG_DARK);

        setJMenuBar(createMenuBar());

        // 2. Initialize Components
        initializeForm();
        initializeTable();

        // 3. Status Bar (Themed)
        statusLabel = new JLabel("Application Ready.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        statusLabel.setBackground(ACCENT_RED);
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setOpaque(true);

        try {
            URL iconUrl = getClass().getResource(TASKBAR_ICON_FILE);
            if (iconUrl != null) {
                Image iconImage = new ImageIcon(iconUrl).getImage();
                this.setIconImage(iconImage); // Set the taskbar and window icon
            }
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }


        // 4. Layout Assembly
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // Add Title
        JLabel mainTitle = new JLabel("PLAYER RECORD MANAGER", SwingConstants.CENTER);
        mainTitle.setFont(TITLE_FONT);
        mainTitle.setForeground(ACCENT_RED);
        contentPanel.add(mainTitle, BorderLayout.NORTH);

        contentPanel.add(new JScrollPane(dataTable), BorderLayout.CENTER);
        contentPanel.add(createFormPanel(), BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Set initial visibility and center the window
        setLocationRelativeTo(null);
        setVisible(true);

        // Initial render of data
        refreshTable();
    }
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
        dashboardItem.addActionListener(e -> launchGUI(new DashboardGUI(controller)));

        JMenuItem cardItem = new JMenuItem("Card Management");
        cardItem.addActionListener(e -> launchGUI(new CardGUI(controller)));

        JMenuItem playerItem = new JMenuItem("Player Management");
        playerItem.addActionListener(e -> launchGUI(new PlayerGUI(controller)));

        // NEW: Deck and Borrow Management Items (from CardGUI)
        JMenuItem deckItem = new JMenuItem("Deck Management");
        deckItem.addActionListener(e -> launchGUI(new DeckGUI(controller)));

        JMenuItem borrowItem = new JMenuItem("Borrow Requests");
        borrowItem.addActionListener(e -> launchGUI(new BorrowReqGUI(controller)));

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        // Set styling for all menu items
        JMenuItem[] items = {dashboardItem, cardItem, playerItem, deckItem, borrowItem, exitItem};
        for (JMenuItem item : items) {
            item.setBackground(INPUT_BG);
            item.setForeground(FG_LIGHT);
        }
        exitItem.setForeground(ACCENT_RED); // Highlight exit

        navMenu.add(dashboardItem);
        navMenu.addSeparator();
        navMenu.add(cardItem);
        navMenu.add(deckItem);
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
        // We dispose of the current frame to show the Dashboard as the primary interface
        // if navigating back, but keep it open if navigating to a utility window.
        if (frame instanceof DashboardGUI) {
            this.dispose();
        }
    }

    /**
     * Creates a themed JTextField for dark backgrounds.
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
     * Creates and configures the JTable component with Player columns.
     */
    private void initializeTable() {
        String[] columnNames = {"ID", "First Name", "Last Name", "City", "Age"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dataTable = new JTable(tableModel);

        // --- Table Styling ---
        dataTable.setBackground(BG_DARK.brighter());
        dataTable.setForeground(FG_LIGHT);
        dataTable.setFont(new Font("Arial", Font.PLAIN, 12));
        dataTable.setSelectionBackground(ACCENT_RED.darker());
        dataTable.setSelectionForeground(Color.WHITE);
        dataTable.setRowHeight(25);

        // Header Styling
        JTableHeader header = dataTable.getTableHeader();
        header.setBackground(new Color(60, 60, 60));
        header.setForeground(FG_LIGHT);
        header.setFont(BOLD_FONT);

        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        dataTable.getColumnModel().getColumn(0).setPreferredWidth(30);

        dataTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && dataTable.getSelectedRow() != -1) {
                loadRecordIntoForm(dataTable.getSelectedRow());
            }
        });
    }

    /**
     * Creates the form panel with input fields and action buttons.
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(BG_DARK.brighter());

        // Apply Themed TitledBorder
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_RED, 2),
                "Player Details (Create / Edit)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                BOLD_FONT.deriveFont(Font.ITALIC, 14),
                FG_LIGHT));

        formPanel.setPreferredSize(new Dimension(300, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel inputGrid = new JPanel(new GridBagLayout());
        inputGrid.setOpaque(false);

        // --- Input Fields Layout ---
        int y = 0;

        // First Name
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; JLabel firstLabel = new JLabel("First Name:"); firstLabel.setForeground(FG_LIGHT); inputGrid.add(firstLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; JLabel lastLabel = new JLabel("Last Name:"); lastLabel.setForeground(FG_LIGHT); inputGrid.add(lastLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(lastNameField, gbc);

        // City Address
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; JLabel cityLabel = new JLabel("City/Address:"); cityLabel.setForeground(FG_LIGHT); inputGrid.add(cityLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(cityAddressField, gbc);

        // Age
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; JLabel ageLabel = new JLabel("Age:"); ageLabel.setForeground(FG_LIGHT); inputGrid.add(ageLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(ageField, gbc);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Styled Buttons
        styleButton(addButton, ACCENT_RED.darker());
        styleButton(updateButton, BUTTON_BLUE);
        styleButton(deleteButton, Color.RED);

        JButton clearButton = new JButton(new AbstractAction("Clear Form") {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        styleButton(clearButton, new Color(90, 90, 90)); // Grey for Clear

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // --- Final Assembly of Form Panel ---
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(10, 10, 10, 10);
        gbcForm.fill = GridBagConstraints.HORIZONTAL;

        // Add input grid
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.weightx = 1.0;
        gbcForm.weighty = 0;
        formPanel.add(inputGrid, gbcForm);

        // Add button panel
        gbcForm.gridy = 1;
        gbcForm.weighty = 0;
        formPanel.add(buttonPanel, gbcForm);

        // Filler component to push elements to the bottom
        gbcForm.gridy = 2;
        gbcForm.weighty = 1.0;
        formPanel.add(new JLabel(""), gbcForm);


        return formPanel;
    }

    /**
     * Initializes all input fields.
     */
    private void initializeForm() {
        // Use the themed field creator
        firstNameField = createThemedField(15);
        lastNameField = createThemedField(15);
        cityAddressField = createThemedField(15);
        ageField = createThemedField(5);

        // Buttons
        addButton = new JButton("Add Player");
        updateButton = new JButton("Update Player");
        deleteButton = new JButton("Delete Player");

        // Action Listeners
        addButton.addActionListener(this::handleAdd);
        updateButton.addActionListener(this::handleUpdate);
        deleteButton.addActionListener(this::handleDelete);
    }

    /**
     * Finds the selected Player by ID and loads it into the form.
     */
    private void loadRecordIntoForm(int selectedRow) {
        if (selectedRow >= 0) {
            try {
                int id = (int) tableModel.getValueAt(selectedRow, 0);

                List<Player> currentPlayers = controller.getAllPlayers();

                Player playerToEdit = currentPlayers.stream()
                        .filter(p -> p.getPlayerId() == id)
                        .findFirst()
                        .orElse(null);

                if (playerToEdit != null) {
                    editingPlayerId = playerToEdit.getPlayerId();
                    firstNameField.setText(playerToEdit.getFirstName());
                    lastNameField.setText(playerToEdit.getLastName());
                    cityAddressField.setText(playerToEdit.getCityAddress());
                    ageField.setText(String.valueOf(playerToEdit.getAge()));

                    statusLabel.setText("Editing Player ID: " + editingPlayerId);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "SQL Error loading player: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading player: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clears the input fields and resets the editing state.
     */
    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        cityAddressField.setText("");
        ageField.setText("");

        editingPlayerId = -1;
        dataTable.clearSelection();
        statusLabel.setText("Form cleared. Ready to add new player.");
    }

    /**
     * Retrieves the data from the controller and updates the JTable model.
     */
    private void refreshTable() {
        // Clear all existing rows
        tableModel.setRowCount(0);

        try {
            // Fetch data from the SQL-backed controller method
            List<Player> players = controller.getAllPlayers();

            // Add rows from the fetched data list
            for (Player player : players) {
                Object[] rowData = {
                        player.getPlayerId(),
                        player.getFirstName(),
                        player.getLastName(),
                        player.getCityAddress(),
                        player.getAge()
                };
                tableModel.addRow(rowData);
            }

            // Update the status bar
            statusLabel.setText("Data fetched successfully. Total players: " + players.size());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load players from database: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("ERROR: Failed to load data. See console for SQL errors.");
        }
    }

    // --- Event Handlers (CRUD Logic calling Controller) ---

    private void handleAdd(ActionEvent e) {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String cityAddress = cityAddressField.getText().trim();
        String ageStr = ageField.getText().trim();
        int age;

        if (firstName.isEmpty() || lastName.isEmpty() || ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First Name, Last Name, and Age are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Player newPlayer = new Player(0, firstName, lastName, cityAddress, age); // ID 0 for new record
            int newId = controller.addPlayer(newPlayer);

            statusLabel.setText("Player '" + firstName + " " + lastName + "' added successfully (ID: " + newId + ").");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "SQL Error adding player: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate(ActionEvent e) {
        if (editingPlayerId == -1) {
            JOptionPane.showMessageDialog(this, "No player selected for update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String cityAddress = cityAddressField.getText().trim();
        String ageStr = ageField.getText().trim();
        int age;

        if (firstName.isEmpty() || lastName.isEmpty() || ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First Name, Last Name, and Age are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a valid number.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Player updatedPlayer = new Player(editingPlayerId, firstName, lastName, cityAddress, age);
            boolean success = controller.updatePlayer(updatedPlayer);

            if (success) {
                statusLabel.setText("Player ID " + editingPlayerId + " updated successfully.");
            } else {
                statusLabel.setText("Update failed for Player ID " + editingPlayerId + ".");
            }

            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "SQL Error updating player: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete(ActionEvent e) {
        if (editingPlayerId == -1) {
            JOptionPane.showMessageDialog(this, "No player selected for delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Player ID: " + editingPlayerId + "? This cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = controller.deletePlayer(editingPlayerId);

                if (success) {
                    statusLabel.setText("Player ID " + editingPlayerId + " deleted successfully.");
                } else {
                    statusLabel.setText("Delete failed for Player ID " + editingPlayerId + ".");
                }

                clearForm();
                refreshTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "SQL Error deleting player: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}