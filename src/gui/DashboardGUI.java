package gui;
import controller.*;
import connection.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

/**
 * The central navigation hub (Dashboard) displayed after a successful login.
 * Allows the user to select which management system (Cards, Players, etc.) to access.
 */
public class DashboardGUI extends JFrame {

    private final MTGDatabaseController controller;
    private final String loggedInUsername;

    // --- Colors & Fonts ---
    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color ACCENT_RED = new Color(255, 60, 0);
    private static final Color FG_LIGHT = new Color(240, 240, 240);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Color BUTTON_COLOR = new Color(50, 50, 50); // Darker gray button base
    private static final String TASKBAR_ICON_FILE = "/images/taskbar_icon.png";


    public DashboardGUI(MTGDatabaseController controller, String username) {
        this.controller = controller;
        this.loggedInUsername = username;

        // --- Frame Setup ---
        setTitle("MTG Commander DB - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(20, 20));

        // 1. Header
        JLabel header = new JLabel("MTG DATABASE MANAGEMENT HUB", SwingConstants.CENTER);
        header.setFont(HEADER_FONT);
        header.setForeground(ACCENT_RED);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        add(header, BorderLayout.NORTH);

        // 2. Navigation Panel (Grid of buttons)
        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.CENTER);

        // 3. Footer (Logout)
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Creates the navigation panel with appropriate buttons based on user role
     */
    private JPanel createNavigationPanel() {
        boolean isAdmin = "admin".equals(loggedInUsername);
        
        if (isAdmin) {
            // 3x2 grid for admin (includes Reports button)
            JPanel navPanel = new JPanel(new GridLayout(3, 2, 30, 30));
            navPanel.setBackground(BG_DARK);
            navPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 50, 50));

            // Create and add buttons
            navPanel.add(createNavButton("Card Inventory", e -> launchGUI(new CardGUI(controller, loggedInUsername))));
            navPanel.add(createNavButton("Player Records", e -> launchGUI(new PlayerGUI(controller, loggedInUsername))));
            navPanel.add(createNavButton("Deck Management", e -> launchGUI(new DeckGUI(controller, loggedInUsername))));
            navPanel.add(createNavButton("Borrow Requests", e -> launchGUI(new BorrowReqGUI(controller, loggedInUsername))));
            navPanel.add(createNavButton("Report Generator", e -> launchGUI(new ReportGUI(controller, loggedInUsername))));
            
            // Empty cell for the last position to maintain grid alignment
            navPanel.add(new JLabel()); 

            return navPanel;
        } else {
            // 2x2 grid for regular users
            JPanel navPanel = new JPanel(new GridLayout(2, 2, 30, 30));
            navPanel.setBackground(BG_DARK);
            navPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 50, 50));

            // Create and add buttons
            navPanel.add(createNavButton("Card Inventory", e -> launchGUI(new CardGUI(controller, loggedInUsername))));
            navPanel.add(createNavButton("Player Records", e -> launchGUI(new PlayerGUI(controller, loggedInUsername))));
            navPanel.add(createNavButton("Deck Management", e -> launchGUI(new DeckGUI(controller, loggedInUsername))));
            navPanel.add(createNavButton("Borrow Requests", e -> launchGUI(new BorrowReqGUI(controller, loggedInUsername))));

            return navPanel;
        }
    }

    /**
     * Creates the footer panel with user info and logout button
     */
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(BG_DARK);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // User info on the left
        JLabel userLabel = new JLabel("Logged in as: " + loggedInUsername);
        userLabel.setForeground(FG_LIGHT);
        userLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerPanel.add(userLabel, BorderLayout.WEST);

        // Logout button on the right
        JButton logoutButton = createStyledButton("Logout", new Color(150, 0, 0));
        logoutButton.addActionListener(e -> handleLogout());

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(BG_DARK);
        logoutPanel.add(logoutButton);
        footerPanel.add(logoutPanel, BorderLayout.EAST);

        return footerPanel;
    }

    /**
     * Helper to create the large navigation buttons.
     */
    private JButton createNavButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT.deriveFont(Font.BOLD, 20f));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(FG_LIGHT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(ACCENT_RED, 2));
        button.setPreferredSize(new Dimension(200, 100));

        button.addActionListener(listener);

        // Add a simple hover effect for flair
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        return button;
    }

    /**
     * Helper to create general utility buttons (like Logout).
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT.deriveFont(14f));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    /**
     * Launches a specific GUI frame and keeps the dashboard open.
     */
    private void launchGUI(JFrame guiFrame) {
        // The individual GUI handles its own visibility
        // This method ensures the new window appears centered and on top
        guiFrame.setVisible(true);
    }

    /**
     * Handles the logout process.
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out and exit?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Close the shared database connection
            DatabaseConnection.closeConnection();
            // Exit the application
            System.exit(0);
        }
    }

}