package Sources.Dialogs;

import Sources.Tags;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Objects;

import static Sources.Common.*;
import static Sources.CommonSQL.*;
import static Sources.Dialogs.OpenDialog.openEditTagDlg;

public class ManageTagsDlg extends JDialog {
    private JPanel contentPane;
    private JButton TagCancelBtn;
    private JPanel TagsPanel;
    private JPanel AddTagPanel;
    private final JComboBox AddTagCb = new JComboBox<>();
    private final JTextField AddTagTxtF = new JTextField();
    private Tags m_tags;
    final JPopupMenu m_popup;
    private int m_row;
    private int m_TagsNumber;

    public ManageTagsDlg() {
        setContentPane(contentPane);
        setModal(true);
        this.m_tags = new Tags();
        fillTagsList();

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
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
                        fillTagsList();
                        contentPane.updateUI();
                        break;
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        JFrame jf = new JFrame();
                        JOptionPane.showMessageDialog(jf, e.getMessage(), "Supression tag impossible", JOptionPane.ERROR_MESSAGE);
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
                            fillTagsList();
                            contentPane.updateUI();
                            break;
                        }catch (SQLException e) {
                            System.out.println(e.getMessage());
                            JFrame jf = new JFrame();
                            JOptionPane.showMessageDialog(jf, e.getMessage(), "Edition tag impossible", JOptionPane.ERROR_MESSAGE);
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
                            //TagsPanel.setPreferredSize(new Dimension(400, (m_TagsNumber+1)*11));
                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                            JFrame jf = new JFrame();
                            JOptionPane.showMessageDialog(jf, e.getMessage(), "Ajout tag impossible", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
            initListenerTag(getTags(), m_popup, TagsPanel);
            TagsPanel.updateUI();
            }
        });
    }
    public ManageTagsDlg(String title , String author) {
        setContentPane(contentPane);
        setModal(true);
        this.m_tags = new Tags();
        fillTagsList(title, author);

        AddTagCb.setEditable(true);
        AddTagPanel.add(AddTagCb);
        fillThemeCB();

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
        m_popup.add(cut);
        m_popup.add(edit);

        TagCancelBtn.addActionListener((ActionEvent e)-> {
            setVisible(false);
            dispose();
        });

        initListenerTag(getTags(), m_popup, TagsPanel);

        cut.addActionListener((ActionEvent evt)-> {
            Component[] componentList = TagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    String TaggingQry = "DELETE FROM Tagging WHERE IdTag='"+getIdTag(getTags().getTag(i).getTextTag(), getTags().getTag(i).getColor())+"' " +
                            "AND idBook='"+getIdBook(title,author)+"'";
                    try (Connection conn = connect(); PreparedStatement TaggingSuppPstmt = conn.prepareStatement(TaggingQry)) {
                        TaggingSuppPstmt.executeUpdate();
                        fillTagsList(title, author);
                        contentPane.updateUI();
                        break;
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        JFrame jf = new JFrame();
                        JOptionPane.showMessageDialog(jf, e.getMessage(), "Supression tag impossible", JOptionPane.ERROR_MESSAGE);
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
                        if(!diag.getNewTextTag().equals(getTags().getTag(i).getTextTag())){
                            String TaggingSuppQry = "DELETE FROM Tagging WHERE IdTag='"+getIdTag(getTags().getTag(i).getTextTag(), getTags().getTag(i).getColor())+"' AND IdBook='"+getIdBook(title, author)+"'";
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

                                TaggingInsertPstmt.setInt(1, getIdBook(title, author));
                                TaggingInsertPstmt.setInt(2, getIdTag(diag.getNewTextTag(), diag.getNewColorTag().getRGB()));
                                TaggingInsertPstmt.executeUpdate();
                            }catch (SQLException e) {
                                System.out.println(e.getMessage());
                                JFrame jf = new JFrame();
                                JOptionPane.showMessageDialog(jf, e.getMessage(), "Edition tag impossible", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        else{
                            String TagsUpdateQry = "UPDATE Tags SET Color=?"+
                                    "WHERE Tag='"+getTags().getTag(i).getTextTag()+"'";

                            try (Connection conn = connect(); PreparedStatement TagsUpdatePstmt = conn.prepareStatement(TagsUpdateQry)) {
                                TagsUpdatePstmt.setInt(1, diag.getNewColorTag().getRGB());
                                TagsUpdatePstmt.executeUpdate();
                            }catch (SQLException e) {
                                System.out.println(e.getMessage());
                                JFrame jf = new JFrame();
                                JOptionPane.showMessageDialog(jf, e.getMessage(), "Edition tag impossible", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        fillTagsList(title, author);
                        contentPane.updateUI();
                        break;
                    }
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, TagsPanel);
            TagsPanel.updateUI();
        });
        AddTagCb.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
            if (!Objects.equals(AddTagCb.getSelectedItem(), "")) {
                if (evt.getKeyCode()== KeyEvent.VK_ENTER){
                    boolean tagFind = fillPaneTags(getTags(), TagsPanel, AddTagCb,true);
                    if(!tagFind) {
                        getTags().getTag(getTags().getSizeTags()-1).setBorderColor(contentPane.getBackground());
                        String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                                "VALUES (?,?);";
                        try (Connection conn = connect(); PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry)) {


                            String TagsInsertQry = "INSERT INTO Tags (Tag,Color)" +
                                    " SELECT '" + getTags().getTag(getTags().getSizeTags() - 1).getTextTag() + "', '" + getTags().getTag(getTags().getSizeTags() - 1).getColor() + "'" +
                                    " WHERE NOT EXISTS(SELECT * FROM Tags WHERE Tag='" + getTags().getTag(getTags().getSizeTags() - 1).getTextTag() + "' AND Color='" + getTags().getTag(getTags().getSizeTags() - 1).getColor() + "')";
                            PreparedStatement TagsInsertPstmt = conn.prepareStatement(TagsInsertQry);
                            TagsInsertPstmt.executeUpdate();

                            TaggingPstmt.setInt(1, getIdBook(title, author));
                            TaggingPstmt.setInt(2, getIdTag(getTags().getTag(getTags().getSizeTags() - 1).getTextTag(), getTags().getTag(getTags().getSizeTags() - 1).getColor()));
                            TaggingPstmt.executeUpdate();
                            m_TagsNumber=m_TagsNumber+1;
                            //TagsPanel.setPreferredSize(new Dimension(400, (m_TagsNumber)*15));
                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                            JFrame jf = new JFrame();
                            JOptionPane.showMessageDialog(jf, e.getMessage(), "Ajout tag impossible", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
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
            AddTagCb.setSelectedIndex(0);
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

        try(Connection conn = connect()){
            Statement statement = conn.createStatement();
            ResultSet qry = statement.executeQuery("SELECT Tag, Color FROM Tags ORDER BY Tag ASC");

            while (qry.next()){
                m_TagsNumber = qry.getRow();
                String textTag = qry.getString(1);
                int colorTag = qry.getInt(2);

                tags.createTag(textTag);
                tags.getTag(tags.getSizeTags()-1).setColor(colorTag);
                tags.getTag(tags.getSizeTags()-1).setBorderColor(contentPane.getBackground());

                TagsPanel.add(tags.getTag(tags.getSizeTags()-1));
            }
            //TagsPanel.setPreferredSize(new Dimension(400, m_TagsNumber*11));
            setTags(tags);

            qry.close();
            conn.close();
            statement.close();
        } catch ( Exception e ) {
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement des tags impossible", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void fillTagsList(String title, String author){
        TagsPanel.removeAll();
        Tags tags = new Tags();
        try(Connection conn = connect()){
            Statement statement = conn.createStatement();
            String strQry = "SELECT Tag, Color FROM Tags ";
            strQry = strQry+
                    "INNER JOIN Tagging ON Tags.ID=Tagging.IdTag " +
                    "INNER JOIN Book ON Tagging.idBook=Book.ID " +
                    "WHERE Book.ID='" + getIdBook(title, author) + "' ";
            strQry = strQry +"ORDER BY Tag ASC";

            ResultSet qry = statement.executeQuery(strQry);
            while (qry.next()){
                m_TagsNumber = qry.getRow();
                String textTag = qry.getString(1);
                int colorTag = qry.getInt(2);

                tags.createTag(textTag);
                tags.getTag(tags.getSizeTags()-1).setColor(colorTag);
                tags.getTag(tags.getSizeTags()-1).setBorderColor(contentPane.getBackground());

                TagsPanel.add(tags.getTag(tags.getSizeTags()-1));
            }
            //TagsPanel.setPreferredSize(new Dimension(400, m_TagsNumber*15));
            setTags(tags);

            qry.close();
            conn.close();
            statement.close();
        } catch ( Exception e ) {
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement des tags impossible", JOptionPane.ERROR_MESSAGE);
        }
    }
    @SuppressWarnings("unchecked")
    public void fillThemeCB(){
        this.AddTagCb.addItem("");
        for (int i = 0; i<loadTags().getSizeTags(); i++){
            this.AddTagCb.addItem(loadTags().getTag(i).getTextTag());
        }
    }
}
