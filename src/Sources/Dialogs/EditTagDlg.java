package Sources.Dialogs;

import Sources.Tag;
import Sources.Tags;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class EditTagDlg extends JDialog {
    private JPanel contentPane;
    private JPanel TagBtnPanel;
    private JButton TagOkBtn;
    private JButton TagCancelBtn;
    private JTextField TagNameTextField;
    private JPanel TagColorPanel;
    private JColorChooser m_ColorChooser = new JColorChooser();
    private Tag m_tag;
    private Tags m_tags;
    private boolean m_isValid =false;
    private boolean m_isUpdate = false;

    public EditTagDlg(Tag tag, Tags tags) {
        setContentPane(contentPane);
        setModal(true);
        this.m_tag = tag;
        this.m_tags = tags;
        initComponents();
        setSize(780,480);

        TagCancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {//Quit dlg without taking into account the input
                setIsValid(false);
                setVisible(false);
                dispose();
            }
        });
        TagOkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean tagSelect = false;
                if(getTags() != null){
                    int i = 0;
                    while(!tagSelect && i<getTags().getSizeTags()){
                        if(Objects.equals(getNewTextTag(), getTags().getTag(i).getTextTag())){
                            tagSelect =true;
                        }
                        else i++;
                    }
                }

                if(!tagSelect){//si le tag n'a pas été sélectionné
                    if(!textTagFind()){//n'est pas dans la bdd
                        if (!Objects.equals(getNewTextTag(), "")){
                            setIsUpdate(false);
                            setIsValid(true);
                            setVisible(false);
                            dispose();
                        }
                        else{
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !");
                        }
                    }
                    else{
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "Ce tag existe déjà !");
                    }
                }
                else if(getTag().getTextTag().equals(getNewTextTag()) && !colorTagFind()){//if we wan't to just edit the color of a tag
                    setIsUpdate(true);
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                }
                else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Ce tag existe déjà !");
                }
            }
        });
    }
    public Tags getTags(){
        return this.m_tags;
    }
    public Tag getTag(){
        return this.m_tag;
    }
    public String getNewTextTag(){
        return TagNameTextField.getText();
    }
    public Color getNewColorTag(){
        return m_ColorChooser.getColor();
    }
    public boolean isValide(){
        return this.m_isValid;
    }
    public boolean textTagFind(){
        boolean tagFind = false;
        int i = 0;
        String sql = "SELECT Tag FROM Tags";
        try (Connection conn = connect()){
            Statement statement = conn.createStatement();
            ResultSet tagsQry = statement.executeQuery(sql);
            while (tagsQry.next()){
                if(getNewTextTag().equals(tagsQry.getString(1))){
                    tagFind=true;
                }
            }
            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return tagFind;
    }
    public boolean colorTagFind(){
        boolean tagFind = false;
        int i = 0;
        String sql = "SELECT Color FROM Tags WHERE Tag='"+getNewTextTag()+ "'";
        try (Connection conn = connect()){
            Statement statement = conn.createStatement();
            ResultSet tagsQry = statement.executeQuery(sql);

            if(getNewColorTag().getRGB()==tagsQry.getInt(1)){
                tagFind=true;
            }

            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return tagFind;
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
    public boolean isUpdate(){
        return this.m_isUpdate;
    }

    public void setIsValid(boolean valid){
        this.m_isValid = valid;
    }
    public void setIsUpdate(boolean update){
        this.m_isUpdate = update;
    }
    public void initComponents(){

        //TagColorPanel.add(this.m_ColorChooser);
        m_ColorChooser.setColor(getTag().getBackground());
        AbstractColorChooserPanel[] panels = m_ColorChooser.getChooserPanels();
        for (AbstractColorChooserPanel panel: panels)
        {
            if ("HSL".equals(panel.getDisplayName()))
            {
                TagColorPanel.add(panel);
            }
        }
        TagColorPanel.add(m_ColorChooser.getPreviewPanel());
        TagNameTextField.setText(getTag().getTextTag());
    }
}
