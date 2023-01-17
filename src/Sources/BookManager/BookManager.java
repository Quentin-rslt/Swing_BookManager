package Sources.BookManager;

import Sources.BookManager.Dialogs.AddBookDlg;
import Sources.BookManager.Dialogs.AddReading;
import Sources.BookManager.Dialogs.EditBookDlg;
import Sources.BookManager.Dialogs.FiltersDlg;
import Sources.MainWindow;
import Sources.Components.MyManagerTable;
import Sources.Components.MyManagerRoundBorderComponents;
import Sources.Components.Tags;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static Sources.BookManager.CommonBookManager.*;
import static Sources.BookManager.CommonBookManager.isItInFilteredBookList;
import static Sources.BookManager.CommonBookManagerSQL.*;
import static Sources.BookManager.CommonBookManagerSQL.filtersBook;
import static Sources.BookManager.Dialogs.OpenBookManagerDialog.*;
import static Sources.BookManager.MenuBarBookManager.createMenuBar;
import static Sources.Common.*;
import static Sources.CommonSQL.connect;
import static Sources.Dialogs.OpenDialogs.*;

public class BookManager extends JDialog{
    private JLabel PersonalNoteLabel;
    private JLabel FirstReadingLabel;
    private JLabel LastReadingLabel;
    private JPanel BookPhotoPanel;
    private JLabel TitleLabel;
    private JLabel NumberPageLabel;
    private JLabel NoteLabel;
    private JTextPane BookSummary;
    private JButton AddBookBtn;
    private JButton FiltersBookBtn;
    private JButton CancelFiltersBtn;
    private JLabel CountReadingLabel;
    private JLabel ReleaseYearLAbel;
    private JLabel BookTimeAverageLabel;
    private JPanel BookTagsPanel;
    private JScrollPane JSpane;
    private JButton BookManageTagsBtn;
    private JTextField BookFastSearch;
    private MyManagerTable ReadingsTable;
    private JLabel CountBookLbl;
    private JScrollPane BooksJSPane;
    private JPanel contentPane;
    private JScrollPane ReadingsJSPane;
    private String m_title;
    private String m_author;
    private int m_rowSelected = 0;
    JPopupMenu m_popup;
    private FiltersDlg m_filtersDiag;
    private Boolean isFiltered;
    private boolean m_isFastSearch;
    private ManageReading m_manageReading;
    private int rowReading;
    private int m_deleteBookKey;
    private int m_editBookKey;
    private int m_deleteReadKey;
    private int m_editReadKey;
    private int m_addReadingKey;
    private int m_addBookKey;
    private int m_manageTagsKey;
    private int m_critKey;
    private int m_manageAllTagsKey;
    private int m_resetKey;

    private int m_deleteBookModif;
    private int m_editBookModif;
    private int m_deleteReadModif;
    private int m_editReadModif;
    private int m_addReadingModif;
    private int m_manageTagsModif;
    private int m_addBookModif;
    private int m_critModif;
    private int m_manageAllTagsModif;
    private int m_resetModif;
    private final MainWindow m_mainWindow;
    private MyManagerTable BooksTable;
    JMenuItem m_add;
    JMenuItem m_cut;
    JMenuItem m_edit;
    JMenuItem m_openManageTags;

    public BookManager(MainWindow parent){
        setContentPane(contentPane);
        m_mainWindow = parent;

        setIsFiltered(false);
        loadParameters();
        initComponents();
        initLoadComponents();
        initListeners();
    }
    /****************************** Getters ***********************************/
    public String getMTitle(){
        return m_title;
    }
    public String getAuthor(){
        return m_author;
    }
    public int getRowSelected() {
        return m_rowSelected;
    }
    public int getRowSelectedByBook(String title, String author){//return the row find by a title and an author
        int row = 0;
        int i= 0;
        while (i<BooksTable.getRowCount()) {
            if(Objects.equals(BooksTable.getValueAt(i, 0).toString(), title) && Objects.equals(BooksTable.getValueAt(i, 1).toString(), author)) {
                row = i;
                break;
            }
            i++;
        }

        return row;
    }
    public JTable getBooksTable(){
        return this.BooksTable;
    }
    public JTextField getBookFastSearch(){
        return this.BookFastSearch;
    }
    public Boolean isFiltered() {
        return isFiltered;
    }
    public Boolean isFastSearch(){
        return this.m_isFastSearch;
    }
    public JTable getReadingsTable() {
        return ReadingsTable;
    }
    public ManageReading getManageReading() {
        return m_manageReading;
    }
    public int getEditBookModif() {
        return m_editBookModif;
    }
    public int getEditBookKey() {
        return m_editBookKey;
    }
    public int getEditReadModif() {
        return m_editReadModif;
    }
    public int getEditReadKey() {
        return m_editReadKey;
    }
    public int getAddReadingKey() {
        return m_addReadingKey;
    }
    public int getManageTagsKey() {
        return m_manageTagsKey;
    }
    public int getDeleteBookModif() {
        return m_deleteBookModif;
    }
    public int getDeleteBookKey() {
        return m_deleteBookKey;
    }
    public int getDeleteReadModif() {
        return m_deleteReadModif;
    }
    public int getDeleteReadKey() {
        return m_deleteReadKey;
    }
    public int getAddReadingModif() {
        return m_addReadingModif;
    }
    public int getManageTagsModif() {
        return m_manageTagsModif;
    }
    public int getAddBookKey() {
        return m_addBookKey;
    }
    public int getAddBookModif() {
        return m_addBookModif;
    }
    public int getCritKey() {
        return m_critKey;
    }
    public int getManageAllTagsKey() {
        return m_manageAllTagsKey;
    }
    public int getCritModif() {
        return m_critModif;
    }
    public int getManageAllTagsModif() {
        return m_manageAllTagsModif;
    }
    public int getResetKey() {
        return m_resetKey;
    }
    public int getResetModif() {
        return m_resetModif;
    }
    public int getRowReading() {
        return rowReading;
    }
    public MainWindow getMainWindow(){return this.m_mainWindow;}
    public JPanel getContentPanel(){return this.contentPane;}

    /****************************** Void ***********************************/
    public void setDiagFilters(FiltersDlg dlg){
        this.m_filtersDiag=dlg;
    }
    public void setFastSearch(boolean fast){
        this.m_isFastSearch=fast;
    }
    public void setRowSelected(int m_rowSelected) {
        this.m_rowSelected = m_rowSelected;
    }
    public void setMTitle(String title){
        m_title=title;
    }
    public void setAuthor(String author){
        m_author=author;
    }
    public void setRowReading(int rowReading) {
        this.rowReading = rowReading;
    }
    public void setDeleteBookKey(int m_deletekey) {
        this.m_deleteBookKey = m_deletekey;
    }
    public void setEditBookKey(int m_editKey) {
        this.m_editBookKey = m_editKey;
    }
    public void setAddReadingKey(int m_addReadingKey) {
        this.m_addReadingKey = m_addReadingKey;
    }
    public void setManageTagsKey(int m_manageTagsKey) {
        this.m_manageTagsKey = m_manageTagsKey;
    }
    public void setDeleteBookModif(int m_deleteModif) {
        this.m_deleteBookModif = m_deleteModif;
    }
    public void setEditBookModif(int m_editModif) {
        this.m_editBookModif = m_editModif;
    }
    public void setDeleteReadKey(int m_deleteReadKey) {
        this.m_deleteReadKey = m_deleteReadKey;
    }
    public void setEditReadKey(int m_editReadKey) {
        this.m_editReadKey = m_editReadKey;
    }
    public void setDeleteReadModif(int m_deleteReadModif) {
        this.m_deleteReadModif = m_deleteReadModif;
    }
    public void setEditReadModif(int m_editReadModif) {
        this.m_editReadModif = m_editReadModif;
    }
    public void setAddReadingModif(int m_addReadingModif) {
        this.m_addReadingModif = m_addReadingModif;
    }
    public void setManageTagsModif(int m_manageTagsModif) {
        this.m_manageTagsModif = m_manageTagsModif;
    }
    public void setAddBookKey(int m_addBookKey) {
        this.m_addBookKey = m_addBookKey;
    }
    public void setAddBookModif(int m_addBookModif) {
        this.m_addBookModif = m_addBookModif;
    }
    public void setCritKey(int m_critKey) {
        this.m_critKey = m_critKey;
    }
    public void setManageAllTagsKey(int m_manageAllTagsKey) {
        this.m_manageAllTagsKey = m_manageAllTagsKey;
    }
    public void setCritModif(int m_critModif) {
        this.m_critModif = m_critModif;
    }
    public void setManageAllTagsModif(int m_manageAllTagsModif) {
        this.m_manageAllTagsModif = m_manageAllTagsModif;
    }
    public void setResetKey(int m_resetKey) {
        this.m_resetKey = m_resetKey;
    }
    public void setResetModif(int m_resetModif) {
        this.m_resetModif = m_resetModif;
    }
    public void fillBookTable(boolean isFiltered){
        BooksTable.getTableModel().setRowCount(0);
        CancelFiltersBtn.setEnabled(isFiltered);
        m_mainWindow.getJMenuBar().getMenu(1).getItem(7).setEnabled(isFiltered);
        try(Connection conn = connect()){
            Statement statement = conn.createStatement();
            ResultSet rs;
            if(isFiltered){
                StringBuilder qry = new StringBuilder("SELECT Book.Title, Book.Author FROM Book " +
                        "INNER JOIN Reading ON Reading.Title=Book.Title AND Reading.Author=Book.Author ");

                if (m_filtersDiag.getTags().getSizeTags()>0) {
                    qry.append("INNER JOIN Tagging ON Book.ID=Tagging.IdBook INNER JOIN Tags ON Tagging.idTag=Tags.ID WHERE Tags.Tag IN(");
                    for(int i = 0; i<m_filtersDiag.getTags().getSizeTags();i++){
                        if(i<m_filtersDiag.getTags().getSizeTags()-1){
                            qry.append("'").append(m_filtersDiag.getTags().getTag(i).getTextTag()).append("', ");
                        }
                        else {
                            qry.append("'").append(m_filtersDiag.getTags().getTag(i).getTextTag()).append("') ");
                        }
                    }
                    qry.append("AND Book.Title LIKE '%").append(m_filtersDiag.getFilterTitle()).append("%'");
                }else {
                    qry.append("WHERE Book.Title LIKE '%").append(m_filtersDiag.getFilterTitle()).append("%'");
                }
                qry.append("AND Book.Author LIKE '%").append(m_filtersDiag.getFilterAuthor()).append("%'").append("AND Book.ReleaseYear BETWEEN '").append(m_filtersDiag.getFirstDatRelease()).append("' AND '").append(m_filtersDiag.getLastDateRelease()).append("'").append("AND Book.NotePerso BETWEEN '").append(m_filtersDiag.getFirstNote()).append("' AND '").append(m_filtersDiag.getLastNote()).append("'").append("AND Book.NumberOP BETWEEN '").append(m_filtersDiag.getFirstNumberOP()).append("' AND '").append(m_filtersDiag.getLastNumberOP()).append("'").append("AND Book.NumberReading BETWEEN '").append(m_filtersDiag.getFirstNumberOR()).append("' AND '").append(m_filtersDiag.getLastNumberOR()).append("'").append("AND Book.AvReadingTime BETWEEN '").append(m_filtersDiag.getFirstAvTime()).append("' AND '").append(m_filtersDiag.getLastAvTime()).append("'").append("AND Book.NoteBabelio BETWEEN '").append(m_filtersDiag.getFirstNoteBB()).append("' AND '").append(m_filtersDiag.getLastNoteBB()).append("'");
                if(m_filtersDiag.isFiltered()){
                    qry.append("AND Reading.StartReading BETWEEN '").append(m_filtersDiag.getFirstStartDate()).append("' AND '").append(m_filtersDiag.getLastStartDate()).append("'").append("AND Reading.EndReading BETWEEN '").append(m_filtersDiag.getFirstEndDate()).append("' AND '").append(m_filtersDiag.getLastEndDate()).append("'");
                }

                if(!m_filtersDiag.getTextSort().equals("EndReading") && !m_filtersDiag.getTextSort().equals("StartReading")) {
                    if(m_filtersDiag.isAscending()){
                        qry.append(" GROUP BY Book.Title ORDER BY Book.").append(m_filtersDiag.getTextSort()).append(" ASC;");
                    }else{
                        qry.append(" GROUP BY Book.Title ORDER BY Book.").append(m_filtersDiag.getTextSort()).append(" DESC;");
                    }
                }else{
                    if(m_filtersDiag.isAscending()){
                        qry.append(" GROUP BY Book.Title ORDER BY Reading.").append(m_filtersDiag.getTextSort()).append(" ASC;");
                    }else{
                        qry.append(" GROUP BY Book.Title ORDER BY Reading.").append(m_filtersDiag.getTextSort()).append(" DESC;");
                    }
                }

                rs = statement.executeQuery(qry.toString());
            } else{
                rs = statement.executeQuery("SELECT * FROM Book;");//Execute a Query to retrieve all the values from the database by grouping the duplicates
            }
            // (with their name and author)
            while (rs.next()) {//Fill in the table of the list of Book
                String title = rs.getString("Title");//Retrieve the title
                String author = rs.getString("Author");//Retrieve the author

                String[] header = {"Titre","Auteur"};
                Object[] data = {title, author};

                BooksTable.getTableModel().setColumnIdentifiers(header);//Create the header
                BooksTable.getTableModel().addRow(data);//add to tablemodel the data
            }
            BooksTable.initTable();
            if(BooksTable.getRowCount()>0) {
                BooksTable.getTableHeader().setResizingAllowed(false);
                BooksTable.getColumnModel().getColumn(0).setPreferredWidth((int) ((BooksTable.getPreferredScrollableViewportSize().width+1) / 1.5));
                BooksTable.getColumnModel().getColumn(1).setPreferredWidth((BooksTable.getPreferredScrollableViewportSize().width) / 3);
            }

            rs.close();
            conn.close();
            statement.close();
        } catch ( Exception e ) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement tableau livre impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public void fillReadingTable(String title, String author) {
        ReadingsTable.getTableModel().setRowCount(0);
        try (Connection conn = connect()) {
            Statement m_statement = conn.createStatement();
            ResultSet qry = m_statement.executeQuery("SELECT StartReading, EndReading FROM Reading WHERE Title='" + title + "' AND Author='" + author + "'");

            while (qry.next()) {
                String startReading = qry.getString("StartReading");
                String endReading = qry.getString("EndReading");

                long days;
                String StdDays = "";
                boolean isOk = ((qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Inconnu")) ||
                        (qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Pas fini")))
                        || ((!qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Inconnu")) ||
                        (!qry.getString("StartReading").equals("Inconnu") && qry.getString("EndReading").equals("Pas fini")))
                        || ((qry.getString("StartReading").equals("Inconnu") && !qry.getString("EndReading").equals("Inconnu")) ||
                        (qry.getString("StartReading").equals("Inconnu") && !qry.getString("EndReading").equals("Pas fini")));
                if (!isOk) {
                    LocalDate start = LocalDate.parse(qry.getString("StartReading"));
                    LocalDate stop = LocalDate.parse(qry.getString("EndReading"));
                    days = ChronoUnit.DAYS.between(start, stop)+1;
                    StdDays = days + " jours";
                }

                String[] header = {"Début de lecture", "Fin de lecture", "Temps de lecture"};
                Object[] data = {startReading, endReading, StdDays};

                ReadingsTable.getTableModel().setColumnIdentifiers(header);//Create the header
                ReadingsTable.getTableModel().addRow(data);//add to tablemodel the data
            }
            ReadingsTable.initTable();

            contentPane.updateUI();
            qry.close();
            conn.close();
            m_statement.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement tableau lecture impossible", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void initLoadComponents(){
        if(BooksTable.getRowCount() != 0) {//Vérif if the table is not empty; when starting the app, load and focus on the first book of the table
            setMTitle(BooksTable.getValueAt(0, 0).toString());
            setAuthor(BooksTable.getValueAt(0, 1).toString());
            setRowReading(0);
            setRowSelected(0);
            loadComponents(getMTitle(), getAuthor());

            BooksTable.getActionMap().put("tab", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    if(BooksTable.getRowCount()>0) {
                        ReadingsTable.requestFocusInWindow();
                        m_manageReading.setStartReading(ReadingsTable.getValueAt(getRowReading(), 0).toString());
                        m_manageReading.setEndReading(ReadingsTable.getValueAt(getRowReading(), 1).toString());
                    }
                }
            });
            BooksTable.getActionMap().put("up", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    if(getRowSelected()>0) {
                        setRowSelected(getRowSelected() - 1);
                        setRowReading(0);
                        setMTitle(BooksTable.getValueAt(getRowSelected(), 0).toString());
                        setAuthor(BooksTable.getValueAt(getRowSelected(), 1).toString());
                        loadComponents(getMTitle(), getAuthor());
                    }
                }
            });
            BooksTable.getActionMap().put("dow", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    if(getRowSelected()<BooksTable.getRowCount()-1) {
                        setRowSelected(getRowSelected() + 1);
                        setRowReading(0);
                        setMTitle(BooksTable.getValueAt(getRowSelected(), 0).toString());
                        setAuthor(BooksTable.getValueAt(getRowSelected(), 1).toString());
                        loadComponents(getMTitle(), getAuthor());
                    }
                }
            });

            FiltersBookBtn.setEnabled(true);
        }else{
            FiltersBookBtn.setEnabled(false);
            resetBookManager(this, false);
        }
    }
    public void loadComponents(String title, String author){
        fillReadingTable(title,author);
        m_manageReading = new ManageReading(this, ReadingsTable);
        ReadingsTable.setRowSelectionInterval(getRowReading(),getRowReading());
        ReadingsTable.scrollFolowRow(getRowReading());
        m_manageReading.setStartReading(ReadingsTable.getValueAt(getRowReading(), 0).toString());
        m_manageReading.setEndReading(ReadingsTable.getValueAt(getRowReading(), 1).toString());

        BooksTable.setRowSelectionInterval(getRowSelected(), getRowSelected());
        BooksTable.scrollFolowRow(getRowSelected());

        FiltersBookBtn.setEnabled(true);
        BookManageTagsBtn.setEnabled(true);

        loadBook(title, author);
        loadReading(title, author);
        loadTags(title, author);
    }
    public void loadBook(String title, String author){
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet bookQry = statement.executeQuery("SELECT * FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");

            //Number of book
            CountBookLbl.setText("Livres : " + getBooksTable().getRowCount());

            TitleLabel.setText(title);

            //Release year label
            ReleaseYearLAbel.setText("Année de sortie : "+bookQry.getString(8));

            //Number of page label
            NumberPageLabel.setText("Nombre de page : "+bookQry.getString(5));

            //Note on babelio
            NoteLabel.setText("Note Babelio : "+bookQry.getString(7));

            //Summary
            BookSummary.setText(bookQry.getString(11));

            //Personal note
            PersonalNoteLabel.setText("Ma note : "+bookQry.getString(6));

            //Image
            addImageToPanel(bookQry.getString(4),BookPhotoPanel);

            //Number of reading label
            CountReadingLabel.setText("Nombre de lecture : "+bookQry.getInt(10));

            //Average time label
            int average = bookQry.getInt(9);
            if(average>0){
                BookTimeAverageLabel.setText("Temps moyen de lecture : "+average+" jours");
            }
            if (average==0) {
                BookTimeAverageLabel.setText("Temps moyen de lecture : Pas de moyenne possible");
            }

            conn.close();
            statement.close();
        }
        catch ( Exception e ) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement composants impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public void loadReading(String title, String author){
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();

            //First reading
            ResultSet FirstReadQry = statement.executeQuery("SELECT EndReading FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'ORDER BY EndReading ASC");
            boolean findFirst = false;
            while (FirstReadQry.next() && !findFirst){
                if(FirstReadQry.getString(1).equals("Inconnu") || FirstReadQry.getString(1).equals("Pas fini")){
                    FirstReadingLabel.setText("Première lecture : Pas de lecture fini");
                }
                else if(!FirstReadQry.getString(1).equals("Inconnu") && !FirstReadQry.getString(1).equals("Pas fini")){
                    FirstReadingLabel.setText("Première lecture : "+FirstReadQry.getString(1));
                    findFirst =true;
                }
            }

            //Last reading
            ResultSet LastReadQry = statement.executeQuery("SELECT EndReading FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'ORDER BY EndReading DESC");
            boolean findLast = false;
            while (LastReadQry.next() && !findLast){
                if(LastReadQry.getString(1).equals("Inconnu") || LastReadQry.getString(1).equals("Pas fini")){
                    LastReadingLabel.setText("Dernière lecture : Pas de lecture fini");
                }
                else if(!LastReadQry.getString(1).equals("Inconnu") && !LastReadQry.getString(1).equals("Pas fini")){
                    LastReadingLabel.setText("Dernière lecture : "+LastReadQry.getString(1));
                    findLast =true;
                }
            }

            conn.close();
            statement.close();
        }
        catch ( Exception e ) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement composants impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public void loadTags(String title, String author){
        Tags tags = new Tags();
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();

            //Tags Label
            ResultSet tagsQry = statement.executeQuery("SELECT Tag,Color FROM Tags JOIN Tagging on Tags.ID=Tagging.IdTag " +
                    "WHERE Tagging.IdBook='"+getIdBook(title, author)+"' ORDER BY Tag ASC");
            BookTagsPanel.removeAll();
            while (tagsQry.next()){
                tags.createTag(tagsQry.getString(1));
                tags.getTag(tagsQry.getRow()-1).setColor(tagsQry.getInt(2));
                for(int i=0; i<tags.getSizeTags();i++) {
                    BookTagsPanel.add(tags.getTag(i));
                }
            }
            BookTagsPanel.updateUI();

            conn.close();
            statement.close();
        } catch ( Exception e ) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement composants impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public void resetBookComponents(){
        ReadingsTable.getTableModel().setRowCount(0);
        TitleLabel.setText("Titre livre");
        ReleaseYearLAbel.setText("Année de sortie :");
        NumberPageLabel.setText("Nombre de page :");
        BookTimeAverageLabel.setText("Temps moyen de lecture : ");
        CountReadingLabel.setText("Nombre de lecture :");
        NoteLabel.setText("Note :");
        PersonalNoteLabel.setText("Ma note :");
        FirstReadingLabel.setText("Première lecture :");
        LastReadingLabel.setText("Dernière lecture :");
        CountBookLbl.setText("Livre : 0");
        BookSummary.setText("");
        BookTagsPanel.removeAll();
        BookPhotoPanel.removeAll();
        contentPane.setBorder(null);
        if(getNumberOfBook() == 0){
            FiltersBookBtn.setEnabled(false);
            resetBookManager(this, false);
        }
        contentPane.updateUI();
    }
    public void setIsFiltered(Boolean filtered) {
        isFiltered = filtered;
    }
    public void fastSearchBook(String editorText){
        fillBookTable(isFiltered());
        setFastSearch(!editorText.isBlank());


        for(int row = 0; row < getBooksTable().getRowCount(); row++){
            boolean bookFind = false;
            int column = 0;
            while( column < getBooksTable().getColumnCount() && !bookFind) {
                String cellText = getBooksTable().getModel().getValueAt(row,column).toString();
                StringBuilder testCellText = new StringBuilder();

                if (editorText.length() < cellText.length()) {
                    for (int filterIndex = 0; filterIndex < editorText.length(); filterIndex++) {
                        testCellText.append(cellText.charAt(filterIndex));
                    }
                    if(editorText.equalsIgnoreCase(testCellText.toString())){
                        bookFind = true;
                    }
                }
                column++;
            }

            if(!bookFind){
                BooksTable.getTableModel().removeRow(row);
                row--;
            }
        }

        AbstractBorder roundBrdMax = new MyManagerRoundBorderComponents(contentPane.getBackground(),1,30, 0,0,0);
        AbstractBorder roundBrdMin = new MyManagerRoundBorderComponents(contentPane.getBackground(),1,30, BooksTable.getPreferredScrollableViewportSize().height-(BooksTable.getRowCount()*BooksTable.getRowHeight()),0,0);
        if(BooksTable.getRowCount()>13)
            BooksTable.setBorder(roundBrdMax);
        else
            BooksTable.setBorder(roundBrdMin);

        if(getRowReading()+1>getReadingsTable().getRowCount()){
            setRowReading(0);
        }
        isItInFilteredBookList(this, false);
    }
    public void initBinding(){
        m_mainWindow.setJMenuBar(createMenuBar(this));
        contentPane.updateUI();
        BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "dow");
        BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "tab");

        ReadingsTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        ReadingsTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "dow");
        ReadingsTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "tab");
    }
    public void loadParameters(){
        try {
            Path file = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/save.dat");
            if ( Files.exists(file) ) {
                for(String line : Files.readAllLines(file)) {
                    String[] data = line.split(";");
                    setAddBookKey(Integer.parseInt(data[0]));
                    setAddBookModif(Integer.parseInt(data[1]));

                    setAddReadingKey(Integer.parseInt(data[2]));
                    setAddReadingModif(Integer.parseInt(data[3]));

                    setDeleteBookKey(Integer.parseInt(data[4]));
                    setDeleteBookModif(Integer.parseInt(data[5]));

                    setEditBookKey(Integer.parseInt(data[6]));
                    setEditBookModif(Integer.parseInt(data[7]));

                    setDeleteReadKey(Integer.parseInt(data[8]));
                    setDeleteReadModif(Integer.parseInt(data[9]));

                    setEditReadKey(Integer.parseInt(data[10]));
                    setEditReadModif(Integer.parseInt(data[11]));

                    setManageTagsKey(Integer.parseInt(data[12]));
                    setManageTagsModif(Integer.parseInt(data[13]));

                    setCritKey(Integer.parseInt(data[14]));
                    setCritModif(Integer.parseInt(data[15]));

                    setManageAllTagsKey(Integer.parseInt(data[16]));
                    setManageAllTagsModif(Integer.parseInt(data[17]));

                    setResetKey(Integer.parseInt(data[18]));
                    setResetModif(Integer.parseInt(data[19]));
                }
            }
            else {
                setAddReadingKey(KeyEvent.VK_A);
                setManageTagsKey(KeyEvent.VK_T);
                setEditBookKey(KeyEvent.VK_E);
                setDeleteBookKey(KeyEvent.VK_D);
                setEditReadKey(KeyEvent.VK_E);
                setDeleteReadKey(KeyEvent.VK_D);
                setAddBookKey(KeyEvent.VK_A);
                setCritKey(KeyEvent.VK_C);
                setManageAllTagsKey(KeyEvent.VK_T);
                setAddReadingModif(0);
                setManageTagsModif(0);
                setEditBookModif(0);
                setDeleteBookModif(0);
                setEditReadModif(KeyEvent.KEY_LOCATION_STANDARD);
                setDeleteReadModif(KeyEvent.KEY_LOCATION_STANDARD);
                setAddBookModif(KeyEvent.KEY_LOCATION_STANDARD);
                setCritModif(KeyEvent.KEY_LOCATION_STANDARD);
                setManageAllTagsModif(KeyEvent.KEY_LOCATION_STANDARD);
                setResetKey(KeyEvent.VK_R);
                setResetModif(KeyEvent.KEY_LOCATION_STANDARD);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Rechargement shortcut impossible", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void initListeners(){
        BooksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {//set main UI when we clicked on an element of the array, retrieved from the db
                super.mouseReleased(evt);
                BooksTable.requestFocusInWindow();
                int newLine= BooksTable.rowAtPoint(evt.getPoint());

                if(newLine != getRowSelected()) {
                    setRowSelected(BooksTable.rowAtPoint(evt.getPoint()));
                    setRowReading(0);
                    setMTitle(BooksTable.getValueAt(getRowSelected(), 0).toString());
                    setAuthor(BooksTable.getValueAt(getRowSelected(), 1).toString());
                    loadComponents(getMTitle(), getAuthor());
                }
                if(evt.getButton() == MouseEvent.BUTTON3) {//if we right-click show a popup to edit the book
                    m_popup.show(BooksTable, evt.getX(), evt.getY());
                }
                if(evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1){
                    EditBookDlg diag = openEditBookDlg(getMTitle(), getAuthor());
                    editBook(diag,BookManager.this);
                }
            }
        });
        AddBookBtn.addActionListener((ActionEvent evt) -> {
            setNameOfImage("");
            AddBookDlg diag = openAddBookDlg();
            addBook(diag,this);
        });
        m_cut.addActionListener((ActionEvent evt) -> deleteBook(this));
        m_edit.addActionListener((ActionEvent evt) -> {
            EditBookDlg diag = openEditBookDlg(getMTitle(), getAuthor());
            editBook(diag,this);
        });
        m_add.addActionListener((ActionEvent evt) -> {
            AddReading diag = openAddReadingDlg(getMTitle(), getAuthor());
            addReading(diag, this);
        });
        m_openManageTags.addActionListener((ActionEvent evt)->{
            openManageTagsDlg(getMTitle(), getAuthor());
            contentPane.updateUI();
            fillBookTable(isFiltered());
            isItInFilteredBookList(this,false);
            if(isFastSearch()){
                fastSearchBook(BookFastSearch.getText());
            }
        });
        FiltersBookBtn.addActionListener((ActionEvent e)-> {
            m_filtersDiag = openFilterDlg();
            filtersBook(m_filtersDiag, this);
        });
        CancelFiltersBtn.addActionListener((ActionEvent e) -> {
            contentPane.updateUI();
            setIsFiltered(false);
            fillBookTable(isFiltered());
            isItInFilteredBookList(this,false);
            if(isFastSearch()){
                fastSearchBook(getBookFastSearch().getText());
            }
        });
        BookManageTagsBtn.addActionListener((ActionEvent e) -> {
            openManageTagsDlg();
            contentPane.updateUI();
            fillBookTable(isFiltered());
            isItInFilteredBookList(this, false);
            if(isFastSearch()){
                fastSearchBook(BookFastSearch.getText());
            }
        });
        BookFastSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                m_mainWindow.getJMenuBar().setEnabled(false);
            }
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                m_mainWindow.getJMenuBar().setEnabled(true);
            }
        });
        BookFastSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                fastSearchBook(BookFastSearch.getText());
            }
        });
    }
    public void initPopupMenu(){
        m_openManageTags = new JMenuItem("Gérer ses tags", new ImageIcon(getLogo("tag.png")));
        m_edit = new JMenuItem("Modifier", new ImageIcon(getLogo("edit.png")));
        m_cut = new JMenuItem("Supprimer", new ImageIcon(getLogo("remove.png")));
        m_add = new JMenuItem("Ajouter une lecture", new ImageIcon(getLogo("add.png")));

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        m_popup.add(m_add);
        m_popup.add(m_cut);
        m_popup.add(m_edit);
        m_popup.add(m_openManageTags);
    }
    public void initComponents(){
        //init all tables
        initTables();
        //init Popup menu
        initPopupMenu();
        //Init summuray components
        AbstractBorder roundBrd = new MyManagerRoundBorderComponents(contentPane.getBackground(),3,30,0,0,20);
        BookSummary.setBorder(roundBrd);
        BookSummary.setFont(new Font("Arial", Font.BOLD, 13));
        JSpane.setBorder(null);

        contentPane.getRootPane().setDefaultButton(CancelFiltersBtn);
    }
    public void initTables(){
        //init book table and fill it
        BooksTable = new MyManagerTable(500,455,30, 13,1, contentPane.getBackground());
        BooksJSPane.getViewport().add(BooksTable);
        //init reading table
        ReadingsTable = new MyManagerTable(500,130,30, 3,1, contentPane.getBackground());
        ReadingsJSPane.getViewport().add(ReadingsTable);
        //init bind of reading table and book table => to navigate with cross
        initBinding();
        fillBookTable(isFiltered());
    }
}
