package Sources;

import javax.swing.*;
import static Sources.Dialogs.OpenDialog.*;

public class MenuBar {
    public static JMenuBar createMenuBar(MainWindow parent, String title, String author) {
        JMenu helpMenu = new JMenu("Aide");
        JMenuItem aboutMenuItem = new JMenuItem("A propos");
        helpMenu.add(aboutMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu(parent, title, author));
        menuBar.add(createViewMenu());
        menuBar.add(helpMenu);

        return menuBar;
    }
    private static JMenu createFileMenu(){
        //Export menu
        JMenu exportMenu = new JMenu("Exporter");
        JMenuItem exportJsonMenuItem = new JMenuItem("JSON");
        JMenuItem exportCsvMenuItem = new JMenuItem("CSV");
        exportMenu.add(exportJsonMenuItem);
        exportMenu.add(exportCsvMenuItem);

        //Import menu
        JMenu importMenu = new JMenu("Importer ");
        JMenuItem importJsonMenuItem = new JMenuItem("JSON");
        JMenuItem importCsvMenuItem = new JMenuItem("CSV");
        importMenu.add(importJsonMenuItem);
        importMenu.add(importCsvMenuItem);

        //Quit menuItem
        JMenuItem eMenuItem = new JMenuItem("Quitter");
        eMenuItem.setToolTipText("Quitter l'application");
        eMenuItem.addActionListener((event) -> System.exit(0));

        //Param menuItem
        JMenuItem paramMenuItem = new JMenuItem("Paramètres");

        //File Menu
        JMenu fileMenu = new JMenu("Fichier");
        fileMenu.add(exportMenu);
        fileMenu.add(importMenu);
        fileMenu.addSeparator();
        fileMenu.add(paramMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(eMenuItem);

        return fileMenu;
    }
    public static JMenu createEditMenu(MainWindow parent, String title, String author){
        //Add menu
        JMenu addMenu = new JMenu("Ajouter ");
        JMenuItem addBookMenuItem = new JMenuItem("Un livre");
        addBookMenuItem.addActionListener((e->openAddBookDlg()));
        JMenuItem addReadingMenuItem = new JMenuItem("Une lecture");
        addReadingMenuItem.addActionListener((e->openAddReadingDlg(title, author)));
        addMenu.add(addBookMenuItem);
        addMenu.add(addReadingMenuItem);

        //Manage menu
        JMenu manageMenu = new JMenu("Gérer ");
        JMenuItem manageTagMenuItem = new JMenuItem("Les tags");
        manageTagMenuItem.addActionListener((e->openManageTagsDlg()));
        JMenuItem manageReadingMenuItem = new JMenuItem("Les lectures");
        manageReadingMenuItem.addActionListener((e->parent.setCounterManageReading(openManageReadingDlg(parent, title, author))));
        manageMenu.add(manageTagMenuItem);
        manageMenu.add(manageReadingMenuItem);

        //Edit book
        JMenuItem editBookMenuItem = new JMenuItem("Modifier le livre");
        editBookMenuItem.addActionListener((e->openEditBookDlg(title, author)));

        //Delete book
        JMenuItem supprBookMenuItem = new JMenuItem("Supprimer le livre");

        //Filters book
        JMenuItem filterMenuItem = new JMenuItem("Filtrer");
        filterMenuItem.addActionListener((e -> openFilterDlg()));

        //Edit Menu
        JMenu editMenu = new JMenu("Editer");
        editMenu.add(addMenu);
        editMenu.add(manageMenu);
        editMenu.addSeparator();
        editMenu.add(editBookMenuItem);
        editMenu.add(supprBookMenuItem);
        editMenu.addSeparator();
        editMenu.add(filterMenuItem);

        return editMenu;
    }
    public static JMenu createViewMenu(){
        JMenu viewMenu = new JMenu("Affichage");
        JMenuItem logMenuItem = new JMenuItem("Log");
        viewMenu.add(logMenuItem);

        return viewMenu;
    }
}
