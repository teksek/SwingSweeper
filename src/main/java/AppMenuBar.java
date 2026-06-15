import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;

import javax.swing.*;
import java.util.prefs.Preferences;

public class AppMenuBar extends JMenuBar {
    public AppMenuBar(Preferences prefs, JFrame window) {
        JMenu themeSubMenu = new JMenu("Motyw");

        JMenuItem blackThemeItem = new JMenuItem("Czarny");
        blackThemeItem.addActionListener(e -> {
            prefs.put("theme", "dark");
            FlatDarkLaf.setup();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem whiteThemeItem = new JMenuItem("Biały");
        whiteThemeItem.addActionListener(e -> {
            prefs.put("theme", "light");
            FlatLightLaf.setup();
            SwingUtilities.updateComponentTreeUI(window);
        });

        JMenuItem systemThemeItem = new JMenuItem("Systemowy");
        systemThemeItem.addActionListener(e -> {
            prefs.put("theme", "system");
            OsThemeDetector detector = OsThemeDetector.getDetector();
            if (detector.isDark()) FlatDarkLaf.setup();
            else FlatLightLaf.setup();
            SwingUtilities.updateComponentTreeUI(window);
        });

        themeSubMenu.add(blackThemeItem);
        themeSubMenu.add(whiteThemeItem);
        themeSubMenu.add(systemThemeItem);

        add(themeSubMenu);
    }
}
