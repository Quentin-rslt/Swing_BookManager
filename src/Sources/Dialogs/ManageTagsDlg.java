package Sources.Dialogs;

import Sources.RoundBorderCp;
import Sources.Tags;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

import static Sources.Common.*;
import static Sources.Dialogs.OpenDialog.openEditTagDlg;

public class ManageTagsDlg extends JDialog {
    private JPanel contentPane;
    private JButton TagCancelBtn;
    private JPanel TagsPanel;
    private Tags m_tags;
    final JPopupMenu m_popup;
    private int m_row;

    public ManageTagsDlg() {
        setContentPane(contentPane);
        setModal(true);
        this.m_tags = new Tags();
        fillTagsList();
        AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),3,30,0,0,0);
        TagsPanel.setBackground(new Color(51,45,45));
        TagsPanel.setBorder(roundBrd);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
        m_popup.add(cut);
        m_popup.add(edit);

        TagCancelBtn.addActionListener((ActionEvent e)-> {
            setVisible(false);
            dispose();
        });

        for(int i=0; i<getTags().getSizeTags();i++){
            int finalI = i;
            getTags().getTag(i).addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                    getTags().getTag(finalI).setBackground(Color.white);
                    TagsPanel.updateUI();
                }
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                    getTags().getTag(finalI).setBackground(new Color(getTags().getTag(finalI).getColor()));
                    TagsPanel.updateUI();
                }
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if(!e.getComponent().getComponentAt(e.getX(),e.getY()).equals(TagsPanel)){
                        if(e.getButton() == MouseEvent.BUTTON3) {
                            m_popup.show(getTags().getTag(finalI), e.getX(), e.getY());//show a popup to edit the reading
                            m_popup.setInvoker(e.getComponent().getComponentAt(e.getX(),e.getY()));
                        }
                    }
                }
            });
        }

        cut.addActionListener((ActionEvent evt)-> {
            Component[] componentList = TagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    String Tags = "DELETE FROM Tags WHERE Tag='"+getTags().getTag(i).getTextTag()+"'";
                    String TaggingQry = "DELETE FROM Tagging WHERE IdTag='"+getIdTag(getTags().getTag(i).getTextTag(), getTags().getTag(i).getColor())+"'";
                    try (Connection conn = connect(); PreparedStatement TaggingPstmt = conn.prepareStatement(Tags); PreparedStatement TagsPstmt = conn.prepareStatement(TaggingQry)) {
                        TagsPstmt.executeUpdate();
                        TaggingPstmt.executeUpdate();
                        fillTagsList();
                        contentPane.updateUI();
                        break;
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                i++;
            }
        });
        edit.addActionListener((ActionEvent evt) ->{
            Component[] componentList = TagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    EditTagDlg diag = openEditTagDlg(getTags().getTag(i));
                    if(diag.isValide()){
                        String TagsUpdateQry = "UPDATE Tags SET Tag=?,Color=?"+
                                "WHERE Tag='"+getTags().getTag(i).getTextTag()+"'";

                        try (Connection conn = connect(); PreparedStatement TagsUpdatePstmt = conn.prepareStatement(TagsUpdateQry)) {
                            TagsUpdatePstmt.setString(1, diag.getNewTextTag());
                            TagsUpdatePstmt.setInt(2, diag.getNewColorTag().getRGB());
                            TagsUpdatePstmt.executeUpdate();
                            fillTagsList();
                            contentPane.updateUI();
                            break;
                        }catch (SQLException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
                i++;
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
        TagsPanel.removeAll();
        Tags tags = new Tags();
        TagsPanel.setPreferredSize(new Dimension(300, 170));
        try(Connection conn = connect()){
            Statement statement = conn.createStatement();
            ResultSet qry = statement.executeQuery("SELECT Tag, Color FROM Tags ORDER BY Tag ASC");

            while (qry.next()){
                String textTag = qry.getString(1);
                int colorTag = qry.getInt(2);

                tags.createTag(textTag);
                tags.getTag(tags.getSizeTags()-1).setColor(colorTag);
                tags.getTag(tags.getSizeTags()-1).setBorderColor(new Color(51,45,45));

                TagsPanel.add(tags.getTag(tags.getSizeTags()-1));
            }
            setTags(tags);

            qry.close();
            conn.close();
            statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
