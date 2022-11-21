package Sources;

import Sources.Dialogs.*;
import Themes.DarkTheme.DarkTheme;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static Sources.Common.*;
import static Sources.CommonSQL.*;
import static Sources.Dialogs.OpenDialog.*;
import static Sources.MenuBar.createMenuBar;

public class MainWindow extends JDialog {
    private JPanel contentPane;
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
    private JTable BooksTable;
    private JTextField BookFastSearch;
    private JTable ReadingsTable;
    private JLabel CountBookLbl;
    private final DefaultTableModel m_tableBookModel = new DefaultTableModel(){//Create a Jtable with the tablemodel not editable
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };
    private final DefaultTableModel m_tableReadingModel = new DefaultTableModel(){//Create a Jtable with the tablemodel not editable
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };
    private Statement m_statement = null;
    private static String m_title;
    private static String m_author;
    private int m_rowSelected = 0;
    final JPopupMenu m_popup;
    private FiltersDlg m_filtersDiag;
    private Tags m_tags = new Tags();
    private Boolean isFiltered;
    private boolean m_isFastSearch;
    private ManageReading m_manageReading;
    private int rowReading;


    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        setIsFiltered(false);
        fillBookTable(isFiltered());

        AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),3,30,0,0,20);
        BookSummary.setBorder(roundBrd);
        BookSummary.setFont(new Font("Arial", Font.BOLD, 13));
        contentPane.getRootPane().setDefaultButton(CancelFiltersBtn);
        JSpane.setBorder(null);

        BooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ReadingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem add = new JMenuItem("Ajouter une lecture", new ImageIcon(getImageAdd()));
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));

        JMenuItem openManageTags = new JMenuItem("Gérer ses tags");

        m_popup.add(add);
        m_popup.add(cut);
        m_popup.add(edit);
        m_popup.add(openManageTags);
        setJMenuBar(createMenuBar(this));

        if(BooksTable.getRowCount() != 0) {//Vérif if the table is not empty; when starting the app, load and focus on the first book of the table

            setMTitle(BooksTable.getValueAt(0, 0).toString());
            setAuthor(BooksTable.getValueAt(0, 1).toString());
            setRowReading(0);
            setRowSelected(0);
            loadComponents(getMTitle(), getAuthor());

            BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
            BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
            BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "dow");
            BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
            BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "tab");
            BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "addReading");
            BooksTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "manageTags");


            BooksTable.getActionMap().put("delete", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    deleteBook(MainWindow.this);
                }
            });
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
            BooksTable.getActionMap().put("enter", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    EditBookDlg diag = openEditBookDlg();
                    editBook(diag,MainWindow.this);
                }
            });
            BooksTable.getActionMap().put("addReading", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    AddReading diag = openAddReadingDlg();
                    addReading(diag, MainWindow.this);
                }
            });
            BooksTable.getActionMap().put("manageTags", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    openManageTagsDlg(getMTitle(), getAuthor());
                    contentPane.updateUI();
                    fillBookTable(isFiltered());
                    isItInFilteredBookList(MainWindow.this,false);
                    if(isFastSearch()){
                        fastSearchBook(BookFastSearch.getText());
                    }
                }
            });

            FiltersBookBtn.setEnabled(true);
        }else{
            FiltersBookBtn.setEnabled(false);
            resetApp(this, false);
        }

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
                EditBookDlg diag = openEditBookDlg();
                editBook(diag,MainWindow.this);
            }
            }
        });
        AddBookBtn.addActionListener((ActionEvent evt) -> {
            setNameOfImage("");
            AddBookDlg diag = openAddBookDlg();
            addBook(diag,this);
        });
        cut.addActionListener((ActionEvent evt) -> deleteBook(this));
        edit.addActionListener((ActionEvent evt) -> {
            EditBookDlg diag = openEditBookDlg();
            editBook(diag,this);
        });
        add.addActionListener((ActionEvent evt) -> {
            AddReading diag = openAddReadingDlg();
            addReading(diag, this);
        });
        openManageTags.addActionListener((ActionEvent evt)->{
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
        BookFastSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                fastSearchBook(BookFastSearch.getText());
            }
        });
    }

    /****************************** Get ***********************************/
    public Tags getTags(){
        return this.m_tags;
    }
    public static String getMTitle(){
        return m_title;
    }
    public static String getAuthor(){
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
    public JPanel getContentPanel(){
        return this.contentPane;
    }
    public JTable getReadingsTable() {
        return ReadingsTable;
    }
    public ManageReading getManageReading() {
        return m_manageReading;
    }

    /****************************** Void ***********************************/
    public void setDiagFilters(FiltersDlg dlg){
        this.m_filtersDiag=dlg;
    }
    public void setFastSearch(boolean fast){
        this.m_isFastSearch=fast;
    }
    public void setTags(Tags tags){
        this.m_tags=tags;
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
    public void connectionDB(){
        try (Connection conn = connect()) {
            Class.forName("org.sqlite.JDBC");
            m_statement = conn.createStatement();

            String BookSql = "CREATE TABLE IF NOT EXISTS Book" +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " Title TEXT, " +
                    " Author TEXT, " +
                    " Image TEXT, " +
                    " NumberOP INT, " +
                    " NotePerso INT, " +
                    " NoteBabelio INT, " +
                    " ReleaseYear TEXT, " +
                    " AvReadingTime INT, " +
                    " NumberReading INT, " +
                    " Summary TEXT)";

            String ReadSql = "CREATE TABLE IF NOT EXISTS Reading" +
                    "(ID INT, " +
                    " Title TEXT, " +
                    " Author TEXT, " +
                    " StartReading TEXT, " +
                    " EndReading TEXT)";

            String TagsSql = "CREATE TABLE IF NOT EXISTS Tags" +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " Tag TEXT, " +
                    " Color INT)";

            String TaggingSql = "CREATE TABLE IF NOT EXISTS Tagging" +
                    "(IdBook INT, " +
                    " IdTag INT)";

            m_statement.executeUpdate(BookSql);//Create the book table
            m_statement.executeUpdate(ReadSql);//Create the reading table
            m_statement.executeUpdate(TagsSql);//Create the tags table
            m_statement.executeUpdate(TaggingSql);//Create the tagging table

            conn.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    public void fillBookTable(boolean isFiltered){
        m_tableBookModel.setRowCount(0);
        CancelFiltersBtn.setEnabled(isFiltered);
        try(Connection conn = connect()){
            m_statement = conn.createStatement();
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

                rs = m_statement.executeQuery(qry.toString());
            } else{
                rs = m_statement.executeQuery("SELECT * FROM Book;");//Execute a Query to retrieve all the values from the database by grouping the duplicates
            }
            // (with their name and author)
            while (rs.next()) {//Fill in the table of the list of Book
                String title = rs.getString("Title");//Retrieve the title
                String author = rs.getString("Author");//Retrieve the author

                String[] header = {"Titre","Auteur"};
                if(title.contains("''")){
                    title = title.replace("''","'");
                }
                Object[] data = {title, author};

                m_tableBookModel.setColumnIdentifiers(header);//Create the header
                m_tableBookModel.addRow(data);//add to tablemodel the data
            }
            BooksTable.setModel(m_tableBookModel);

            AbstractBorder roundBrdMax = new RoundBorderCp(contentPane.getBackground(),1,30, 0,0,0);
            AbstractBorder roundBrdMin = new RoundBorderCp(contentPane.getBackground(),1,30, BooksTable.getPreferredScrollableViewportSize().height-(BooksTable.getRowCount()*BooksTable.getRowHeight()),0,0);
            if(BooksTable.getRowCount()>13)
                BooksTable.setBorder(roundBrdMax);
            else
                BooksTable.setBorder(roundBrdMin);

            if(BooksTable.getRowCount()>0) {
                BooksTable.getTableHeader().setResizingAllowed(false);
                BooksTable.getColumnModel().getColumn(0).setPreferredWidth((int) ((BooksTable.getPreferredScrollableViewportSize().width+1) / 1.5));
                BooksTable.getColumnModel().getColumn(1).setPreferredWidth((BooksTable.getPreferredScrollableViewportSize().width) / 3);
            }

            rs.close();
            conn.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    public void fillReadingTable(String title, String author) {
        m_tableReadingModel.setRowCount(0);
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
                    days = ChronoUnit.DAYS.between(start, stop);
                    StdDays = days + " jours";
                }

                String[] header = {"Début de lecture", "Fin de lecture", "Temps de lecture"};
                Object[] data = {startReading, endReading, StdDays};

                m_tableReadingModel.setColumnIdentifiers(header);//Create the header
                m_tableReadingModel.addRow(data);//add to tablemodel the data
            }
            ReadingsTable.setModel(m_tableReadingModel);
            AbstractBorder roundBrdMax = new RoundBorderCp(contentPane.getBackground(), 1, 30, 0, 0, 0);
            AbstractBorder roundBrdMin = new RoundBorderCp(contentPane.getBackground(), 1, 30, ReadingsTable.getPreferredScrollableViewportSize().height - (ReadingsTable.getRowCount() * ReadingsTable.getRowHeight()), 0, 0);
            if (ReadingsTable.getRowCount() > 3)
                ReadingsTable.setBorder(roundBrdMax);
            else
                ReadingsTable.setBorder(roundBrdMin);

            contentPane.updateUI();
            qry.close();
            conn.close();
            m_statement.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
    public void loadComponents(String title, String author){
        Tags tags = new Tags();
        fillReadingTable(title,author);
        m_manageReading = new ManageReading(MainWindow.this, ReadingsTable);
        ReadingsTable.setRowSelectionInterval(getRowReading(),getRowReading());
        BooksTable.setRowSelectionInterval(getRowSelected(), getRowSelected());
        FiltersBookBtn.setEnabled(true);
        BookManageTagsBtn.setEnabled(true);
        try(Connection conn = connect()) {
            Class.forName("org.sqlite.JDBC");
            m_statement = conn.createStatement();

            //Title label
            if(title.contains("''''")){
                String newTitle = title.replace("''''", "'");
                TitleLabel.setText(newTitle);
            }else{
                TitleLabel.setText(title);
            }

            //Tags Label
            ResultSet tagsQry = m_statement.executeQuery("SELECT Tag,Color FROM Tags JOIN Tagging on Tags.ID=Tagging.IdTag " +
                    "WHERE Tagging.IdBook='"+getIdBook(title, author)+"' ORDER BY Tag ASC");
            BookTagsPanel.removeAll();
            while (tagsQry.next()){
                tags.createTag(tagsQry.getString(1));
                tags.getTag(tagsQry.getRow()-1).setColor(tagsQry.getInt(2));
                setTags(tags);
                for(int i=0; i<tags.getSizeTags();i++) {
                    BookTagsPanel.add(tags.getTag(i));
                }
            }
            BookTagsPanel.updateUI();

            //Release year label
            ResultSet ReleaseYearQry = m_statement.executeQuery("SELECT ReleaseYear FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            ReleaseYearLAbel.setText("Année de sortie : "+ReleaseYearQry.getString(1));

            //Number of page label
            ResultSet NumberOPQry = m_statement.executeQuery("SELECT NumberOP FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            NumberPageLabel.setText("Nombre de page : "+NumberOPQry.getString(1));

            //Number of reading label
            ResultSet NumberReadingQry = m_statement.executeQuery("SELECT NumberReading FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            CountReadingLabel.setText("Nombre de lecture : "+NumberReadingQry.getInt(1));

            //Average time label
            ResultSet AvReadingTimeQry = m_statement.executeQuery("SELECT AvReadingTime FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            int average = AvReadingTimeQry.getInt(1);
            if(average>0){
                BookTimeAverageLabel.setText("Temps moyen de lecture : "+average+" jours");
            }
            if (average==0)
                BookTimeAverageLabel.setText("Temps moyen de lecture : Pas de moyenne possible");


            //Note on babelio
            ResultSet NoteBBQry = m_statement.executeQuery("SELECT NoteBabelio FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            NoteLabel.setText("Note Babelio : "+NoteBBQry.getString(1));

            //Summary
            ResultSet SummaryQry = m_statement.executeQuery("SELECT Summary FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            BookSummary.setText(SummaryQry.getString(1));

            //Personal note
            ResultSet NotePersoQry = m_statement.executeQuery("SELECT NotePerso FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            PersonalNoteLabel.setText("Ma note : "+NotePersoQry.getString(1));

            //First reading
            ResultSet FirstReadQry = m_statement.executeQuery("SELECT EndReading FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'ORDER BY EndReading ASC");
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
            ResultSet LastReadQry = m_statement.executeQuery("SELECT EndReading FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'ORDER BY EndReading DESC");
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
            if(getBooksTable().getRowCount()>1) {
                CountBookLbl.setText("Livres : " + getBooksTable().getRowCount());
            }else{
                CountBookLbl.setText("Livre : " + getBooksTable().getRowCount());
            }

            //Image
            ResultSet ImageQry = m_statement.executeQuery("SELECT Image FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            addImageToPanel(ImageQry.getString(1),BookPhotoPanel);

            setJMenuBar(createMenuBar(this));

            conn.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    public void initComponents(){
        m_tableReadingModel.setRowCount(0);
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
            resetApp(this, false);
        }
        contentPane.updateUI();
    }
    public void setIsFiltered(Boolean filtered) {
        isFiltered = filtered;
    }
    public void fastSearchBook(String text){
        fillBookTable(isFiltered());
        setFastSearch(!text.isBlank());
        for(int row = 0 ; row < getBooksTable().getRowCount() ; row++) {
            int cellsNotCorrespondingToFilter = 0;
            for(int column = 0 ; column < getBooksTable().getColumnCount() ; column++) {
                String cellText = getBooksTable().getModel().getValueAt(row,column).toString();
                for(int filterIndex = 0; filterIndex < text.length() ; filterIndex++) {
                    if(filterIndex<cellText.length()) {
                        if (cellText.charAt(filterIndex) != text.charAt(filterIndex)) {
                            cellsNotCorrespondingToFilter++;
                            break;
                        }
                    }else{
                        cellsNotCorrespondingToFilter++;
                        break;
                    }
                }
            }
            //If no cell in the line corresponds to the search
            if(cellsNotCorrespondingToFilter == getBooksTable().getColumnCount()) {
                m_tableBookModel.removeRow(row);
                BooksTable.updateUI();
                row--;
            }
        }

        AbstractBorder roundBrdMax = new RoundBorderCp(contentPane.getBackground(),1,30, 0,0,0);
        AbstractBorder roundBrdMin = new RoundBorderCp(contentPane.getBackground(),1,30, BooksTable.getPreferredScrollableViewportSize().height-(BooksTable.getRowCount()*BooksTable.getRowHeight()),0,0);
        if(BooksTable.getRowCount()>13)
            BooksTable.setBorder(roundBrdMax);
        else
            BooksTable.setBorder(roundBrdMin);

        if(getRowReading()+1>getReadingsTable().getRowCount()){
            setRowReading(0);
        }
        isItInFilteredBookList(this, false);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new DarkTheme());
        }catch( Exception ex ) {
            System.err.println( "Failed to load darkTheme" );
        }

        MainWindow parent = new MainWindow();
        parent.setTitle("Book manager");
        parent.setSize(1500,844);
        parent.setLocationRelativeTo(null);
        parent.setVisible(true);
        System.exit(0);
    }
    public int getRowReading() {
        return rowReading;
    }

    public void setRowReading(int rowReading) {
        this.rowReading = rowReading;
    }
}