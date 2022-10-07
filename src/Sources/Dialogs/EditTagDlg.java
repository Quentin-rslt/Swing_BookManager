package Sources.Dialogs;

import Sources.Tag;
import Sources.Tags;

import javax.swing.*;
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

    public EditTagDlg(Tag tag) {
        setContentPane(contentPane);
        setModal(true);
        this.m_tag = tag;
        initComponents();

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
                System.out.println(getNewColorTag());
                if (!Objects.equals(getNewTextTag(), "") && !textTagFind()){
                    setIsUpdate(false);
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                }
                else if(!Objects.equals(getNewTextTag(), "") && textTagFind() && !colorTagFind()){
                    setIsUpdate(true);
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                }
                else if(textTagFind() && colorTagFind()){
                    setIsUpdate(false);
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                }
                else if(Objects.equals(getNewTextTag(), "")){
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !");
                }
            }
        });
    }
    public EditTagDlg(String tag, ArrayList<String> listOfCb, Tags tags) {
        setContentPane(contentPane);
        setModal(true);
        this.m_tags = tags;
        this.m_tag = new Tag();
        this.m_tag.setTextTag(tag);
        initComponents();

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
                //check if the tag is present in the list of tags (avoids duplication)
                boolean tagFind = false;
                int i = 0;
                while(!tagFind && i<getTags().getSizeTags()){
                    if(Objects.equals(getNewTextTag(), getTags().getTag(i).getTextTag())){
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "Le tag existe déjà !");
                        tagFind =true;
                    }
                    else i++;
                }
                boolean isInCB = false;
                //check if the tag is present in the comboBox (avoids duplication)
                for (String s : listOfCb) {
                    if (s.equals(getNewTextTag())) {
                        isInCB = true;
                    }
                }
                if(!tagFind){
                    if(!isInCB){
                        if (!Objects.equals(getNewTextTag(), "") && !textTagFind()){
                            setIsUpdate(false);
                            setIsValid(true);
                            setVisible(false);
                            dispose();
                        }
                        else if(!Objects.equals(getNewTextTag(), "") && textTagFind() && !colorTagFind()){
                            setIsUpdate(true);
                            setIsValid(true);
                            setVisible(false);
                            dispose();
                        }
                        else if(textTagFind() && colorTagFind()){
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "Ce tag existe déjà !");
                        }
                        else if(Objects.equals(getNewTextTag(), "")){
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !");
                        }
                    }
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
    public boolean isUpdate(){
        return this.m_isUpdate;
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

    public void setIsValid(boolean valid){
        this.m_isValid = valid;
    }
    public void setIsUpdate(boolean update){
        this.m_isUpdate = update;
    }
    public void initComponents(){
        TagColorPanel.add(this.m_ColorChooser);
        m_ColorChooser.setColor(getTag().getBackground());
        TagNameTextField.setText(getTag().getTextTag());
    }
}
