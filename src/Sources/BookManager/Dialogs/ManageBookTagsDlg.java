package Sources.BookManager.Dialogs;

import Sources.Components.MyManagerComboBox;
import Sources.Components.Tags;
import Sources.Dialogs.EditTagDlg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import static Sources.BookManager.CommonBookManagerSQL.getIdBook;
import static Sources.Common.*;
import static Sources.CommonSQL.*;
import static Sources.Dialogs.OpenDialogs.openEditTagDlg;

public class ManageBookTagsDlg extends JDialog {
    private JPanel contentPane;
    private JButton TagCancelBtn;
    private JPanel TagsPanel;
    private JPanel AddTagPanel;
    private MyManagerComboBox AddTagCb;
    private Tags m_tags;
    private JPopupMenu m_popup;
    private int m_row;
    private final String m_title;
    private final String m_author;
    private JMenuItem m_cut;
    private JMenuItem m_edit;

    public ManageBookTagsDlg(String title , String author) {
        setContentPane(contentPane);
        setModal(true);

        this.m_title = title;
        this.m_author = author;
        this.m_tags = new Tags();
        setTags(loadTags(title, author, TagsPanel));

        initComponents();
        initPopupMenu();
        initListener();
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
    public void initComponents(){
        AddTagCb = new MyManagerComboBox(true);
        AddTagPanel.add(AddTagCb);
        fillTagsCB(AddTagCb);
    }
    public void initPopupMenu(){
        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        m_cut = new JMenuItem("Supprimer", new ImageIcon(getLogo("remove.png")));
        m_edit = new JMenuItem("Modifier", new ImageIcon(getLogo("edit.png")));
        m_popup.add(m_cut);
        m_popup.add(m_edit);
    }
    public void initListener(){
        initListenerTag(getTags(), m_popup, TagsPanel);
        TagCancelBtn.addActionListener((ActionEvent e)-> {
            setVisible(false);
            dispose();
        });
        this.m_cut.addActionListener((ActionEvent evt)-> {
            Component[] componentList = TagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    String TaggingQry = "DELETE FROM Tagging WHERE IdTag='"+getIdTag(getTags().getTag(i).getTextTag(), getTags().getTag(i).getColor())+"' " +
                            "AND idBook='"+getIdBook(this.m_title,this.m_author)+"'";
                    try (Connection conn = connect(); PreparedStatement TaggingSuppPstmt = conn.prepareStatement(TaggingQry)) {
                        TaggingSuppPstmt.executeUpdate();
                        setTags(loadTags(this.m_title, this.m_author, TagsPanel));
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
        this.m_edit.addActionListener((ActionEvent evt) ->{
            Component[] componentList = TagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    EditTagDlg diag = openEditTagDlg(getTags().getTag(i));
                    if(diag.isValide()){
                        if(!diag.getNewTextTag().equals(getTags().getTag(i).getTextTag())){
                            String TaggingSuppQry = "DELETE FROM Tagging WHERE IdTag='"+getIdTag(getTags().getTag(i).getTextTag(), getTags().getTag(i).getColor())+"' AND IdBook='"+getIdBook(this.m_title, this.m_author)+"'";
                            String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                                    "VALUES (?,?);";
                            String TagsQry = "INSERT INTO Tags (Tag,Color)"+
                                    "VALUES (?,?);";
                            try (Connection conn = connect(); PreparedStatement TagsInsertPstmt = conn.prepareStatement(TagsQry);
                                 PreparedStatement TaggingInsertPstmt = conn.prepareStatement(TaggingQry); PreparedStatement TaggingSuppPstmt = conn.prepareStatement(TaggingSuppQry)) {
                                TaggingSuppPstmt.executeUpdate();

                                TagsInsertPstmt.setString(1, diag.getNewTextTag());
                                TagsInsertPstmt.setInt(2, diag.getNewColorTag().getRGB());
                                TagsInsertPstmt.executeUpdate();

                                TaggingInsertPstmt.setInt(1, getIdBook(this.m_title, this.m_author));
                                TaggingInsertPstmt.setInt(2, getIdTag(diag.getNewTextTag(), diag.getNewColorTag().getRGB()));
                                TaggingInsertPstmt.executeUpdate();
                            }catch (SQLException e) {
                                JFrame jf = new JFrame();
                                JOptionPane.showMessageDialog(jf, e.getMessage(), "Edition tag impossible", JOptionPane.ERROR_MESSAGE);
                                throw new RuntimeException(e.getMessage());
                            }
                        }
                        else{
                            String TagsUpdateQry = "UPDATE Tags SET Color=?"+
                                    "WHERE Tag='"+getTags().getTag(i).getTextTag()+"'";

                            try (Connection conn = connect(); PreparedStatement TagsUpdatePstmt = conn.prepareStatement(TagsUpdateQry)) {
                                TagsUpdatePstmt.setInt(1, diag.getNewColorTag().getRGB());
                                TagsUpdatePstmt.executeUpdate();
                            }catch (SQLException e) {
                                JFrame jf = new JFrame();
                                JOptionPane.showMessageDialog(jf, e.getMessage(), "Edition tag impossible", JOptionPane.ERROR_MESSAGE);
                                throw new RuntimeException(e.getMessage());
                            }
                        }
                        setTags(loadTags(this.m_title, this.m_author, TagsPanel));
                        contentPane.updateUI();
                        break;
                    }
                }
                i++;
            }
            fillTagsCB(AddTagCb);
            initListenerTag(getTags(), m_popup, TagsPanel);
            TagsPanel.updateUI();
        });
        AddTagCb.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                if (!Objects.equals(AddTagCb.getEditor().getItem().toString(), "")) {
                    if (evt.getKeyCode() != KeyEvent.VK_DELETE && evt.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
                        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                            int numberTags = TagsPanel.getComponents().length;
                            boolean tagFind = fillPaneTags(getTags(), TagsPanel, AddTagCb, true);
                            if (!tagFind) {
                                // if number of tag rise, so we can create the tag, else it's that we've canceled the creation of tag
                                if (numberTags < TagsPanel.getComponents().length) {
                                    getTags().getTag(getTags().getSizeTags() - 1).setBorderColor(contentPane.getBackground());
                                    String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                                            "VALUES (?,?);";
                                    try (Connection conn = connect(); PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry)) {
                                        String TagsInsertQry = "INSERT INTO Tags (Tag,Color)" +
                                                " SELECT '" + getTags().getTag(getTags().getSizeTags() - 1).getTextTag() + "', '" + getTags().getTag(getTags().getSizeTags() - 1).getColor() + "'" +
                                                " WHERE NOT EXISTS(SELECT * FROM Tags WHERE Tag='" + getTags().getTag(getTags().getSizeTags() - 1).getTextTag() + "' AND Color='" + getTags().getTag(getTags().getSizeTags() - 1).getColor() + "')";
                                        PreparedStatement TagsInsertPstmt = conn.prepareStatement(TagsInsertQry);
                                        TagsInsertPstmt.executeUpdate();

                                        TaggingPstmt.setInt(1, getIdBook(m_title, m_author));
                                        TaggingPstmt.setInt(2, getIdTag(getTags().getTag(getTags().getSizeTags() - 1).getTextTag(), getTags().getTag(getTags().getSizeTags() - 1).getColor()));
                                        TaggingPstmt.executeUpdate();

                                        fillTagsCB(AddTagCb);
                                    } catch (SQLException e) {
                                        JFrame jf = new JFrame();
                                        JOptionPane.showMessageDialog(jf, e.getMessage(), "Ajout tag impossible", JOptionPane.ERROR_MESSAGE);
                                        throw new RuntimeException(e.getMessage());
                                    }
                                }
                            }
                        } else {
                            AddTagCb.searchItemCB();
                        }
                    }
                }
                else{
                    AddTagCb.setSelectedIndex(0);
                }
                initListenerTag(getTags(), m_popup, TagsPanel);
                TagsPanel.updateUI();
            }
        });
        AddTagCb.getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                AddTagCb.showPopup();
            }
        });
    }
}
