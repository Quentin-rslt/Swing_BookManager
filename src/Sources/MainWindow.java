package Sources;

import Sources.BookManager.BookManager;
import Themes.DarkTheme.DarkTheme;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

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

    private int m_deleteBookKey;
    private int m_editBookKey;
    private int m_deleteReadKey;
    private int m_editReadKey;
    private int m_addReadingKey;
    private int m_addBookKey;
    private int m_manageTagsKey;
    private int m_critKey;
    private int m_manageAllTagsKey;
    private int m_resetKey;

    private int m_deleteBookModif;
    private int m_editBookModif;
    private int m_deleteReadModif;
    private int m_editReadModif;
    private int m_addReadingModif;
    private int m_manageTagsModif;
    private int m_addBookModif;
    private int m_critModif;
    private int m_manageAllTagsModif;
    private int m_resetModif;
    private BookManager m_bookManager;

    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        m_bookManager = new BookManager(this);
        loadParameters();
    }

    /****************************** Get ***********************************/
    public BookManager getM_bookManager() {
        return m_bookManager;
    }
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
    public int getEditBookModif() {
        return m_editBookModif;
    }
    public int getEditBookKey() {
        return m_editBookKey;
    }
    public int getEditReadModif() {
        return m_editReadModif;
    }
    public int getEditReadKey() {
        return m_editReadKey;
    }
    public int getAddReadingKey() {
        return m_addReadingKey;
    }
    public int getManageTagsKey() {
        return m_manageTagsKey;
    }
    public int getDeleteBookModif() {
        return m_deleteBookModif;
    }
    public int getDeleteBookKey() {
        return m_deleteBookKey;
    }
    public int getDeleteReadModif() {
        return m_deleteReadModif;
    }
    public int getDeleteReadKey() {
        return m_deleteReadKey;
    }
    public int getAddReadingModif() {
        return m_addReadingModif;
    }
    public int getManageTagsModif() {
        return m_manageTagsModif;
    }
    public int getAddBookKey() {
        return m_addBookKey;
    }
    public int getAddBookModif() {
        return m_addBookModif;
    }
    public int getCritKey() {
        return m_critKey;
    }
    public int getManageAllTagsKey() {
        return m_manageAllTagsKey;
    }
    public int getCritModif() {
        return m_critModif;
    }
    public int getManageAllTagsModif() {
        return m_manageAllTagsModif;
    }
    public int getResetKey() {
        return m_resetKey;
    }
    public int getResetModif() {
        return m_resetModif;
    }


    /****************************** Setter *********************************/
    public void setDeleteBookKey(int m_deletekey) {
        this.m_deleteBookKey = m_deletekey;
    }
    public void setEditBookKey(int m_editKey) {
        this.m_editBookKey = m_editKey;
    }
    public void setAddReadingKey(int m_addReadingKey) {
        this.m_addReadingKey = m_addReadingKey;
    }
    public void setManageTagsKey(int m_manageTagsKey) {
        this.m_manageTagsKey = m_manageTagsKey;
    }
    public void setDeleteBookModif(int m_deleteModif) {
        this.m_deleteBookModif = m_deleteModif;
    }
    public void setEditBookModif(int m_editModif) {
        this.m_editBookModif = m_editModif;
    }
    public void setDeleteReadKey(int m_deleteReadKey) {
        this.m_deleteReadKey = m_deleteReadKey;
    }
    public void setEditReadKey(int m_editReadKey) {
        this.m_editReadKey = m_editReadKey;
    }
    public void setDeleteReadModif(int m_deleteReadModif) {
        this.m_deleteReadModif = m_deleteReadModif;
    }
    public void setEditReadModif(int m_editReadModif) {
        this.m_editReadModif = m_editReadModif;
    }
    public void setAddReadingModif(int m_addReadingModif) {
        this.m_addReadingModif = m_addReadingModif;
    }
    public void setManageTagsModif(int m_manageTagsModif) {
        this.m_manageTagsModif = m_manageTagsModif;
    }
    public void setAddBookKey(int m_addBookKey) {
        this.m_addBookKey = m_addBookKey;
    }
    public void setAddBookModif(int m_addBookModif) {
        this.m_addBookModif = m_addBookModif;
    }
    public void setCritKey(int m_critKey) {
        this.m_critKey = m_critKey;
    }
    public void setManageAllTagsKey(int m_manageAllTagsKey) {
        this.m_manageAllTagsKey = m_manageAllTagsKey;
    }
    public void setCritModif(int m_critModif) {
        this.m_critModif = m_critModif;
    }
    public void setManageAllTagsModif(int m_manageAllTagsModif) {
        this.m_manageAllTagsModif = m_manageAllTagsModif;
    }
    public void setResetKey(int m_resetKey) {
        this.m_resetKey = m_resetKey;
    }
    public void setResetModif(int m_resetModif) {
        this.m_resetModif = m_resetModif;
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

    public void loadParameters(){
        try {
            Path file = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/save.dat");
            if ( Files.exists(file) ) {
                for(String line : Files.readAllLines(file)) {
                    String[] data = line.split(";");
                    setAddBookKey(Integer.parseInt(data[0]));
                    setAddBookModif(Integer.parseInt(data[1]));

                    setAddReadingKey(Integer.parseInt(data[2]));
                    setAddReadingModif(Integer.parseInt(data[3]));

                    setDeleteBookKey(Integer.parseInt(data[4]));
                    setDeleteBookModif(Integer.parseInt(data[5]));

                    setEditBookKey(Integer.parseInt(data[6]));
                    setEditBookModif(Integer.parseInt(data[7]));

                    setDeleteReadKey(Integer.parseInt(data[8]));
                    setDeleteReadModif(Integer.parseInt(data[9]));

                    setEditReadKey(Integer.parseInt(data[10]));
                    setEditReadModif(Integer.parseInt(data[11]));

                    setManageTagsKey(Integer.parseInt(data[12]));
                    setManageTagsModif(Integer.parseInt(data[13]));

                    setCritKey(Integer.parseInt(data[14]));
                    setCritModif(Integer.parseInt(data[15]));

                    setManageAllTagsKey(Integer.parseInt(data[16]));
                    setManageAllTagsModif(Integer.parseInt(data[17]));

                    setResetKey(Integer.parseInt(data[18]));
                    setResetModif(Integer.parseInt(data[19]));
                }
            }
            else {
                setAddReadingKey(KeyEvent.VK_A);
                setManageTagsKey(KeyEvent.VK_T);
                setEditBookKey(KeyEvent.VK_E);
                setDeleteBookKey(KeyEvent.VK_D);
                setEditReadKey(KeyEvent.VK_E);
                setDeleteReadKey(KeyEvent.VK_D);
                setAddBookKey(KeyEvent.VK_A);
                setCritKey(KeyEvent.VK_C);
                setManageAllTagsKey(KeyEvent.VK_T);
                setAddReadingModif(0);
                setManageTagsModif(0);
                setEditBookModif(0);
                setDeleteBookModif(0);
                setEditReadModif(KeyEvent.KEY_LOCATION_STANDARD);
                setDeleteReadModif(KeyEvent.KEY_LOCATION_STANDARD);
                setAddBookModif(KeyEvent.KEY_LOCATION_STANDARD);
                setCritModif(KeyEvent.KEY_LOCATION_STANDARD);
                setManageAllTagsModif(KeyEvent.KEY_LOCATION_STANDARD);
                setResetKey(KeyEvent.VK_R);
                setResetModif(KeyEvent.KEY_LOCATION_STANDARD);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Rechargement shortcut impossible", JOptionPane.ERROR_MESSAGE);
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