package Sources.BookManager;

import Sources.BookManager.Dialogs.EditReadingDlg;
import Sources.Components.MyManagerTable;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import static Sources.BookManager.CommonBookManagerSQL.*;
import static Sources.BookManager.Dialogs.BookManagerOpenDialog.openEditReadingDlg;
import static Sources.Common.getLogo;
import static Sources.CommonSQL.connect;

public class ManageReading {
    private final BookManager m_bookManager;
    private final MyManagerTable m_readingsTable;
    private final String m_title;
    private final String m_author;
    private String m_startReading = "";
    private String m_endReading = "";
    private JPopupMenu m_popup;
    private JMenuItem m_cut;
    private JMenuItem m_edit;

    public ManageReading(BookManager parent, MyManagerTable ReadingsTable) {
        this.m_bookManager = parent;
        this.m_readingsTable = ReadingsTable;
        this.m_title = parent.getMTitle();
        this.m_author= parent.getAuthor();

        initPopupMenu();
        initListener();
    }

    public String getStartReading() {
        return m_startReading;
    }
    public String getEndReading() {
        return m_endReading;
    }
    public int getRowCount(){
        return m_readingsTable.getRowCount();
    }

    public void setStartReading(String m_dateReading) {
        this.m_startReading = m_dateReading;
    }
    public void setEndReading(String m_dateReading) {
        this.m_endReading = m_dateReading;
    }
    public void resetIdReading(int rowCount){
        String ReadingQry = "DELETE FROM Reading WHERE Title='"+this.m_title+"' AND Author='"+this.m_author+"'";//clear all the table
        String InsetrQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                "VALUES (?,?,?,?,?);";
        try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement InsetrPstmt = conn.prepareStatement(InsetrQry)){
            ReadingPstmt.executeUpdate();//Delete all the table
            for(int i=0;i<rowCount; i++){//filled the table against the bookList
                InsetrPstmt.setInt(1, i);
                InsetrPstmt.setString(2, this.m_title);
                InsetrPstmt.setString(3, this.m_author);
                InsetrPstmt.setString(4, m_readingsTable.getValueAt(i, 0).toString());
                InsetrPstmt.setString(5, m_readingsTable.getValueAt(i, 1).toString());
                InsetrPstmt.executeUpdate();
            }
        } catch (SQLException e) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Remise à zéro id lecture impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public void initPopupMenu(){
        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        m_cut = new JMenuItem("Supprimer", new ImageIcon(getLogo("remove.png")));
        m_edit = new JMenuItem("Modifier", new ImageIcon(getLogo("edit.png")));
        m_popup.add(m_cut);
        m_popup.add(m_edit);

    }
    public void initListener(){
        initListenerReadingsTable();
        m_readingsTable.getActionMap().put("up", new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                if(m_bookManager.getRowReading()>0) {
                    m_bookManager.setRowReading(m_bookManager.getRowReading()-1);
                    m_readingsTable.setRowSelectionInterval(m_bookManager.getRowReading(), m_bookManager.getRowReading());
                    m_readingsTable.scrollFolowRow(m_bookManager.getRowReading());
                    setStartReading(m_readingsTable.getValueAt(m_bookManager.getRowReading(), 0).toString());
                    setEndReading(m_readingsTable.getValueAt(m_bookManager.getRowReading(), 1).toString());
                }
            }
        });
        m_readingsTable.getActionMap().put("dow", new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                if(m_bookManager.getRowReading()<m_readingsTable.getRowCount()-1) {
                    m_bookManager.setRowReading(m_bookManager.getRowReading()+1);
                    m_readingsTable.scrollFolowRow(m_bookManager.getRowReading());
                    m_readingsTable.setRowSelectionInterval(m_bookManager.getRowReading(), m_bookManager.getRowReading());
                    setStartReading(m_readingsTable.getValueAt(m_bookManager.getRowReading(), 0).toString());
                    setEndReading(m_readingsTable.getValueAt(m_bookManager.getRowReading(), 1).toString());
                }
            }
        });
        m_readingsTable.getActionMap().put("tab", new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                if(m_bookManager.getBooksTable().getRowCount()>0) {
                    m_bookManager.getBookFastSearch().requestFocusInWindow();
                }
            }
        });

        m_readingsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                m_readingsTable.requestFocusInWindow();
                m_bookManager.setRowReading(m_readingsTable.rowAtPoint(evt.getPoint()));
                m_readingsTable.setRowSelectionInterval(m_bookManager.getRowReading(), m_bookManager.getRowReading());//we focus the row when we right on the item

                setStartReading( m_readingsTable.getValueAt(m_bookManager.getRowReading(), 0).toString());
                setEndReading(m_readingsTable.getValueAt(m_bookManager.getRowReading(), 1).toString());
                if(evt.getButton() == MouseEvent.BUTTON3) {
                    m_readingsTable.setRowSelectionInterval(m_bookManager.getRowReading(), m_bookManager.getRowReading());//we focus the row when we right on the item
                    m_popup.show(m_readingsTable, evt.getX(), evt.getY());//show a popup to edit the reading
                }
                if(evt.getClickCount() == 2 && evt.getButton() == MouseEvent.BUTTON1){
                    EditReadingDlg diag = openEditReadingDlg(getStartReading(), getEndReading(), m_bookManager.getMTitle(), m_bookManager.getAuthor());//Open a dialog where we can edit the date reading
                    editReading(diag, m_bookManager);
                }
            }
        });
        this.m_cut.addActionListener((ActionEvent evt) ->deleteReading(m_bookManager));
        this.m_edit.addActionListener((ActionEvent evt) ->{
            EditReadingDlg diag = openEditReadingDlg(getStartReading(), getEndReading(), m_bookManager.getMTitle(), m_bookManager.getAuthor());//Open a dialog where we can edit the date reading
            editReading(diag, m_bookManager);
        });
    }
    public void initListenerReadingsTable(){
        for(int i=0; i<m_readingsTable.getRowCount();i++){
            MouseListener[] mouseListeners =  m_readingsTable.getMouseListeners();
            for (MouseListener mouseListener : mouseListeners) {
                m_readingsTable.removeMouseListener(mouseListener);
            }
        }
    }
}
