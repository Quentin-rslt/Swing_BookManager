package Sources;

import Sources.Dialogs.*;
import Themes.DarkTheme.DarkTheme;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static Sources.Common.*;

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
    private final DefaultTableModel m_tableModel = new DefaultTableModel(){//Create a Jtable with the tablemodel not editable
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };
    private Statement m_statement = null;
    private String m_title;
    private String m_author;
    private int m_rowSelected = 0;
    final JPopupMenu m_popup;
    private FiltersDlg m_diag;



    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        loadDB(false);

        AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),3,30,0,0,20);
        BookSummary.setBorder(roundBrd);
        contentPane.getRootPane().setDefaultButton(CancelFiltersBtn);
        JSpane.setBorder(null);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem add = new JMenuItem("Ajouter une lecture", new ImageIcon(getImageAdd()));
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
        m_popup.add(add);
        m_popup.add(cut);
        m_popup.add(edit);

        if(BooksTable.getRowCount() != 0) {//Vérif if the table is not empty; when starting the app, load and focus on the first book of the table
            setMTitle(BooksTable.getValueAt(0, 0).toString());
            setAuthor(BooksTable.getValueAt(0, 1).toString());
            loadComponents(getMTitle(), getAuthor());
            BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(), getAuthor()), getRowSelected(getMTitle(), getAuthor()));
            ManageReadingsBtn.setEnabled(true);
            FiltersBookBtn.setEnabled(true);
        }else{
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
                loadComponents(getMTitle(), getAuthor());
                if(evt.getButton() == MouseEvent.BUTTON3) {//if we right click show a popup to edit the book
                    BooksTable.setRowSelectionInterval(getRowSelected(), getRowSelected());//we focus the row when we right on the item
                    m_popup.show(BooksTable, evt.getX(), evt.getY());
                }
                if(evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1){
                    ManageReadingDlg diag = new ManageReadingDlg(getMTitle(), getAuthor());
                    diag.setTitle("Gérer les lectures");
                    diag.setSize(500,570);
                    diag.setLocationRelativeTo(null);
                    diag.setVisible(true);
                    contentPane.updateUI();
                    loadDB(false);
                    if(diag.isEmpty()){
                        initComponents();
                    }
                    else {
                        BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()),getRowSelected(getMTitle(),getAuthor()));//focus on the book where you have managed your readings
                        loadComponents(getMTitle(), getAuthor());
                    }
                }
            }
        });
        AddBookBtn.addActionListener(new ActionListener() {//open the dlg for add a reading
            public void actionPerformed(ActionEvent evt) {
                setNameOfBook("");

                AddBookDlg diag = new AddBookDlg();
                diag.setTitle("Ajouter un livre");
                diag.setIconImage(getImageAdd());
                diag.setSize(900,610);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);
                if (diag.isValide()){
                    String BookQry = "INSERT INTO Book (Title,Author,Image,NumberOP,NotePerso,NoteBabelio,ReleaseYear,Summary) " +
                            "VALUES (?,?,?,?,?,?,?,?);";
                    String ReadingQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                            "VALUES (?,?,?,?,?);";
                    String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                            "VALUES (?,?);";
                    contentPane.updateUI();
                    try (Connection conn = connect(); PreparedStatement BookPstmt = conn.prepareStatement(BookQry); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry);
                         PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry)) {
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
                            ReadingPstmt.setString(5, diag.getNewBookEndReading());;
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

                        setMTitle(diag.getNewBookTitle());
                        setAuthor(diag.getNewBookAuthor());
                        loadComponents(diag.getNewBookTitle(), diag.getNewBookAuthor());
                        loadDB(false);
                        //Focus in the jtable on the book created
                        BooksTable.setRowSelectionInterval(getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()), getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()));

                        conn.close();
                        m_statement.close();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
        ManageReadingsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ManageReadingDlg diag = new ManageReadingDlg(getMTitle(), getAuthor());
                diag.setTitle("Gérer les lectures");
                diag.setSize(500,570);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);
                contentPane.updateUI();
                loadDB(false);
                if(diag.isEmpty()){
                    initComponents();
                }
                else {
                    BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()),getRowSelected(getMTitle(),getAuthor()));//focus on the book where you have managed your readings
                    loadComponents(getMTitle(), getAuthor());
                }
            }
        });
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
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

                    deleteImageMainRessource(getMTitle(), getAuthor());
                    try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(boolQry); PreparedStatement pstmt2 = conn.prepareStatement(ReadingQry);
                         PreparedStatement taggingPstmt = conn.prepareStatement(TaggingQry)) {
                        // execute the delete statement
                        pstmt.executeUpdate();
                        pstmt2.executeUpdate();
                        taggingPstmt.executeUpdate();
                        loadDB(false);
                        BooksTable.setRowSelectionInterval(0, 0);
                        setMTitle(BooksTable.getValueAt(0, 0).toString());
                        setAuthor(BooksTable.getValueAt(0, 1).toString());
                        loadComponents(getMTitle(), getAuthor());
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                EditBookDlg diag = new EditBookDlg(getMTitle(), getAuthor());
                diag.setTitle("Modifier un livre");
                diag.setIconImage(getImageEdit());
                diag.setSize(900,610);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);
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
                        loadDB(false);
                        loadComponents(diag.getNewTitle(), diag.getNewAuthor());//reload changes made to the book
                        BooksTable.setRowSelectionInterval(getRowSelected(diag.getNewTitle(), diag.getNewAuthor()), getRowSelected(diag.getNewTitle(), diag.getNewAuthor()));//focus on the edited book
                        conn.close();
                        ReadingPstmt.close();
                        BookPstmt.close();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                AddReading diag = new AddReading(getMTitle(), getAuthor());
                diag.setTitle("Ajouter une lecture");
                diag.setSize(550,250);
                File fileAdd = new File("Ressource/Icons/add.png");
                String pathAdd = fileAdd.getAbsolutePath();
                Image imgAdd = Toolkit.getDefaultToolkit().getImage(pathAdd);
                imgAdd = imgAdd.getScaledInstance(16,16,Image.SCALE_AREA_AVERAGING);
                diag.setIconImage(imgAdd);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);

                if (diag.getIsValid()){
                    String ReadingQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                            "VALUES (?,?,?,?,?);";
                    contentPane.updateUI();
                    try(Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry)){
                        m_statement = conn.createStatement();
                        ResultSet rs = m_statement.executeQuery("SELECT * FROM Book WHERE Title='"+diag.getMtitle()+"' AND Author='"+diag.getAuthor()+ "'");
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
                        setMTitle(diag.getMtitle());
                        setAuthor(diag.getAuthor());
                        loadComponents(diag.getMtitle(), diag.getAuthor());
                        loadDB(false);
                        //Focus in the jtable on a reading created from an existing book
                        BooksTable.setRowSelectionInterval(getRowSelected(diag.getMtitle(), diag.getAuthor()), getRowSelected(diag.getMtitle(), diag.getAuthor()));

                        rs.close();
                        conn.close();
                        m_statement.close();
                    }catch (SQLException e){
                        System.out.println(e.getMessage());
                        System.exit(0);
                    }
                }
            }
        });
        FiltersBookBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_diag = new FiltersDlg();
                m_diag.setTitle("Filter la liste");
                m_diag.setSize(500,230);
                m_diag.setLocationRelativeTo(null);
                m_diag.setVisible(true);
                contentPane.updateUI();
                if(m_diag.getIsValid()){
                    loadDB(true);
                    setMTitle(BooksTable.getValueAt(0, 0).toString());
                    setAuthor(BooksTable.getValueAt(0, 1).toString());
                    loadComponents(getMTitle(), getAuthor());
                    BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()), getRowSelected(getMTitle(),getAuthor()));
                }
                else{
                    loadDB(false);
                    setMTitle(BooksTable.getValueAt(0, 0).toString());
                    setAuthor(BooksTable.getValueAt(0, 1).toString());
                    loadComponents(getMTitle(), getAuthor());
                    BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()), getRowSelected(getMTitle(),getAuthor()));
                }
            }
        });
        CancelFiltersBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPane.updateUI();
                loadDB(false);
                setMTitle(BooksTable.getValueAt(0, 0).toString());
                setAuthor(BooksTable.getValueAt(0, 1).toString());
                loadComponents(getMTitle(), getAuthor());
                BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()), getRowSelected(getMTitle(),getAuthor()));
            }
        });
        BookSummary.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
            }
        });
        BookManageTagsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ManageTagsDlg diag = new ManageTagsDlg();
                diag.setTitle("Gérer les tags");
                diag.setSize(500,570);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);
                contentPane.updateUI();
                loadDB(false);
                BooksTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()),getRowSelected(getMTitle(),getAuthor()));//focus on the book where you have managed your readings
                loadComponents(getMTitle(), getAuthor());
            }
        });
    }

    public String getMTitle(){
        return m_title;
    }
    public String getAuthor(){
        return m_author;
    }
    public int getRowSelected() {
        return m_rowSelected;
    }
    public int getRowSelected(String title, String author){//return the row find by a title and an author
        FiltersBookBtn.setEnabled(true);
        ManageReadingsBtn.setEnabled(true);
        int row = 0;
        for (int i= 0; i<BooksTable.getRowCount();i++)
            if(Objects.equals(BooksTable.getValueAt(i, 0).toString(), title) && Objects.equals(BooksTable.getValueAt(i, 1).toString(), author))
                row = i;

        return row;
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
            System.out.println("Table created successfully");

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
            System.out.println("Table connexion successfully");
            ResultSet rs = null;
            if(isFiltered){
                String qry = "SELECT Title, Author FROM Book " +
                        "WHERE Title LIKE '%" + m_diag.getMTitle() + "%'"+
                        "AND Author LIKE '%" + m_diag.getAuthor() + "%'"+
                        "AND ReleaseYear BETWEEN '"+m_diag.getFirstDatRelease()+"' AND '"+m_diag.getLastDateRelease()+"'"+
                        "AND NotePerso BETWEEN '"+m_diag.getFirstNote()+"' AND '"+m_diag.getLastNote()+"';";
                rs = m_statement.executeQuery(qry);
            }
            else{
                rs = m_statement.executeQuery("SELECT * FROM Book;");//Execute a Query to retrieve all the values from the database by grouping the duplicates
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
            jsPane.setPreferredSize(new Dimension(440,585));

            AbstractBorder roundBrdMax = new RoundBorderCp(contentPane.getBackground(),1,30, 0,0,0);
            AbstractBorder roundBrdMin = new RoundBorderCp(contentPane.getBackground(),1,30, 542-(BooksTable.getRowCount()*BooksTable.getRowHeight()),11,0);
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
        Tags tags = new Tags();
        try(Connection conn = connect()) {
            Class.forName("org.sqlite.JDBC");
            m_statement = conn.createStatement();

            //Title label
            ResultSet titleQry = m_statement.executeQuery("SELECT Title FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            TitleLabel.setText(titleQry.getString(1));

            //Tags Label
            ResultSet tagsQry = m_statement.executeQuery("SELECT Tag,Color FROM Tags JOIN Tagging on Tags.ID=Tagging.IdTag " +
                    "WHERE Tagging.IdBook='"+getIdBook(title, author)+"'");
            BookTagsPanel.removeAll();
            while (tagsQry.next()){
                tags.createTag(tagsQry.getString(1));
                tags.getTag(tagsQry.getRow()-1).setColor(tagsQry.getInt(2));
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
            Statement statement = conn.createStatement();
            ResultSet CountReadingQry = statement.executeQuery("SELECT COUNT(*) FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'");
            CountReadingLabel.setText("Nombre de lecture : "+CountReadingQry.getString(1));

            //Average time lable
            String sql = "SELECT StartReading, EndReading FROM Reading WHERE Title='"+title+"' AND Author='"+author+"'";
            ResultSet qry = m_statement.executeQuery(sql);
            long days = 0;
            int dateValid = 0;
            while (qry.next()){
                if((qry.getString(1).equals("Inconnu") && qry.getString(2).equals("Inconnu")) ||
                        (qry.getString(1).equals("Inconnu") && qry.getString(2).equals("Pas fini"))){
                } else if((!qry.getString(1).equals("Inconnu") && qry.getString(2).equals("Inconnu")) ||
                        (!qry.getString(1).equals("Inconnu") && qry.getString(2).equals("Pas fini"))){
                } else if((qry.getString(1).equals("Inconnu") && !qry.getString(2).equals("Inconnu")) ||
                        ((qry.getString(1).equals("Inconnu") && !qry.getString(2).equals("Pas fini")))){
                }
                else{
                    dateValid++;
                    LocalDate start = LocalDate.parse(qry.getString(1)) ;
                    LocalDate stop = LocalDate.parse(qry.getString(2)) ;
                    days = days + ChronoUnit.DAYS.between(start , stop);
                    BookTimeAverageLabel.setText("Temps moyen de lecture : "+days/dateValid+" jours");
                }
                if (dateValid==0)
                    BookTimeAverageLabel.setText("Temps moyen de lecture : Pas de moyenne possible");
            }

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

            conn.close();
            m_statement.close();
            statement.close();
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
        FiltersBookBtn.setEnabled(false);
        BookPhotoPanel.removeAll();
        contentPane.updateUI();
        contentPane.setBorder(null);
    }

    public int getIdTag(String tag, int color) {
        int i =0;
        try (Connection conn = connect()) {
            m_statement = conn.createStatement();
            ResultSet idBook = m_statement.executeQuery("SELECT ID FROM Tags WHERE Tag='"+tag+"' AND Color='"+color+ "'");
            i=idBook.getInt(1);
            idBook.close();
            conn.close();
            m_statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return i;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new DarkTheme());
        }catch( Exception ex ) {
            System.err.println( "Failed to load darkTheme" );
        }

        MainWindow dialog = new MainWindow();
        dialog.setTitle("Book manager");
        dialog.setSize(1290,700);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        System.exit(0);
    }
}

