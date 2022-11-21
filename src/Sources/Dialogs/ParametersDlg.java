package Sources.Dialogs;

import Sources.MainWindow;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public ParametersDlg(MainWindow parent) {
        setContentPane(contentPane);
        setModal(true);
        initComponents(parent);
        ParamSaveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.setAddReadingKey(getParamAddReadingKey());
                parent.setEditKey(getParamEditKey());
                parent.setManageTagsKey(getParamManageTagsKey());
                parent.setDeletekey(getParamDeletekey());
                parent.setAddReadingModif(getParamAddReadingModif());
                parent.setEditModif(getParamEditModif());
                parent.setDeleteModif(getParamDeleteModif());
                parent.setManageTagsModif(getParamManageTagsModif());

                save();
                parent.initBinding();
                setVisible(false);
                dispose();
            }
        });
        ParamCancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
    }
    public int getParamAddReadingModif(){
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(getNameModifKey(ParamAddReadingModif))) {
                    value = f.getInt(getNameModifKey(ParamAddReadingModif));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
    public int getParamDeleteModif(){
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(getNameModifKey(ParamDeleteModif))) {
                    value = f.getInt(getNameModifKey(ParamDeleteModif));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
    public int getParamEditModif(){
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(getNameModifKey(ParamEditModif))) {
                    value = f.getInt(getNameModifKey(ParamEditModif));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
    public int getParamManageTagsModif(){
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(getNameModifKey(ParamManageTagsModif))) {
                    value = f.getInt(getNameModifKey(ParamManageTagsModif));
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
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
    public int getParamManageTagsKey() {
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(ParamManageTagsKey.getSelectedItem())) {
                    value = f.getInt(ParamManageTagsKey.getSelectedItem());
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
    public int getParamDeletekey() {
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(ParamDeleteKey.getSelectedItem())) {
                    value = f.getInt(ParamDeleteKey.getSelectedItem());
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
    public int getParamEditKey() {
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(ParamEditKey.getSelectedItem())) {
                    value = f.getInt(ParamEditKey.getSelectedItem());
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
    public int getParamAddReadingKey() {
        int value=0;
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(ParamAddReadingKey.getSelectedItem())) {
                    value = f.getInt(ParamAddReadingKey.getSelectedItem());
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
    @SuppressWarnings("unchecked")
    public void fillParamCBKey(){
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                ParamAddReadingKey.addItem(f.getName());
                ParamEditKey.addItem(f.getName());
                ParamDeleteKey.addItem(f.getName());
                ParamManageTagsKey.addItem(f.getName());
            }
        }
    }
    public void fillParamCBModif(){
        fillCBModif(ParamAddReadingModif);
        fillCBModif(ParamDeleteModif);
        fillCBModif(ParamEditModif);
        fillCBModif(ParamManageTagsModif);
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
    public void initCBSelection(MainWindow parent){
        try {
            Field[] fields = KeyEvent.class.getDeclaredFields();

            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                    if (f.getInt(f.getName()) == parent.getAddReadingKey()) {
                        ParamAddReadingKey.setSelectedItem(f.getName());
                    }
                    if (f.getInt(f.getName()) == parent.getEditKey()) {
                        ParamEditKey.setSelectedItem(f.getName());
                    }
                    if (f.getInt(f.getName()) == parent.getDeletekey()) {
                        ParamDeleteKey.setSelectedItem(f.getName());
                    }
                    if (f.getInt(f.getName()) == parent.getManageTagsKey()) {
                        ParamManageTagsKey.setSelectedItem(f.getName());
                    }
                }
            }
            initCBModifSelection(ParamAddReadingModif, parent.getAddReadingModif());
            initCBModifSelection(ParamEditModif, parent.getEditModif());
            initCBModifSelection(ParamManageTagsModif, parent.getManageTagsModif());
            initCBModifSelection(ParamDeleteModif, parent.getDeleteModif());

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public void initComponents(MainWindow parent){
        fillParamCBKey();
        fillParamCBModif();
        initCBSelection(parent);
    }
    public void save(){
        Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/");
        Path file = folder.resolve("save.dat");
        try(BufferedWriter writer = Files.newBufferedWriter(file)){
            writeData(writer, String.valueOf(getParamAddReadingKey()));
            writeData(writer, String.valueOf(getParamAddReadingModif()));
            writeData(writer, String.valueOf(getParamDeletekey()));
            writeData(writer, String.valueOf(getParamDeleteModif()));
            writeData(writer, String.valueOf(getParamEditKey()));
            writeData(writer, String.valueOf(getParamEditModif()));
            writeData(writer, String.valueOf(getParamManageTagsKey()));
            writeData(writer, String.valueOf(getParamManageTagsModif()));
        }catch (IOException e){
            System.err.println("Sauvergarde impossible");
            e.printStackTrace();
        }
    }
    private void writeData(BufferedWriter writer, String value) throws IOException {
        writer.write(value);
        writer.write(';');
    }
}
