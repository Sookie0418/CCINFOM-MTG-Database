import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class DashboardGUI extends JFrame {

    private final MTGDatabaseController controller;

    private static final Color BG_DARK = new Color(30, 30, 30);
    private static final Color ACCENT_RED = new Color(255, 60, 0);
    private static final Color FG_LIGHT = new Color(240, 240, 240);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 28);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Color BUTTON_COLOR = new Color(50, 50, 50); // Darker gray button base
    private static final String TASKBAR_ICON_FILE = "taskbar_icon.png";


    public DashboardGUI(MTGDatabaseController controller) {
        this.controller = controller;

        setTitle("MTG Commander DB - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(20, 20));

        JLabel header = new JLabel("MTG DATABASE MANAGEMENT HUB", SwingConstants.CENTER);
        header.setFont(HEADER_FONT);
        header.setForeground(ACCENT_RED);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        add(header, BorderLayout.NORTH);

        JPanel navPanel = new JPanel(new GridLayout(2, 2, 30, 30)); // 2x2 grid with spacing
        navPanel.setBackground(BG_DARK);
        navPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 50, 50));

        try {
            File iconFile = new File(TASKBAR_ICON_FILE);
            if (iconFile.exists()) {
                Image iconImage = new ImageIcon(iconFile.getAbsolutePath()).getImage();
                this.setIconImage(iconImage); // Set the taskbar and window icon
            }
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }

        navPanel.add(createNavButton("Card Inventory", e -> launchGUI(new CardGUI(controller))));
        navPanel.add(createNavButton("Player Records", e -> launchGUI(new PlayerGUI(controller))));
        navPanel.add(createNavButton("Deck Management", e -> {
            JOptionPane.showMessageDialog(this, "Deck Management coming soon!", "Placeholder", JOptionPane.INFORMATION_MESSAGE);
        }));
        navPanel.add(createNavButton("Borrow Requests", e -> {
            JOptionPane.showMessageDialog(this, "Borrow Requests coming soon!", "Placeholder", JOptionPane.INFORMATION_MESSAGE);
        }));

        add(navPanel, BorderLayout.CENTER);

        JButton logoutButton = createStyledButton("Logout", new Color(150, 0, 0));
        logoutButton.addActionListener(e -> handleLogout());

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(BG_DARK);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        footerPanel.add(logoutButton);
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
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

            DatabaseConnection.closeConnection();
            System.exit(0);
        }
    }
}