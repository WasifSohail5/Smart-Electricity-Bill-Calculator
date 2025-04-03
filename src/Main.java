import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create splash screen
            SplashScreen splash = new SplashScreen(3000);

            // Show splash and launch main application when done
            splash.showSplashAndExit(() -> {
                new EnhancedElectricityCalculator();
            });
        });
    }

    public void showApplianceSelection() {
        new EnhancedElectricityCalculator();
    }
}