import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.time.LocalDate;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
public class EnhancedElectricityCalculator extends JFrame {

    // Main panels
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Sidebar buttons
    private List<SidebarButton> sidebarButtons = new ArrayList<>();

    // Dashboard components
    private JPanel dashboardPanel;
    private JPanel usageSummaryPanel;
    private JPanel billHistoryPanel;
    private JPanel quickAddPanel;

    // Calculator components
    private JPanel calculatorPanel;
    private JTextField deviceNameField;
    private JTextField wattsField;
    private JTextField hoursField;
    private JSlider efficiencySlider;
    private JComboBox<String> deviceTypeComboBox;
    private JLabel resultLabel;
    private JPanel resultPanel;

    // Devices panel components
    private JPanel devicesPanel;
    private JTable devicesTable;
    private DefaultTableModel tableModel;

    // Settings panel
    private JPanel settingsPanel;
    private JComboBox<String> themeSelector;
    private JSlider animationSpeedSlider;

    // Common elements
    private JProgressBar progressBar;
    private JLabel statusLabel;

    // Data storage
    private List<ApplianceData> savedAppliances = new ArrayList<>();
    private List<BillHistory> billHistory = new ArrayList<>();

    // Chart component
    private JFreeChart usageChart;
    private ChartPanel chartPanel;

    // Constants for styling
    private final int SIDEBAR_WIDTH = 220;
    private final int ANIMATION_DURATION = 300; // ms
    private Color primaryColor = new Color(41, 128, 185);
    private Color accentColor = new Color(52, 152, 219);
    private Color backgroundColor = new Color(245, 248, 250);
    private Color textColor = new Color(52, 73, 94);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    // Unit price tiers
    private final double[] KWH_TIERS = {50, 100, 200, 300, 700, Double.MAX_VALUE};
    private final double[] UNIT_PRICES = {4.81, 7.87, 10.54, 12.89, 21.88, 24.93};

    public EnhancedElectricityCalculator() {
        setTitle("Smart Electricity Bill Calculator");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Apply look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize data with sample values
        initializeSampleData();

        // Create main layout
        setLayout(new BorderLayout());

        // Create the sidebar and content panels
        sidebarPanel = createSidebarPanel();
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);

        // Create all content panels
        dashboardPanel = createDashboardPanel();
        calculatorPanel = createCalculatorPanel();
        devicesPanel = createDevicesPanel();
        settingsPanel = createSettingsPanel();

        // Add panels to card layout
        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(calculatorPanel, "calculator");
        contentPanel.add(devicesPanel, "devices");
        contentPanel.add(settingsPanel, "settings");

        // Add components to main frame
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Status bar at bottom
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        // Show the dashboard by default
        cardLayout.show(contentPanel, "dashboard");
        highlightSelectedButton(0);

        // Make the window visible
        setVisible(true);
    }

    // Look for the existing createSidebarPanel method and modify it:
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        panel.setBackground(new Color(52, 73, 94));
        panel.setLayout(new BorderLayout());

        // App title/logo panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setBackground(new Color(44, 62, 80));
        JLabel logoLabel = new JLabel("Smart Energy");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setIcon(new ImageIcon(createLogo(32, 32)));
        logoPanel.add(logoLabel);
        panel.add(logoPanel, BorderLayout.NORTH);

        // Navigation buttons
        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(52, 73, 94));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Menu items
        String[][] menuItems = {
                {"Dashboard", "dashboard", "C:\\Users\\wasif\\IdeaProjects\\untitled1\\src\\resources\\icons\\home.png"},
                {"Calculator", "calculator", "C:\\Users\\wasif\\IdeaProjects\\untitled1\\src\\resources\\icons\\calculator.png"},
                {"My Devices", "devices", "C:\\Users\\wasif\\IdeaProjects\\untitled1\\src\\resources\\icons\\devices.png"},
                {"Settings", "settings", "C:\\Users\\wasif\\IdeaProjects\\untitled1\\src\\resources\\icons\\settings.png"}
        };

        // Debug output
        for (String[] item : menuItems) {
            File f = new File(item[2]);
            System.out.println("Icon path: " + item[2] + ", exists: " + f.exists());
        }

        for (int i = 0; i < menuItems.length; i++) {
            final int index = i;
            final String cardName = menuItems[i][1];

            SidebarButton button = new SidebarButton(menuItems[i][0], menuItems[i][2]);
            navPanel.add(button);
            navPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebarButtons.add(button);

            button.addActionListener(e -> {
                animateTransition(cardName);
                highlightSelectedButton(index);
            });
        }

        panel.add(navPanel, BorderLayout.CENTER);

        // User profile panel at bottom of sidebar
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(44, 62, 80));
        userPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 60));
        userPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel userIcon;
        try {
            File userIconFile = new File("C:\\Users\\wasif\\IdeaProjects\\ElectricityCalculator\\src\\resources\\icons\\user.png");
            if (userIconFile.exists()) {
                // Load and resize the user icon
                ImageIcon originalIcon = new ImageIcon(userIconFile.getPath());
                Image scaledImage = originalIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                userIcon = new JLabel(new ImageIcon(scaledImage));
            } else {
                System.out.println("User icon not found, using generated avatar instead");
                // Fall back to the generated avatar with initials
                userIcon = new JLabel(new ImageIcon(createCircularAvatar(32, "WS")));
            }
        } catch (Exception e) {
            System.out.println("Error loading user icon: " + e.getMessage());
            // Fall back to the generated avatar with initials
            userIcon = new JLabel(new ImageIcon(createCircularAvatar(32, "WS")));
        }
        JLabel userName = new JLabel("Wasif Sohail");
        userName.setForeground(Color.WHITE);
        userName.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(userName, BorderLayout.CENTER);

        JButton logoutButton = new JButton("X");
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        userPanel.add(logoutButton, BorderLayout.EAST);
        panel.add(userPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Dashboard header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Date & time panel
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dateTimePanel.setBackground(backgroundColor);

        JLabel dateLabel = new JLabel(LocalDate.now().toString());
        dateLabel.setFont(NORMAL_FONT);
        dateLabel.setForeground(textColor);
        dateTimePanel.add(dateLabel);

        headerPanel.add(dateTimePanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Create main dashboard content
        JPanel mainContent = new JPanel();
        mainContent.setBackground(backgroundColor);
        mainContent.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Create stat cards for summary
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(backgroundColor);

        statsPanel.add(createStatCard("Monthly Usage", "356 kWh", "↑ 5.2%", new Color(41, 128, 185), true));
        statsPanel.add(createStatCard("Estimated Bill", "Rs 4,289", "↑ 7.1%", new Color(46, 204, 113), true));
        statsPanel.add(createStatCard("Peak Device", "AC (160 kWh)", "", new Color(155, 89, 182), false));
        statsPanel.add(createStatCard("Active Devices", "12", "↑ 2", new Color(230, 126, 34), false));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        mainContent.add(statsPanel, gbc);

        // Create usage chart panel
        JPanel chartPanel = createUsageChartPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.weightx = 0.7;
        gbc.weighty = 0.8;
        mainContent.add(chartPanel, gbc);

        // Create device list panel
        JPanel devicesListPanel = createDeviceListPanel();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.3;
        gbc.weighty = 0.5;
        mainContent.add(devicesListPanel, gbc);

        // Create quick tips panel
        JPanel tipsPanel = createTipsPanel();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.3;
        gbc.weighty = 0.3;
        mainContent.add(tipsPanel, gbc);

        panel.add(mainContent, BorderLayout.CENTER);

        // Create bottom action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setBackground(backgroundColor);

        JButton addDeviceButton = createStyledButton("Add New Device", new Color(52, 152, 219));
        addDeviceButton.addActionListener(e -> {
            animateTransition("calculator");
            highlightSelectedButton(1); // Calculator button
        });

        JButton generateReportButton = createStyledButton("Generate Report", new Color(155, 89, 182));
        generateReportButton.addActionListener(e -> showGeneratingReportDialog());

        actionPanel.add(addDeviceButton);
        actionPanel.add(generateReportButton);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String title, String value, String change, Color color, boolean showChange) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Add rounded corners and shadow
        card.setBorder(new CompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(150, 150, 150));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(textColor);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // Create colored icon
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(0, 0, 12, 12);
                g2d.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(12, 12));

        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titleContainer.setBackground(Color.WHITE);
        titleContainer.add(iconPanel);
        titleContainer.add(titleLabel);

        topPanel.add(titleContainer, BorderLayout.WEST);

        if (showChange && !change.isEmpty()) {
            JLabel changeLabel = new JLabel(change);
            changeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            if (change.contains("↑")) {
                changeLabel.setForeground(new Color(231, 76, 60)); // Red for increase
            } else {
                changeLabel.setForeground(new Color(46, 204, 113)); // Green for decrease
            }

            topPanel.add(changeLabel, BorderLayout.EAST);
        }

        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createUsageChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Monthly Energy Consumption");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(textColor);

        // Create chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add sample data
        dataset.addValue(250, "Usage", "Jan");
        dataset.addValue(290, "Usage", "Feb");
        dataset.addValue(310, "Usage", "Mar");
        dataset.addValue(290, "Usage", "Apr");
        dataset.addValue(320, "Usage", "May");
        dataset.addValue(356, "Usage", "Jun");

        dataset.addValue(200, "Last Year", "Jan");
        dataset.addValue(230, "Last Year", "Feb");
        dataset.addValue(280, "Last Year", "Mar");
        dataset.addValue(260, "Last Year", "Apr");
        dataset.addValue(300, "Last Year", "May");
        dataset.addValue(320, "Last Year", "Jun");

        usageChart = ChartFactory.createLineChart(
                null,              // Chart title
                "Month",           // X-axis label
                "kWh",             // Y-axis label
                dataset,           // Dataset
                PlotOrientation.VERTICAL,
                true,              // Include legend
                true,              // Tooltips
                false              // URLs
        );

        // Customize chart appearance
        CategoryPlot plot = usageChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(240, 240, 240));
        plot.setDomainGridlinePaint(new Color(240, 240, 240));

        chartPanel = new ChartPanel(usageChart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setBackground(Color.WHITE);

        // Chart controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(Color.WHITE);

        String[] periods = {"Last 6 Months", "Last Year", "Last 2 Years"};
        JComboBox<String> periodSelector = new JComboBox<>(periods);

        JToggleButton compareButton = new JToggleButton("Compare with Previous Year");
        compareButton.setSelected(true);

        controlPanel.add(compareButton);
        controlPanel.add(periodSelector);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(controlPanel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDeviceListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Top Energy Consumers");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(textColor);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Device list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        // Sample devices
        String[][] devices = {
                {"Air Conditioner", "160 kWh", "1920 Rs"},
                {"Refrigerator", "75 kWh", "591 Rs"},
                {"Water Heater", "45 kWh", "355 Rs"},
                {"Washing Machine", "35 kWh", "276 Rs"},
                {"Television", "25 kWh", "197 Rs"}
        };

        for (String[] device : devices) {
            JPanel deviceRow = createDeviceRow(device[0], device[1], device[2]);
            listPanel.add(deviceRow);
            listPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);

        JButton viewAllButton = new JButton("View All Devices");
        viewAllButton.setFont(NORMAL_FONT);
        viewAllButton.setForeground(primaryColor);
        viewAllButton.setBackground(Color.WHITE);
        viewAllButton.setBorderPainted(false);
        viewAllButton.setContentAreaFilled(false);
        viewAllButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllButton.addActionListener(e -> {
            animateTransition("devices");
            highlightSelectedButton(2); // Devices button
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(viewAllButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDeviceRow(String name, String usage, String cost) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(textColor);

        JLabel usageLabel = new JLabel(usage);
        usageLabel.setFont(NORMAL_FONT);
        usageLabel.setForeground(new Color(100, 100, 100));

        JLabel costLabel = new JLabel(cost);
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        costLabel.setForeground(primaryColor);

        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(usageLabel, BorderLayout.CENTER);
        panel.add(costLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTipsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Energy Saving Tips");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(textColor);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel tipsContainer = new JPanel();
        tipsContainer.setLayout(new BoxLayout(tipsContainer, BoxLayout.Y_AXIS));
        tipsContainer.setBackground(Color.WHITE);

        String[] tips = {
                "Lower AC temperature by 1°C to save up to 6% energy",
                "Clean refrigerator coils regularly for optimal efficiency",
                "Replace incandescent bulbs with LEDs to save up to 80% energy"
        };

        for (String tip : tips) {
            JPanel tipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            tipPanel.setBackground(Color.WHITE);

            JLabel bulletLabel = new JLabel("•");
            bulletLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            bulletLabel.setForeground(accentColor);

            JLabel tipLabel = new JLabel(tip);
            tipLabel.setFont(NORMAL_FONT);

            tipPanel.add(bulletLabel);
            tipPanel.add(tipLabel);

            tipsContainer.add(tipPanel);
            tipsContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(tipsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCalculatorPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("Energy Cost Calculator");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Main content - split into input and result panels
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // Input panel
        JPanel inputPanel = createCalculatorInputPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        contentPanel.add(inputPanel, gbc);

        // Result panel
        resultPanel = createCalculatorResultPanel();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        contentPanel.add(resultPanel, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(NORMAL_FONT);
        resetButton.addActionListener(e -> resetCalculatorFields());

        JButton calculateButton = createStyledButton("Calculate", primaryColor);
        calculateButton.addActionListener(e -> calculateAndDisplayResult());

        JButton saveButton = createStyledButton("Save Device", accentColor);
        saveButton.addActionListener(e -> saveCurrentDevice());

        buttonPanel.add(resetButton);
        buttonPanel.add(calculateButton);
        buttonPanel.add(saveButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCalculatorInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel titleLabel = new JLabel("Device Information");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Device type selection
        JPanel deviceTypePanel = createInputRow("Device Type:", null);

        String[] deviceTypes = {
                "Select Device Type",
                "Air Conditioner",
                "Refrigerator",
                "Ceiling Fan",
                "Light Bulb",
                "Television",
                "Water Heater",
                "Washing Machine",
                "Computer",
                "Microwave Oven",
                "Other"
        };

        deviceTypeComboBox = new JComboBox<>(deviceTypes);
        deviceTypeComboBox.setFont(NORMAL_FONT);
        deviceTypeComboBox.addActionListener(e -> {
            if (deviceTypeComboBox.getSelectedIndex() > 0) {
                // Set default values based on selection
                setDeviceDefaults((String) deviceTypeComboBox.getSelectedItem());
            }
        });

        deviceTypePanel.add(deviceTypeComboBox);
        panel.add(deviceTypePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Device name
        JPanel namePanel = createInputRow("Device Name:", null);
        deviceNameField = new JTextField(20);
        deviceNameField.setFont(NORMAL_FONT);
        namePanel.add(deviceNameField);
        panel.add(namePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Power consumption
        JPanel wattsPanel = createInputRow("Power (Watts):", "Enter device wattage from label");
        wattsField = new JTextField(20);
        wattsField.setFont(NORMAL_FONT);
        wattsPanel.add(wattsField);
        panel.add(wattsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Hours of use
        JPanel hoursPanel = createInputRow("Hours Used Per Day:", "1-24 hours");
        hoursField = new JTextField(20);
        hoursField.setFont(NORMAL_FONT);
        hoursPanel.add(hoursField);
        panel.add(hoursPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Efficiency slider
        JPanel efficiencyPanel = createInputRow("Usage Efficiency:", "Adjusts for cycles/variable use");

        efficiencySlider = new JSlider(JSlider.HORIZONTAL, 10, 100, 100);
        efficiencySlider.setMajorTickSpacing(30);
        efficiencySlider.setMinorTickSpacing(10);
        efficiencySlider.setPaintTicks(true);
        efficiencySlider.setPaintLabels(true);
        efficiencySlider.setBackground(Color.WHITE);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(10, new JLabel("10%"));
        labelTable.put(40, new JLabel("40%"));
        labelTable.put(70, new JLabel("70%"));
        labelTable.put(100, new JLabel("100%"));
        efficiencySlider.setLabelTable(labelTable);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setBackground(Color.WHITE);
        sliderPanel.add(efficiencySlider, BorderLayout.CENTER);

        efficiencyPanel.add(sliderPanel);
        panel.add(efficiencyPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Monthly usage days (preset to 30 but adjustable)
        JPanel daysPanel = createInputRow("Days Per Month:", null);

        SpinnerModel daysModel = new SpinnerNumberModel(30, 1, 31, 1);
        JSpinner daysSpinner = new JSpinner(daysModel);
        daysSpinner.setFont(NORMAL_FONT);
        daysSpinner.setPreferredSize(new Dimension(80, 30));

        daysPanel.add(daysSpinner);
        panel.add(daysPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tariff information panel
        JPanel tariffPanel = new JPanel();
        tariffPanel.setLayout(new BoxLayout(tariffPanel, BoxLayout.Y_AXIS));
        tariffPanel.setBackground(new Color(245, 245, 245));
        tariffPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tariffPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tariffTitle = new JLabel("Current Electricity Tariff");
        tariffTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tariffTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tariffDetails = new JLabel("<html>1-50 kWh: Rs4.81/unit<br>51-100 kWh: Rs7.87/unit<br>101-200 kWh: Rs10.54/unit<br>201+ kWh: Rs12.89+ /unit</html>");
        tariffDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tariffDetails.setAlignmentX(Component.LEFT_ALIGNMENT);

        tariffPanel.add(tariffTitle);
        tariffPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        tariffPanel.add(tariffDetails);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(tariffPanel);

        return panel;
    }

    private JPanel createInputRow(String labelText, String tooltip) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(NORMAL_FONT);
        label.setPreferredSize(new Dimension(150, 30));

        panel.add(label);

        if (tooltip != null && !tooltip.isEmpty()) {
            JLabel tooltipLabel = new JLabel(" (?)");
            tooltipLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            tooltipLabel.setForeground(new Color(100, 100, 100));
            tooltipLabel.setToolTipText(tooltip);
            panel.add(tooltipLabel);
        }

        return panel;
    }

    private JPanel createCalculatorResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Calculation Result");
        titleLabel.setFont(HEADING_FONT);
        titleLabel.setForeground(textColor);

        JPanel resultContent = new JPanel();
        resultContent.setLayout(new BoxLayout(resultContent, BoxLayout.Y_AXIS));
        resultContent.setBackground(Color.WHITE);

        // Initially empty result
        resultLabel = new JLabel("<html><div style='text-align: center;'>" +
                "<p>Enter device information and click Calculate to see the estimated electricity cost.</p>" +
                "<p>You can save frequently used devices for quick access.</p>" +
                "</div></html>");
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add progress bar for calculation animation
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setForeground(accentColor);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel costBreakdownTitle = new JLabel("Cost Breakdown");
        costBreakdownTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        costBreakdownTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultContent.add(Box.createVerticalGlue());
        resultContent.add(resultLabel);
        resultContent.add(Box.createRigidArea(new Dimension(0, 20)));
        resultContent.add(progressBar);
        resultContent.add(Box.createRigidArea(new Dimension(0, 20)));
        resultContent.add(costBreakdownTitle);

        // Initially empty cost breakdown chart (will be populated on calculation)
        JPanel chartPanel = new JPanel();
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(250, 150));
        chartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultContent.add(chartPanel);

        resultContent.add(Box.createVerticalGlue());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(resultContent, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDevicesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(backgroundColor);

        JLabel titleLabel = new JLabel("My Devices");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Search field
        JTextField searchField = new JTextField(20);
        searchField.setFont(NORMAL_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton searchButton = new JButton("Search");
        searchButton.setFont(NORMAL_FONT);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(backgroundColor);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        headerPanel.add(searchPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Devices table
        String[] columnNames = {"Device Name", "Type", "Power (W)", "Hours/Day", "Monthly kWh", "Monthly Cost", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column is editable
            }
        };

        // Add sample data
        addSampleDevicesToTable();

        devicesTable = new JTable(tableModel);
        devicesTable.setFont(NORMAL_FONT);
        devicesTable.setRowHeight(40);
        devicesTable.setShowVerticalLines(false);
        devicesTable.setShowHorizontalLines(true);
        devicesTable.setGridColor(new Color(240, 240, 240));

        // Custom header renderer
        JTableHeader header = devicesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(textColor);

        // Set column widths
        devicesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        devicesTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        devicesTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        devicesTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        devicesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        devicesTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        devicesTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        // Add action buttons column
        devicesTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        devicesTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(devicesTable);
        scrollPane.setBorder(new ShadowBorder());
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with stats and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(backgroundColor);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(backgroundColor);

        JLabel statsLabel = new JLabel("Total Devices: 12   |   Total Monthly Cost: Rs4,289");
        statsLabel.setFont(NORMAL_FONT);
        statsPanel.add(statsLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(backgroundColor);

        JButton exportButton = new JButton("Export List");
        exportButton.setFont(NORMAL_FONT);

        JButton addButton = createStyledButton("Add New Device", primaryColor);
        addButton.addActionListener(e -> {
            animateTransition("calculator");
            highlightSelectedButton(1); // Calculator button
        });

        actionPanel.add(exportButton);
        actionPanel.add(addButton);

        bottomPanel.add(statsPanel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(textColor);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Main settings content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new CompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Appearance section
        JLabel appearanceLabel = new JLabel("Appearance");
        appearanceLabel.setFont(HEADING_FONT);
        appearanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Theme selector
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themePanel.setBackground(Color.WHITE);
        themePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel themeLabel = new JLabel("Theme:");
        themeLabel.setFont(NORMAL_FONT);
        themeLabel.setPreferredSize(new Dimension(150, 30));

        String[] themes = {"Light Theme", "Dark Theme", "System Default"};
        themeSelector = new JComboBox<>(themes);
        themeSelector.setFont(NORMAL_FONT);

        themePanel.add(themeLabel);
        themePanel.add(themeSelector);

        // Animation speed
        JPanel animationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        animationPanel.setBackground(Color.WHITE);
        animationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel animationLabel = new JLabel("Animation Speed:");
        animationLabel.setFont(NORMAL_FONT);
        animationLabel.setPreferredSize(new Dimension(150, 30));

        animationSpeedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        animationSpeedSlider.setMajorTickSpacing(25);
        animationSpeedSlider.setPaintTicks(true);
        animationSpeedSlider.setPaintLabels(true);
        animationSpeedSlider.setBackground(Color.WHITE);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("Off"));
        labelTable.put(25, new JLabel("Slow"));
        labelTable.put(50, new JLabel("Normal"));
        labelTable.put(75, new JLabel("Fast"));
        labelTable.put(100, new JLabel("Instant"));
        animationSpeedSlider.setLabelTable(labelTable);

        animationPanel.add(animationLabel);
        animationPanel.add(animationSpeedSlider);

        // Currency selector
        JPanel currencyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        currencyPanel.setBackground(Color.WHITE);
        currencyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel currencyLabel = new JLabel("Currency:");
        currencyLabel.setFont(NORMAL_FONT);
        currencyLabel.setPreferredSize(new Dimension(150, 30));

        String[] currencies = {"Pakistani Rupee (Rs.)", "US Dollar ($)", "Euro (€)", "British Pound (£)"};
        JComboBox<String> currencySelector = new JComboBox<>(currencies);
        currencySelector.setFont(NORMAL_FONT);

        currencyPanel.add(currencyLabel);
        currencyPanel.add(currencySelector);

        // Tariff and billing settings section
        JLabel tariffLabel = new JLabel("Tariff and Billing");
        tariffLabel.setFont(HEADING_FONT);
        tariffLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create tariff editor panel
        JPanel tariffEditorPanel = createTariffEditorPanel();
        tariffEditorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Billing cycle
        JPanel billingCyclePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        billingCyclePanel.setBackground(Color.WHITE);
        billingCyclePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel billingCycleLabel = new JLabel("Billing Cycle Start:");
        billingCycleLabel.setFont(NORMAL_FONT);
        billingCycleLabel.setPreferredSize(new Dimension(150, 30));

        SpinnerModel dayModel = new SpinnerNumberModel(1, 1, 28, 1);
        JSpinner daySpinner = new JSpinner(dayModel);
        daySpinner.setFont(NORMAL_FONT);

        JLabel ofMonthLabel = new JLabel(" of each month");
        ofMonthLabel.setFont(NORMAL_FONT);

        billingCyclePanel.add(billingCycleLabel);
        billingCyclePanel.add(daySpinner);
        billingCyclePanel.add(ofMonthLabel);

        // Account section
        JLabel accountLabel = new JLabel("Account");
        accountLabel.setFont(HEADING_FONT);
        accountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // User info panel
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setBackground(Color.WHITE);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userNameLabel = new JLabel("Wasif Sohail");
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel userEmailLabel = new JLabel("wasifsohail66@gmail.com");
        userEmailLabel.setFont(NORMAL_FONT);
        userEmailLabel.setForeground(new Color(100, 100, 100));

        userInfoPanel.add(new JLabel(new ImageIcon(createCircularAvatar(40, "user.png"))));

        JPanel userTextPanel = new JPanel();
        userTextPanel.setLayout(new BoxLayout(userTextPanel, BoxLayout.Y_AXIS));
        userTextPanel.setBackground(Color.WHITE);
        userTextPanel.add(userNameLabel);
        userTextPanel.add(userEmailLabel);

        userInfoPanel.add(userTextPanel);

        // Data backup panel
        JPanel dataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dataPanel.setBackground(Color.WHITE);
        dataPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton exportDataButton = new JButton("Export Data");
        exportDataButton.setFont(NORMAL_FONT);

        JButton importDataButton = new JButton("Import Data");
        importDataButton.setFont(NORMAL_FONT);

        JButton clearDataButton = new JButton("Clear All Data");
        clearDataButton.setFont(NORMAL_FONT);

        dataPanel.add(exportDataButton);
        dataPanel.add(importDataButton);
        dataPanel.add(clearDataButton);

        // Add all components to panel with spacing
        contentPanel.add(appearanceLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(themePanel);
        contentPanel.add(animationPanel);
        contentPanel.add(currencyPanel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        contentPanel.add(tariffLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(tariffEditorPanel);
        contentPanel.add(billingCyclePanel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(new JSeparator());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        contentPanel.add(accountLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(userInfoPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(dataPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(NORMAL_FONT);

        JButton saveButton = createStyledButton("Save Changes", primaryColor);

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTariffEditorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("Electricity Rate Tiers");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] tierLabels = {"1-50 kWh", "51-100 kWh", "101-200 kWh", "201-300 kWh", "301-700 kWh", "700+ kWh"};
        double[] rates = {4.81, 7.87, 10.54, 12.89, 21.88, 24.93};

        for (int i = 0; i < tierLabels.length; i++) {
            JPanel tierPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            tierPanel.setBackground(Color.WHITE);
            tierPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel tierLabel = new JLabel(tierLabels[i] + ": ");
            tierLabel.setFont(NORMAL_FONT);
            tierLabel.setPreferredSize(new Dimension(100, 25));

            JTextField rateField = new JTextField(String.valueOf(rates[i]), 5);
            rateField.setFont(NORMAL_FONT);

            JLabel unitLabel = new JLabel(" Rs/unit");
            unitLabel.setFont(NORMAL_FONT);

            tierPanel.add(tierLabel);
            tierPanel.add(rateField);
            tierPanel.add(unitLabel);

            panel.add(tierPanel);
            if (i < tierLabels.length - 1) {
                panel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel versionLabel = new JLabel("v2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(150, 150, 150));

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(versionLabel, BorderLayout.EAST);

        return panel;
    }

    private void animateTransition(String cardName) {
        // Get animation speed from settings
        int speed = animationSpeedSlider != null ? animationSpeedSlider.getValue() : 50;

        if (speed <= 0) {
            // Skip animation if speed is set to off
            cardLayout.show(contentPanel, cardName);
            statusLabel.setText("Switched to " + cardName);
            return;
        }

        // Calculate duration based on speed
        int duration = (int) (ANIMATION_DURATION * (1 - speed / 100.0)) + 50;

        // Create transition animation
        final Timer timer = new Timer(10, null);
        final long startTime = System.currentTimeMillis();

        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);

            if (progress >= 1f) {
                timer.stop();
                cardLayout.show(contentPanel, cardName);
                statusLabel.setText("Switched to " + cardName);
                contentPanel.setOpaque(true);
                contentPanel.setBackground(backgroundColor);
                contentPanel.repaint();
            } else {
                // Fade out effect
                float alpha = 1f - progress;
                contentPanel.setBackground(new Color(
                        backgroundColor.getRed(),
                        backgroundColor.getGreen(),
                        backgroundColor.getBlue(),
                        (int) (alpha * 255)
                ));
                contentPanel.repaint();
            }
        });

        timer.start();
    }

    private void highlightSelectedButton(int index) {
        for (int i = 0; i < sidebarButtons.size(); i++) {
            sidebarButtons.get(i).setSelected(i == index);
        }
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(NORMAL_FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private Image createLogo(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw lightning bolt icon
        g2d.setColor(new Color(255, 193, 7));

        int[] xPoints = {width / 2, width / 4, width / 2, 3 * width / 4};
        int[] yPoints = {0, height / 2, height / 2, height};
        g2d.fillPolygon(xPoints, yPoints, 4);

        g2d.dispose();
        return image;
    }

    private Image createCircularAvatar(int size, String imageName) {
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw circle for avatar background
        g2.setColor(new Color(52, 152, 219));
        g2.fillOval(0, 0, size, size);

        // Draw text initials
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        FontMetrics fm = g2.getFontMetrics();
        String text = "WS";
        g2.drawString(text, (size - fm.stringWidth(text)) / 2, (size + fm.getAscent() - fm.getDescent()) / 2);

        g2.dispose();
        return result;
    }

    // Logic methods

    private void setDeviceDefaults(String deviceType) {
        switch (deviceType) {
            case "Air Conditioner":
                deviceNameField.setText("Air Conditioner");
                wattsField.setText("1500");
                hoursField.setText("8");
                efficiencySlider.setValue(50); // Cycles on/off
                break;
            case "Refrigerator":
                deviceNameField.setText("Refrigerator");
                wattsField.setText("150");
                hoursField.setText("24");
                efficiencySlider.setValue(40); // Cycles
                break;
            case "Ceiling Fan":
                deviceNameField.setText("Ceiling Fan");
                wattsField.setText("75");
                hoursField.setText("12");
                efficiencySlider.setValue(100); // Constant use when on
                break;
            case "Light Bulb":
                deviceNameField.setText("LED Light");
                wattsField.setText("9");
                hoursField.setText("6");
                efficiencySlider.setValue(100); // Constant use when on
                break;
            case "Television":
                deviceNameField.setText("LED TV");
                wattsField.setText("100");
                hoursField.setText("5");
                efficiencySlider.setValue(100);
                break;
            case "Water Heater":
                deviceNameField.setText("Water Heater");
                wattsField.setText("2000");
                hoursField.setText("1");
                efficiencySlider.setValue(100);
                break;
            case "Washing Machine":
                deviceNameField.setText("Washing Machine");
                wattsField.setText("500");
                hoursField.setText("1");
                efficiencySlider.setValue(80);
                break;
            case "Computer":
                deviceNameField.setText("Desktop Computer");
                wattsField.setText("200");
                hoursField.setText("4");
                efficiencySlider.setValue(100);
                break;
            case "Microwave Oven":
                deviceNameField.setText("Microwave Oven");
                wattsField.setText("1200");
                hoursField.setText("0.5");
                efficiencySlider.setValue(100);
                break;
            case "Other":
                deviceNameField.setText("");
                wattsField.setText("");
                hoursField.setText("");
                efficiencySlider.setValue(100);
                break;
        }
    }

    private void resetCalculatorFields() {
        deviceTypeComboBox.setSelectedIndex(0);
        deviceNameField.setText("");
        wattsField.setText("");
        hoursField.setText("");
        efficiencySlider.setValue(100);
        resultLabel.setText("<html><div style='text-align: center;'>" +
                "<p>Enter device information and click Calculate to see the estimated electricity cost.</p>" +
                "<p>You can save frequently used devices for quick access.</p>" +
                "</div></html>");
        statusLabel.setText("Fields reset");
    }

    private void calculateAndDisplayResult() {
        // Check if inputs are valid
        if (!validateCalculatorInputs()) {
            return;
        }

        // Show progress bar animation
        progressBar.setVisible(true);
        progressBar.setValue(0);

        // Disable main buttons during calculation
        Component[] components = ((JPanel) progressBar.getParent()).getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                ((JButton) component).setEnabled(false);
            }
        }

        // Use a timer for animation and to simulate calculation
        Timer timer = new Timer(30, new ActionListener() {
            int progress = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 5;
                progressBar.setValue(progress);

                if (progress >= 100) {
                    ((Timer) e.getSource()).stop();
                    displayCalculationResults();
                    progressBar.setVisible(false);

                    // Re-enable buttons
                    for (Component component : components) {
                        if (component instanceof JButton) {
                            ((JButton) component).setEnabled(true);
                        }
                    }
                }
            }
        });
        timer.start();
    }

    private boolean validateCalculatorInputs() {
        // Check if device type is selected
        if (deviceTypeComboBox.getSelectedIndex() == 0) {
            showErrorMessage("Please select a device type.");
            return false;
        }

        // Check if device name is entered
        if (deviceNameField.getText().trim().isEmpty()) {
            showErrorMessage("Please enter a device name.");
            return false;
        }

        // Check if watts is a valid number
        try {
            double watts = Double.parseDouble(wattsField.getText().trim());
            if (watts <= 0) {
                showErrorMessage("Please enter a valid power rating (watts).");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("Please enter a valid number for power rating.");
            return false;
        }

        // Check if hours is a valid number
        try {
            double hours = Double.parseDouble(hoursField.getText().trim());
            if (hours <= 0 || hours > 24) {
                showErrorMessage("Please enter valid hours between 0 and 24.");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorMessage("Please enter a valid number for hours.");
            return false;
        }

        return true;
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Invalid Input",
                JOptionPane.WARNING_MESSAGE);
    }

    private void displayCalculationResults() {
        try {
            String deviceName = deviceNameField.getText();
            double watts = Double.parseDouble(wattsField.getText().trim());
            double hours = Double.parseDouble(hoursField.getText().trim());
            int efficiencyPercent = efficiencySlider.getValue();

            // Adjust watts based on efficiency setting
            double effectiveWatts = watts * (efficiencyPercent / 100.0);

            // Calculate monthly kWh
            double kwh = ((effectiveWatts * hours) * 30) / 1000;

            // Find appropriate unit price tier
            double unitPrice = findUnitPrice(kwh);

            // Calculate costs
            double dailyCost = (effectiveWatts * hours * unitPrice) / 1000;
            double monthlyCost = dailyCost * 30;
            double annualCost = monthlyCost * 12;

            // Format for display
            DecimalFormat df = new DecimalFormat("#,##0.00");
            DecimalFormat dfKwh = new DecimalFormat("#,##0.##");

            // Create summary for display
            StringBuilder resultHtml = new StringBuilder();
            resultHtml.append("<html><div style='text-align: center;'>");

            // Device name and icons
            resultHtml.append("<h2 style='margin-bottom: 5px;'>").append(deviceName).append("</h2>");

            // Monthly cost - highlighted
            resultHtml.append("<div style='background-color: #f0f8ff; padding: 10px; margin: 10px 0; border-radius: 5px;'>");
            resultHtml.append("<p style='font-size: 14px; color: #666; margin: 0;'>MONTHLY COST</p>");
            resultHtml.append("<p style='font-size: 24px; font-weight: bold; color: #2980b9; margin: 5px 0;'>₨").append(df.format(monthlyCost)).append("</p>");
            resultHtml.append("</div>");

            // Detailed breakdown
            resultHtml.append("<table style='width: 100%; border-collapse: collapse;'>");

            // Power consumption
            resultHtml.append("<tr><td align='left'>Power Rating:</td><td align='right'>").append(watts).append(" watts</td></tr>");

            // Efficiency adjusted
            if (efficiencyPercent < 100) {
                resultHtml.append("<tr><td align='left'>Effective Power:</td><td align='right'>").append(df.format(effectiveWatts)).append(" watts</td></tr>");
            }

            // Usage details
            resultHtml.append("<tr><td align='left'>Daily Usage:</td><td align='right'>").append(hours).append(" hours</td></tr>");
            resultHtml.append("<tr><td align='left'>Energy Used:</td><td align='right'>").append(dfKwh.format(kwh)).append(" kWh/month</td></tr>");
            resultHtml.append("<tr><td align='left'>Rate Applied:</td><td align='right'>Rs").append(df.format(unitPrice)).append("/kWh</td></tr>");

            // Cost breakdown
            resultHtml.append("<tr><td align='left'>Daily Cost:</td><td align='right'>Rs").append(df.format(dailyCost)).append("</td></tr>");
            resultHtml.append("<tr><td align='left' style='font-weight: bold;'>Monthly Cost:</td><td align='right' style='font-weight: bold;'>Rs").append(df.format(monthlyCost)).append("</td></tr>");
            resultHtml.append("<tr><td align='left'>Annual Cost:</td><td align='right'>Rs").append(df.format(annualCost)).append("</td></tr>");

            resultHtml.append("</table>");

            // Annual comparison to visualize impact
            resultHtml.append("<p style='margin-top: 15px;'>This device represents approximately <b>").
                    append(df.format((monthlyCost / 4289) * 100)).
                    append("%</b> of an average monthly electricity bill.</p>");

            // Energy saving suggestion if applicable
            if (getDeviceEfficiency(deviceName, watts) == DeviceEfficiency.LOW) {
                resultHtml.append("<p style='color: #e74c3c; margin-top: 10px;'>This device has a high energy consumption. Consider an energy-efficient alternative to save up to 30% on electricity costs.</p>");
            }

            resultHtml.append("</div></html>");

            // Set the result label
            resultLabel.setText(resultHtml.toString());

            // Update status
            statusLabel.setText("Calculation completed for " + deviceName);
        } catch (Exception e) {
            resultLabel.setText("Error in calculation");
            statusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double findUnitPrice(double kwh) {
        for (int i = 0; i < KWH_TIERS.length; i++) {
            if (kwh <= KWH_TIERS[i]) {
                return UNIT_PRICES[i];
            }
        }
        return UNIT_PRICES[UNIT_PRICES.length - 1]; // default to highest tier
    }

    private void saveCurrentDevice() {
        if (!validateCalculatorInputs()) {
            return;
        }

        try {
            String deviceName = deviceNameField.getText();
            double watts = Double.parseDouble(wattsField.getText().trim());
            double hours = Double.parseDouble(hoursField.getText().trim());
            int efficiencyPercent = efficiencySlider.getValue();
            String deviceType = (String) deviceTypeComboBox.getSelectedItem();

            // Calculate monthly kWh
            double effectiveWatts = watts * (efficiencyPercent / 100.0);
            double kwh = ((effectiveWatts * hours) * 30) / 1000;
            double unitPrice = findUnitPrice(kwh);
            double monthlyCost = ((effectiveWatts * hours * unitPrice) / 1000) * 30;

            // Create new appliance data object
            ApplianceData appliance = new ApplianceData(
                    deviceName, deviceType, watts, hours, efficiencyPercent,
                    kwh, monthlyCost, new Date()
            );

            // Add to list of appliances
            savedAppliances.add(appliance);

            // Add to table model
            DecimalFormat df = new DecimalFormat("#,##0.00");
            DecimalFormat dfKwh = new DecimalFormat("#,##0.##");

            Object[] rowData = {
                    deviceName,
                    deviceType,
                    watts,
                    hours,
                    dfKwh.format(kwh),
                    "₨" + df.format(monthlyCost),
                    "Edit/Delete"
            };

            tableModel.addRow(rowData);

            // Show confirmation
            JOptionPane.showMessageDialog(this,
                    deviceName + " has been added to your device list.",
                    "Device Saved",
                    JOptionPane.INFORMATION_MESSAGE);

            // Update status
            statusLabel.setText(deviceName + " saved to device list");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving device: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Helper methods

    private void initializeSampleData() {
        // Create sample appliance data
        savedAppliances.add(new ApplianceData("Living Room AC", "Air Conditioner", 1500, 8, 50, 160, 1920, new Date()));
        savedAppliances.add(new ApplianceData("Kitchen Refrigerator", "Refrigerator", 150, 24, 40, 75, 591, new Date()));
        savedAppliances.add(new ApplianceData("Bathroom Water Heater", "Water Heater", 2000, 1, 100, 45, 355, new Date()));
        savedAppliances.add(new ApplianceData("Washing Machine", "Washing Machine", 500, 1, 80, 35, 276, new Date()));
        savedAppliances.add(new ApplianceData("Living Room TV", "Television", 100, 5, 100, 25, 197, new Date()));
        savedAppliances.add(new ApplianceData("Bedroom AC", "Air Conditioner", 1200, 7, 50, 95, 1150, new Date()));
        savedAppliances.add(new ApplianceData("Kitchen Microwave", "Microwave Oven", 1200, 0.5, 100, 18, 142, new Date()));
        savedAppliances.add(new ApplianceData("Living Room Lights", "Light Bulb", 75, 6, 100, 14, 110, new Date()));
        savedAppliances.add(new ApplianceData("Bedroom Fan", "Ceiling Fan", 75, 8, 100, 18, 142, new Date()));
        savedAppliances.add(new ApplianceData("Study Computer", "Computer", 200, 4, 100, 24, 189, new Date()));
        savedAppliances.add(new ApplianceData("Bedroom Lights", "Light Bulb", 45, 4, 100, 5.4, 43, new Date()));
        savedAppliances.add(new ApplianceData("Kitchen Fan", "Ceiling Fan", 60, 3, 100, 5.4, 43, new Date()));

        // Create sample bill history
        billHistory.add(new BillHistory("January", 305, 2890, "2024-01-25"));
        billHistory.add(new BillHistory("February", 290, 2540, "2024-02-25"));
        billHistory.add(new BillHistory("March", 320, 3450, "2024-03-25"));
        billHistory.add(new BillHistory("April", 356, 4289, "2024-04-25"));
    }

    private void addSampleDevicesToTable() {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        DecimalFormat dfKwh = new DecimalFormat("#,##0.##");

        for (ApplianceData appliance : savedAppliances) {
            Object[] rowData = {
                    appliance.getName(),
                    appliance.getType(),
                    appliance.getWatts(),
                    appliance.getHours(),
                    dfKwh.format(appliance.getKwh()),
                    "Rs" + df.format(appliance.getMonthlyCost()),
                    "Edit/Delete"
            };

            tableModel.addRow(rowData);
        }
    }

    private enum DeviceEfficiency {
        HIGH, MEDIUM, LOW
    }

    private DeviceEfficiency getDeviceEfficiency(String deviceName, double watts) {
        // Simple efficiency check based on device type and watts
        String deviceNameLower = deviceName.toLowerCase();

        if (deviceNameLower.contains("ac") || deviceNameLower.contains("air conditioner")) {
            if (watts < 1000) return DeviceEfficiency.HIGH;
            else if (watts < 1500) return DeviceEfficiency.MEDIUM;
            else return DeviceEfficiency.LOW;
        } else if (deviceNameLower.contains("refrigerator") || deviceNameLower.contains("fridge")) {
            if (watts < 100) return DeviceEfficiency.HIGH;
            else if (watts < 150) return DeviceEfficiency.MEDIUM;
            else return DeviceEfficiency.LOW;
        } else if (deviceNameLower.contains("light") || deviceNameLower.contains("bulb")) {
            if (watts < 10) return DeviceEfficiency.HIGH;
            else if (watts < 30) return DeviceEfficiency.MEDIUM;
            else return DeviceEfficiency.LOW;
        } else if (deviceNameLower.contains("fan")) {
            if (watts < 50) return DeviceEfficiency.HIGH;
            else if (watts < 70) return DeviceEfficiency.MEDIUM;
            else return DeviceEfficiency.LOW;
        }

        // Default efficiency check based on watts
        if (watts < 100) return DeviceEfficiency.HIGH;
        else if (watts < 500) return DeviceEfficiency.MEDIUM;
        else return DeviceEfficiency.LOW;
    }

    private void showGeneratingReportDialog() {
        JDialog dialog = new JDialog(this, "Generating Report", true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel messageLabel = new JLabel("Generating comprehensive energy report...");
        messageLabel.setFont(NORMAL_FONT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);

        panel.add(messageLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);

        dialog.add(panel);

        // Start a timer to simulate report generation
        Timer timer = new Timer(2000, e -> {
            ((Timer) e.getSource()).stop();
            dialog.dispose();
            showReportGeneratedDialog();
        });
        timer.start();

        dialog.setVisible(true);
    }

    private void showReportGeneratedDialog() {
        JOptionPane.showMessageDialog(this,
                "Your energy usage report has been generated and is ready for viewing.\n" +
                        "A copy has also been sent to your registered email address.",
                "Report Generated",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Inner classes for components

    private class SidebarButton extends JButton {
        private boolean isSelected = false;

        public SidebarButton(String text, String iconPath) {
            super(text);
            setFont(NORMAL_FONT);
            setForeground(Color.WHITE);
            setBackground(new Color(52, 73, 94));
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Load icon from file path
            try {
                File iconFile = new File(iconPath);
                if (iconFile.exists()) {
                    // Load and resize icon
                    ImageIcon originalIcon = new ImageIcon(iconPath);
                    Image scaledImage = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    setIcon(new ImageIcon(scaledImage));

                    // Add some padding
                    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                } else {
                    System.out.println("Icon file not found: " + iconPath);
                }
            } catch (Exception e) {
                System.out.println("Error loading icon: " + e.getMessage());
                e.printStackTrace();
            }

            // Set preferred size
            setPreferredSize(new Dimension(SIDEBAR_WIDTH - 20, 40));
            setMaximumSize(new Dimension(SIDEBAR_WIDTH - 20, 40));

            // Add hover effects
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected) {
                        setContentAreaFilled(true);
                        setBackground(new Color(52, 73, 94).brighter());
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isSelected) {
                        setContentAreaFilled(false);
                    }
                }
            });
        }

        // Keep only one setSelected method
        public void setSelected(boolean selected) {
            isSelected = selected;
            if (selected) {
                setContentAreaFilled(true);
                setBackground(primaryColor);
            } else {
                setContentAreaFilled(false);
            }
        }
    }

    private class ShadowBorder extends AbstractBorder {
        private static final int SHADOW_SIZE = 5;
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw rounded rectangle
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(x, y, width - SHADOW_SIZE, height - SHADOW_SIZE, 10, 10);
            
            // Draw shadow
            for (int i = 0; i < SHADOW_SIZE; i++) {
                float alpha = 0.1f - (i * 0.02f);
                if (alpha < 0) alpha = 0;
                
                g2d.setColor(new Color(0, 0, 0, alpha));
                g2d.drawRoundRect(x + i, y + i, width - i * 2, height - i * 2, 10, 10);
            }
            
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE, SHADOW_SIZE);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = SHADOW_SIZE;
            return insets;
        }
    }
    
    private class ModernScrollBarUI extends BasicScrollBarUI {
        private final int THUMB_SIZE = 10;
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(new Color(240, 240, 240));
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setPaint(new Color(180, 180, 180));
            
            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                g2.fillRoundRect(
                    thumbBounds.x + (thumbBounds.width - THUMB_SIZE) / 2,
                    thumbBounds.y,
                    THUMB_SIZE,
                    thumbBounds.height,
                    THUMB_SIZE,
                    THUMB_SIZE);
            } else {
                g2.fillRoundRect(
                    thumbBounds.x,
                    thumbBounds.y + (thumbBounds.height - THUMB_SIZE) / 2,
                    thumbBounds.width,
                    THUMB_SIZE,
                    THUMB_SIZE,
                    THUMB_SIZE);
            }
            
            g2.dispose();
        }
        
        @Override
        protected void setThumbBounds(int x, int y, int width, int height) {
            super.setThumbBounds(x, y, width, height);
            scrollbar.repaint();
        }
    }
    
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Edit/Delete");
            setBackground(primaryColor);
            setForeground(Color.WHITE);
            return this;
        }
    }
    
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String action;
        private boolean isPushed;
        private int editingRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            action = (value == null) ? "" : value.toString();
            button.setText(action);
            button.setBackground(primaryColor);
            button.setForeground(Color.WHITE);
            isPushed = true;
            editingRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Show edit/delete popup menu
                JPopupMenu menu = new JPopupMenu();
                JMenuItem editItem = new JMenuItem("Edit");
                JMenuItem deleteItem = new JMenuItem("Delete");
                
                editItem.addActionListener(e -> {
                    // Open calculator with this device's values
                    ApplianceData appliance = savedAppliances.get(editingRow);
                    loadApplianceToCalculator(appliance);
                    animateTransition("calculator");
                    highlightSelectedButton(1); // Calculator button
                });
                
                deleteItem.addActionListener(e -> {
                    // Show confirmation dialog
                    int option = JOptionPane.showConfirmDialog(
                        button, 
                        "Are you sure you want to delete this device?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);
                    
                    if (option == JOptionPane.YES_OPTION) {
                        // Remove from data list and table
                        savedAppliances.remove(editingRow);
                        tableModel.removeRow(editingRow);
                        statusLabel.setText("Device deleted");
                    }
                });
                
                menu.add(editItem);
                menu.add(deleteItem);
                menu.show(button, 0, button.getHeight());
            }
            isPushed = false;
            return action;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
    
    private void loadApplianceToCalculator(ApplianceData appliance) {
        // Set device type
        String type = appliance.getType();
        for (int i = 0; i < deviceTypeComboBox.getItemCount(); i++) {
            if (deviceTypeComboBox.getItemAt(i).equals(type)) {
                deviceTypeComboBox.setSelectedIndex(i);
                break;
            }
        }
        
        // Set other fields
        deviceNameField.setText(appliance.getName());
        wattsField.setText(String.valueOf(appliance.getWatts()));
        hoursField.setText(String.valueOf(appliance.getHours()));
        efficiencySlider.setValue(appliance.getEfficiency());
        
        // Calculate and show results
        calculateAndDisplayResult();
    }
    
    // Data classes
    
    private class ApplianceData {
        private String name;
        private String type;
        private double watts;
        private double hours;
        private int efficiency;
        private double kwh;
        private double monthlyCost;
        private Date addedDate;
        
        public ApplianceData(String name, String type, double watts, double hours, int efficiency, double kwh, double monthlyCost, Date addedDate) {
            this.name = name;
            this.type = type;
            this.watts = watts;
            this.hours = hours;
            this.efficiency = efficiency;
            this.kwh = kwh;
            this.monthlyCost = monthlyCost;
            this.addedDate = addedDate;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public double getWatts() { return watts; }
        public double getHours() { return hours; }
        public int getEfficiency() { return efficiency; }
        public double getKwh() { return kwh; }
        public double getMonthlyCost() { return monthlyCost; }
        public Date getAddedDate() { return addedDate; }
    }
    
    private class BillHistory {
        private String month;
        private double kwh;
        private double amount;
        private String date;
        
        public BillHistory(String month, double kwh, double amount, String date) {
            this.month = month;
            this.kwh = kwh;
            this.amount = amount;
            this.date = date;
        }
        
        public String getMonth() { return month; }
        public double getKwh() { return kwh; }
        public double getAmount() { return amount; }
        public String getDate() { return date; }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EnhancedElectricityCalculator();
        });
    }
}