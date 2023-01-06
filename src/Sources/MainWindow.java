package Sources;

import Sources.BookManager.BookManager;
import Sources.BuySellManager.Transaction;
import Themes.DarkTheme.DarkTheme;

import javax.swing.*;
import java.sql.*;

import static Sources.Common.getLogo;
import static Sources.CommonSQL.connect;

public class MainWindow extends JDialog {
    private JPanel contentPane;
    private JPanel TransactionsPanel;
    private JPanel BooksPanel;

    public MainWindow() {
        setContentPane(contentPane);
        setModal(true);
        connectionDB();
        //Book panel
        BookManager booksManager = new BookManager(this);
        BooksPanel.add(booksManager.getContentPane());
        //Transaction panel
        Transaction transaction = new Transaction();
        TransactionsPanel.add(transaction.getContentPane());

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

            String TransactionSql = "CREATE TABLE IF NOT EXISTS Transactions" +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " IsABuy BOOLEAN, " +
                    " Name TEXT, " +
                    " Brand TEXT, " +
                    " Image TEXT, " +
                    " ReleaseYear INT, " +
                    " Date TEXT, " +
                    " Price FLOAT, " +
                    " Description TEXT)";

            String LinkTagTransactionSql = "CREATE TABLE IF NOT EXISTS LinkTagTransaction" +
                    "(IdTransaction INT, " +
                    " IdTag INT)";

            statement.executeUpdate(BookSql);//Create the book table
            statement.executeUpdate(ReadSql);//Create the reading table
            statement.executeUpdate(TagsSql);//Create the tags table
            statement.executeUpdate(TaggingSql);//Create the tagging table
            statement.executeUpdate(TransactionSql);//Create the Transaction table
            statement.executeUpdate(LinkTagTransactionSql);//Create the LinkTagTransaction table

            conn.close();
            statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Ouverture BDD impossible", JOptionPane.ERROR_MESSAGE);
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