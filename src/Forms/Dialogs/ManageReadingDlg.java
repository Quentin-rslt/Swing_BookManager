package Forms.Dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ManageReadingDlg extends JDialog {
    private JPanel contentPane;
    private JPanel BookListPanel;
    private JPanel BookBtnPanel;
    private JButton CancelBtn;

    private Connection m_connection;
    private Statement m_statement;
    private JTable  m_bookListTable = new JTable();
    private JScrollPane m_pane;
    private DefaultTableModel m_tableModel = new DefaultTableModel();
    private String m_title = "";
    private String m_author = "";

    public ManageReadingDlg(String title, String author) {
        setContentPane(contentPane);
        setTitle(title);
        setAuthor(author);
        fillBookList();
        setModal(true);
    }

    public String getAuthor() {
        return m_author;
    }
    public String getTitle() {
        return m_title;
    }

    public void setAuthor(String m_author) {
        this.m_author = m_author;
    }
    public void setTitle(String m_title) {
        this.m_title = m_title;
    }
    public void fillBookList(){
        try{
            Class.forName("org.sqlite.JDBC");
            m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            m_statement = m_connection.createStatement();
            ResultSet qry = m_statement.executeQuery("SELECT Title, Author, DateReading FROM BookManager WHERE Title='"+getTitle()+"' AND Author='"+getAuthor()+ "'");//Retrieved from the bdd the URL of the book image

            while (qry.next()){
                String title = qry.getString("Title");//Retrieve the title
                String author = qry.getString("Author");//Retrieve the author
                String dateReading = qry.getString("DateReading");

                String[ ] header = {"Titre","Auteur","Date de lecture"};
                Object[] data = {title, author, dateReading};

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

            m_connection.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
