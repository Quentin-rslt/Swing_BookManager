package Sources;

import Sources.Dialogs.EditTagDlg;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Common {
    static JFileChooser jf= new JFileChooser();
    static String m_name="";
    public static void addImageToPanel(String nom,JPanel panel){//Apply to our panel an image with path
        Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager");
        File file = new File(folder+"/"+nom);
        if(nom.equals("Default.jpg"))
            file = new File("Ressource/Image/"+nom);
        String path = file.getAbsolutePath();

        Image img = Toolkit.getDefaultToolkit().getImage(path);
        img=img.getScaledInstance(rescaleImage(file).width, rescaleImage(file).height, Image.SCALE_AREA_AVERAGING);
        ImageIcon icon = new ImageIcon(img);
        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(icon);

        panel.updateUI();//reload the panel
        panel.removeAll();
        panel.add(imgLabel);
    }
    public static void addImageToResource(){
        if(jf.getSelectedFile()!=null && !getNameOfBook().equals("Default.jpg")){
            Path src = Paths.get(jf.getSelectedFile().getAbsolutePath());
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager");
            Path dest = Paths.get(folder+"/"+getNameOfBook());
            try {
                Files.createDirectories(folder);
                Files.copy(src, dest);
                rescaleResolutionImage(dest.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void updateImageToResource(String title, String author){
        if(jf.getSelectedFile()!=null && !getNameOfBook().equals(getBookNameBdd(title, author))){
            System.out.println(getNameOfBook());
            Path src = Paths.get(jf.getSelectedFile().getAbsolutePath());
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager");
            Path dest = Paths.get(folder+"/"+getNameOfBook());
            try {
                Files.createDirectories(folder);
                Files.copy(src, dest);
                rescaleResolutionImage(dest.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void deleteImageMainResource(String title, String author){
        if(!getBookNameBdd(title, author).equals("Default.jpg")){
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager");
            Path dest = Paths.get(folder+"/"+getBookNameBdd(title, author));//delete the image of the deleted book
            try {
                Files.delete(dest);
            } catch (Exception evt) {
                throw new RuntimeException(evt.getMessage(), evt);
            }
        }
    }
    public static void deleteImageResource(String title, String author){
        if(jf.getSelectedFile()!=null && !getNameOfBook().equals(getBookNameBdd(title, author)) && !getBookNameBdd(title, author).equals("Default.jpg")){
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager");
            Path dest = Paths.get(folder+"/"+getBookNameBdd(title, author));//delete the image of the deleted book
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
                        cb.setSelectedIndex(0);
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
    public static void setNameOfBook(String name){
        m_name = name;
    }

    public static void selectNameOfBook(JPanel panel){
        FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
        jf.setFileFilter(imageFilter);
        jf.setPreferredSize(new Dimension(850,600));
        int rVal;
        do{
            rVal = jf.showOpenDialog(panel);
            if (JFileChooser.APPROVE_OPTION == rVal){ //Opens the file panel to select an image
                setNameOfBook(randomNameOfBook(jf.getSelectedFile().getName()));
                //String path = jf.getSelectedFile().getPath();
                if (accept(jf.getSelectedFile())){
                    Image img = previewRescaleResolutionImage(jf.getSelectedFile());
//                    Image img = Toolkit.getDefaultToolkit().getImage(path);
                    img=img.getScaledInstance(rescaleImage(jf.getSelectedFile()).width, rescaleImage(jf.getSelectedFile()).height, Image.SCALE_AREA_AVERAGING);
                    ImageIcon icon = new ImageIcon(img);
                    JLabel imgLabel = new JLabel();
                    imgLabel.setIcon(icon);

                    panel.updateUI();//reload the panel
                    panel.removeAll();
                    panel.add(imgLabel);
                }else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Veuillez choisir un format jpg ou png ou jpeg !");
                }
            }
        } while (!accept(jf.getSelectedFile()) && rVal==0);
    }
    public static Dimension rescaleImage(File file){
        Dimension size;
        BufferedImage image;
        try {
            image = ImageIO.read(file);
            double height = image.getHeight();
            double width = image.getWidth();
            double ratio = width/height;
            if(ratio>1){
                size = new Dimension(266, (int) (266/ratio));
            }else{
                size = new Dimension(266,400);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return size;
    }
    public static void rescaleResolutionImage(File file){
        BufferedImage image;
        BufferedImage outputImage;
        try {
            image = ImageIO.read(file);
            if(image.getWidth()>700){
                Image resultingImage = image.getScaledInstance(image.getWidth()/2, image.getHeight()/2, Image.SCALE_DEFAULT);
                outputImage = new BufferedImage(image.getWidth()/2, image.getHeight()/2, BufferedImage.TYPE_INT_RGB);
                outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
                ImageIO.write(outputImage, getFormat(file.getName()), file);
            }else{
                Image resultingImage = image.getScaledInstance(image.getWidth(), image.getHeight(), Image.SCALE_DEFAULT);
                outputImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static Image previewRescaleResolutionImage(File file){
        BufferedImage image;
        BufferedImage outputImage;
        try {
            image = ImageIO.read(file);
            if(image.getWidth()>700){
                Image resultingImage = image.getScaledInstance(image.getWidth()/2, image.getHeight()/2, Image.SCALE_DEFAULT);
                outputImage = new BufferedImage(image.getWidth()/2, image.getHeight()/2, BufferedImage.TYPE_INT_RGB);
                outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
            }else{
                Image resultingImage = image.getScaledInstance(image.getWidth(), image.getHeight(), Image.SCALE_DEFAULT);
                outputImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputImage;
    }
    public static boolean accept(File pathname) {
        boolean isAccept = true;
        if(pathname!=null){
            String filename = pathname.getName();
            isAccept = filename.endsWith("jpg") || filename.endsWith("jpeg") || filename.endsWith("png");
        }
        return isAccept;
    }
    public static String randomNameOfBook(String oldName){
        String name="";
        try (Connection conn = connect()) {
            boolean find;
            Statement statement = conn.createStatement();
            int randomNumber;
            do{ // we go through each line again until we find the name of the boo
                randomNumber = ThreadLocalRandom.current().nextInt(0, 999999999 + 1);
                ResultSet ImageQry = statement.executeQuery("SELECT Image FROM Book");

                String[] nameWithoutFormat;

                find=true;//if wwe find a name of book
                while (ImageQry.next()){//new random if the number already exist
                    nameWithoutFormat = ImageQry.getString(1).split("\\.");
                    if(nameWithoutFormat[0].equals(Integer.toString(randomNumber))){
                        find = false;
                        break;
                    }
                }
            } while (!find);
            name = randomNumber+"."+getFormat(oldName);
            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return name;
    }
    public static String getFormat(String name){
        return name.substring(name.lastIndexOf('.')+1);
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
    public static String getBookNameBdd(String title, String author) {
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
    public static String getNameOfBook(){
        return m_name;
    }
}
