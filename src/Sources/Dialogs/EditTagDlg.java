package Sources.Dialogs;

import Sources.Components.Tag;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

import static Sources.CommonSQL.textTagFind;

public class EditTagDlg extends JDialog {
    private JPanel contentPane;
    private JButton TagOkBtn;
    private JButton TagResetBtn;
    private JTextField TagNameTextField;
    private JPanel TagColorPanel;
    final JColorChooser m_ColorChooser = new JColorChooser();
    final Tag m_tag;

    private boolean m_isValid =false;
    private boolean m_isUpdate = false;

    public EditTagDlg(Tag tag) {
        setContentPane(contentPane);
        setModal(true);

        this.m_tag = tag;

        initComponents();
        initListener();
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
        for (AbstractColorChooserPanel panel: panels) {
            if ("HSL".equals(panel.getDisplayName())) {
                TagColorPanel.add(panel);
            }
        }
        TagColorPanel.add(m_ColorChooser.getPreviewPanel());
        TagNameTextField.setText(getTag().getTextTag());
    }
    public void initListener(){
        TagResetBtn.addActionListener((ActionEvent e)-> {//Quit dlg without taking into account the input
            initComponents();
        });
        TagOkBtn.addActionListener((ActionEvent e) ->{
            if(!textTagFind(getNewTextTag())){//is not in DB
                if (!Objects.equals(getNewTextTag(), "")){
                    setIsUpdate(false);
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                }
                else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !", "Tag saisie invalide", JOptionPane.ERROR_MESSAGE);
                }
            }
            else if(getTag().getTextTag().equals(getNewTextTag())){//if we want to just edit the color of a tag
                setIsUpdate(true);
                setIsValid(true);
                setVisible(false);
                dispose();
            }
            else{
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Ce tag existe déjà !", "Tag saisie invalide", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
