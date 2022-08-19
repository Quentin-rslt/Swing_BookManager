package Forms.Dialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;

public class ManageReadingDlg extends JDialog {
    private JPanel contentPane;
    private JPanel BookListPanel;
    private JPanel BookBtnPanel;
    private JButton CancelBtn;

    private Connection m_connection;
    private Statement m_statement;
    private JTable  m_bookListTable = new JTable(){//Create a Jtable with the tablemodel not editable
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };
    private JScrollPane m_pane;
    private DefaultTableModel m_tableModel = new DefaultTableModel();
    private String m_title = "";
    private String m_author = "";
    private String m_dateReading = "";
    private JPopupMenu m_popup;
    private boolean m_isEmpty = false;

    public ManageReadingDlg(String title, String author) {
        setContentPane(contentPane);
        setTitle(title);
        setAuthor(author);
        fillBookList();

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer");
        JMenuItem edit = new JMenuItem("Modifier");
        m_popup.add(cut);
        m_popup.add(edit);

        setModal(true);
        CancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(m_bookListTable.getRowCount()>0)
                    setIsEmpty(false);
                else
                    setIsEmpty(true);
                setVisible(false);
                dispose();
            }
        });
        m_bookListTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                int row = m_bookListTable.rowAtPoint(evt.getPoint());
                setTitle(m_bookListTable.getValueAt(row, 0).toString()); //get the value of the column of the table
                setAuthor(m_bookListTable.getValueAt(row, 1).toString());
                setDateReading(m_dateReading = m_bookListTable.getValueAt(row, 2).toString());
                //if(m_bookListTable.isRowSelected(row)){
                    if(evt.getButton() == MouseEvent.BUTTON3) {
                        m_bookListTable.setRowSelectionInterval(row, row);//we focus the row when we right on the item
                        m_popup.show(contentPane, evt.getX(), evt.getY());//show a popup to edit the reading
                    }
                //}
            }
        });
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if(m_bookListTable.getRowCount()>1){//If there is more than one reading you don't need to know if the person really wants to delete the book
                    String sql = "DELETE FROM BookManager WHERE Title='"+getTitle()+"' AND Author='"+getAuthor()+"' AND DateReading='"+getDateReading()+"'";//Delete in bdd the item that we want delete
                    try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        // execute the delete statement
                        pstmt.executeUpdate();
                        contentPane.updateUI();
                        BookListPanel.removeAll();
                        fillBookList();

                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                else{
                    JFrame jFrame = new JFrame();
                    int n = JOptionPane.showConfirmDialog(//Open a optionPane to verify if the user really want to delete the book return 0 il they want and 1 if they refuse
                            jFrame,
                            "Cette acion supprimeras complétement le livre et sera irréversible. \n"+"Etes-vous sûr de vouloir le faire ?",
                            "An Inane Question",
                            JOptionPane.YES_NO_OPTION);
                    if(n==0){
                        String sql = "DELETE FROM BookManager WHERE Title='"+getTitle()+"' AND Author='"+getAuthor()+"' AND DateReading='"+getDateReading()+"'";//Delete in bdd the item that we want delete
                        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                            // execute the delete statement
                            pstmt.executeUpdate();
                            contentPane.updateUI();
                            BookListPanel.removeAll();
                            fillBookList();

                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        });
    }

    private Connection connect() {
        //SQLite connection string
        String url = "jdbc:sqlite:BookManager.db";
        try {
            m_connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return m_connection;
    }
    public String getAuthor() {
        return m_author;
    }
    public String getTitle() {
        return m_title;
    }
    public String getDateReading() {
        return m_dateReading;
    }
    public boolean isEmpty() {
        return m_isEmpty;
    }

    public void setAuthor(String m_author) {
        this.m_author = m_author;
    }
    public void setTitle(String m_title) {
        this.m_title = m_title;
    }
    public void setIsEmpty(boolean m_isEmpty) {
        this.m_isEmpty = m_isEmpty;
    }
    public void setDateReading(String m_dateReading) {
        this.m_dateReading = m_dateReading;
    }
    public void fillBookList(){
        m_tableModel.setRowCount(0);
        try{
            Class.forName("org.sqlite.JDBC");
            m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            m_statement = m_connection.createStatement();
            ResultSet qry = m_statement.executeQuery("SELECT Title, Author, DateReading FROM BookManager WHERE Title='"+getTitle()+"' AND Author='"+getAuthor()+ "'");//Retrieved from the bdd the URL of the book image

            while (qry.next()){
                String title = qry.getString("Title");//Retrieve the title
                String author = qry.getString("Author");//Retrieve the author
                String dateReading = qry.getString("DateReading");

                String[] header = {"Titre","Auteur","Date de lecture"};
                Object[] data = {title, author, dateReading};

                m_tableModel.setColumnIdentifiers(header);//Create the header
                m_tableModel.addRow(data);//add to tablemodel the data

                m_bookListTable.setModel(m_tableModel);
                m_bookListTable.setFocusable(false);
                m_pane = new JScrollPane(m_bookListTable);//Create a scrollpane with the Jtable for the error that did not display the header

                BookListPanel.add(m_pane);//add the scrolpane to our Jpanel
            }
            qry.close();
            m_connection.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}