package Sources;

import Sources.Dialogs.*;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static Sources.Common.*;
import static Sources.CommonSQL.*;
import static Sources.Dialogs.OpenDialog.*;
import static Sources.ImportExportData.*;
import static Sources.MainWindow.getAuthor;
import static Sources.MainWindow.getMTitle;

public class MenuBar {
    private static JMenuItem addReadingMenuItem;
    private static JMenuItem manageTagsMenuItem;
    private static JMenuItem editBookMenuItem;
    private static JMenuItem editReadingMenuItem;
    private static JMenuItem supprBookMenuItem;
    private static JMenuItem supprReadingMenuItem;
    public static JMenuBar createMenuBar(MainWindow parent) {
        JMenu helpMenu = new JMenu("Aide");
        JMenuItem aboutMenuItem = new JMenuItem("A propos");
        aboutMenuItem.addActionListener(e -> openAboutDlg());
        helpMenu.add(aboutMenuItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu(parent));
        menuBar.add(createEditMenu(parent));
        menuBar.add(createViewMenu());
        menuBar.add(helpMenu);

        return menuBar;
    }
    private static JMenu createFileMenu(MainWindow parent){
        //Export menu
        JMenu exportMenu = new JMenu("Exporter");
        JMenuItem exportDBMenuItem = new JMenuItem("Database");
        exportDBMenuItem.addActionListener((e -> {
            exportDB();
            JFrame jFrame = new JFrame();
            JOptionPane.showMessageDialog(jFrame, "L'exportation a été effectué");
        }));
        JMenuItem exportJsonMenuItem = new JMenuItem("JSON");
        JMenuItem exportCsvMenuItem = new JMenuItem("CSV");
        exportCsvMenuItem.addActionListener((e -> {
            boolean good =exportCSV();
            JFrame jFrame = new JFrame();
            if(good){
                JOptionPane.showMessageDialog(jFrame, "L'exportation a été effectué");
            }else{
                JOptionPane.showMessageDialog(jFrame,"L'exportation n'a pas pu être effectué","ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }) );
        exportMenu.add(exportDBMenuItem);
        exportMenu.add(exportJsonMenuItem);
        exportMenu.add(exportCsvMenuItem);

        //Import menu
        JMenu importMenu = new JMenu("Importer ");
        JMenuItem importDBMenuItem = new JMenuItem("Database");
        importDBMenuItem.addActionListener((e -> {
            int good = importDB(parent);
            if(good==1) {
                parent.setIsFiltered(false);
                parent.fillBookTable(parent.isFiltered());
                parent.setMTitle(parent.getBooksTable().getValueAt(0, 0).toString());
                parent.setAuthor(parent.getBooksTable().getValueAt(0, 1).toString());
                parent.setRowSelected(0);
                parent.setRowReading(0);
                parent.loadComponents(getMTitle(), getAuthor());//reload changes made to the book
                resetApp(parent, true);
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "L'importation des données a été effectué");
            }else if(good==0){
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "L'importation des données n'a pas pu être effectué","ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }));
        JMenuItem importJsonMenuItem = new JMenuItem("JSON");
        JMenuItem importCsvMenuItem = new JMenuItem("CSV");
        importCsvMenuItem.addActionListener((e -> {
            int good = importCSV(parent);
            if(good==1) {
                parent.setIsFiltered(false);
                parent.fillBookTable(parent.isFiltered());
                parent.setMTitle(parent.getBooksTable().getValueAt(0, 0).toString());
                parent.setAuthor(parent.getBooksTable().getValueAt(0, 1).toString());
                parent.loadComponents(getMTitle(), getAuthor());//reload changes made to the book
                parent.getBooksTable().setRowSelectionInterval(0, 0);
                resetApp(parent, true);
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "L'importation des données a été effectué");
            }else if(good==0){
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "L'importation des données n'a pas pu être effectué","ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }));
        importMenu.add(importDBMenuItem);
        importMenu.add(importJsonMenuItem);
        importMenu.add(importCsvMenuItem);

        //Quit menuItem
        JMenuItem eMenuItem = new JMenuItem("Quitter");
        eMenuItem.setToolTipText("Quitter l'application");
        eMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        eMenuItem.addActionListener((event) -> System.exit(0));

        //Param menuItem
        JMenuItem paramMenuItem = new JMenuItem("Paramètres");
        paramMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        paramMenuItem.addActionListener((e -> openParametersDlg(parent)));

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
    public static JMenu createEditMenu(MainWindow parent){
        //Add menu
        JMenu addMenu = new JMenu("Ajouter ");
        JMenuItem addBookMenuItem = new JMenuItem("Un livre");
        addBookMenuItem.addActionListener((e->{
            setNameOfImage("");
            AddBookDlg diag=openAddBookDlg();
            addBook(diag, parent);
        }));
        addBookMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getAddBookKey(), parent.getAddBookModif()));

        addReadingMenuItem = new JMenuItem("Une lecture");
        addReadingMenuItem.addActionListener((e->{
            AddReading diag = openAddReadingDlg();
            addReading(diag, parent);
        }));
        addReadingMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getAddReadingKey(), parent.getAddReadingModif()));
        addMenu.add(addBookMenuItem);
        addMenu.add(addReadingMenuItem);

        //Manage menu
        //All tags
        JMenu manageMenu = new JMenu("Gérer ");
        JMenuItem manageAllTagsMenuItem = new JMenuItem("Les tags");
        manageAllTagsMenuItem.addActionListener((e->{
            openManageTagsDlg();
            parent.getContentPanel().updateUI();
            parent.fillBookTable(parent.isFiltered());
            isItInFilteredBookList(parent, false);
            if(parent.isFastSearch()){
                parent.fastSearchBook(parent.getBookFastSearch().getText());
            }
        }));
        manageAllTagsMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getManageAllTagsKey(), parent.getManageAllTagsModif()));
        //Only tags of one book
        manageTagsMenuItem = new JMenuItem("Ses tags");
        manageTagsMenuItem.addActionListener((e->{
            openManageTagsDlg(getMTitle(),getAuthor());
            parent.getContentPanel().updateUI();
            parent.fillBookTable(parent.isFiltered());
            isItInFilteredBookList(parent, false);
            if(parent.isFastSearch()){
                parent.fastSearchBook(parent.getBookFastSearch().getText());
            }
        }));
        manageTagsMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getManageTagsKey(), parent.getManageTagsModif()));
        manageMenu.add(manageAllTagsMenuItem);
        manageMenu.add(manageTagsMenuItem);

        //Edit delete book and reading
        JMenu editBookRead = new JMenu("Modifier");
        JMenu deleteBookRead = new JMenu("Supprimer");
        //Edit book
        editBookMenuItem = new JMenuItem("Le livre");
        editBookMenuItem.addActionListener((e->{
            EditBookDlg diag = openEditBookDlg();
            editBook(diag,parent);
        }));
        editBookMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getEditKey(), parent.getEditModif()));

        //Delete book
        supprBookMenuItem = new JMenuItem("Le livre");
        supprBookMenuItem.addActionListener((e -> deleteBook(parent)));
        supprBookMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getDeletekey(), parent.getDeleteModif()));

        //Edit reading
        editReadingMenuItem = new JMenuItem("La lecture");
        editReadingMenuItem.addActionListener((e->{
            EditReadingDlg diag = openEditReadingDlg(parent.getManageReading().getStartReading(),parent.getManageReading().getEndReading());
            editReading(diag,parent);
        }));
        editReadingMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getEditKey(), parent.getEditModif()));

        //Delete reading
        supprReadingMenuItem = new JMenuItem("La lecture");
        supprReadingMenuItem.addActionListener((e -> deleteReading(parent)));
        supprReadingMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getDeletekey(), parent.getDeleteModif()));

        //Filters book
        JMenuItem filterMenuItem = new JMenuItem("Critères");
        filterMenuItem.addActionListener((e -> {
            FiltersDlg diag = openFilterDlg();
            parent.setDiagFilters(diag);
            filtersBook(diag, parent);
        }));
        filterMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getCritKey(), parent.getCritModif()));

        editBookRead.add(editBookMenuItem);
        editBookRead.add(editReadingMenuItem);
        deleteBookRead.add(supprBookMenuItem);
        deleteBookRead.add(supprReadingMenuItem);

        //Reset Filters
        JMenuItem resetFilterMenuItem = new JMenuItem("Rénitialiser les critères");
        resetFilterMenuItem.addActionListener(e -> {
            parent.getContentPanel().updateUI();
            parent.setIsFiltered(false);
            parent.fillBookTable(parent.isFiltered());
            isItInFilteredBookList(parent,false);
            if(parent.isFastSearch()){
                parent.fastSearchBook(parent.getBookFastSearch().getText());
            }
        });
        resetFilterMenuItem.setAccelerator(KeyStroke.getKeyStroke(parent.getResetKey(), parent.getResetModif()));

        //Edit Menu
        JMenu editMenu = new JMenu("Editer");
        editMenu.add(addMenu);
        editMenu.add(manageMenu);
        editMenu.addSeparator();
        editMenu.add(editBookRead);
        editMenu.add(deleteBookRead);
        editMenu.addSeparator();
        editMenu.add(filterMenuItem);
        editMenu.add(resetFilterMenuItem);

        return editMenu;
    }
    public static JMenu createViewMenu(){
        JMenu viewMenu = new JMenu("Affichage");
        JMenuItem logMenuItem = new JMenuItem("Log");
        viewMenu.add(logMenuItem);

        return viewMenu;
    }
    public static JMenuItem getAddReadingMenuItem() {
        return addReadingMenuItem;
    }

    public static JMenuItem getManageTagsMenuItem() {
        return manageTagsMenuItem;
    }
    public static JMenuItem getEditBookMenuItem() {
        return editBookMenuItem;
    }

    public static JMenuItem getEditReadingMenuItem() {
        return editReadingMenuItem;
    }

    public static JMenuItem getSupprBookMenuItem() {
        return supprBookMenuItem;
    }

    public static JMenuItem getSupprReadingMenuItem() {
        return supprReadingMenuItem;
    }
}
