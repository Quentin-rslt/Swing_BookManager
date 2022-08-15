package Forms;

import Forms.Dialogs.AddBookDlg;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import java.sql.*;

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
    private JTable  m_bookListTable = new JTable();
    private JScrollPane m_pane;
    private DefaultTableModel m_tableModel = new DefaultTableModel();
    private Statement m_statement = null;
    private Connection m_connection = null;

    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        loadDB();
        if(m_bookListTable != null){//Vérif if the table is not empty
            m_bookListTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {//set main UI when we clicked on an element of the array, retrieved from the db
                    super.mouseClicked(evt);
                    int row = m_bookListTable.rowAtPoint(evt.getPoint());
                    String title  = m_bookListTable.getValueAt(row, 0).toString(); //get the value of the column of the table
                    String author  = m_bookListTable.getValueAt(row, 1).toString();

                    //Fill in the data on the app
                    try(Connection conn = connect()) {
                        Class.forName("org.sqlite.JDBC");
                        m_statement = conn.createStatement();

                        //Title label
                        ResultSet titleQry = m_statement.executeQuery("SELECT Title FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        TitleLabel.setText(titleQry.getString(1));

                        //Author label
                        ResultSet authorQry = m_statement.executeQuery("SELECT Author FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        AuthorLabel.setText("Auteur : "+authorQry.getString(1));

                        //Release year label
                        ResultSet NumberOPQry = m_statement.executeQuery("SELECT NumberOP FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        NumberPageLabel.setText("Nombre de page : "+NumberOPQry.getString(1));

                        //Number of reading label
                        ResultSet ReleaseYearQry = m_statement.executeQuery("SELECT ReleaseYear FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        ReleaseYearLAbel.setText("Année de sortie : "+ReleaseYearQry.getString(1));

                        //Number of reading label
                        ResultSet CountReadingQry = m_statement.executeQuery("SELECT COUNT(*) FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        CountReadingLabel.setText("Nombre de lecture : "+CountReadingQry.getString(1));

                        //Note on babelio
                        ResultSet NoteBBQry = m_statement.executeQuery("SELECT NoteBabelio FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        NoteLabel.setText("Note : "+NoteBBQry.getString(1));

                        //Summary
                        ResultSet SummaryQry = m_statement.executeQuery("SELECT Summary FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        BookSummary.setText(SummaryQry.getString(1));

                        //Personal note
                        ResultSet NotePersoQry = m_statement.executeQuery("SELECT NotePerso FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        PersonalNoteLabel.setText("Ma note : "+NotePersoQry.getString(1));

                        //First reading
                        ResultSet FirstReadQry = m_statement.executeQuery("SELECT DateReading FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'ORDER BY DateReading ASC LIMIT 1");
                        FirstReadingLabel.setText("Première lecture : "+FirstReadQry.getString(1));

                        //Last reading
                        ResultSet LastReadQry = m_statement.executeQuery("SELECT DateReading FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'ORDER BY DateReading DESC LIMIT 1");
                        LastReadingLabel.setText("Dernière lecture : "+FirstReadQry.getString(1));

                        //Image
                        ResultSet ImageQry = m_statement.executeQuery("SELECT Image FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");

                        Image img = Toolkit.getDefaultToolkit().getImage(ImageQry.getString(1));
                        img=img.getScaledInstance(200, 300, Image.SCALE_DEFAULT);//set size of image
                        ImageIcon icon = new ImageIcon(img);
                        JLabel imgLabel = new JLabel();
                        imgLabel.setIcon(icon);

                        BookPhotoPanel.removeAll();//clean the panel before to add an image
                        BookPhotoPanel.add(imgLabel);

                        conn.close();
                        m_statement.close();
                    } catch ( Exception e ) {
                        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                        System.exit(0);
                    }
                }
            });
        }
        AddBookBtn.addActionListener(new ActionListener() {//open the dlg for add a reading
            public void actionPerformed(ActionEvent evt) {
                AddBookDlg diag = new AddBookDlg();
                diag.setSize(800,500);
                diag.setVisible(true);
                if (diag.isValide()){
                    String qry = "INSERT INTO BookManager (Title,Author,Image,NumberOP,NotePerso,NoteBabelio,DateReading,ReleaseYear,Summary) " +
                            "VALUES (?,?,?,?,?,?,?,?,?);";

                    try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(qry)) {
                        m_statement = conn.createStatement();

                        //If nothing is selected in the combobox
                        if(!diag.getIsAlreadyRead()){
                            if(!diag.isDateUnknown()){
                                pstmt.setString(1, diag.getNewBookTitle());
                                pstmt.setString(2, diag.getNewBookAuthor());
                                pstmt.setString(3, diag.getURL());
                                pstmt.setInt(4, diag.getNewBookNumberOP());
                                pstmt.setInt(5, diag.getNewBookPersonalNote());
                                pstmt.setInt(6, diag.getNewBookBBLNote());
                                pstmt.setString(7, diag.getNewBookDateReading());
                                pstmt.setString(8, diag.getNewBookReleaseYear());
                                pstmt.setString(9, diag.getNewBookSummary());
                                pstmt.executeUpdate();//Insert the new Book
                                loadDB();
                            }
                            else {
                                pstmt.setString(1, diag.getNewBookTitle());
                                pstmt.setString(2, diag.getNewBookAuthor());
                                pstmt.setString(3, diag.getURL());
                                pstmt.setInt(4, diag.getNewBookNumberOP());
                                pstmt.setInt(5, diag.getNewBookPersonalNote());
                                pstmt.setInt(6, diag.getNewBookBBLNote());
                                pstmt.setString(7, "Inconnu");
                                pstmt.setString(8, diag.getNewBookReleaseYear());
                                pstmt.setString(9, diag.getNewBookSummary());
                                pstmt.executeUpdate();//Insert the new Boo
                                loadDB();
                            }
                        }
                        else{
                            ResultSet rs = m_statement.executeQuery("SELECT * FROM BookManager WHERE Title='"+diag.getTitle()+"' AND Author='"+diag.getAuthor()+ "'");
                            if(!diag.isDateUnknown()){
                                pstmt.setString(1, diag.getTitle());
                                pstmt.setString(2, diag.getAuthor());
                                pstmt.setString(3, rs.getString(3));
                                pstmt.setInt(4, rs.getInt(4));
                                pstmt.setInt(5, rs.getInt(5));
                                pstmt.setInt(6, rs.getInt(6));
                                pstmt.setString(7, rs.getString(7));
                                pstmt.setString(8, rs.getString(8));
                                pstmt.setString(9, rs.getString(9));
                                pstmt.executeUpdate();//Insert the new Book
                                loadDB();
                            }
                            else {
                                pstmt.setString(1, diag.getTitle());
                                pstmt.setString(2, diag.getAuthor());
                                pstmt.setString(3, rs.getString(3));
                                pstmt.setInt(4, rs.getInt(4));
                                pstmt.setInt(5, rs.getInt(5));
                                pstmt.setInt(6, rs.getInt(6));
                                pstmt.setString(7, "Inconnu");
                                pstmt.setString(8, rs.getString(8));
                                pstmt.setString(9, rs.getString(9));
                                pstmt.executeUpdate();//Insert the new Book
                                loadDB();
                            }
                            rs.close();
                        }
                        conn.close();
                        m_statement.close();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
    }
    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:BookManager.db";
        try {
            m_connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return m_connection;
    }

    public void connectionDB(){
        try (Connection conn = this.connect()) {
            Class.forName("org.sqlite.JDBC");
            m_statement = conn.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS BookManager" +
                    "(Title TEXT, " +
                    " Author TEXT, " +
                    " Image TEXT, " +
                    " NumberOP INT, " +
                    " NotePerso INT, " +
                    " NoteBabelio INT, " +
                    " DateReading TEXT, " +
                    " ReleaseYear TEXT, " +
                    " Summary TEXT)";

            m_statement.executeUpdate(sql);//Create the BDD
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
        try (Connection conn = this.connect()){
            m_statement = conn.createStatement();
            System.out.println("Table connexion successfully");

            ResultSet rs = m_statement.executeQuery("SELECT * FROM BookManager GROUP BY Title, Author;");//Execute a Query to retrieve all the values from the database by grouping the duplicates
                                                                                                            // (with their name and author)
            while (rs.next()) {//Fill in the table of the list of Book
                String title = rs.getString("Title");//Retrieve the title
                String author = rs.getString("Author");//Retrieve the author

                String[ ] header = {"Titre","Auteur"};
                Object[] data = {title, ""+author};

                m_tableModel.setColumnIdentifiers(header);//Create the header
                m_tableModel.addRow(data);//add to tablemodel the data

                m_bookListTable = new JTable(m_tableModel){//Create a Jtable with the tablemodel not editable
                    public boolean isCellEditable(int rowIndex, int colIndex) {
                        return false; //Disallow the editing of any cell
                    }
                };
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());

        }catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }
        MainWindow dialog = new MainWindow();
        dialog.setSize(1000,550);
        dialog.setVisible(true);
        System.exit(0);
    }
}
