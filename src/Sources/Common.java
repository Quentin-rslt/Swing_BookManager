package Sources;

import Sources.Components.MyManagerRoundBorderComponents;
import Sources.Components.Tag;
import Sources.Components.Tags;
import Sources.Dialogs.EditTagDlg;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static Sources.BookManager.CommonBookManagerSQL.getImageBDD;
import static Sources.Dialogs.OpenDialogs.*;
import static Sources.CommonSQL.connect;
import static Sources.CommonSQL.loadTags;

public class Common {
    private static final JFileChooser jf= new JFileChooser();
    private static String m_nameImage ="";
    public static void addImageToPanel(String nom,JPanel panel){//Apply to our panel an image with path
        Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"MyManager/Image");
        File file = new File(folder+"/"+nom);
        if(nom.equals("Default.jpg"))
            file = new File("Ressource/Image/"+nom);
        String path = file.getAbsolutePath();

        Image img = Toolkit.getDefaultToolkit().getImage(path);
        Dimension d = rescaleImage(file);
        img=img.getScaledInstance(d.width, d.height, Image.SCALE_AREA_AVERAGING);
        ImageIcon icon = new ImageIcon(img);
        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(icon);
        AbstractBorder roundBrdMax = new MyManagerRoundBorderComponents(panel.getBackground(),3,30, 0,0,0);
        imgLabel.setBorder(roundBrdMax);

        panel.updateUI();//reload the panel
        panel.removeAll();
        panel.add(imgLabel);
    }
    public static void addImageToResource(){
        if(jf.getSelectedFile()!=null && !getNameOfImage().equals("Default.jpg")){
            Path src = Paths.get(jf.getSelectedFile().getAbsolutePath());
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"MyManager/Image");
            Path dest = Paths.get(folder+"/"+ getNameOfImage());
            try {
                Files.createDirectories(folder);
                Files.copy(src, dest);
                rescaleResolutionImage(dest.toFile());
            } catch (IOException e) {
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Ajout image ressource impossible", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(e.getMessage());
            }
        }
    }
    public static void updateImageToResource(String title, String author){
        if(jf.getSelectedFile()!=null && !getNameOfImage().equals(getImageBDD(title, author))){
            Path src = Paths.get(jf.getSelectedFile().getAbsolutePath());
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"MyManager/Image");
            Path dest = Paths.get(folder+"/"+ getNameOfImage());
            try {
                Files.createDirectories(folder);
                Files.copy(src, dest);
                rescaleResolutionImage(dest.toFile());
            } catch (IOException e) {
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "MAJ image ressource impossible", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(e.getMessage());
            }
        }
    }
    public static void deleteImageMainResource(String title, String author){
        if(getImageBDD(title,author)!=null && !getImageBDD(title, author).equals("Default.jpg")){
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"MyManager/Image");
            Path dest = Paths.get(folder+"/"+ getImageBDD(title, author));//delete the image of the deleted book
            try {
                Files.delete(dest);
            } catch (Exception e) {
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Suppression image ressource impossible", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(e.getMessage());
            }
        }
    }
    public static void deleteImageToImport(){
        File directory = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Image").toFile();
        try {
            for (File file: Objects.requireNonNull(directory.listFiles())) {
                if (!file.isDirectory()) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        } catch (Exception e) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Suppression image pour import impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public static void deleteImageResource(String title, String author){
        if(jf.getSelectedFile()!=null && !getNameOfImage().equals(getImageBDD(title, author)) && !getImageBDD(title, author).equals("Default.jpg")){
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"MyManager/Image");
            Path dest = Paths.get(folder+"/"+ getImageBDD(title, author));//delete the image of the deleted book
            try {
                Files.delete(dest);
            } catch (Exception e) {
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Suppression image ressource impossible", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(e.getMessage());
            }
        }
    }
    public static void selectImage(JPanel panel){
        FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
        jf.setFileFilter(imageFilter);
        jf.setPreferredSize(new Dimension(850,600));
        int rVal;
        do{
            rVal = jf.showOpenDialog(panel);
            if (JFileChooser.APPROVE_OPTION == rVal){ //Opens the file panel to select an image
                setNameOfImage(randomNameOfImage(jf.getSelectedFile().getName()));
                String path = jf.getSelectedFile().getPath();
                if (accept(jf.getSelectedFile())){
                    Image img = Toolkit.getDefaultToolkit().getImage(path);
                    Dimension d = rescaleImage(jf.getSelectedFile());
                    img=img.getScaledInstance(d.width, d.height, Image.SCALE_AREA_AVERAGING);
                    ImageIcon icon = new ImageIcon(img);
                    JLabel imgLabel = new JLabel();
                    imgLabel.setIcon(icon);
                    AbstractBorder roundBrdMax = new MyManagerRoundBorderComponents(panel.getBackground(),3,30, 0,0,0);
                    imgLabel.setBorder(roundBrdMax);

                    panel.updateUI();//reload the panel
                    panel.removeAll();
                    panel.add(imgLabel);
                }else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Veuillez choisir un format jpg ou png ou jpeg !","ERROR",JOptionPane.ERROR_MESSAGE);
                }
            }
        } while (!accept(jf.getSelectedFile()) && rVal==0);
    }
    public static void setNameOfImage(String name){
        m_nameImage = name;
    }
    public static void rescaleResolutionImage(File file){
        BufferedImage image;
        BufferedImage outputImage;
        try {
            image = ImageIO.read(file);
            float width = image.getWidth();
            float height = image.getHeight();
            int maxWidth = 1000;
            if(width>maxWidth){
                float ratio = (width/height);
                int maxHeight = (int) (maxWidth/ratio); //rescale the height of the image with a maximum width of 611px
                Image resultingImage = image.getScaledInstance(maxWidth, maxHeight, Image.SCALE_DEFAULT);
                outputImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
                outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
                ImageIO.write(outputImage, getFormat(file.getName()), file);
            }else{
                Image resultingImage = image.getScaledInstance(image.getWidth(), image.getHeight(), Image.SCALE_DEFAULT);
                outputImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
            }

        } catch (IOException e) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Rescale image impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public static void initListenerTag(Tags tags, JPopupMenu m_popup, JPanel panel){
        for(int i=0; i<tags.getSizeTags();i++){
            MouseListener[] mouseListeners =  tags.getTag(i).getMouseListeners();
            for (MouseListener mouseListener : mouseListeners) {
                tags.getTag(i).removeMouseListener(mouseListener);
            }
        }
        for(int i=0; i<tags.getSizeTags();i++){
            int finalI = i;
            tags.getTag(i).addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                    Color color = new Color(tags.getTag(finalI).getColor());
                    tags.getTag(finalI).setBackground(color.brighter());
                    panel.updateUI();
                }
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                    tags.getTag(finalI).setBackground(new Color(tags.getTag(finalI).getColor()));
                    panel.updateUI();
                }
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if(!e.getComponent().getComponentAt(e.getX(),e.getY()).equals(panel)){
                        if(e.getButton() == MouseEvent.BUTTON3) {
                            m_popup.show(tags.getTag(finalI), e.getX(), e.getY());//show a popup to edit the reading
                            m_popup.setInvoker(e.getComponent().getComponentAt(e.getX(),e.getY()));
                        }
                    }
                }
            });
        }
    }
    public static boolean isInFilteredList(String col1, String col2, JTable table){
        boolean isFiltered=false;
        for(int i=0; i<table.getRowCount();i++){
            if(table.getModel().getValueAt(i,0).equals(col1) && table.getModel().getValueAt(i,1).equals(col2)){
                isFiltered=true;
            }
        }
        return isFiltered;
    }
    public static boolean fillPaneTags(Tags tags, JPanel panel, JComboBox cb, boolean canCreate){
        boolean tagFind = false;
        int i = 0;
        while(!tagFind && i<tags.getSizeTags()){
            if(Objects.equals(Objects.requireNonNull(cb.getSelectedItem()).toString().toLowerCase(), tags.getTag(i).getTextTag().toLowerCase())){
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Vous avez déjà sélectionné ce tag !");
                tagFind =true;
            }
            else i++;
        }
        if(!tagFind){
            boolean isInCB = false;
            for (int j = 0; j < cb.getItemCount(); j++) {
                if (Objects.requireNonNull(cb.getSelectedItem()).toString().equalsIgnoreCase(cb.getItemAt(j).toString())) {
                    isInCB = true;
                }
            }
            //if the tag don't exist open editTagTagWindow to configure the color or the text
            if (!isInCB) {
                if(canCreate) {
                    EditTagDlg diag = openEditTagDlg(new Tag(Objects.requireNonNull(cb.getSelectedItem()).toString()));
                    if (diag.isValide()) {
                        Tag tag = new Tag(diag.getNewTextTag());
                        tag.setColor(diag.getNewColorTag().getRGB());
                        tags.addTag(tag);

                        for (int j = 0; j < tags.getSizeTags(); j++) {
                            panel.add(tags.getTag(j));
                            if (cb.getItemCount() > 0)
                                cb.setSelectedIndex(0);
                        }
                        panel.updateUI();
                    }
                }
            } else {
                String sql = "SELECT Color FROM Tags WHERE Tag='" + cb.getSelectedItem().toString() + "'";
                try (Connection connection = connect()){
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
                } catch (Exception e) {
                    JFrame jf = new JFrame();
                    JOptionPane.showMessageDialog(jf, e.getMessage(), "Ajout tag impossible", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(e.getMessage());
                }
            }

        }
        return tagFind;
    }
    public static boolean fillPaneTags(Tags tags, JPanel panel, JTextField txt){
        boolean tagFind = false;
        int i = 0;
        while(!tagFind && i<tags.getSizeTags()){
            if(Objects.equals(txt.getText().toLowerCase(), tags.getTag(i).getTextTag().toLowerCase())){
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Vous avez déjà sélectionné ce tag !");
                tagFind =true;
            }
            else i++;
        }
        if(!tagFind){
            EditTagDlg diag = openEditTagDlg(new Tag(Objects.requireNonNull(txt.getText())));
            if(diag.isValide()){
                Tag tag = new Tag(diag.getNewTextTag());
                tag.setColor(diag.getNewColorTag().getRGB());
                tags.addTag(tag);

                for(int j=0; j<tags.getSizeTags();j++){
                    panel.add(tags.getTag(j));
                    txt.setText("");
                }
                panel.updateUI();
            }
        }
        return tagFind;
    }
    public static Dimension rescaleImage(File file){
        Dimension size;
        BufferedImage image;
        try {
            image = ImageIO.read(file);
            double height = image.getHeight();
            double width = image.getWidth();
            double ratio = width/height;
            if(ratio<0.6){
                size = new Dimension((int) (570*ratio),  570);
            }else{
                size = new Dimension(350, (int) (350/ratio));
            }

        } catch (IOException e) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Rescale image impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }

        return size;
    }
    public static boolean accept(File pathname) {
        boolean isAccept = true;
        if(pathname!=null){
            String filename = pathname.getName();
            isAccept = filename.endsWith("jpg") || filename.endsWith("jpeg") || filename.endsWith("png");
        }
        return isAccept;
    }
    public static boolean acceptDB(File pathname) {
        boolean isAccept = true;
        if(pathname!=null){
            String filename = pathname.getName();
            isAccept = filename.endsWith("db") && !filename.equals("MyManager.db");
        }
        return isAccept;
    }
    public static String randomNameOfImage(String oldName){
        String name;
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
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Nom image impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
        return name;
    }
    public static String getFormat(String name){
        return name.substring(name.lastIndexOf('.')+1);
    }
    public static Image getLogo(String name){
        File fileParam = new File("Ressource/Icons/"+name);
        String path = fileParam.getAbsolutePath();
        Image img = Toolkit.getDefaultToolkit().getImage(path);
        img = img.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);

        return img;
    }
    public static Image getLogo(int w, int h){
        File fileLogo = new File("Ressource/Icons/logo_BookManager.png");
        String pathLogo = fileLogo.getAbsolutePath();
        Image logo = Toolkit.getDefaultToolkit().getImage(pathLogo);
        logo = logo.getScaledInstance(w,h,Image.SCALE_AREA_AVERAGING);

        return logo;
    }
    public static String getNameOfImage(){
        return m_nameImage;
    }
    @SuppressWarnings("unchecked")
    public static void fillTagsCB(JComboBox jComboBox){
        jComboBox.removeAllItems();
        jComboBox.addItem("");
        for (int i = 0; i<loadTags().getSizeTags(); i++){
            jComboBox.addItem(loadTags().getTag(i).getTextTag());
        }
    }
}
