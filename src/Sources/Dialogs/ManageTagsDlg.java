package Sources.Dialogs;

import Sources.Components.Tags;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Objects;

import static Sources.Common.*;
import static Sources.Dialogs.OpenDialogs.*;
import static Sources.CommonSQL.*;

public class ManageTagsDlg extends JDialog {
    private JPanel contentPane;
    private JButton TagCancelBtn;
    private JPanel TagsPanel;
    private JPanel AddTagPanel;
    private final JTextField AddTagTxtF = new JTextField();
    private Tags m_tags;
    final JPopupMenu m_popup;
    private int m_row;

    public ManageTagsDlg() {
        setContentPane(contentPane);
        setModal(true);
        this.m_tags = new Tags();
        setTags(loadTags(TagsPanel));

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getLogo("remove.png")));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getLogo("edit.png")));
        m_popup.add(cut);
        m_popup.add(edit);

        TagCancelBtn.addActionListener((ActionEvent e)-> {
            setVisible(false);
            dispose();
        });
        AddTagPanel.add(AddTagTxtF);
        initListenerTag(getTags(), m_popup, TagsPanel);

        cut.addActionListener((ActionEvent evt)-> {
            Component[] componentList = TagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    String Tags = "DELETE FROM Tags WHERE Tag='"+getTags().getTag(    i).getTextTag()+"'";
                    String TaggingQry = "DELETE FROM Tagging WHERE IdTag='"+getIdTag(getTags().getTag(i).getTextTag(), getTags().getTag(i).getColor())+"'";
                    try (Connection conn = connect(); PreparedStatement TaggingPstmt = conn.prepareStatement(Tags); PreparedStatement TagsPstmt = conn.prepareStatement(TaggingQry)) {
                        TagsPstmt.executeUpdate();
                        TaggingPstmt.executeUpdate();
                        setTags(loadTags(TagsPanel));
                        contentPane.updateUI();
                        break;
                    } catch (SQLException e) {
                        JFrame jf = new JFrame();
                        JOptionPane.showMessageDialog(jf, e.getMessage(), "Supression tag impossible", JOptionPane.ERROR_MESSAGE);
                        throw new RuntimeException(e.getMessage());
                    }
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, TagsPanel);
            TagsPanel.updateUI();
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
                            setTags(loadTags(TagsPanel));
                            contentPane.updateUI();
                            break;
                        }catch (SQLException e) {
                            JFrame jf = new JFrame();
                            JOptionPane.showMessageDialog(jf, e.getMessage(), "Edition tag impossible", JOptionPane.ERROR_MESSAGE);
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, TagsPanel);
            TagsPanel.updateUI();
        });
        AddTagTxtF.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
            if (!Objects.equals(AddTagTxtF.getText(), "")) {
                if (evt.getKeyCode()== KeyEvent.VK_ENTER){
                    boolean tagFind = fillPaneTags(getTags(), TagsPanel, AddTagTxtF);
                    if(!tagFind) {
                        getTags().getTag(getTags().getSizeTags()-1).setBorderColor(contentPane.getBackground());
                        try (Connection conn = connect()) {
                            String TagsInsertQry = "INSERT INTO Tags (Tag,Color)" +
                                    " SELECT '" + getTags().getTag(getTags().getSizeTags() - 1).getTextTag() + "', '" + getTags().getTag(getTags().getSizeTags() - 1).getColor() + "'" +
                                    " WHERE NOT EXISTS(SELECT * FROM Tags WHERE Tag='" + getTags().getTag(getTags().getSizeTags() - 1).getTextTag() + "' AND Color='" + getTags().getTag(getTags().getSizeTags() - 1).getColor() + "')";
                            PreparedStatement TagsInsertPstmt = conn.prepareStatement(TagsInsertQry);
                            TagsInsertPstmt.executeUpdate();
                        } catch (SQLException e) {
                            JFrame jf = new JFrame();
                            JOptionPane.showMessageDialog(jf, e.getMessage(), "Ajout tag impossible", JOptionPane.ERROR_MESSAGE);
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                }
            }
            initListenerTag(getTags(), m_popup, TagsPanel);
            TagsPanel.updateUI();
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
}
