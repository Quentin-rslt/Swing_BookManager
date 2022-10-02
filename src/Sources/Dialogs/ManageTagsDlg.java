package Sources.Dialogs;

import Sources.RoundBorderCp;
import Sources.Tags;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

    public ManageTagsDlg() {
        setContentPane(contentPane);
        setModal(true);
        this.m_tags = new Tags();
        fillTagsList();
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

    public void fillTagsList(){
        m_tableModel.setRowCount(0);
        try(Connection conn = connect()){
            Statement statement = conn.createStatement();
            ResultSet qry = statement.executeQuery("SELECT Tag, Color FROM Tags");

            while (qry.next()){
                String textTag = qry.getString(1);
                int colorTag = qry.getInt(2);

                getTags().createTag(textTag);
                getTags().getTag(getTags().getSizeTags()-1).setColor(colorTag);


                String[] header = {"Tags"};
                Object[] data = {getTags().getTag(qry.getRow()-1).getTextTag()};

                m_tableModel.setColumnIdentifiers(header);//Create the header
                m_tableModel.addRow(data);//add to tablemodel the data

                m_tagsListTable.setModel(m_tableModel);
                m_tagsListTable.setFocusable(false);
                JScrollPane pane = new JScrollPane(m_tagsListTable);//Create a scrollpane with the Jtable for the error that did not display the header
                AbstractBorder roundHeader = new RoundBorderCp(contentPane.getBackground(),1,30,0,0);
                AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),1,30, 400-(m_tagsListTable.getRowCount()*m_tagsListTable.getRowHeight()),0);
                m_tagsListTable.getTableHeader().setBorder(roundHeader);
                m_tagsListTable.setBorder(roundBrd);

                TagsListPanel.add(pane);//add the scrolpane to our Jpanel
            }
            qry.close();
            conn.close();
            statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
