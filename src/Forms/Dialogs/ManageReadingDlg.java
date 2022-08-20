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
    private int m_row;

    public ManageReadingDlg(String title, String author) {
        setContentPane(contentPane);
        setMTitle(title);
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
                setRow(m_bookListTable.rowAtPoint(evt.getPoint()));
                setMTitle(m_bookListTable.getValueAt(getRow(), 0).toString()); //get the value of the column of the table
                setAuthor(m_bookListTable.getValueAt(getRow(), 1).toString());
                setDateReading(m_dateReading = m_bookListTable.getValueAt(getRow(), 2).toString());
                if(evt.getButton() == MouseEvent.BUTTON3) {
                    m_bookListTable.setRowSelectionInterval(getRow(), getRow());//we focus the row when we right on the item
                    m_popup.show(contentPane, evt.getX(), evt.getY());//show a popup to edit the reading
                }
            }
        });
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if(m_bookListTable.getRowCount()>1){//If there is more than one reading you don't need to know if the person really wants to delete the book
                    String sql = "DELETE FROM BookManager WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND DateReading='"+getDateReading()+"'";//Delete in bdd the item that we want delete
                    try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        // execute the delete statement
                        pstmt.executeUpdate();
                        contentPane.updateUI();
                        BookListPanel.removeAll();
                        fillBookList();
                        conn.close();
                        pstmt.close();

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
                        String sql = "DELETE FROM BookManager WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND DateReading='"+getDateReading()+"'";//Delete in bdd the item that we want delete
                        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                            // execute the delete statement
                            pstmt.executeUpdate();
                            contentPane.updateUI();
                            BookListPanel.removeAll();
                            fillBookList();
                            conn.close();
                            pstmt.close();

                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        });
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                EditReadingDlg diag = new EditReadingDlg(getMTitle(), getAuthor(), getDateReading());//Open a dialog where we can edit the date reading
                diag.setTitle("Modifier une lecture");
                diag.setSize(500,150);
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);

                if(diag.isValid()){
                    String sql = "UPDATE BookManager SET DateReading='"+diag.getBookNewDateReading()+"' WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND DateReading='"+getDateReading()+"'";//Edit in bdd the item that we want to change the reading date
                    try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        // execute the uptdate statement
                        pstmt.executeUpdate();
                        contentPane.updateUI();
                        BookListPanel.removeAll();
                        fillBookList();
                        m_bookListTable.setRowSelectionInterval(getRow(), getRow());//Focus on the reading that we edit
                        conn.close();
                        pstmt.close();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
    }

    private Connection connect() {
        Connection connection = null;
        String url = "jdbc:sqlite:BookManager.db";
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }
    public String getAuthor() {
        return m_author;
    }
    public String getMTitle() {
        return m_title;
    }
    public String getDateReading() {
        return m_dateReading;
    }
    public boolean isEmpty() {
        return m_isEmpty;
    }
    public int getRow() {
        return m_row;
    }

    public void setAuthor(String m_author) {
        this.m_author = m_author;
    }
    public void setMTitle(String m_title) {
        this.m_title = m_title;
    }
    public void setIsEmpty(boolean m_isEmpty) {
        this.m_isEmpty = m_isEmpty;
    }
    public void setDateReading(String m_dateReading) {
        this.m_dateReading = m_dateReading;
    }
    public void setRow(int m_row) {
        this.m_row = m_row;
    }
    public void fillBookList(){
        m_tableModel.setRowCount(0);
        try(Connection conn = connect()){
            m_statement = conn.createStatement();
            ResultSet qry = m_statement.executeQuery("SELECT Title, Author, DateReading FROM BookManager WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+ "'");//Retrieved from the bdd the URL of the book image

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
            conn.close();
            m_statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
