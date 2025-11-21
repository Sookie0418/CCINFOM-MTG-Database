package gui;
import controller.MTGDatabaseController;
import transactions.RegisterUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;

/**
 * Swing frame responsible for handling user authentication (Login).
 * Features a custom, borderless dark theme with background image and custom window controls.
 */
public class LoginGUI extends JFrame {

    private final MTGDatabaseController controller;
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    // Image files
    private static final String BACKGROUND_FILE = "/images/bg.png";
    private static final String TASKBAR_ICON_FILE = "/images/taskbar_icon.png";
    private static final String LOGO_FILE = "/images/logo.png";

    // Colors
    private static final Color BG_DARK = new Color(10, 10, 15);
    private static final Color ACCENT_RED = new Color(255, 60, 0);
    private static final Color ACCENT_BLUE = new Color(0, 120, 215);
    private static final Color INPUT_BG = new Color(35, 35, 35);
    private static final Color INPUT_BORDER_INACTIVE = new Color(80, 80, 80);
    private static final Color INPUT_BORDER_ACTIVE = ACCENT_RED;
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 180);

    // Logo size
    private static final int LOGO_WIDTH = 380;
    private static final int LOGO_HEIGHT = 110;

    private int pX, pY;


    public LoginGUI(MTGDatabaseController controller) {
        this.controller = controller;
        setTitle("MTG Database Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLocationRelativeTo(null);

        try {
            URL iconUrl = getClass().getResource(TASKBAR_ICON_FILE);
            if (iconUrl != null) {
                Image iconImage = new ImageIcon(iconUrl).getImage();
                this.setIconImage(iconImage); // Set the taskbar and window icon
            }
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }

        JPanel mainPanel = new StaticBackgroundPanel();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(createCustomTitleBar(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false); // See through to background

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBackground(new Color(0, 0, 0, 150));
        formPanel.setBorder(BorderFactory.createEmptyBorder(80, 100, 60, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = createThemedTextField(30, "Username");
        passwordField = createThemedPasswordField(30, "Password");
        JButton loginButton = new JButton("LOG IN");
        JButton registerButton = new JButton("CREATE ACCOUNT");

        JLabel logoLabel = createLogoLabel();

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(logoLabel, gbc);

        JLabel subtitleLabel = new JLabel("SECURE ACCESS REQUIRED", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(150, 150, 150));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 40, 0);
        formPanel.add(subtitleLabel, gbc);

        gbc.insets = new Insets(15, 0, 15, 0);

        // Username
        gbc.gridy = 2;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        // Login Button
        loginButton.setFont(new Font("Arial", Font.BOLD, 24));
        loginButton.setBackground(ACCENT_RED);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        loginButton.addActionListener(new AbstractAction("LOG IN") {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginButton.setBackground(ACCENT_RED.darker());
                Timer timer = new Timer(100, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent timerEvent) {
                        handleLogin(e);
                        loginButton.setBackground(ACCENT_RED);
                        ((Timer)timerEvent.getSource()).stop();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });

        registerButton.setFont(new Font("Arial", Font.BOLD, 18));
        registerButton.setBackground(ACCENT_BLUE);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        registerButton.addActionListener(new AbstractAction("CREATE ACCOUNT") {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerButton.setBackground(ACCENT_BLUE.darker());
                Timer timer = new Timer(100, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent timerEvent) {
                        handleRegister();
                        registerButton.setBackground(ACCENT_BLUE);
                        ((Timer)timerEvent.getSource()).stop();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });

        gbc.gridy = 5; gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(registerButton, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(40, 0, 5, 0);
        formPanel.add(loginButton, gbc);

        centerPanel.add(formPanel, new GridBagConstraints());
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        getRootPane().setDefaultButton(loginButton);
        setContentPane(mainPanel);
        setVisible(true);
    }

    /**
     * Creates and loads logo image label and resizes to fit.
     */
    private JLabel createLogoLabel() {
        JLabel label = new JLabel();
        try {
            URL logoUrl = getClass().getResource(LOGO_FILE);
            if (logoUrl != null) {
                ImageIcon originalIcon = new ImageIcon(logoUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(LOGO_WIDTH, LOGO_HEIGHT, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setText("MTG COMMANDER DB");
                label.setFont(new Font("Arial", Font.BOLD, 36));
                label.setForeground(ACCENT_RED);
                label.setHorizontalAlignment(SwingConstants.CENTER);
            }
        } catch (Exception e) {
            label.setText("MTG COMMANDER DB");
            label.setFont(new Font("Arial", Font.BOLD, 36));
            label.setForeground(ACCENT_RED);
            System.err.println("Error loading logo image: " + e.getMessage());
        }
        return label;
    }

    /**
     * Creates custom title bar with drag capability and window controls.
     */
    private JPanel createCustomTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(20, 20, 25));
        titleBar.setPreferredSize(new Dimension(this.getWidth(), 30));

        JLabel titleLabel = new JLabel("  MTG Database Login");
        titleLabel.setForeground(new Color(180, 180, 180));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);

        JButton minimizeButton = createControlButton("_", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setState(JFrame.ICONIFIED);
            }
        });

        JButton closeButton = createControlButton("X", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        controls.add(minimizeButton);
        controls.add(closeButton);
        titleBar.add(controls, BorderLayout.EAST);

        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                pX = me.getX();
                pY = me.getY();
            }
        });
        titleBar.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent me) {
                setLocation(getLocation().x + me.getX() - pX, getLocation().y + me.getY() - pY);
            }
        });

        return titleBar;
    }

    /**
     * Helper to create themed control buttons.
     */
    private JButton createControlButton(String text, Action action) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(new Color(20, 20, 25));
        button.setForeground(new Color(180, 180, 180));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(40, 30));

        button.addActionListener(action);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (text.equals("X")) {
                    button.setBackground(ACCENT_RED.darker());
                } else {
                    button.setBackground(new Color(40, 40, 50));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(20, 20, 25));
            }
        });

        return button;
    }


    /**
     * Custom JPanel for drawing the static background image and overlay.
     */
    private class StaticBackgroundPanel extends JPanel {
        private Image staticBackgroundImage;

        public StaticBackgroundPanel() {
            try {
                URL bgUrl = getClass().getResource(BACKGROUND_FILE);
                if (bgUrl != null) {
                    staticBackgroundImage = new ImageIcon(bgUrl).getImage();
                }
            } catch (Exception e) {
                System.err.println("Error loading static background image: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int w = getWidth();
            int h = getHeight();

            if (staticBackgroundImage != null) {
                g.drawImage(staticBackgroundImage, 0, 0, w, h, this);
            } else {
                g.setColor(getBackground());
                g.fillRect(0, 0, w, h);
            }

            g.setColor(OVERLAY_COLOR);
            g.fillRect(0, 0, w, h);
        }
    }


    /**
     * Creates a themed JTextField for dark backgrounds, with themed focus.
     */
    private JTextField createThemedTextField(int columns, String placeholderText) {
        JTextField field = new JTextField(columns);
        field.setBackground(INPUT_BG);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Arial", Font.PLAIN, 18));

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER_INACTIVE, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 55));
        field.setText(placeholderText);
        field.setHorizontalAlignment(SwingConstants.LEFT);

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER_ACTIVE, 2),
                        BorderFactory.createEmptyBorder(9, 14, 9, 14)
                ));
                if (field.getText().equals(placeholderText)) {
                    field.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER_INACTIVE, 1),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                if (field.getText().isEmpty()) {
                    field.setText(placeholderText);
                }
            }
        });

        return field;
    }

    private JPasswordField createThemedPasswordField(int columns, String placeholderText) {
        JPasswordField field = new JPasswordField(columns);
        field.setBackground(INPUT_BG);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setEchoChar((char) 0);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER_INACTIVE, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 55));
        field.setText(placeholderText);
        field.setHorizontalAlignment(SwingConstants.LEFT);

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER_ACTIVE, 2),
                        BorderFactory.createEmptyBorder(9, 14, 9, 14)
                ));
                if (String.valueOf(field.getPassword()).equals(placeholderText)) {
                    field.setText("");
                    field.setEchoChar('•'); // Hide password when typing
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(INPUT_BORDER_INACTIVE, 1),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                if (field.getPassword().length == 0) {
                    field.setText(placeholderText);
                    field.setEchoChar((char) 0); // Show placeholder text
                }
            }
        });

        return field;
    }


    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        try {
            if (controller.validateUser(username, password)) {
                JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                this.dispose();

                SwingUtilities.invokeLater(() -> new DashboardGUI(controller));
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred during login: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        // Create registration dialog
        JDialog registerDialog = new JDialog(this, "Create New Account", true);
        registerDialog.setUndecorated(true);
        registerDialog.setLayout(new BorderLayout());
        registerDialog.setBackground(BG_DARK);
        registerDialog.setSize(500, 600);
        registerDialog.setLocationRelativeTo(this);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2));

        // Title bar
        JPanel titleBar = createRegisterTitleBar(registerDialog);
        mainPanel.add(titleBar, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_DARK);
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Title
        JLabel titleLabel = new JLabel("CREATE ACCOUNT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ACCENT_BLUE);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        formPanel.add(titleLabel, gbc);

        gbc.insets = new Insets(10, 0, 10, 0);

        // Username field
        JTextField regUsernameField = createThemedTextField(20, "Username");
        gbc.gridy = 1;
        formPanel.add(regUsernameField, gbc);

        // Password field
        JPasswordField regPasswordField = createThemedPasswordField(20, "Password");
        gbc.gridy = 2;
        formPanel.add(regPasswordField, gbc);

        // Confirm Password field
        JPasswordField regConfirmPasswordField = createThemedPasswordField(20, "Confirm Password");
        gbc.gridy = 3;
        formPanel.add(regConfirmPasswordField, gbc);

        // Register button
        JButton confirmRegisterButton = new JButton("CREATE ACCOUNT");
        confirmRegisterButton.setFont(new Font("Arial", Font.BOLD, 18));
        confirmRegisterButton.setBackground(ACCENT_BLUE);
        confirmRegisterButton.setForeground(Color.WHITE);
        confirmRegisterButton.setFocusPainted(false);
        confirmRegisterButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        gbc.gridy = 4;
        gbc.insets = new Insets(30, 0, 10, 0);
        formPanel.add(confirmRegisterButton, gbc);

        // Cancel button
        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setBackground(INPUT_BG);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 0, 0);
        formPanel.add(cancelButton, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        registerDialog.add(mainPanel);

        // Add action listeners
        confirmRegisterButton.addActionListener(e -> {
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword());
            String confirmPassword = new String(regConfirmPasswordField.getPassword());

            // Validate inputs
            if (username.equals("Username") || username.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "Please enter a username.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.equals("Password") || password.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "Please enter a password.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (confirmPassword.equals("Confirm Password") || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "Please confirm your password.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(registerDialog, "Passwords do not match.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 8) {
                JOptionPane.showMessageDialog(registerDialog, "Password must be at least 8 characters long.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (username.contains(" ")) {
                JOptionPane.showMessageDialog(registerDialog, "Username cannot contain spaces.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Attempt registration
            try {
                // Use RegisterUser class for registration
                boolean success = RegisterUser.promptAndRegister(null, new java.util.Scanner(username + "\n" + password + "\n" + confirmPassword + "\n"));

                if (success) {
                    JOptionPane.showMessageDialog(registerDialog, "Account created successfully!\nYou can now login with your credentials.", "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                    registerDialog.dispose();

                    // Clear fields for next use
                    usernameField.setText("");
                    passwordField.setText("");
                    passwordField.setEchoChar((char) 0);
                    passwordField.setText("Password");
                } else {
                    JOptionPane.showMessageDialog(registerDialog, "Registration failed. Username may already exist.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(registerDialog, "An error occurred during registration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> registerDialog.dispose());

        registerDialog.setVisible(true);
    }

    /**
     * Creates title bar for registration dialog
     */
    private JPanel createRegisterTitleBar(JDialog dialog) {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(20, 20, 25));
        titleBar.setPreferredSize(new Dimension(500, 30));

        JLabel titleLabel = new JLabel("  Create New Account");
        titleLabel.setForeground(new Color(180, 180, 180));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);

        JButton closeButton = createControlButton("X", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        controls.add(closeButton);
        titleBar.add(controls, BorderLayout.EAST);

        // Add drag functionality
        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                pX = me.getX();
                pY = me.getY();
            }
        });
        titleBar.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent me) {
                dialog.setLocation(dialog.getLocation().x + me.getX() - pX, dialog.getLocation().y + me.getY() - pY);
            }
        });

        return titleBar;
    }
}