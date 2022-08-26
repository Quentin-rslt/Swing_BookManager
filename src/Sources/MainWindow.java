package Sources;

import Sources.Dialogs.AddBookDlg;
import Sources.Dialogs.EditBookDlg;
import Sources.Dialogs.ManageReadingDlg;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        loadDB();

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer");
        JMenuItem edit = new JMenuItem("Modifier");
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
                }
            });
        }
        else{
            ManageReadingsBtn.setEnabled(false);
        }
        AddBookBtn.addActionListener(new ActionListener() {//open the dlg for add a reading
            public void actionPerformed(ActionEvent evt) {
                AddBookDlg diag = new AddBookDlg();
                diag.setTitle("Ajouter une lecture");
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
                        //If nothing is selected in the combobox
                        if(!diag.getIsAlreadyRead()){
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
                                loadDB();
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
                                loadDB();
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
                                loadDB();
                                //Focus in the jtable on the book created
                                m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()), getRowSelected(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                            }
                        }
                        else{
                            ResultSet rs = m_statement.executeQuery("SELECT * FROM Book WHERE Title='"+diag.getMTitle()+"' AND Author='"+diag.getAuthor()+ "'");
                            if(!diag.isDateUnknown()&& !diag.isNotDOne()){
                                ReadingPstmt.setInt(1, setIdReading(diag.getMTitle(), diag.getAuthor()));
                                ReadingPstmt.setString(2, diag.getMTitle());
                                ReadingPstmt.setString(3, diag.getAuthor());
                                ReadingPstmt.setString(4, diag.getNewBookStartReading());
                                ReadingPstmt.setString(5, diag.getNewBookEndReading());

                                ReadingPstmt.executeUpdate();//Insert the new reading
                                setMTitle(diag.getMTitle());
                                setAuthor(diag.getAuthor());
                                loadComponents(diag.getMTitle(), diag.getAuthor());
                                loadDB();
                                //Focus in the jtable on a reading created from an existing book
                                m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getMTitle(), diag.getAuthor()), getRowSelected(diag.getMTitle(), diag.getAuthor()));
                            }else if (!diag.isDateUnknown() && diag.isNotDOne()) {
                                ReadingPstmt.setInt(1, setIdReading(diag.getMTitle(), diag.getAuthor()));
                                ReadingPstmt.setString(2,  diag.getMTitle());
                                ReadingPstmt.setString(3, diag.getAuthor());
                                ReadingPstmt.setString(4, diag.getNewBookStartReading());
                                ReadingPstmt.setString(5, "Pas fini");

                                ReadingPstmt.executeUpdate();//Insert the new reading
                                setMTitle(diag.getNewBookTitle());
                                setAuthor(diag.getNewBookAuthor());
                                loadComponents(diag.getMTitle(), diag.getAuthor());
                                loadDB();
                                //Focus in the jtable on a reading created from an existing book
                                m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getMTitle(), diag.getAuthor()), getRowSelected(diag.getMTitle(), diag.getAuthor()));
                            }
                            else {
                                ReadingPstmt.setInt(1, setIdReading(diag.getMTitle(), diag.getAuthor()));
                                ReadingPstmt.setString(2, diag.getMTitle());
                                ReadingPstmt.setString(3, diag.getAuthor());
                                ReadingPstmt.setString(4, "Inconnu");
                                ReadingPstmt.setString(5, "Inconnu");

                                ReadingPstmt.executeUpdate();//Insert the new reading
                                setMTitle(diag.getMTitle());
                                setAuthor(diag.getAuthor());
                                loadComponents(diag.getMTitle(), diag.getAuthor());
                                loadDB();
                                //Focus in the jtable on a reading created from an existing book
                                m_bookListTable.setRowSelectionInterval(getRowSelected(diag.getMTitle(), diag.getAuthor()), getRowSelected(diag.getMTitle(), diag.getAuthor()));
                            }
                            rs.close();
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
                loadDB();
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
                        loadDB();

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
                        loadDB();
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
    }
    private Connection connect() {
        // SQLite connection string
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
    public void loadDB(){
        m_tableModel.setRowCount(0);
        try(Connection conn = this.connect()){
            m_statement = conn.createStatement();
            System.out.println("Table connexion successfully");

            ResultSet rs = m_statement.executeQuery("SELECT * FROM Book GROUP BY Title, Author;");//Execute a Query to retrieve all the values from the database by grouping the duplicates
            // (with their name and author)
            while (rs.next()) {//Fill in the table of the list of Book
                String title = rs.getString("Title");//Retrieve the title
                String author = rs.getString("Author");//Retrieve the author

                String[ ] header = {"Titre","Auteur"};
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
                    BookTimeAverageLabel.setText("Temps moyen de lecture : : "+days/dateValid+" jours");
                }
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
            ResultSet FirstReadQry = m_statement.executeQuery("SELECT EndReading FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'ORDER BY EndReading ASC LIMIT 1");
            FirstReadingLabel.setText("Première lecture : "+FirstReadQry.getString(1));

            //Last reading
            ResultSet LastReadQry = m_statement.executeQuery("SELECT EndReading FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'ORDER BY EndReading DESC LIMIT 1");
            LastReadingLabel.setText("Dernière lecture : "+FirstReadQry.getString(1));

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

