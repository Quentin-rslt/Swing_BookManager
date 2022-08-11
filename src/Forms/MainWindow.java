package Forms;

import javax.swing.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
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
    private JTable BookTableList;
    private JPanel BookEditListPanel;
    private JButton AddBookBtn;
    private JButton FiltersBookBtn;
    private JButton CancelFiltersBtn;

    DefaultTableModel tableModel = new DefaultTableModel();

    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        loadDB();
    }

    public void connectionDB(){
        Statement statement = null;
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            statement = connection.createStatement();

            String sql = "CREATE TABLE IF NOT EXISTS BookManager" +
                    "(Title TEXT, " +
                    " Auhtor TEXT, " +
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
        Statement statement = null;
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            statement = connection.createStatement();
            System.out.println("Table connexion successfully");

            ResultSet rs = statement.executeQuery( "SELECT * FROM BookManager;" );

            while ( rs.next() ) {
                String title = rs.getString("Title");
                String author = rs.getString("Auhtor");

                String[ ] columnName = { "Titre","Auteur"};
                Object[ ] data = {title, ""+author};

                tableModel.setColumnIdentifiers(columnName);
                tableModel.addRow(data);
                tableModel.fireTableDataChanged();
            }
            BookTableList.setModel(tableModel);

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
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
