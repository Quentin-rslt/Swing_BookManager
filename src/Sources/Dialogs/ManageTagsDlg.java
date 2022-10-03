package Sources.Dialogs;

import Sources.RoundBorderCp;
import Sources.Tags;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ManageTagsDlg extends JDialog {
    private JPanel contentPane;
    private JButton TagCancelBtn;
    private JPanel TagsListPanel;
    private DefaultTableModel m_tableModel = new DefaultTableModel();
    private Tags m_tags;

    private JTable  m_tagsListTable = new JTable(){//Create a Jtable with the tablemodel not editable
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };
    private JPopupMenu m_popup;
    private int m_row;

    public ManageTagsDlg() {
        setContentPane(contentPane);
        setModal(true);
        this.m_tags = new Tags();
        fillTagsList();
        m_tagsListTable.setRowSelectionInterval(0, 0);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        File fileRemove = new File("Ressource/Icons/remove.png");
        String pathRemove = fileRemove.getAbsolutePath();
        Image imgRemove = Toolkit.getDefaultToolkit().getImage(pathRemove);
        imgRemove = imgRemove.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(imgRemove));

        File fileEdit = new File("Ressource/Icons/edit.png");
        String pathEdit = fileEdit.getAbsolutePath();
        Image imgEdit = Toolkit.getDefaultToolkit().getImage(pathEdit);
        imgEdit = imgEdit.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(imgEdit));
        m_popup.add(cut);
        m_popup.add(edit);

        TagCancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        m_tagsListTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                setRow(m_tagsListTable.rowAtPoint(evt.getPoint()));
                if(evt.getButton() == MouseEvent.BUTTON3) {
                    m_tagsListTable.setRowSelectionInterval(getRow(), getRow());//we focus the row when we right on the item
                    m_popup.show(TagsListPanel, evt.getX(), evt.getY());//show a popup to edit the reading
                }
            }
        });

        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String Tags = "DELETE FROM Tags WHERE Tag='"+getTags().getTag(getRow()).getTextTag()+"'";
                String TaggingQry = "DELETE FROM Tagging WHERE IdTag='"+getIdTag(getTags().getTag(getRow()).getTextTag(), getTags().getTag(getRow()).getColor())+"'";
                try (Connection conn = connect(); PreparedStatement TaggingPstmt = conn.prepareStatement(Tags); PreparedStatement TagsPstmt = conn.prepareStatement(TaggingQry)) {
                    TagsPstmt.executeUpdate();
                    TaggingPstmt.executeUpdate();
                    TagsListPanel.removeAll();
                    fillTagsList();
                    m_tagsListTable.setRowSelectionInterval(0, 0);
                    contentPane.updateUI();
                    conn.close();
                    TagsPstmt.close();
                    TaggingPstmt.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
    }

    public int getRow() {
        return m_row;
    }
    public int getRowCount(){
        return m_tagsListTable.getRowCount();
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
    public Tags getTags(){
        return this.m_tags;
    }
    public int getIdTag(String tag, int color) {
        int i =0;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet idBook = statement.executeQuery("SELECT ID FROM Tags WHERE Tag='"+tag+"' AND Color='"+color+ "'");
            i=idBook.getInt(1);
            idBook.close();
            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return i;
    }

    public void setTags(Tags tags){
        this.m_tags = tags;
    }
    public void setRow(int m_row) {
        this.m_row = m_row;
    }
    public void fillTagsList(){
        m_tableModel.setRowCount(0);
        Tags tags = new Tags();
        try(Connection conn = connect()){
            Statement statement = conn.createStatement();
            ResultSet qry = statement.executeQuery("SELECT Tag, Color FROM Tags");

            while (qry.next()){
                String textTag = qry.getString(1);
                int colorTag = qry.getInt(2);

                tags.createTag(textTag);
                tags.getTag(tags.getSizeTags()-1).setColor(colorTag);

                String[] header = {"Tags"};
                Object[] data = {tags.getTag(qry.getRow()-1).getTextTag()};

                m_tableModel.setColumnIdentifiers(header);//Create the header
                m_tableModel.addRow(data);//add to tablemodel the data
            }
            setTags(tags);

            m_tagsListTable.setModel(m_tableModel);
            m_tagsListTable.setFocusable(false);
            JScrollPane pane = new JScrollPane(m_tagsListTable);//Create a scrollpane with the Jtable for the error that did not display the header
            AbstractBorder roundHeader = new RoundBorderCp(contentPane.getBackground(),1,30,0,0,0);
            AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),1,30, 400-(m_tagsListTable.getRowCount()*m_tagsListTable.getRowHeight()),0,0);
            m_tagsListTable.getTableHeader().setBorder(roundHeader);
            m_tagsListTable.setBorder(roundBrd);

            TagsListPanel.add(pane);//add the scrolpane to our Jpanel

            qry.close();
            conn.close();
            statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
