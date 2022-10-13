package Sources.Dialogs;

import Sources.RoundBorderCp;
import Sources.Tags;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

import static Sources.Common.*;

public class ManageTagsDlg extends JDialog {
    private JPanel contentPane;
    private JButton TagCancelBtn;
    private JTable TagsTable;
    private DefaultTableModel m_tableModel = new DefaultTableModel(){//Create a Jtable with the tablemodel not editable
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false; //Disallow the editing of any cell
        }
    };
    private Tags m_tags;
    private JPopupMenu m_popup;
    private int m_row;

    public ManageTagsDlg() {
        setContentPane(contentPane);
        setModal(true);
        this.m_tags = new Tags();
        fillTagsList();
        TagsTable.setRowSelectionInterval(0, 0);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
        m_popup.add(cut);
        m_popup.add(edit);

        TagCancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        TagsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                setRow(TagsTable.rowAtPoint(evt.getPoint()));
                if(evt.getButton() == MouseEvent.BUTTON3) {
                    TagsTable.setRowSelectionInterval(getRow(), getRow());//we focus the row when we right on the item
                    m_popup.show(TagsTable, evt.getX(), evt.getY());//show a popup to edit the reading
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
                    fillTagsList();
                    contentPane.updateUI();
                    conn.close();
                    TagsPstmt.close();
                    TaggingPstmt.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                EditTagDlg diag = new EditTagDlg(getTags().getTag(TagsTable.getSelectedRow()));
                diag.setTitle("Modifier le tag");
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);

                if(diag.isValide()){
                    String TagsUpdateQry = "UPDATE Tags SET Tag=?,Color=?"+
                            "WHERE Tag='"+getTags().getTag(TagsTable.getSelectedRow()).getTextTag()+"'";

                    try (Connection conn = connect(); PreparedStatement TagsUpdatePstmt = conn.prepareStatement(TagsUpdateQry)) {
                        TagsUpdatePstmt.setString(1, diag.getNewTextTag());
                        TagsUpdatePstmt.setInt(2, diag.getNewColorTag().getRGB());
                        TagsUpdatePstmt.executeUpdate();

                        fillTagsList();
                    }catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
    }

    public int getRow() {
        return m_row;
    }

    public Tags getTags(){
        return this.m_tags;
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

            TagsTable.setModel(m_tableModel);
            AbstractBorder roundBrdMax = new RoundBorderCp(contentPane.getBackground(),1,30, 0,0,0);
            AbstractBorder roundBrdMin = new RoundBorderCp(contentPane.getBackground(),1,30, 400-(TagsTable.getRowCount()*TagsTable.getRowHeight()),0,0);
            if(TagsTable.getRowCount()>11)
                TagsTable.setBorder(roundBrdMax);
            else
                TagsTable.setBorder(roundBrdMin);
            TagsTable.setRowSelectionInterval(0, 0);
            qry.close();
            conn.close();
            statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
