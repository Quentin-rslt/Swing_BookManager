package Sources;

import Sources.Dialogs.*;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class MainWindow extends JDialog {
    private JPanel contentPane;
    private JPanel LeftPanel;
    private JPanel LeftPanelSouth;
    private JLabel PersonalNoteLabel;
    private JLabel FirstReadingLabel;
    private JLabel LastReadingLabel;
    private JPanel LeftPanelWest;
    private JPanel BookPhotoPanel;
    private JPanel BookExtractPanel;
    private JButton ManageReadingsBtn;
    private JLabel TitleLabel;
    private JPanel LeftPanelCenter;
    private JPanel BookInfoGlobalPanel;
    private JLabel AuthorLabel;
    private JLabel NumberPageLabel;
    private JLabel NoteLabel;
    private JPanel BookSummaryPanel;
    private JTextPane BookSummary;
    private JLabel ExtractLabel;
    private JPanel RightPanel;
    private JPanel BookListPanel;
    private JPanel BookEditListPanel;
    private JButton AddBookBtn;
    private JButton FiltersBookBtn;
    private JButton CancelFiltersBtn;
    private JLabel CountReadingLabel;
    private JToolBar Menubar;
    private JLabel ReleaseYearLAbel;
    private JLabel BookTimeAverageLabel;
    private JLabel TagsLabel;

    private JTable  m_bookListTable = new JTable(){//Create a Jtable with the tablemodel not editable
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };
    private JScrollPane m_pane;
    private DefaultTableModel m_tableModel = new DefaultTableModel();
    private Statement m_statement = null;
    private String m_title;
    private String m_author;
    private int m_rowSelected = 0;
    private JPopupMenu m_popup;
    private FiltersDlg m_diag;

    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        loadDB(false);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        File fileAdd = new File("Ressource/Icons/add.png");
        String pathAdd = fileAdd.getAbsolutePath();
        Image imgAdd = Toolkit.getDefaultToolkit().getImage(pathAdd);
        imgAdd = imgAdd.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);
        JMenuItem add = new JMenuItem("Ajouter une lecture", new ImageIcon(imgAdd));

        File fileRemove = new File("Ressource/Icons/remove.png");
        String pathRemove = fileRemove.getAbsolutePath();
        Image imgRemove = Toolkit.getDefaultToolkit().getImage(pathRemove);
        imgRemove = imgRemove.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(imgRemove));

        File fileEdit = new File("Ressource/Icons/edit.png");
        String pathEdit = fileEdit.getAbsolutePath();
        Image imgEdit = Toolkit.getDefaultToolkit().getImage(pathEdit);
        imgEdit = imgEdit.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(imgEdit));

        m_popup.add(add);
        m_popup.add(cut);
        m_popup.add(edit);

        if(m_bookListTable.getRowCount() != 0){//Vérif if the table is not empty; when starting the app, load and focus on the first book of the table
            setMTitle(m_bookListTable.getValueAt(0, 0).toString());
            setAuthor(m_bookListTable.getValueAt(0, 1).toString());
            loadComponents(getMTitle(), getAuthor());
            m_bookListTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()), getRowSelected(getMTitle(),getAuthor()));
            ManageReadingsBtn.setEnabled(true);

            m_bookListTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent evt) {//set main UI when we clicked on an element of the array, retrieved from the db
                    super.mouseReleased(evt);
                    ManageReadingsBtn.setEnabled(true);
                    setRowSelected(m_bookListTable.rowAtPoint(evt.getPoint()));
                    setMTitle(m_bookListTable.getValueAt(getRowSelected(), 0).toString()); //get the value of the column of the table
                    setAuthor(m_bookListTable.getValueAt(getRowSelected(), 1).toString());
                    loadComponents(getMTitle(), getAuthor());
                    if(evt.getButton() == MouseEvent.BUTTON3) {//if we right click show a popup to edit the book
                        m_bookListTable.setRowSelectionInterval(getRowSelected(), getRowSelected());//we focus the row when we right on the item
                        m_popup.show(BookListPanel, evt.getX(), evt.getY());
                    }
                    if(evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1){
                        ManageReadingDlg diag = new ManageReadingDlg(getMTitle(), getAuthor());
                        diag.setTitle("Gérer les lectures");
                        diag.setSize(500,300);
                        diag.setLocationRelativeTo(null);
                        diag.setVisible(true);
                        contentPane.updateUI();
                        BookListPanel.removeAll();//refresh the table of book
                        loadDB(false);
                        if(diag.isEmpty()){
                            initComponents();
                        }
                        else {
                            m_bookListTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()),getRowSelected(getMTitle(),getAuthor()));//focus on the book where you have managed your readings
                            loadComponents(getMTitle(), getAuthor());
                        }
                    }
                }
            });
        }
        else{
            ManageReadingsBtn.setEnabled(false);
        }
        AddBookBtn.addActionListener(new ActionListener() {//open the dlg for add a reading
            public void actionPerformed(ActionEvent evt) {
                AddBookDlg diag = new AddBookDlg();
                diag.setTitle("Ajouter un livvre");
                File fileAdd = new File("Ressource/Icons/add.png");
                String pathAdd = fileAdd.getAbsolutePath();
                Image imgAdd = Toolkit.getDefaultToolkit().getImage(pathAdd);
                imgAdd = imgAdd.getScaledInstance(16,16,Image.SCALE_AREA_AVERAGING);
                diag.setIconImage(imgAdd);
                diag.setSize(800,550);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);
                if (diag.isValide()){
                    String BookQry = "INSERT INTO Book (Title,Author,Image,NumberOP,NotePerso,NoteBabelio,ReleaseYear,Summary) " +
                            "VALUES (?,?,?,?,?,?,?,?);";
                    String ReadingQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                            "VALUES (?,?,?,?,?);";
                    contentPane.updateUI();
                    BookListPanel.removeAll();//refresh the table of book
                    try (Connection conn = connect(); PreparedStatement BookPstmt = conn.prepareStatement(BookQry); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry)) {
                        m_statement = conn.createStatement();
                        if(!diag.isDateUnknown() && !diag.isNotDOne()){
                            ReadingPstmt.setInt(1, setIdReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                            ReadingPstmt.setString(2, diag.getNewBookTitle());
                            ReadingPstmt.setString(3, diag.getNewBookAuthor());
                            ReadingPstmt.setString(4, diag.getNewBookStartReading());
                            ReadingPstmt.setString(5, diag.getNewBookEndReading());

                            BookPstmt.setString(1, diag.getNewBookTitle());
                            BookPstmt.setString(2, diag.getNewBookAuthor());
                            BookPstmt.setString(3, diag.getURL());
                            BookPstmt.setString(4, diag.getNewBookNumberOP());
                            BookPstmt.setString(5, diag.getNewBookPersonalNote());
                            BookPstmt.setString(6, diag.getNewBookBBLNote());
                            BookPstmt.setString(7, diag.getNewBookReleaseYear());
                            BookPstmt.setString(8, diag.getNewBookSummary());
                            BookPstmt.executeUpdate();//Insert the new Book
                            ReadingPstmt.executeUpdate();//Insert the new reading
                            setMTitle(diag.getNewBookTitle());
                            setAuthor(diag.getNewBookAuthor());
                            loadComponents(diag.getNewBookTitle(), diag.getNewBookAuthor());
                            loadDB(false);
                            //Focus in the jtable on the book created
                            m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()), getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                        } else if (!diag.isDateUnknown() && diag.isNotDOne()) {
                            ReadingPstmt.setInt(1, setIdReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                            ReadingPstmt.setString(2, diag.getNewBookTitle());
                            ReadingPstmt.setString(3, diag.getNewBookAuthor());
                            ReadingPstmt.setString(4, diag.getNewBookStartReading());
                            ReadingPstmt.setString(5, "Pas fini");

                            BookPstmt.setString(1, diag.getNewBookTitle());
                            BookPstmt.setString(2, diag.getNewBookAuthor());
                            BookPstmt.setString(3, diag.getURL());
                            BookPstmt.setString(4, diag.getNewBookNumberOP());
                            BookPstmt.setString(5, diag.getNewBookPersonalNote());
                            BookPstmt.setString(6, diag.getNewBookBBLNote());
                            BookPstmt.setString(7, diag.getNewBookReleaseYear());
                            BookPstmt.setString(8, diag.getNewBookSummary());
                            BookPstmt.executeUpdate();//Insert the new Book
                            ReadingPstmt.executeUpdate();//Insert the new reading
                            setMTitle(diag.getNewBookTitle());
                            setAuthor(diag.getNewBookAuthor());
                            loadComponents(diag.getNewBookTitle(), diag.getNewBookAuthor());
                            loadDB(false);
                            //Focus in the jtable on the book created
                            m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()), getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                        } else {
                            ReadingPstmt.setInt(1, setIdReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                            ReadingPstmt.setString(2, diag.getNewBookTitle());
                            ReadingPstmt.setString(3, diag.getNewBookAuthor());
                            ReadingPstmt.setString(4, "Inconnu");
                            ReadingPstmt.setString(5, "Inconnu");

                            BookPstmt.setString(1, diag.getNewBookTitle());
                            BookPstmt.setString(2, diag.getNewBookAuthor());
                            BookPstmt.setString(3, diag.getURL());
                            BookPstmt.setString(4, diag.getNewBookNumberOP());
                            BookPstmt.setString(5, diag.getNewBookPersonalNote());
                            BookPstmt.setString(6, diag.getNewBookBBLNote());
                            BookPstmt.setString(7, diag.getNewBookReleaseYear());
                            BookPstmt.setString(8, diag.getNewBookSummary());
                            BookPstmt.executeUpdate();//Insert the new Book in table Book
                            ReadingPstmt.executeUpdate();//Insert the new reading
                            setMTitle(diag.getNewBookTitle());
                            setAuthor(diag.getNewBookAuthor());
                            loadComponents(diag.getNewBookTitle(), diag.getNewBookAuthor());
                            loadDB(false);
                            //Focus in the jtable on the book created
                            m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()), getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                        }

                        conn.close();
                        m_statement.close();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        System.exit(0);
                    }
                }
            }
        });
        ManageReadingsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ManageReadingDlg diag = new ManageReadingDlg(getMTitle(), getAuthor());
                diag.setTitle("Gérer les lectures");
                diag.setSize(500,300);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);
                contentPane.updateUI();
                BookListPanel.removeAll();//refresh the table of book
                loadDB(false);
                if(diag.isEmpty()){
                    initComponents();
                }
                else {
                    m_bookListTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()),getRowSelected(getMTitle(),getAuthor()));//focus on the book where you have managed your readings
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
                    String sql = "DELETE FROM Book WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";//sql to delete the book in table book when we right click
                    String sql2 = "DELETE FROM Reading WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";//sql to delete the book in table reading when we right click
                    try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql); PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                        // execute the delete statement
                        pstmt.executeUpdate();
                        pstmt2.executeUpdate();
                        initComponents();
                        BookListPanel.removeAll();
                        loadDB(false);

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
                File fileEdit = new File("Ressource/Icons/edit.png");
                String pathEdit = fileEdit.getAbsolutePath();
                Image imgEdit = Toolkit.getDefaultToolkit().getImage(pathEdit);
                imgEdit = imgEdit.getScaledInstance(16,16,Image.SCALE_AREA_AVERAGING);
                diag.setIconImage(imgEdit);
                diag.setSize(800,500);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);
                if (diag.isValid()){
                    String BookQry = "UPDATE Book SET Title=?, Author=?, Image=?, NumberOP=?, NotePerso=?, NoteBabelio=?, ReleaseYear=?, Summary=?"+
                            "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";//Edit in bdd the book that we want to change
                    String ReadingQry = "UPDATE Reading SET Title=?, Author=?"+
                            "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";//Edit in bdd the book that we want to change
                    try (Connection conn = connect(); PreparedStatement BookPstmt = conn.prepareStatement(BookQry); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry)) {
                        // execute the uptdate statement
                        ReadingPstmt.setString(1, diag.getNewTitle());
                        ReadingPstmt.setString(2, diag.getNewAuthor());
                        BookPstmt.setString(1, diag.getNewTitle());
                        BookPstmt.setString(2, diag.getNewAuthor());
                        BookPstmt.setString(3, diag.getNewURL());
                        BookPstmt.setString(4, diag.getNewNumberPage());
                        BookPstmt.setString(5, diag.getNewPersonnalNote());
                        BookPstmt.setString(6, diag.getNewBBLNote());
                        BookPstmt.setString(7, diag.getNewReleaseyear());
                        BookPstmt.setString(8, diag.getNewSummary());
                        BookPstmt.executeUpdate();
                        ReadingPstmt.executeUpdate();

                        contentPane.updateUI();
                        BookListPanel.removeAll();
                        loadDB(false);
                        loadComponents(diag.getNewTitle(), diag.getNewAuthor());//reload changes made to the book
                        m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getNewTitle(), diag.getNewAuthor()), getRowSelected(diag.getNewTitle(), diag.getNewAuthor()));//focus on the edited book
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
                    BookListPanel.removeAll();//refresh the table of book
                    try(Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry)){
                        m_statement = conn.createStatement();
                        ResultSet rs = m_statement.executeQuery("SELECT * FROM Book WHERE Title='"+diag.getMtitle()+"' AND Author='"+diag.getAuthor()+ "'");
                        if(!diag.isDateUnknown()&& !diag.isNotDone()){
                            ReadingPstmt.setInt(1, setIdReading(diag.getMtitle(), diag.getAuthor()));
                            ReadingPstmt.setString(2, diag.getMtitle());
                            ReadingPstmt.setString(3, diag.getAuthor());
                            ReadingPstmt.setString(4, diag.getNewStartReading());
                            ReadingPstmt.setString(5, diag.getNewEndReading());

                            ReadingPstmt.executeUpdate();//Insert the new reading
                            setMTitle(diag.getMtitle());
                            setAuthor(diag.getAuthor());
                            loadComponents(diag.getMtitle(), diag.getAuthor());
                            loadDB(false);
                            //Focus in the jtable on a reading created from an existing book
                            m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getMtitle(), diag.getAuthor()), getRowSelected(diag.getMtitle(), diag.getAuthor()));
                        }else if (!diag.isDateUnknown() && diag.isNotDone()) {
                            ReadingPstmt.setInt(1, setIdReading(diag.getMtitle(), diag.getAuthor()));
                            ReadingPstmt.setString(2,  diag.getMtitle());
                            ReadingPstmt.setString(3, diag.getAuthor());
                            ReadingPstmt.setString(4, diag.getNewStartReading());
                            ReadingPstmt.setString(5, "Pas fini");

                            ReadingPstmt.executeUpdate();//Insert the new reading
                            setMTitle(diag.getMtitle());
                            setAuthor(diag.getAuthor());
                            loadComponents(diag.getMtitle(), diag.getAuthor());
                            loadDB(false);
                            //Focus in the jtable on a reading created from an existing book
                            m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getMtitle(), diag.getAuthor()), getRowSelected(diag.getMtitle(), diag.getAuthor()));
                        }
                        else {
                            ReadingPstmt.setInt(1, setIdReading(diag.getMtitle(), diag.getAuthor()));
                            ReadingPstmt.setString(2, diag.getMtitle());
                            ReadingPstmt.setString(3, diag.getAuthor());
                            ReadingPstmt.setString(4, "Inconnu");
                            ReadingPstmt.setString(5, "Inconnu");

                            ReadingPstmt.executeUpdate();//Insert the new reading
                            setMTitle(diag.getMtitle());
                            setAuthor(diag.getAuthor());
                            loadComponents(diag.getMtitle(), diag.getAuthor());
                            loadDB(false);
                            //Focus in the jtable on a reading created from an existing book
                            m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getMtitle(), diag.getAuthor()), getRowSelected(diag.getMtitle(), diag.getAuthor()));
                        }
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
                m_diag.setSize(400,200);
                m_diag.setLocationRelativeTo(null);
                m_diag.setVisible(true);
                contentPane.updateUI();
                BookListPanel.removeAll();//refresh the table of book
                if(m_diag.getIsValid()){
                    loadDB(true);
                    setMTitle(m_bookListTable.getValueAt(0, 0).toString());
                    setAuthor(m_bookListTable.getValueAt(0, 1).toString());
                    loadComponents(getMTitle(), getAuthor());
                    m_bookListTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()), getRowSelected(getMTitle(),getAuthor()));
                }
                else{
                    loadDB(false);
                    setMTitle(m_bookListTable.getValueAt(0, 0).toString());
                    setAuthor(m_bookListTable.getValueAt(0, 1).toString());
                    loadComponents(getMTitle(), getAuthor());
                    m_bookListTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()), getRowSelected(getMTitle(),getAuthor()));
                }
            }
        });
        CancelFiltersBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPane.updateUI();
                BookListPanel.removeAll();
                loadDB(false);
                setMTitle(m_bookListTable.getValueAt(0, 0).toString());
                setAuthor(m_bookListTable.getValueAt(0, 1).toString());
                loadComponents(getMTitle(), getAuthor());
                m_bookListTable.setRowSelectionInterval(getRowSelected(getMTitle(),getAuthor()), getRowSelected(getMTitle(),getAuthor()));
            }
        });
    }
    private Connection connect() {
        Connection connection = null;
        String url = "jdbc:sqlite:BookManager.db";
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
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
        int row = 0;
        for (int i= 0; i<m_bookListTable.getRowCount();i++)
            if(Objects.equals(m_bookListTable.getValueAt(i, 0).toString(), title) && Objects.equals(m_bookListTable.getValueAt(i, 1).toString(), author))
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
        try (Connection conn = this.connect()) {
            Class.forName("org.sqlite.JDBC");
            m_statement = conn.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS Book" +
                    "(Title TEXT, " +
                    " Author TEXT, " +
                    " Image TEXT, " +
                    " NumberOP INT, " +
                    " NotePerso INT, " +
                    " NoteBabelio INT, " +
                    " ReleaseYear TEXT, " +
                    " Summary TEXT)";

            String sql2 = "CREATE TABLE IF NOT EXISTS Reading" +
                    "(ID INT, " +
                    " Title TEXT, " +
                    " Author TEXT, " +
                    " StartReading TEXT, " +
                    " EndReading TEXT)";

            m_statement.executeUpdate(sql);//Create the BDD
            m_statement.executeUpdate(sql2);//Create the BDD
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
        try(Connection conn = this.connect()){
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
            else if(!isFiltered){
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

                m_bookListTable.setModel(m_tableModel);
                m_bookListTable.setFocusable(false);

                m_pane = new JScrollPane(m_bookListTable);//Create a scrollpane with the Jtable for the error that did not display the header

                BookListPanel.add(m_pane);//add the scrolpane to our Jpanel
            }
            rs.close();
            conn.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void loadComponents(String title, String author){
        try(Connection conn = connect()) {
            Class.forName("org.sqlite.JDBC");
            m_statement = conn.createStatement();

            //Title label
            ResultSet titleQry = m_statement.executeQuery("SELECT Title FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            TitleLabel.setText(titleQry.getString(1));

            //Author label
            ResultSet authorQry = m_statement.executeQuery("SELECT Author FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            AuthorLabel.setText("Auteur : "+authorQry.getString(1));

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
                    findFirst = false;
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
                    findLast = false;
                }
                else if(!LastReadQry.getString(1).equals("Inconnu") && !LastReadQry.getString(1).equals("Pas fini")){
                    LastReadingLabel.setText("Dernière lecture : "+LastReadQry.getString(1));
                    findLast =true;
                }
            }

            //Image
            ResultSet ImageQry = m_statement.executeQuery("SELECT Image FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");

            Image img = Toolkit.getDefaultToolkit().getImage(ImageQry.getString(1));
            img=img.getScaledInstance(200, 300, Image.SCALE_AREA_AVERAGING);//set size of image
            ImageIcon icon = new ImageIcon(img);
            JLabel imgLabel = new JLabel();
            imgLabel.setIcon(icon);

            BookPhotoPanel.removeAll();//clean the panel before to add an image
            BookPhotoPanel.add(imgLabel);

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
        AuthorLabel.setText("Auteur :");
        ReleaseYearLAbel.setText("Année de sortie :");
        NumberPageLabel.setText("Nombre de page :");
        BookTimeAverageLabel.setText("Temps moyen de lecture : ");
        CountReadingLabel.setText("Nombre de lecture :");
        NoteLabel.setText("Note :");
        PersonalNoteLabel.setText("Ma note :");
        FirstReadingLabel.setText("Première lecture :");
        LastReadingLabel.setText("Dernière lecture :");
        BookSummary.setText("");
        ManageReadingsBtn.setEnabled(false);
        BookPhotoPanel.removeAll();
        contentPane.updateUI();
    }
    public int setIdReading(String title, String author) {
        int i =0;
        try (Connection conn = connect()) {
            m_statement = conn.createStatement();
            ResultSet rs = m_statement.executeQuery("SELECT * FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'");
            while (rs.next()){
                i++;
            }
            rs.close();
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
            UIManager.setLookAndFeel(new FlatDarkLaf());

        }catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }
        MainWindow dialog = new MainWindow();
        dialog.setTitle("Book manager");
        dialog.setSize(1000,550);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        System.exit(0);
    }
}

