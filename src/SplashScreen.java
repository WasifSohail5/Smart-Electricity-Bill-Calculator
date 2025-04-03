import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SplashScreen extends JWindow {
    private final int duration;

    public SplashScreen(int duration) {
        this.duration = duration;
        initUI();
    }

    private void initUI() {
        JPanel content = createContent();
        getContentPane().add(content);
        pack();
        setLocationRelativeTo(null);
    }

    public void showSplashAndExit(Runnable onCompletion) {
        setVisible(true);

        // Close splash after duration and launch main application
        Timer timer = new Timer(duration, e -> {
            dispose();
            onCompletion.run(); // Run the completion task (launch main app)
        });
        timer.setRepeats(false);
        timer.start();
    }

    private JPanel createContent() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2));

        // App name
        JLabel appLabel = new JLabel("Smart Electricity Bill Calculator");
        appLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        appLabel.setForeground(Color.WHITE);
        appLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Logo
        JLabel logoLabel = createLogoLabel();

        // Create a single panel for all bottom components
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(new Color(41, 128, 185));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Loading text
        JLabel loadingLabel = new JLabel("Loading application...");
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingLabel.setForeground(Color.WHITE);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setForeground(Color.WHITE);
        progressBar.setBackground(new Color(52, 152, 219));
        progressBar.setMaximumSize(new Dimension(450, 10));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Version and creator info
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(41, 128, 185));
        infoPanel.setMaximumSize(new Dimension(480, 30));

        JLabel versionLabel = new JLabel("Version 2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(200, 230, 255));

        JLabel authorLabel = new JLabel("by Wasif Sohail");
        authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        authorLabel.setForeground(new Color(200, 230, 255));
        authorLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        infoPanel.add(versionLabel, BorderLayout.WEST);
        infoPanel.add(authorLabel, BorderLayout.EAST);

        // Add all bottom components to the bottom panel IN ORDER
        bottomPanel.add(loadingLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        bottomPanel.add(progressBar);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        bottomPanel.add(infoPanel);

        // Add components to main panel
        panel.add(appLabel, BorderLayout.NORTH);
        panel.add(logoLabel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Set size
        panel.setPreferredSize(new Dimension(500, 300));

        return panel;
    }

    private JLabel createLogoLabel() {
        // Create logo icon
        ImageIcon icon = new ImageIcon(createLogoImage(150, 150));

        JLabel label = new JLabel(icon);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        return label;
    }

    private Image createLogoImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw circular background
        g2d.setColor(Color.WHITE);
        g2d.fillOval(0, 0, width, height);

        // Draw lightning bolt icon
        g2d.setColor(new Color(255, 193, 7));

        int[] xPoints = {width/2, width/4, width/2, 3*width/4};
        int[] yPoints = {0, height/2, height/2, height};
        g2d.fillPolygon(xPoints, yPoints, 4);

        g2d.dispose();
        return image;
    }
}