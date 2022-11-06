package Sources;

import Sources.Dialogs.EditReadingDlg;
import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import static Sources.Common.*;
import static Sources.CommonSQL.*;

public class ManageReading {
    JTable m_readingsTable;
    private String m_title = "";
    private String m_author = "";
    private String m_startReading = "";
    private String m_endReading = "";
    final JPopupMenu m_popup;
    private int m_row;

    public ManageReading(MainWindow parent, String title, String author, JTable ReadingsTable) {
        this.m_readingsTable = ReadingsTable;
        setMTitle(title);
        setAuthor(author);
        parent.fillReadingsList(getMTitle(),getAuthor());
        if(ReadingsTable.getRowCount()>0)
            ReadingsTable.setRowSelectionInterval(0, 0);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
        m_popup.add(cut);
        m_popup.add(edit);


        ReadingsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
            setRow(ReadingsTable.rowAtPoint(evt.getPoint()));
            setStartReading(m_startReading = ReadingsTable.getValueAt(getRow(), 0).toString());
            setEndReading(m_endReading = ReadingsTable.getValueAt(getRow(), 1).toString());
            if(evt.getButton() == MouseEvent.BUTTON3) {
                ReadingsTable.setRowSelectionInterval(getRow(), getRow());//we focus the row when we right on the item
                m_popup.show(ReadingsTable, evt.getX(), evt.getY());//show a popup to edit the reading
            }
            }
        });
        cut.addActionListener((ActionEvent evt) ->{
            String ReadingQry = "DELETE FROM Reading WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+getRow()+"'";//Delete in bdd the item that we want delete
            String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=? WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
            if(ReadingsTable.getRowCount()>1){//If there is more than one reading you don't need to know if the person really wants to delete the book
                try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)) {
                    ReadingPstmt.executeUpdate();
                    AvNumPstmt.setInt(1, averageTime(getMTitle(), getAuthor()));
                    AvNumPstmt.setInt(2, getNumberOfReading(getMTitle(), getAuthor()));
                    AvNumPstmt.executeUpdate();

                    parent.getContentPanel().updateUI();
                    parent.fillReadingsList(getMTitle(),getAuthor());
                    resetIdReading(getMTitle(), getAuthor(), getRowCount());//refresh all ID in the table ReadingDate
                    ReadingsTable.setRowSelectionInterval(0, 0);

                    //load bdd in MainWindow
                    parent.loadDB(parent.isFiltered());
                    isItInFilteredBookList(getMTitle(),getAuthor(),parent);
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            else{
                deleteBook(getMTitle(),getAuthor(),parent);
            }
        });
        edit.addActionListener((ActionEvent evt) ->{
            EditReadingDlg diag = new EditReadingDlg(getMTitle(), getAuthor(),getStartReading(), getEndReading());//Open a dialog where we can edit the date reading
            diag.setIconImage(getImageEdit());
            diag.setTitle("Modifier une lecture");
            diag.setSize(500,210);
            diag.setLocationRelativeTo(null);
            diag.setVisible(true);

            if(diag.isValid()){
                String sql = "UPDATE Reading SET StartReading=?, EndReading=?" +
                        "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+getRow()+"'";//Edit in bdd the item that we want to change the reading date
                String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=? WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
                try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)) {
                    // execute the uptdate statement
                    pstmt.setString(1, diag.getNewStartReading());
                    pstmt.setString(2, diag.getNewEndReading());
                    pstmt.executeUpdate();

                    AvNumPstmt.setInt(1, averageTime(getMTitle(), getAuthor()));
                    AvNumPstmt.setInt(2, getNumberOfReading(getMTitle(), getAuthor()));
                    AvNumPstmt.executeUpdate();

                    parent.getContentPanel().updateUI();
                    parent.fillReadingsList(getMTitle(),getAuthor());
                    ReadingsTable.setRowSelectionInterval(getRow(), getRow());//Focus on the reading that we edit
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }

                //if the book is no longer in the filters then load on the first line
                parent.loadDB(parent.isFiltered());
                isItInFilteredBookList(getMTitle(),getAuthor(),parent);

            }
        });
    }


    public String getAuthor() {
        return m_author;
    }
    public String getMTitle() {
        return m_title;
    }
    public String getStartReading() {
        return m_startReading;
    }
    public String getEndReading() {
        return m_endReading;
    }
    public int getRow() {
        return m_row;
    }
    public int getRowCount(){
        return m_readingsTable.getRowCount();
    }

    public void setAuthor(String m_author) {
        this.m_author = m_author;
    }
    public void setMTitle(String m_title) {
        this.m_title = m_title;
    }
    public void setStartReading(String m_dateReading) {
        this.m_startReading = m_dateReading;
    }
    public void setEndReading(String m_dateReading) {
        this.m_endReading = m_dateReading;
    }
    public void setRow(int m_row) {
        this.m_row = m_row;
    }
    public void resetIdReading(String title, String author, int rowCount){
        String ReadingQry = "DELETE FROM Reading WHERE Title='"+title+"' AND Author='"+author+"'";//clear all the table
        String InsetrQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                "VALUES (?,?,?,?,?);";
        try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement InsetrPstmt = conn.prepareStatement(InsetrQry)){
            ReadingPstmt.executeUpdate();//Delete all the table
            for(int i=0;i<rowCount; i++){//filled the table against the bookList
                InsetrPstmt.setInt(1, i);
                InsetrPstmt.setString(2, title);
                InsetrPstmt.setString(3, author);
                InsetrPstmt.setString(4, m_readingsTable.getValueAt(i, 0).toString());
                InsetrPstmt.setString(5, m_readingsTable.getValueAt(i, 1).toString());
                InsetrPstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
