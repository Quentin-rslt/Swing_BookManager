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


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

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
    private JTable m_bookListTable;
    private JScrollPane m_pane;
    private DefaultTableModel m_tableModel = new DefaultTableModel();
    private Statement m_statement = null;
    private Connection m_connection = null;

    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        loadDB();
        if(m_bookListTable != null){
            m_bookListTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    super.mouseClicked(evt);
                    int row = m_bookListTable.rowAtPoint(evt.getPoint());
                    String title  = m_bookListTable.getValueAt(row, 0).toString();
                    String author  = m_bookListTable.getValueAt(row, 1).toString();

                    //Fill in the data on the app
                    try {
                        Class.forName("org.sqlite.JDBC");
                        m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                        m_statement = m_connection.createStatement();

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
                        img=img.getScaledInstance(200, 300, Image.SCALE_DEFAULT);
                        ImageIcon icon = new ImageIcon(img);
                        JLabel imgLabel = new JLabel();
                        imgLabel.setDisabledIcon(icon);
                        imgLabel.setIcon(icon);

                        BookPhotoPanel.removeAll();
                        BookPhotoPanel.add(imgLabel);
                    } catch ( Exception e ) {
                        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                        System.exit(0);
                    }
                }
            });
        }
        AddBookBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AddBookDlg diag = new AddBookDlg();
                diag.setSize(800,500);
                diag.setVisible(true);

            }
        });
    }

    public void connectionDB(){
        try {
            Class.forName("org.sqlite.JDBC");
            m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            m_statement = m_connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS BookManager" +
                    "(Title TEXT, " +
                    " Author TEXT, " +
                    " Image TEXT, " +
                    " NumberOP INT, " +
                    " NotePerso INT, " +
                    " NoteBabelio INT, " +
                    " DateReading DATE, " +
                    " ReleaseYear INT, " +
                    " Summary TEXT)";

            m_statement.executeUpdate(sql);
            System.out.println("Table created successfully");
            m_connection.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void loadDB(){
        try {
            Class.forName("org.sqlite.JDBC");
            m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            m_statement = m_connection.createStatement();
            System.out.println("Table connexion successfully");

            ResultSet rs = m_statement.executeQuery("SELECT * FROM BookManager GROUP BY Title, Author;");

            while (rs.next()) {
                String title = rs.getString("Title");
                String author = rs.getString("Author");

                String[ ] header = {"Titre","Auteur"};
                Object[] data = {title, ""+author};

                m_tableModel.setColumnIdentifiers(header);
                m_tableModel.addRow(data);

                m_bookListTable = new JTable(m_tableModel){
                    public boolean isCellEditable(int rowIndex, int colIndex) {
                        return false; //Disallow the editing of any cell
                    }
                };
                m_bookListTable.setFocusable(false);
                m_pane = new JScrollPane(m_bookListTable);
                BookListPanel.add(m_pane);
            }

            rs.close();
            m_connection.close();
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
