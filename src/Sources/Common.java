package Sources;

import Sources.Dialogs.EditTagDlg;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.Objects;

public class Common {
    static JFileChooser jf= new JFileChooser();
    static String name="";
    public static void addImageToPanel(String nom,JPanel panel){//Apply to our panel an image with path
        File file = new File("Ressource/Image/"+nom);
        String path = file.getAbsolutePath();

        Image img = Toolkit.getDefaultToolkit().getImage(path);
        img=img.getScaledInstance(266, 400, Image.SCALE_AREA_AVERAGING);
        ImageIcon icon = new ImageIcon(img);
        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(icon);

        panel.updateUI();//reload the panel
        panel.removeAll();
        panel.add(imgLabel);
    }
    public static String setNameOfBook(JPanel panel){
        if (JFileChooser.APPROVE_OPTION == jf.showOpenDialog(panel)){ //Opens the file panel to select an image
            name = "2.jpg";
            String path = jf.getSelectedFile().getPath();

            Image img = Toolkit.getDefaultToolkit().getImage(path);
            img=img.getScaledInstance(266, 400, Image.SCALE_AREA_AVERAGING);
            ImageIcon icon = new ImageIcon(img);
            JLabel imgLabel = new JLabel();
            imgLabel.setIcon(icon);

            panel.updateUI();//reload the panel
            panel.removeAll();
            panel.add(imgLabel);
        }
        return name;
    }
    public static void addImageToRessource(){
        Path src = Paths.get(jf.getSelectedFile().getAbsolutePath());
        Path dest = Paths.get("Ressource/Image/"+name);
        try {
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    public static void deleteImageToRessource(String title, String author){
        if(!getBookName(title, author).equals("Default.jpg")){
            Path dest = Paths.get("Ressource/Image/"+getBookName(title, author));//delete the image of the deleted book
            try {
                Files.delete(dest);
            } catch (Exception evt) {
                throw new RuntimeException(evt.getMessage(), evt);
            }
        }
    }
    public static void fillPaneTags(Tags tags, JPanel panel, JComboBox cb){
        boolean tagFind = false;
        int i = 0;
        while(!tagFind && i<tags.getSizeTags()){
            if(Objects.equals(cb.getSelectedItem(), tags.getTag(i).getTextTag())){
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Vous avez déjà sélectionné ce tag !");
                tagFind =true;
            }
            else i++;
        }
        if(!tagFind){
            boolean isInCB = false;
            for(int j = 0; j<cb.getItemCount();j++){
                if(Objects.requireNonNull(cb.getSelectedItem()).toString().equals(cb.getItemAt(j).toString())){
                    isInCB=true;
                }
            }
            //if the tag don't exist open editTagTagWindow to configure the color or the text
            if(!isInCB){
                EditTagDlg diag = new EditTagDlg(new Tag(Objects.requireNonNull(cb.getSelectedItem()).toString()));
                diag.setTitle("Créer un tag");
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);

                if(diag.isValide()){
                    Tag tag = new Tag(diag.getNewTextTag());
                    tag.setColor(diag.getNewColorTag().getRGB());
                    tags.addTag(tag);

                    for(int j=0; j<tags.getSizeTags();j++){
                        panel.add(tags.getTag(j));
                    }
                    panel.updateUI();
                }
            }
            else{
                String sql = "SELECT Color FROM Tags WHERE Tag='"+cb.getSelectedItem().toString()+ "'";
                try {
                    Class.forName("org.sqlite.JDBC");
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                    Statement statement = connection.createStatement();
                    ResultSet tagsQry = statement.executeQuery(sql);

                    tags.createTag(Objects.requireNonNull(cb.getSelectedItem()).toString());
                    tags.getTag(tags.getSizeTags() - 1).setColor(tagsQry.getInt(1));
                    for (int j = 0; j < tags.getSizeTags(); j++) {
                        panel.add(tags.getTag(j));
                        cb.setSelectedIndex(0);
                    }
                    connection.close();
                    statement.close();
                }catch (Exception e ){
                    System.exit(0);
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static Image getImageAdd(){
        File fileAdd = new File("Ressource/Icons/add.png");
        String pathAdd = fileAdd.getAbsolutePath();
        Image imgAdd = Toolkit.getDefaultToolkit().getImage(pathAdd);
        imgAdd = imgAdd.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);

        return imgAdd;
    }
    public static Image getImageCut(){
        File fileRemove = new File("Ressource/Icons/remove.png");
        String pathRemove = fileRemove.getAbsolutePath();
        Image imgRemove = Toolkit.getDefaultToolkit().getImage(pathRemove);
        imgRemove = imgRemove.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);

        return imgRemove;
    }
    public static Image getImageEdit(){
        File fileEdit = new File("Ressource/Icons/edit.png");
        String pathEdit = fileEdit.getAbsolutePath();
        Image imgEdit = Toolkit.getDefaultToolkit().getImage(pathEdit);
        imgEdit = imgEdit.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);

        return imgEdit;
    }
    public static Connection connect() {
        Connection connection = null;
        String url = "jdbc:sqlite:BookManager.db";
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }
    public static int getIdReading(String title, String author) {
        int i =0;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'");
            i=rs.getInt(1);
            rs.close();
            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return i;
    }
    public static int getIdBook(String title, String author) {
        int i =0;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet idBook = statement.executeQuery("SELECT ID FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            i=idBook.getInt(1);
            idBook.close();
            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return i;
    }
    public static int getIdTag(String tag, int color) {
        int i =0;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet idBook = statement.executeQuery("SELECT ID FROM Tags WHERE Tag='"+tag+"' AND Color='"+color+ "'");
            i=idBook.getInt(1);
            idBook.close();
            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return i;
    }
    public static String getBookName(String title, String author) {
        String name ="";
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet ImageQry = statement.executeQuery("SELECT Image FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            name=ImageQry.getString(1);
            ImageQry.close();
            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return name;
    }
}