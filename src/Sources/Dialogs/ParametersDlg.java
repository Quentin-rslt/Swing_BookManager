package Sources.Dialogs;

import Sources.MainWindow;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

public class ParametersDlg extends JDialog {
    private JPanel contentPane;
    private JComboBox ParamAddReadingKey;
    private JComboBox ParamChoiceLangCB;
    private JButton ParamCreateThemeBtn;
    private JComboBox ParamChoiceThemeCB;
    private JButton ParamCancelBtn;
    private JButton ParamSaveBtn;
    private JComboBox ParamManageTagsKey;
    private JComboBox ParamEditKey;
    private JComboBox ParamDeleteKey;
    private JComboBox ParamAddReadingModif;
    private JComboBox ParamManageTagsModif;
    private JComboBox ParamEditModif;
    private JComboBox ParamDeleteModif;
    private JComboBox ParamAddBookModif;
    private JComboBox ParamAddBookKey;
    private JComboBox ParamManageAllTagsKey;
    private JComboBox ParamManageAllTagsModif;
    private JComboBox ParamCritKey;
    private JComboBox ParamCritModif;
    private JComboBox ParamResetKey;
    private JComboBox ParamResetModif;
    private JComboBox ParamLogKey;
    private JComboBox ParamLogModif;
    private JPanel LabelPanel;
    private final ArrayList<String> listOfshortcuts = new ArrayList<>();

    public ParametersDlg(MainWindow parent) {
        setContentPane(contentPane);
        setModal(true);
        initComponents(parent);
        ParamSaveBtn.addActionListener(e -> {
            listOfshortcuts.clear();
            fillListOfShortcut();
            if(shortcutsIsValid()) {
                //Keys
                parent.setAddBookKey(getParamAddBookKey());
                parent.setAddReadingKey(getParamAddReadingKey());
                parent.setEditKey(getParamEditKey());
                parent.setManageTagsKey(getParamManageTagsKey());
                parent.setDeletekey(getParamDeletekey());
                parent.setCritKey(getParamCritKey());
                parent.setManageAllTagsKey(getParamManageAllTagsKey());
                parent.setResetKey(getParamResetKey());
                //Modifiers
                parent.setAddBookModif(getParamAddBookModif());
                parent.setAddReadingModif(getParamAddReadingModif());
                parent.setEditModif(getParamEditModif());
                parent.setDeleteModif(getParamDeleteModif());
                parent.setManageTagsModif(getParamManageTagsModif());
                parent.setCritModif(getParamCritModif());
                parent.setManageAllTagsModif(getParamManageAllTagsModif());
                parent.setResetModif(getParamResetModif());

                save();
                parent.initBinding();
                setVisible(false);
                dispose();
            }
        });
        ParamCancelBtn.addActionListener(e -> {
            setVisible(false);
            dispose();
        });
    }
    public int getParamAddBookModif(){
        return valueModif(ParamAddBookModif);
    }
    public int getParamAddReadingModif(){
        return valueModif(ParamAddReadingModif);
    }
    public int getParamDeleteModif(){
        return valueModif(ParamDeleteModif);
    }
    public int getParamEditModif(){
        return valueModif(ParamEditModif);
    }
    public int getParamManageTagsModif(){
        return valueModif(ParamManageTagsModif);
    }
    public int getParamCritModif(){
        return valueModif(ParamCritModif);
    }
    public int getParamManageAllTagsModif(){
        return valueModif(ParamManageAllTagsModif);
    }
    public int getParamResetModif(){return valueModif(ParamResetModif);}
    public String getNameModifKey(JComboBox jComboBox){
        String name = "";
        if(Objects.requireNonNull(jComboBox.getSelectedItem()).equals("SHIFT")){
            name = "KEY_LOCATION_STANDARD";
        }
        if(Objects.requireNonNull(jComboBox.getSelectedItem()).equals("CONTROL")){
            name = "KEY_LOCATION_LEFT";
        }
        if(Objects.requireNonNull(jComboBox.getSelectedItem()).equals("ALT")){
            name = "VK_BACK_SPACE";
        }
        return name;
    }
    public int valueModif(JComboBox comboBox){
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(getNameModifKey(comboBox))) {
                    value = f.getInt(getNameModifKey(comboBox));
                    break;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
    public int getParamAddBookKey() {
        return valueKey(ParamAddBookKey);
    }
    public int getParamManageTagsKey() {
        return valueKey(ParamManageTagsKey);
    }
    public int getParamDeletekey() {
        return valueKey(ParamDeleteKey);
    }
    public int getParamEditKey() {
        return valueKey(ParamEditKey);
    }
    public int getParamAddReadingKey() {
        return valueKey(ParamAddReadingKey);
    }
    public int getParamCritKey() {
        return valueKey(ParamCritKey);
    }
    public int getParamManageAllTagsKey() {
        return valueKey(ParamManageAllTagsKey);
    }
    public int getParamResetKey(){return valueKey(ParamResetKey);}
    public int valueKey(JComboBox comboBox){
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                String name = f.getName();
                if(f.getName().contains("VK_")) {
                    name= f.getName().replace("VK_", "");
                }

                if (name.equals(comboBox.getSelectedItem())) {
                    value = f.getInt(f.getName());
                    break;
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
    public boolean shortcutsIsValid(){
        boolean valid = true;
        for (int j = 0; j< listOfshortcuts.size(); j++) {
            for (int i = 0; i< listOfshortcuts.size(); i++) {
                if(i!=j) {
                    if (listOfshortcuts.get(j).equals(listOfshortcuts.get(i))) {
                        messageError(j, i, false, false);
                        valid = false;
                    }
                }
            }
            if(!valid){
                break;
            }
            String[] shortCutAfter = listOfshortcuts.get(j).split("-");
            int modif = Integer.parseInt(shortCutAfter[0]);
            int key = Integer.parseInt(shortCutAfter[1]);
            if(modif == KeyEvent.KEY_LOCATION_LEFT && key == KeyEvent.VK_P){
                messageError(j,j,true, false);
                valid = false;
            }
            if(modif == KeyEvent.KEY_LOCATION_LEFT && key == KeyEvent.VK_Q){
                messageError(j, j,false, true);
                valid = false;
            }
        }

        return valid;
    }
    public void messageError(int j,int i, boolean param, boolean quit){
        String text;
        Component[] components = LabelPanel.getComponents();
        JPanel jPanelAfter = (JPanel) components[j];
        JLabel jLabelAfter = (JLabel) jPanelAfter.getComponent(0);

        JPanel jPanelBefore = (JPanel) components[i];
        JLabel jLabelBefore = (JLabel) jPanelBefore.getComponent(0);

        text = "Le raccourcie \""+jLabelAfter.getText()+"\"  existe déjà pour ";
        if(param && !quit){
            text = text +"accéder aux paramètres";
        }else if(!param && quit){
            text = text + "quitter l'application";
        }else{
            text = text + "le raccourcie \""+jLabelBefore.getText()+"\"";
        }
        JFrame jFrame = new JFrame();
        JOptionPane.showMessageDialog(jFrame, text, "Raccourcie déjà existant", JOptionPane.ERROR_MESSAGE);
    }
    public void fillListOfShortcut(){
        String addReadingShortCut = getParamAddReadingModif()+"-"+getParamAddReadingKey();
        listOfshortcuts.add(addReadingShortCut);

        String addBookShortCut = getParamAddBookModif()+"-"+getParamAddBookKey();
        listOfshortcuts.add(addBookShortCut);

        String addManageTagsShortCut = getParamManageTagsModif()+"-"+getParamManageTagsKey();
        listOfshortcuts.add(addManageTagsShortCut);

        String addEditShortCut = getParamEditModif()+"-"+getParamEditKey();
        listOfshortcuts.add(addEditShortCut);

        String addDeleteShortCut = getParamDeleteModif()+"-"+getParamDeletekey();
        listOfshortcuts.add(addDeleteShortCut);

        String addParamManageAllTagsShortCut = getParamManageAllTagsModif()+"-"+getParamManageAllTagsKey();
        listOfshortcuts.add(addParamManageAllTagsShortCut);

        String addCritShortCut = getParamCritModif()+"-"+getParamCritKey();
        listOfshortcuts.add(addCritShortCut);

        String addResetShortCut = getParamResetModif()+"-"+getParamResetKey();
        listOfshortcuts.add(addResetShortCut);
    }
    @SuppressWarnings("unchecked")
    public void fillParamCBKey(){
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                if(!f.getName().contains("KEY_")) {
                    String name = f.getName();
                    if (f.getName().contains("VK_")) {
                        name = f.getName().replace("VK_", "");
                    }

                    ParamAddReadingKey.addItem(name);
                    ParamEditKey.addItem(name);
                    ParamDeleteKey.addItem(name);
                    ParamManageTagsKey.addItem(name);
                    ParamAddBookKey.addItem(name);
                    ParamCritKey.addItem(name);
                    ParamResetKey.addItem(name);
                    ParamLogKey.addItem(name);
                    ParamManageAllTagsKey.addItem(name);
                }
            }
        }
    }
    public void fillParamCBModif(){
        fillCBModif(ParamAddReadingModif);
        fillCBModif(ParamDeleteModif);
        fillCBModif(ParamEditModif);
        fillCBModif(ParamManageTagsModif);
        fillCBModif(ParamAddBookModif);
        fillCBModif(ParamCritModif);
        fillCBModif(ParamLogModif);
        fillCBModif(ParamResetModif);
        fillCBModif(ParamManageAllTagsModif);
    }
    @SuppressWarnings("unchecked")
    public void fillCBModif(JComboBox jComboBox){
        jComboBox.addItem("");
        jComboBox.addItem("SHIFT");
        jComboBox.addItem("CONTROL");
        jComboBox.addItem("ALT");
    }
    public void initCBModifSelection(JComboBox jComboBox, int keyModif){
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                    if (f.getInt(f.getName()) == keyModif && f.getName().equals("KEY_LOCATION_LEFT")) {
                        jComboBox.setSelectedItem("CONTROL");
                    }
                    if (f.getInt(f.getName()) == keyModif && f.getName().equals("KEY_LOCATION_STANDARD")) {
                        jComboBox.setSelectedItem("SHIFT");
                    }
                    if (f.getInt(f.getName()) == keyModif && f.getName().equals("VK_BACK_SPACE")) {
                        jComboBox.setSelectedItem("ALT");
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public void initCBKeySelection(JComboBox jComboBox, int key){
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                    String name = f.getName();
                    if(f.getName().contains("VK_")) {
                        name= f.getName().replace("VK_", "");
                    }
                    if (f.getInt(f.getName()) == key) {
                        jComboBox.setSelectedItem(name);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public void initCBSelection(MainWindow parent){
        initCBKeySelection(ParamAddReadingKey, parent.getAddReadingKey());
        initCBModifSelection(ParamAddReadingModif, parent.getAddReadingModif());

        initCBKeySelection(ParamAddBookKey, parent.getAddBookKey());
        initCBModifSelection(ParamAddBookModif,parent.getAddBookModif());

        initCBKeySelection(ParamManageTagsKey, parent.getManageTagsKey());
        initCBModifSelection(ParamManageTagsModif, parent.getManageTagsModif());

        initCBKeySelection(ParamEditKey, parent.getEditKey());
        initCBModifSelection(ParamEditModif, parent.getEditModif());

        initCBKeySelection(ParamDeleteKey, parent.getDeletekey());
        initCBModifSelection(ParamDeleteModif, parent.getDeleteModif());

        initCBKeySelection(ParamManageAllTagsKey, parent.getManageAllTagsKey());
        initCBModifSelection(ParamManageAllTagsModif,parent.getManageAllTagsModif());

        initCBKeySelection(ParamCritKey, parent.getCritKey());
        initCBModifSelection(ParamCritModif,parent.getCritModif());

        initCBKeySelection(ParamLogKey, 0);
        initCBModifSelection(ParamLogModif,0);

        initCBKeySelection(ParamResetKey, parent.getResetKey());
        initCBModifSelection(ParamResetModif,parent.getResetModif());
    }
    public void initComponents(MainWindow parent){
        fillParamCBKey();
        fillParamCBModif();
        initCBSelection(parent);
        fillListOfShortcut();
    }
    public void save(){
        Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/");
        Path file = folder.resolve("save.dat");
        try(BufferedWriter writer = Files.newBufferedWriter(file)){
            writeData(writer, String.valueOf(getParamAddBookKey()));
            writeData(writer, String.valueOf(getParamAddBookModif()));
            writeData(writer, String.valueOf(getParamAddReadingKey()));
            writeData(writer, String.valueOf(getParamAddReadingModif()));
            writeData(writer, String.valueOf(getParamDeletekey()));
            writeData(writer, String.valueOf(getParamDeleteModif()));
            writeData(writer, String.valueOf(getParamEditKey()));
            writeData(writer, String.valueOf(getParamEditModif()));
            writeData(writer, String.valueOf(getParamManageTagsKey()));
            writeData(writer, String.valueOf(getParamManageTagsModif()));
            writeData(writer, String.valueOf(getParamCritKey()));
            writeData(writer, String.valueOf(getParamCritModif()));
            writeData(writer, String.valueOf(getParamManageAllTagsKey()));
            writeData(writer, String.valueOf(getParamManageAllTagsModif()));
            writeData(writer, String.valueOf(getParamResetKey()));
            writeData(writer, String.valueOf(getParamResetModif()));
        }catch (IOException e){
            System.err.println("Sauvergarde impossible");
            JFrame jFrame = new JFrame();
            JOptionPane.showMessageDialog(jFrame, "Sauvergarde impossible", "Sauvegarde", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void writeData(BufferedWriter writer, String value) throws IOException {
        writer.write(value);
        writer.write(';');
    }
}
