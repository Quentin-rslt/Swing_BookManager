package Sources;

import Sources.BookManager.BookManager;
import Themes.DarkTheme.DarkTheme;

import javax.swing.*;
import java.sql.*;

import static Sources.BookManager.CommonBookManager.*;
import static Sources.BookManager.CommonBookManagerSQL.*;
import static Sources.Common.getLogo;
import static Sources.CommonSQL.connect;

public class MainWindow extends JDialog {
    private JPanel contentPane;
    private JPanel BuySellPane;
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

    private JPanel BooksPane;

    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        new BookManager(this);
    }

    /****************************** Get ***********************************/
    public JPanel getContentPanel(){
        return this.contentPane;
    }
    public JPanel getBuySellPane() {
        return BuySellPane;
    }

    public JLabel getPersonalNoteLabel() {
        return PersonalNoteLabel;
    }

    public JLabel getFirstReadingLabel() {
        return FirstReadingLabel;
    }

    public JLabel getLastReadingLabel() {
        return LastReadingLabel;
    }

    public JPanel getBookPhotoPanel() {
        return BookPhotoPanel;
    }

    public JLabel getTitleLabel() {
        return TitleLabel;
    }

    public JLabel getNumberPageLabel() {
        return NumberPageLabel;
    }

    public JLabel getNoteLabel() {
        return NoteLabel;
    }

    public JTextPane getBookSummary() {
        return BookSummary;
    }

    public JButton getAddBookBtn() {
        return AddBookBtn;
    }

    public JButton getFiltersBookBtn() {
        return FiltersBookBtn;
    }

    public JButton getCancelFiltersBtn() {
        return CancelFiltersBtn;
    }

    public JLabel getCountReadingLabel() {
        return CountReadingLabel;
    }

    public JLabel getReleaseYearLAbel() {
        return ReleaseYearLAbel;
    }

    public JLabel getBookTimeAverageLabel() {
        return BookTimeAverageLabel;
    }

    public JPanel getBookTagsPanel() {
        return BookTagsPanel;
    }

    public JScrollPane getJSpane() {
        return JSpane;
    }

    public JButton getBookManageTagsBtn() {
        return BookManageTagsBtn;
    }

    public JTable getBooksTable() {
        return BooksTable;
    }

    public JTextField getBookFastSearch() {
        return BookFastSearch;
    }

    public JTable getReadingsTable() {
        return ReadingsTable;
    }

    public JLabel getCountBookLbl() {
        return CountBookLbl;
    }

    public JPanel getBooksPane() {
        return BooksPane;
    }


    public void connectionDB(){
        try (Connection conn = connect()) {
            Class.forName("org.sqlite.JDBC");
            Statement statement = conn.createStatement();

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

            statement.executeUpdate(BookSql);//Create the book table
            statement.executeUpdate(ReadSql);//Create the reading table
            statement.executeUpdate(TagsSql);//Create the tags table
            statement.executeUpdate(TaggingSql);//Create the tagging table

            conn.close();
            statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new DarkTheme());
        }catch( Exception ex ) {
            System.err.println( "Failed to load darkTheme" );
        }

        MainWindow parent = new MainWindow();
        parent.setIconImage(getLogo(18, 18));
        parent.setTitle("Book manager");
        parent.setSize(1520,855);
        parent.setLocationRelativeTo(null);
        parent.setVisible(true);
        System.exit(0);
    }
}