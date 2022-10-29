package Sources;

import Sources.Dialogs.*;
import Themes.DarkTheme.DarkTheme;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Objects;

import static Sources.Common.*;
import static Sources.Dialogs.OpenDialog.*;
import static Sources.MenuBar.createMenuBar;

public class MainWindow extends JDialog {
    private JPanel contentPane;
    private JLabel PersonalNoteLabel;
    private JLabel FirstReadingLabel;
    private JLabel LastReadingLabel;
    private JPanel BookPhotoPanel;
    private JButton ManageReadingsBtn;
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
    private JScrollPane jsPane;
    private JTextField BookFastSearch;
    private final DefaultTableModel m_tableModel = new DefaultTableModel(){//Create a Jtable with the tablemodel not editable
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };
    private Statement m_statement = null;
    private static String m_title;
    private static String m_author;
    private int m_rowSelected = 0;
    final JPopupMenu m_popup;
    private FiltersDlg m_diag;
    private Tags m_tags = new Tags();
    private int counterManageReading;
    private ManageReadingDlg m_ManageReadingDiag;
    private Boolean isFiltered;
    private boolean m_isFastSearch;


    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        setIsFiltered(false);
        loadDB(isFiltered());
        setCounterManageReading(0);

        AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),3,30,0,0,20);
        BookSummary.setBorder(roundBrd);
        contentPane.getRootPane().setDefaultButton(CancelFiltersBtn);
        JSpane.setBorder(null);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem add = new JMenuItem("Ajouter une lecture", new ImageIcon(getImageAdd()));
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));

        JMenu manage = new JMenu("Gérer");
        JMenuItem openManageReadings = new JMenuItem("Gérer les lectures");
        JMenuItem openManageTags = new JMenuItem("Gérer ses tags");
        manage.add(openManageReadings);
        manage.add(openManageTags);

        m_popup.add(add);
        m_popup.add(cut);
        m_popup.add(edit);
        m_popup.add(manage);

        if(BooksTable.getRowCount() != 0) {//Vérif if the table is not empty; when starting the app, load and focus on the first book of the table
            setMTitle(BooksTable.getValueAt(0, 0).toString());
            setAuthor(BooksTable.getValueAt(0, 1).toString());
            loadComponents(getMTitle(), getAuthor());
            setJMenuBar(createMenuBar(this,getMTitle(),getAuthor()));

            BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(), getAuthor()), getRowSelected(getMTitle(), getAuthor()));
            ManageReadingsBtn.setEnabled(true);
            FiltersBookBtn.setEnabled(true);
        }else{
            BookManageTagsBtn.setEnabled(false);
            ManageReadingsBtn.setEnabled(false);
            FiltersBookBtn.setEnabled(false);
        }

        BooksTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {//set main UI when we clicked on an element of the array, retrieved from the db
                super.mouseReleased(evt);
                ManageReadingsBtn.setEnabled(true);
                FiltersBookBtn.setEnabled(true);
                setRowSelected(BooksTable.rowAtPoint(evt.getPoint()));
                setMTitle(BooksTable.getValueAt(getRowSelected(), 0).toString()); //get the value of the column of the table
                setAuthor(BooksTable.getValueAt(getRowSelected(), 1).toString());
                if(m_ManageReadingDiag!=null){
                    m_ManageReadingDiag.fillBookList(getMTitle(),getAuthor());
                }
                loadComponents(getMTitle(), getAuthor());
                if(evt.getButton() == MouseEvent.BUTTON3) {//if we right click show a popup to edit the book
                    BooksTable.setRowSelectionInterval(getRowSelected(), getRowSelected());//we focus the row when we right on the item
                    m_popup.show(BooksTable, evt.getX(), evt.getY());
                }
                if(evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1){
                    if(getCounterManageReading()<1){
                        m_ManageReadingDiag=openManageReadingDlg(MainWindow.this,  getMTitle(), getAuthor());
                        setCounterManageReading(1);
                    }
                }
            }
        });
        AddBookBtn.addActionListener((ActionEvent evt) -> {
            setNameOfBook("");

            AddBookDlg diag = openAddBookDlg();
            if (diag.isValide()){
                String BookQry = "INSERT INTO Book (Title,Author,Image,NumberOP,NotePerso,NoteBabelio,ReleaseYear,Summary) " +
                        "VALUES (?,?,?,?,?,?,?,?);";
                String ReadingQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                        "VALUES (?,?,?,?,?);";
                String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                        "VALUES (?,?);";
                String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=?"+
                        "WHERE Title='"+diag.getNewBookTitle()+"' AND Author='"+diag.getNewBookAuthor()+"'";
                contentPane.updateUI();
                try (Connection conn = connect(); PreparedStatement BookPstmt = conn.prepareStatement(BookQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry);
                     PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry)) {
                    m_statement = conn.createStatement();

                    ReadingPstmt.setInt(1, getIdReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                    ReadingPstmt.setString(2, diag.getNewBookTitle());
                    ReadingPstmt.setString(3, diag.getNewBookAuthor());

                    BookPstmt.setString(1, diag.getNewBookTitle());
                    BookPstmt.setString(2, diag.getNewBookAuthor());
                    BookPstmt.setString(3, getNameOfBook());
                    BookPstmt.setString(4, diag.getNewBookNumberOP());
                    BookPstmt.setString(5, diag.getNewBookPersonalNote());
                    BookPstmt.setString(6, diag.getNewBookBBLNote());
                    BookPstmt.setString(7, diag.getNewBookReleaseYear());
                    BookPstmt.setString(8, diag.getNewBookSummary());

                    if(!diag.isDateUnknown() && !diag.isNotDOne()){
                        ReadingPstmt.setString(4, diag.getNewBookStartReading());
                        ReadingPstmt.setString(5, diag.getNewBookEndReading());

                    } else if (!diag.isDateUnknown() && diag.isNotDOne()) {
                        ReadingPstmt.setString(4, diag.getNewBookStartReading());
                        ReadingPstmt.setString(5, "Pas fini");
                    } else {
                        ReadingPstmt.setInt(1, getIdReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                        ReadingPstmt.setString(4, "Inconnu");
                        ReadingPstmt.setString(5, "Inconnu");
                    }
                    BookPstmt.executeUpdate();//Insert the new Book in table Book
                    ReadingPstmt.executeUpdate();//Insert the new reading

                    AvNumPstmt.setInt(1, averageTime(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                    AvNumPstmt.setInt(2, getNumberOfReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                    AvNumPstmt.executeUpdate();

                    for(int i=0; i<diag.getTags().getSizeTags(); i++){
                        if(diag.getTagIsUpdate()){
                            String TagsUpdateQry = "UPDATE Tags SET Color=?"+
                                    "WHERE Tag='"+diag.getTags().getTag(i).getTextTag()+"'";
                            PreparedStatement TagsUpdatePstmt = conn.prepareStatement(TagsUpdateQry);
                            TagsUpdatePstmt.setInt(1, diag.getTags().getTag(i).getColor());
                            TagsUpdatePstmt.executeUpdate();
                        }

                        String TagsInsertQry = "INSERT INTO Tags (Tag,Color)" +
                                " SELECT '"+ diag.getTags().getTag(i).getTextTag() +"', '"+diag.getTags().getTag(i).getColor()+"'" +
                                " WHERE NOT EXISTS(SELECT * FROM Tags WHERE Tag='" + diag.getTags().getTag(i).getTextTag() + "' AND Color='" + diag.getTags().getTag(i).getColor() + "')";
                        PreparedStatement TagsInsertPstmt = conn.prepareStatement(TagsInsertQry);
                        TagsInsertPstmt.executeUpdate();

                        TaggingPstmt.setInt(1, getIdBook(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                        TaggingPstmt.setInt(2, getIdTag(diag.getTags().getTag(i).getTextTag(), diag.getTags().getTag(i).getColor()));
                        TaggingPstmt.executeUpdate();
                    }

                    loadDB(isFiltered());
                    setMTitle(diag.getNewBookTitle());
                    setAuthor(diag.getNewBookAuthor());
                    isItInFilteredBookList();
                    if(isFastSearch()){
                        fastSearchBook(BookFastSearch.getText());
                    }
                    conn.close();
                    m_statement.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        ManageReadingsBtn.addActionListener((ActionEvent e) ->{
            if(getCounterManageReading()<1){
                setCounterManageReading(1);
                m_ManageReadingDiag= openManageReadingDlg(this, getMTitle(),getAuthor());
            }
        });
        cut.addActionListener((ActionEvent evt) -> {
            JFrame jFrame = new JFrame();
            int n = JOptionPane.showConfirmDialog(//Open a optionPane to verify if the user really want to delete the book return 0 il they want and 1 if they refuse
                    jFrame,
                    "Etes-vous sûr de vouloir supprimer définitivement le livre ?\n"+"Cette acion sera irréversible !",
                    "An Inane Question",
                    JOptionPane.YES_NO_OPTION);
            if(n == 0){
                String boolQry = "DELETE FROM Book WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";//sql to delete the book in table book when we right click
                String ReadingQry = "DELETE FROM Reading WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
                String TaggingQry = "DELETE FROM Tagging WHERE IdBook='"+getIdBook(getMTitle(),getAuthor())+"'";

                deleteImageMainResource(getMTitle(), getAuthor());
                try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(boolQry); PreparedStatement pstmt2 = conn.prepareStatement(ReadingQry);
                     PreparedStatement taggingPstmt = conn.prepareStatement(TaggingQry)) {
                    // execute the delete statement
                    pstmt.executeUpdate();
                    pstmt2.executeUpdate();
                    taggingPstmt.executeUpdate();
                    loadDB(isFiltered());
                    isNotInFilteredBookList();
                    if(isFastSearch()){
                        fastSearchBook(BookFastSearch.getText());
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        edit.addActionListener((ActionEvent evt) -> {
            EditBookDlg diag = openEditBookDlg(getMTitle(),getAuthor());
            if (diag.isValid()){
                String BookQry = "UPDATE Book SET Title=?, Author=?, Image=?, NumberOP=?, NotePerso=?, NoteBabelio=?, ReleaseYear=?, Summary=?"+
                        "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";//Edit in bdd the book that we want to change
                String ReadingQry = "UPDATE Reading SET Title=?, Author=?"+
                        "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";//Edit in bdd the book that we want to change
                String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                        "VALUES (?,?);";
                String DeleteTaggingQry = "DELETE FROM Tagging WHERE IdBook='"+getIdBook(getMTitle(),getAuthor())+"'";
                try (Connection conn = connect(); PreparedStatement BookPstmt = conn.prepareStatement(BookQry); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry);
                     PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry); PreparedStatement DeleteTaggingPstmt = conn.prepareStatement(DeleteTaggingQry)) {
                    // execute the uptdate statement
                    ReadingPstmt.setString(1, diag.getNewTitle());
                    ReadingPstmt.setString(2, diag.getNewAuthor());
                    BookPstmt.setString(1, diag.getNewTitle());
                    BookPstmt.setString(2, diag.getNewAuthor());
                    BookPstmt.setString(3, getNameOfBook());
                    BookPstmt.setString(4, diag.getNewNumberPage());
                    BookPstmt.setString(5, diag.getNewPersonnalNote());
                    BookPstmt.setString(6, diag.getNewBBLNote());
                    BookPstmt.setString(7, diag.getNewReleaseyear());
                    BookPstmt.setString(8, diag.getNewSummary());
                    BookPstmt.executeUpdate();
                    ReadingPstmt.executeUpdate();
                    DeleteTaggingPstmt.executeUpdate();

                    for(int i=0; i<diag.getTags().getSizeTags(); i++){
                        if(diag.getTagIsUpdate()){
                            String TagsUpdateQry = "UPDATE Tags SET Color=?"+
                                    "WHERE Tag='"+diag.getTags().getTag(i).getTextTag()+"'";
                            PreparedStatement TagsUpdatePstmt = conn.prepareStatement(TagsUpdateQry);
                            TagsUpdatePstmt.setInt(1, diag.getTags().getTag(i).getColor());
                            TagsUpdatePstmt.executeUpdate();
                        }

                        String TagsInsertQry = "INSERT INTO Tags (Tag,Color)" +
                                " SELECT '"+ diag.getTags().getTag(i).getTextTag() +"', '"+diag.getTags().getTag(i).getColor()+"'" +
                                " WHERE NOT EXISTS(SELECT * FROM Tags WHERE Tag='" + diag.getTags().getTag(i).getTextTag() + "' AND Color='" + diag.getTags().getTag(i).getColor() + "')";
                        PreparedStatement TagsInsertPstmt = conn.prepareStatement(TagsInsertQry);
                        TagsInsertPstmt.executeUpdate();

                        TaggingPstmt.setInt(1, getIdBook(diag.getNewTitle(), diag.getNewAuthor()));
                        TaggingPstmt.setInt(2, getIdTag(diag.getTags().getTag(i).getTextTag(), diag.getTags().getTag(i).getColor()));
                        TaggingPstmt.executeUpdate();
                    }

                    contentPane.updateUI();
                    loadDB(isFiltered());
                    setMTitle(diag.getNewTitle());
                    setAuthor(diag.getNewAuthor());
                    isItInFilteredBookList();
                    if(isFastSearch()){
                        fastSearchBook(BookFastSearch.getText());
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        add.addActionListener((ActionEvent evt) -> {
            AddReading diag = openAddReadingDlg(getMTitle(),getAuthor());

            if (diag.getIsValid()){
                String ReadingQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                        "VALUES (?,?,?,?,?);";
                String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=?"+
                        "WHERE Title='"+diag.getMtitle()+"' AND Author='"+diag.getAuthor()+"'";
                contentPane.updateUI();
                try(Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)){
                    m_statement = conn.createStatement();
                    ReadingPstmt.setInt(1, getIdReading(diag.getMtitle(), diag.getAuthor()));
                    ReadingPstmt.setString(2, diag.getMtitle());
                    ReadingPstmt.setString(3, diag.getAuthor());

                    if(!diag.isDateUnknown()&& !diag.isNotDone()){
                        ReadingPstmt.setString(4, diag.getNewStartReading());
                        ReadingPstmt.setString(5, diag.getNewEndReading());
                    }else if (!diag.isDateUnknown() && diag.isNotDone()) {
                        ReadingPstmt.setString(4, diag.getNewStartReading());
                        ReadingPstmt.setString(5, "Pas fini");
                    }
                    else {
                        ReadingPstmt.setString(4, "Inconnu");
                        ReadingPstmt.setString(5, "Inconnu");
                    }
                    ReadingPstmt.executeUpdate();//Insert the new reading

                    AvNumPstmt.setInt(1, averageTime(diag.getMtitle(), diag.getAuthor()));
                    AvNumPstmt.setInt(2, getNumberOfReading(diag.getMtitle(), diag.getAuthor()));
                    AvNumPstmt.executeUpdate();


                    setMTitle(diag.getMtitle());
                    setAuthor(diag.getAuthor());
                    loadComponents(diag.getMtitle(), diag.getAuthor());
                    loadDB(isFiltered());
                    //Focus in the jtable on a reading created from an existing book
                    BooksTable.setRowSelectionInterval(getRowSelected(diag.getMtitle(), diag.getAuthor()), getRowSelected(diag.getMtitle(), diag.getAuthor()));
                    if (getCounterManageReading() > 0)
                        this.m_ManageReadingDiag.fillBookList(getMTitle(), getAuthor());
                    if(isFastSearch()){
                        fastSearchBook(BookFastSearch.getText());
                    }
                    conn.close();
                    m_statement.close();
                }catch (SQLException e){
                    System.out.println(e.getMessage());
                    System.exit(0);
                }
            }
        });
        openManageReadings.addActionListener((ActionEvent evt)->{
            if(getCounterManageReading()<1){
                setCounterManageReading(1);
                m_ManageReadingDiag= openManageReadingDlg(this, getMTitle(),getAuthor());
            }
        });
        openManageTags.addActionListener((ActionEvent evt)->{
            openManageTagsDlg(getMTitle(), getAuthor());
            contentPane.updateUI();
            loadDB(isFiltered());
            isItInFilteredBookList();
            if(isFastSearch()){
                fastSearchBook(BookFastSearch.getText());
            }
        });
        FiltersBookBtn.addActionListener((ActionEvent e)-> {
            m_diag = openFilterDlg();
            if(isFiltered()){
                loadDB(isFiltered());
            }
            else{
                loadDB(m_diag.getIsValid());
                setIsFiltered(m_diag.getIsValid());
            }
            if(BooksTable.getRowCount()>0){
                setMTitle(BooksTable.getValueAt(0, 0).toString());
                setAuthor(BooksTable.getValueAt(0, 1).toString());
                loadComponents(getMTitle(), getAuthor());
                BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()), getRowSelected(getMTitle(),getAuthor()));
                if(m_ManageReadingDiag!=null){
                    m_ManageReadingDiag.fillBookList(getMTitle(),getAuthor());
                }
            }
            else
                initComponents();
            if(isFastSearch()){
                fastSearchBook(BookFastSearch.getText());
            }
            contentPane.updateUI();
        });
        CancelFiltersBtn.addActionListener((ActionEvent e) -> {
            contentPane.updateUI();
            setIsFiltered(false);
            loadDB(isFiltered());
            setMTitle(BooksTable.getValueAt(0, 0).toString());
            setAuthor(BooksTable.getValueAt(0, 1).toString());
            loadComponents(getMTitle(), getAuthor());
            BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()), getRowSelected(getMTitle(),getAuthor()));
            if(m_ManageReadingDiag!=null){
                m_ManageReadingDiag.fillBookList(getMTitle(),getAuthor());
            }
            if(isFastSearch()){
                fastSearchBook(BookFastSearch.getText());
            }
        });
        BookManageTagsBtn.addActionListener((ActionEvent e) -> {
            openManageTagsDlg();
            contentPane.updateUI();
            loadDB(isFiltered());
            isItInFilteredBookList();
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
    public ManageReadingDlg getManageReadingDiag(){
        return this.m_ManageReadingDiag;
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
    public int getRowSelected(String title, String author){//return the row find by a title and an author
        FiltersBookBtn.setEnabled(true);
        ManageReadingsBtn.setEnabled(true);
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
    public int getCounterManageReading() {
        return counterManageReading;
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

    /****************************** Void ***********************************/
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
            System.exit(0);
        }
    }
    public void loadDB(boolean isFiltered){
        m_tableModel.setRowCount(0);
        CancelFiltersBtn.setEnabled(isFiltered);
        try(Connection conn = connect()){
            m_statement = conn.createStatement();
            ResultSet rs;
            if(isFiltered){
                String qry = "SELECT Book.Title, Book.Author FROM Book " +
                        "INNER JOIN Reading ON Reading.Title=Book.Title AND Reading.Author=Book.Author ";

                if (!m_diag.getTextTag().equals("")) {
                    qry = qry+
                            "INNER JOIN Tagging ON Book.ID=Tagging.IdBook " +
                            "INNER JOIN Tags ON Tagging.idTag=Tags.ID " +
                            "WHERE Tags.Tag='" + m_diag.getTextTag() + "' "+
                            "AND Book.Title LIKE '%" + m_diag.getMTitle() + "%'";
                }else {
                    qry = qry + "WHERE Book.Title LIKE '%" + m_diag.getMTitle() + "%'";
                }
                qry = qry +
                        "AND Book.Author LIKE '%" + m_diag.getAuthor() + "%'" +
                        "AND Book.ReleaseYear BETWEEN '" + m_diag.getFirstDatRelease() + "' AND '" + m_diag.getLastDateRelease() + "'" +
                        "AND Book.NotePerso BETWEEN '" + m_diag.getFirstNote() + "' AND '" + m_diag.getLastNote() + "'"+
                        "AND Book.NumberOP BETWEEN '" + m_diag.getFirstNumberOP() + "' AND '" + m_diag.getLastNumberOP() + "'"+
                        "AND Book.NumberReading BETWEEN '" + m_diag.getFirstNumberOR() + "' AND '" + m_diag.getLastNumberOR() + "'"+
                        "AND Book.AvReadingTime BETWEEN '" + m_diag.getFirstAvTime() + "' AND '" + m_diag.getLastAvTime() + "'"+
                        "AND Book.NoteBabelio BETWEEN '" + m_diag.getFirstNoteBB() + "' AND '" + m_diag.getLastNoteBB() + "'";
                if(m_diag.isFiltered()){
                    qry = qry +
                            "AND Reading.StartReading BETWEEN '" + m_diag.getFirstStartDate() + "' AND '" + m_diag.getLastStartDate() + "'"+
                            "AND Reading.EndReading BETWEEN '" + m_diag.getFirstEndDate() + "' AND '" + m_diag.getLastEndDate() + "'";
                }

                if(!m_diag.getTextSort().equals("EndReading") && !m_diag.getTextSort().equals("StartReading")) {
                    if(m_diag.isAscending()){
                        qry = qry+" GROUP BY Book.Title ORDER BY Book."+m_diag.getTextSort()+" ASC;";
                    }else{
                        qry = qry+" GROUP BY Book.Title ORDER BY Book."+m_diag.getTextSort()+" DESC;";
                    }
                }else{
                    if(m_diag.isAscending()){
                        qry = qry+" GROUP BY Book.Title ORDER BY Reading."+m_diag.getTextSort()+" ASC;";
                    }else{
                        qry = qry+" GROUP BY Book.Title ORDER BY Reading."+m_diag.getTextSort()+" DESC;";
                    }
                }

                rs = m_statement.executeQuery(qry);
            } else{
                rs = m_statement.executeQuery("SELECT * FROM Book ORDER BY Title ASC;");//Execute a Query to retrieve all the values from the database by grouping the duplicates
            }
            // (with their name and author)
            while (rs.next()) {//Fill in the table of the list of Book
                String title = rs.getString("Title");//Retrieve the title
                String author = rs.getString("Author");//Retrieve the author

                String[] header = {"Titre","Auteur"};
                Object[] data = {title, author};

                m_tableModel.setColumnIdentifiers(header);//Create the header
                m_tableModel.addRow(data);//add to tablemodel the data
            }
            BooksTable.setModel(m_tableModel);
            jsPane.setPreferredSize(new Dimension(470,635));

            AbstractBorder roundBrdMax = new RoundBorderCp(contentPane.getBackground(),1,30, 0,0,0);
            AbstractBorder roundBrdMin = new RoundBorderCp(contentPane.getBackground(),1,30, 592-(BooksTable.getRowCount()*BooksTable.getRowHeight()),11,0);
            if(BooksTable.getRowCount()>20)
                BooksTable.setBorder(roundBrdMax);
            else
                BooksTable.setBorder(roundBrdMin);

            rs.close();
            conn.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void loadComponents(String title, String author){
        ManageReadingsBtn.setEnabled(true);
        Tags tags = new Tags();
        try(Connection conn = connect()) {
            Class.forName("org.sqlite.JDBC");
            m_statement = conn.createStatement();

            //Title label
            ResultSet titleQry = m_statement.executeQuery("SELECT Title FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            TitleLabel.setText(titleQry.getString(1));

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
            NoteLabel.setText("Note : "+NoteBBQry.getString(1));

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

            //Image
            ResultSet ImageQry = m_statement.executeQuery("SELECT Image FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            addImageToPanel(ImageQry.getString(1),BookPhotoPanel);

            setJMenuBar(createMenuBar(this, getMTitle(), getAuthor()));

            conn.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void initComponents(){
        TitleLabel.setText("Titre livre");
        ReleaseYearLAbel.setText("Année de sortie :");
        NumberPageLabel.setText("Nombre de page :");
        BookTimeAverageLabel.setText("Temps moyen de lecture : ");
        CountReadingLabel.setText("Nombre de lecture :");
        NoteLabel.setText("Note :");
        PersonalNoteLabel.setText("Ma note :");
        FirstReadingLabel.setText("Première lecture :");
        LastReadingLabel.setText("Dernière lecture :");
        BookSummary.setText("");
        BookTagsPanel.removeAll();
        ManageReadingsBtn.setEnabled(false);
        BookPhotoPanel.removeAll();
        contentPane.updateUI();
        contentPane.setBorder(null);
    }

    public void setManageReading(ManageReadingDlg manageReadingDiag) {
        this.m_ManageReadingDiag = manageReadingDiag;
    }
    public void setCounterManageReading(int counterManageReading) {
        this.counterManageReading = this.counterManageReading+counterManageReading;
    }
    public void resetCounterManageReading(int counterManageReading) {
        this.counterManageReading = counterManageReading;
    }
    public void setIsFiltered(Boolean filtered) {
        isFiltered = filtered;
    }
    public void fastSearchBook(String text){
        loadDB(isFiltered());
        setFastSearch(true);
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
                m_tableModel.removeRow(row);
                BooksTable.updateUI();
                row--;
            }
        }
        AbstractBorder roundBrdMax = new RoundBorderCp(contentPane.getBackground(),1,30, 0,0,0);
        AbstractBorder roundBrdMin = new RoundBorderCp(contentPane.getBackground(),1,30, 592-(BooksTable.getRowCount()*BooksTable.getRowHeight()),11,0);
        if(BooksTable.getRowCount()>20)
            BooksTable.setBorder(roundBrdMax);
        else
            BooksTable.setBorder(roundBrdMin);
        isNotInFilteredBookList();
    }
    public void isNotInFilteredBookList(){
        if (getBooksTable().getRowCount() > 0) {
            setMTitle(getBooksTable().getValueAt(0, 0).toString());
            setAuthor(getBooksTable().getValueAt(0, 1).toString());
            loadComponents(getMTitle(), getAuthor());//reload changes made to the book
            getBooksTable().setRowSelectionInterval(0, 0);
            if (getCounterManageReading() > 0)
                this.m_ManageReadingDiag.fillBookList(getMTitle(), getAuthor());
        } else {
            if (this.m_ManageReadingDiag != null) {
                getManageReadingDiag().setVisible(false);
                getManageReadingDiag().dispose();
                resetCounterManageReading(0);
            }
            initComponents();
        }
    }
    public void isItInFilteredBookList(){
        if(isFiltered()){
            if(isInFilteredList(getMTitle(),getAuthor(), getBooksTable())){
                loadComponents(getMTitle(), getAuthor());//reload changes made to the book
                getBooksTable().setRowSelectionInterval(getRowSelected(getMTitle(), getAuthor()), getRowSelected(getMTitle(), getAuthor()));//focus on the edited book
                if(getCounterManageReading()>0)
                    this.m_ManageReadingDiag.fillBookList(getMTitle(), getAuthor());
            }else{
                isNotInFilteredBookList();
            }
        }else{
            loadComponents(getMTitle(), getAuthor());//reload changes made to the book
            getBooksTable().setRowSelectionInterval(getRowSelected(getMTitle(), getAuthor()), getRowSelected(getMTitle(), getAuthor()));//focus on the edited book
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new DarkTheme());
        }catch( Exception ex ) {
            System.err.println( "Failed to load darkTheme" );
        }

        MainWindow parent = new MainWindow();
        parent.setTitle("Book manager");
        parent.setSize(1350,760);
        parent.setLocationRelativeTo(null);
        parent.setVisible(true);
        System.exit(0);
    }
}

