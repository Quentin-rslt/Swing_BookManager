package Forms;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private JButton ReadExtractBtn;
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
    private JTable m_bookListTable;
    private JScrollPane m_pane;
    private DefaultTableModel m_tableModel = new DefaultTableModel();
    private Statement statement = null;
    private Connection connection = null;

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
                        connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                        statement = connection.createStatement();

                        //Title label
                        ResultSet titleQry = statement.executeQuery("SELECT Title FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        TitleLabel.setText(titleQry.getString(1));

                        //Author label
                        ResultSet authorQry = statement.executeQuery("SELECT Author FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        AuthorLabel.setText("Auteur : "+authorQry.getString(1));

                        //Number of page label
                        ResultSet NumberOPQry = statement.executeQuery("SELECT NumberOP FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        NumberPageLabel.setText("Nombre de page : "+NumberOPQry.getString(1));

                        //Number of page label
                        ResultSet CountReadingQry = statement.executeQuery("SELECT COUNT(*) FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        CountReadingLabel.setText("Nombre de lecture : "+CountReadingQry.getString(1));

                        //Note on babelio
                        ResultSet NoteBBQry = statement.executeQuery("SELECT NoteBabelio FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        NoteLabel.setText("Note : "+NoteBBQry.getString(1));

                        //Summary
                        ResultSet SummaryQry = statement.executeQuery("SELECT Summary FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        BookSummary.setText(SummaryQry.getString(1));

                        //Personal note
                        ResultSet NotePersoQry = statement.executeQuery("SELECT NotePerso FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        PersonalNoteLabel.setText("Note personelle : "+NotePersoQry.getString(1));

                        //First reading
                        ResultSet FirstReadQry = statement.executeQuery("SELECT FirstReading FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        FirstReadingLabel.setText("Première lecture : "+FirstReadQry.getString(1));

                        //Last reading
                        ResultSet LastReadQry = statement.executeQuery("SELECT LastReading FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
                        LastReadingLabel.setText("Dernière lecture : "+FirstReadQry.getString(1));

                        //Image
                        ResultSet ImageQry = statement.executeQuery("SELECT Image FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");

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
    }
    public void connectionDB(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            statement = connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS BookManager" +
                    "(Title TEXT, " +
                    " Author TEXT, " +
                    " Image TEXT, " +
                    " NumberOP INT, " +
                    " NotePerso INT, " +
                    " NoteBabelio INT, " +
                    " LastReading DATE, " +
                    " FirstReading DATE, " +
                    " ReleaseYear INT, " +
                    " Summary TEXT)";

            statement.executeUpdate(sql);
            System.out.println("Table created successfully");
            connection.close();
            statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void loadDB(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            statement = connection.createStatement();
            System.out.println("Table connexion successfully");

            ResultSet rs = statement.executeQuery("SELECT * FROM BookManager GROUP BY Title, Author;");

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
            connection.close();
            statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }


    public static void main(String[] args) {
        MainWindow dialog = new MainWindow();
        dialog.setSize(1000,500);
        dialog.setVisible(true);
        System.exit(0);
    }
}
