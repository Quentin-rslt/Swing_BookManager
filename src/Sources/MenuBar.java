package Sources;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class MenuBar {
    public static JMenuBar createMenuBar() {
        var menuBar = new JMenuBar();

        var fileMenu = new JMenu("Fichier");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        var eMenuItem = new JMenuItem("Quitter");
        eMenuItem.setMnemonic(KeyEvent.VK_E);
        eMenuItem.setToolTipText("Quitter l'application");
        eMenuItem.addActionListener((event) -> System.exit(0));

        fileMenu.add(eMenuItem);
        menuBar.add(fileMenu);

        return menuBar;
    }
}
