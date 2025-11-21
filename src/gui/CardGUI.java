package gui;
import controller.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader; // FIX 1: Import JTableHeader
import javax.swing.border.TitledBorder; // FIX 2: Import TitledBorder
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.net.URL;

/**
 * A Java Swing GUI for managing Card records (CRUD).
 * STYLED with a Dark MTG theme.
 * RENAMED from MTGDatabaseGUI to CardGUI for clarity.
 */
public class CardGUI extends JFrame {

    // --- Colors & Fonts ---
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color FG_LIGHT = new Color(240, 240, 240);
    private static final Color ACCENT_RED = new Color(255, 60, 0);
    private static final Color INPUT_BG = new Color(45, 45, 45);
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final String TASKBAR_ICON_FILE = "/images/taskbar_icon.png";

    // --- UI Components ---
    private JTable dataTable;
    private DefaultTableModel tableModel;

    // Input Fields
    private JTextField nameField;
    private JTextField manaCostField;
    private JTextField typeField;
    private JTextField subtypeField;
    private JTextField powerField;
    private JTextField toughnessField;
    private JTextArea textField; // For card_text
    private JTextField editionField;
    private JComboBox<String> statusComboBox;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JLabel statusLabel;

    private int editingRecordId = -1;
    private MTGDatabaseController controller;
    private static final String[] CARD_STATUSES = {"Legal", "Banned", "Game Changer"};


    // NOTE: Class name changed to CardGUI
    public CardGUI(MTGDatabaseController controller) {
        this.controller = controller;

        // 1. Frame Setup
        setTitle("MTG Commander Database System (Card Management)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close this window, not the whole application
        setSize(1200, 750);
        setLayout(new BorderLayout(15, 15));

        // Set entire frame background color
        getContentPane().setBackground(BG_DARK);

        // 1.5 Add Menu Bar for Navigation
        setJMenuBar(createMenuBar());

        // 2. Initialize Components
        initializeForm();
        initializeTable();

        // 3. Status Bar (Themed)
        statusLabel = new JLabel("Application Ready.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        statusLabel.setBackground(ACCENT_RED);
        statusLabel.setForeground(Color.BLACK); // Black text on red bar
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
        JLabel mainTitle = new JLabel("CARD INVENTORY MANAGER", SwingConstants.CENTER);
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

        JMenuItem playerItem = new JMenuItem("Player Management");
        playerItem.addActionListener(e -> launchGUI(new PlayerGUI(controller)));
        playerItem.setBackground(INPUT_BG);
        playerItem.setForeground(FG_LIGHT);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        exitItem.setBackground(INPUT_BG);
        exitItem.setForeground(ACCENT_RED); // Highlight exit

        navMenu.add(dashboardItem);
        navMenu.add(playerItem);
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
     * Creates a themed JTextField or JPasswordField.
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
     * Creates and configures the JTable component with all Card columns.
     */
    private void initializeTable() {
        String[] columnNames = {"ID", "Name", "Cost", "Type", "Subtype", "P/T", "Edition", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dataTable = new JTable(tableModel);

        // --- Table Styling ---
        dataTable.setBackground(BG_DARK.brighter()); // Slightly lighter dark background
        dataTable.setForeground(FG_LIGHT);
        dataTable.setFont(new Font("Arial", Font.PLAIN, 12));
        dataTable.setSelectionBackground(ACCENT_RED.darker());
        dataTable.setSelectionForeground(Color.WHITE);
        dataTable.setRowHeight(25);

        // Header Styling
        JTableHeader header = dataTable.getTableHeader();
        header.setBackground(new Color(60, 60, 60)); // Dark header
        header.setForeground(FG_LIGHT);
        header.setFont(BOLD_FONT);

        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configure column widths for readability
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        dataTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        dataTable.getColumnModel().getColumn(5).setPreferredWidth(50);

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
        formPanel.setBackground(BG_DARK.brighter()); // Use a prominent dark color for the form
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_RED, 2),
                "Card Details (Create / Edit)",
                TitledBorder.LEFT, // FIX 3
                TitledBorder.TOP, // FIX 4
                BOLD_FONT.deriveFont(Font.ITALIC, 14),
                FG_LIGHT));
        formPanel.setPreferredSize(new Dimension(350, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel inputGrid = new JPanel(new GridBagLayout());
        inputGrid.setOpaque(false); // Make transparent to show formPanel background

        // --- Input Fields Layout ---
        int y = 0;

        // Helper to create a dark label
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setForeground(FG_LIGHT);

        // Name
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; inputGrid.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(nameField, gbc);

        // Mana Cost
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; JLabel costLabel = new JLabel("Mana Cost:"); costLabel.setForeground(FG_LIGHT); inputGrid.add(costLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(manaCostField, gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; JLabel typeLabel = new JLabel("Type:"); typeLabel.setForeground(FG_LIGHT); inputGrid.add(typeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(typeField, gbc);

        // Subtype
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; JLabel subtypeLabel = new JLabel("Subtype:"); subtypeLabel.setForeground(FG_LIGHT); inputGrid.add(subtypeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(subtypeField, gbc);

        // P/T (Grouped)
        JPanel ptPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        ptPanel.setOpaque(false);
        JLabel ptTitle = new JLabel("Power/Toughness:"); ptTitle.setForeground(FG_LIGHT);

        JLabel pLabel = new JLabel("P:"); pLabel.setForeground(FG_LIGHT); ptPanel.add(pLabel);
        ptPanel.add(powerField);
        JLabel tLabel = new JLabel("T:"); tLabel.setForeground(FG_LIGHT); ptPanel.add(tLabel);
        ptPanel.add(toughnessField);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.weightx = 1.0; inputGrid.add(ptTitle, gbc);
        gbc.gridx = 0; gbc.gridy = y + 1; inputGrid.add(ptPanel, gbc);
        y += 2; gbc.gridwidth = 1; // Reset gridwidth

        // Edition
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; JLabel editionLabel = new JLabel("Edition:"); editionLabel.setForeground(FG_LIGHT); inputGrid.add(editionLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(editionField, gbc);

        // Status (Dropdown)
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0; JLabel statusLabel = new JLabel("Status:"); statusLabel.setForeground(FG_LIGHT); inputGrid.add(statusLabel, gbc);
        gbc.gridx = 1; gbc.gridy = y++; gbc.weightx = 1.0; inputGrid.add(statusComboBox, gbc);
        statusComboBox.setBackground(INPUT_BG);
        statusComboBox.setForeground(FG_LIGHT);
        statusComboBox.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));


        // Card Text (TextArea)
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.weightx = 1.0;
        JLabel textLabel = new JLabel("Card Text:"); textLabel.setForeground(FG_LIGHT); inputGrid.add(textLabel, gbc);
        gbc.gridx = 0; gbc.gridy = y + 1; gbc.weighty = 0.5;
        JScrollPane textScrollPane = new JScrollPane(textField);
        textScrollPane.setPreferredSize(new Dimension(300, 100));
        textScrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));

        textField.setBackground(INPUT_BG);
        textField.setForeground(FG_LIGHT);

        inputGrid.add(textScrollPane, gbc);
        y += 2; gbc.weighty = 0; gbc.gridwidth = 1; // Reset


        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Styled Buttons
        styleButton(addButton, ACCENT_RED.darker());
        styleButton(updateButton, new Color(50, 150, 255)); // Blue for Update
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

        // Filler component to push elements to the top
        gbcForm.gridy = 2;
        gbcForm.weighty = 1.0;
        formPanel.add(new JLabel(""), gbcForm);


        return formPanel;
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
     * Initializes all input fields, including new ones for the Card entity.
     */
    private void initializeForm() {
        nameField = createThemedField(15);
        manaCostField = createThemedField(10);
        typeField = createThemedField(15);
        subtypeField = createThemedField(15);
        powerField = createThemedField(5);
        toughnessField = createThemedField(5);
        textField = new JTextArea(5, 20);
        textField.setLineWrap(true);
        editionField = createThemedField(10);
        statusComboBox = new JComboBox<>(CARD_STATUSES);


        // Buttons
        addButton = new JButton("Add Card");
        updateButton = new JButton("Update Card");
        deleteButton = new JButton("Delete Card");

        // Action Listeners
        addButton.addActionListener(this::handleAdd);
        updateButton.addActionListener(this::handleUpdate);
        deleteButton.addActionListener(this::handleDelete);
    }

    /**
     * Finds the selected record in the current table data by ID and loads it into the form.
     */
    private void loadRecordIntoForm(int selectedRow) {
        if (selectedRow >= 0) {
            try {
                int id = (int) tableModel.getValueAt(selectedRow, 0);

                List<entity.Record> currentRecords = controller.getAllCards();

                entity.Record recordToEdit = currentRecords.stream()
                        .filter(r -> r.getId() == id)
                        .findFirst()
                        .orElse(null);

                if (recordToEdit != null) {
                    editingRecordId = recordToEdit.getId();
                    nameField.setText(recordToEdit.getName());
                    manaCostField.setText(recordToEdit.getManaCost());
                    typeField.setText(recordToEdit.getType());
                    subtypeField.setText(recordToEdit.getSubtype());
                    powerField.setText(recordToEdit.getPower());
                    toughnessField.setText(recordToEdit.getToughness());
                    textField.setText(recordToEdit.getText());
                    editionField.setText(recordToEdit.getEdition());
                    statusComboBox.setSelectedItem(recordToEdit.getStatus());

                    statusLabel.setText("Editing Card ID: " + editingRecordId);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "SQL Error loading card: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading card: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clears the input fields and resets the editing state.
     */
    private void clearForm() {
        nameField.setText("");
        manaCostField.setText("");
        typeField.setText("");
        subtypeField.setText("");
        powerField.setText("");
        toughnessField.setText("");
        textField.setText("");
        editionField.setText("");
        statusComboBox.setSelectedIndex(0);

        editingRecordId = -1;
        dataTable.clearSelection();
        statusLabel.setText("Form cleared. Ready to add new card.");
    }

    /**
     * Retrieves the data from the controller and updates the JTable model.
     */
    private void refreshTable() {
        tableModel.setRowCount(0);

        try {
            List<entity.Record> records = controller.getAllCards();

            for (entity.Record record : records) {
                Object[] rowData = {
                        record.getId(),
                        record.getName(),
                        record.getManaCost(),
                        record.getType(),
                        record.getSubtype(),
                        record.getPower() + "/" + record.getToughness(),
                        record.getEdition(),
                        record.getStatus()
                };
                tableModel.addRow(rowData);
            }

            statusLabel.setText("Data fetched successfully. Total cards: " + records.size());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load cards from database: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("ERROR: Failed to load data. See console for SQL errors.");
        }
    }

    // --- Event Handlers (CRUD Logic calling Controller) ---

    private void handleAdd(ActionEvent e) {
        String name = nameField.getText().trim();
        String manaCost = manaCostField.getText().trim();
        String type = typeField.getText().trim();
        String subtype = subtypeField.getText().trim();
        String power = powerField.getText().trim();
        String toughness = toughnessField.getText().trim();
        String text = textField.getText().trim();
        String edition = editionField.getText().trim();
        String status = (String) statusComboBox.getSelectedItem();


        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            controller.addCard(name, manaCost, type, subtype, power, toughness, text, edition, status);

            statusLabel.setText("Card '" + name + "' added successfully.");
            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "SQL Error adding card: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate(ActionEvent e) {
        if (editingRecordId == -1) {
            JOptionPane.showMessageDialog(this, "No card selected for update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String manaCost = manaCostField.getText().trim();
        String type = typeField.getText().trim();
        String subtype = subtypeField.getText().trim();
        String power = powerField.getText().trim();
        String toughness = toughnessField.getText().trim();
        String text = textField.getText().trim();
        String edition = editionField.getText().trim();
        String status = (String) statusComboBox.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            controller.updateCard(editingRecordId, name, manaCost, type, subtype, power, toughness, text, edition, status);

            statusLabel.setText("Card ID " + editingRecordId + " updated successfully.");

            clearForm();
            refreshTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "SQL Error updating card: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete(ActionEvent e) {
        int selectedRow = dataTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a card from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idToDelete = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Card ID: " + idToDelete + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteCard(idToDelete);

                statusLabel.setText("Card ID " + idToDelete + " deleted successfully.");

                clearForm();
                refreshTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "SQL Error deleting card: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}