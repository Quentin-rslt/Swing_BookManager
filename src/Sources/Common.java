package Sources;

import Sources.Dialogs.EditTagDlg;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static Sources.CommonSQL.*;
import static Sources.Dialogs.OpenDialog.openEditTagDlg;
import static Sources.MainWindow.getAuthor;
import static Sources.MainWindow.getMTitle;
import static Sources.MenuBar.getAddReadingMenuItem;

public class Common {
    private static final JFileChooser jf= new JFileChooser();
    private static String m_nameImage ="";
    public static void addImageToPanel(String nom,JPanel panel){//Apply to our panel an image with path
        Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Image");
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

        panel.updateUI();//reload the panel
        panel.removeAll();
        panel.add(imgLabel);
    }
    public static void addImageToResource(){
        if(jf.getSelectedFile()!=null && !getNameOfBook().equals("Default.jpg")){
            Path src = Paths.get(jf.getSelectedFile().getAbsolutePath());
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Image");
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
        if(jf.getSelectedFile()!=null && !getNameOfBook().equals(getImageBDD(title, author))){
            Path src = Paths.get(jf.getSelectedFile().getAbsolutePath());
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Image");
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
        if(getImageBDD(title,author)!=null && !getImageBDD(title, author).equals("Default.jpg")){
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Image");
            Path dest = Paths.get(folder+"/"+ getImageBDD(title, author));//delete the image of the deleted book
            try {
                Files.delete(dest);
            } catch (Exception evt) {
                throw new RuntimeException(evt.getMessage(), evt);
            }
        }
    }
    public static void deleteImageToImport(){
        File directory = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Image").toFile();
        try {
            for (File file: Objects.requireNonNull(directory.listFiles())) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        } catch (Exception evt) {
            throw new RuntimeException(evt.getMessage(), evt);
        }
    }
    public static void deleteImageResource(String title, String author){
        if(jf.getSelectedFile()!=null && !getNameOfBook().equals(getImageBDD(title, author)) && !getImageBDD(title, author).equals("Default.jpg")){
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Image");
            Path dest = Paths.get(folder+"/"+ getImageBDD(title, author));//delete the image of the deleted book
            try {
                Files.delete(dest);
            } catch (Exception evt) {
                throw new RuntimeException(evt.getMessage(), evt);
            }
        }
    }
    public static void selectImageOfBook(JPanel panel){
        FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
        jf.setFileFilter(imageFilter);
        jf.setPreferredSize(new Dimension(850,600));
        int rVal;
        do{
            rVal = jf.showOpenDialog(panel);
            if (JFileChooser.APPROVE_OPTION == rVal){ //Opens the file panel to select an image
                setNameOfImage(randomNameOfBook(jf.getSelectedFile().getName()));
                String path = jf.getSelectedFile().getPath();
                if (accept(jf.getSelectedFile())){
                    Image img = Toolkit.getDefaultToolkit().getImage(path);
                    Dimension d = rescaleImage(jf.getSelectedFile());
                    img=img.getScaledInstance(d.width, d.height, Image.SCALE_AREA_AVERAGING);
                    ImageIcon icon = new ImageIcon(img);
                    JLabel imgLabel = new JLabel();
                    imgLabel.setIcon(icon);

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
            throw new RuntimeException(e);
        }
    }
    public static void isNotInFilteredBookList(MainWindow parent, boolean bookDelete){
        if(bookDelete) {
            if (parent.getBooksTable().getRowCount() > 0) {
                if (parent.getRowSelected() > 0) {
                    parent.setRowSelected(parent.getRowSelected() - 1);
                }
                parent.setRowReading(0);
                parent.setRowSelected(parent.getRowSelected());
                parent.setMTitle(parent.getBooksTable().getValueAt(parent.getRowSelected(), 0).toString());
                parent.setAuthor(parent.getBooksTable().getValueAt(parent.getRowSelected(), 1).toString());
                parent.loadComponents(getMTitle(), getAuthor());//reload changes made to the book
            } else {
                parent.initComponents();
            }
        }else {
            if (parent.getBooksTable().getRowCount() > 0) {
                parent.setMTitle(parent.getBooksTable().getValueAt(0, 0).toString());
                parent.setAuthor(parent.getBooksTable().getValueAt(0, 1).toString());
                parent.setRowSelected(0);
                parent.setRowReading(0);
                parent.loadComponents( getMTitle(), getAuthor());//reload changes made to the book
            } else {
                parent.initComponents();
            }
        }
    }
    public static void isItInFilteredBookList(MainWindow parent, boolean bookDelete){
        if(isInFilteredList(getMTitle(),getAuthor(), parent.getBooksTable())){
            parent.setRowSelected(parent.getRowSelectedByBook(getMTitle(), getAuthor()));
            parent.loadComponents(getMTitle(), getAuthor());//reload changes made to the book
        }
        else{
            isNotInFilteredBookList(parent, bookDelete);
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
    public static void resetApp(MainWindow mainWindow, boolean reset){
//        Component[] components = mainWindow.getContentPanel().getComponents();
//        JButton btn = mainWindow.getAddBookBtn();
//        if(!reset){
//            mainWindow.getContentPanel().setVisible(false);
//            btn.setContentAreaFilled(false);
//            btn.setForeground(Color.white);
//            btn.setText("Votre base est vide, cliquer pour ajouter un nouveau livre");
//            mainWindow.getContentPanel().add(btn);
//        }
//        else{
//            mainWindow.getContentPanel().remove(btn);
//            for(Component component : components){
//                component.setVisible(true);
//            }
//        }
        mainWindow.getJMenuBar().getMenu(0).getItem(0).setEnabled(reset);
        mainWindow.getJMenuBar().getMenu(1).getItem(3).setEnabled(reset);
        mainWindow.getJMenuBar().getMenu(1).getItem(4).setEnabled(reset);
        mainWindow.getJMenuBar().getMenu(1).getItem(6).setEnabled(reset);
        getAddReadingMenuItem().setEnabled(reset);
    }

    public static boolean isInFilteredList(String title, String author, JTable table){
        boolean isFiltered=false;
        for(int i=0; i<table.getRowCount();i++){
            if(table.getModel().getValueAt(i,0).equals(title) && table.getModel().getValueAt(i,1).equals(author)){
                isFiltered=true;
            }
        }
        return isFiltered;
    }
    public static boolean fillPaneTags(Tags tags, JPanel panel, JComboBox cb, boolean canCreate){
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
            for (int j = 0; j < cb.getItemCount(); j++) {
                if (Objects.requireNonNull(cb.getSelectedItem()).toString().equals(cb.getItemAt(j).toString())) {
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
                    System.exit(0);
                    System.out.println(e.getMessage());
                }
            }

        }
        return tagFind;
    }
    public static boolean fillPaneTags(Tags tags, JPanel panel, JTextField txt){
        boolean tagFind = false;
        int i = 0;
        while(!tagFind && i<tags.getSizeTags()){
            if(Objects.equals(txt.getText(), tags.getTag(i).getTextTag())){
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
            throw new RuntimeException(e);
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
            isAccept = filename.endsWith("db") && !filename.equals("BookManager.db");
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
    public static String getNameOfBook(){
        return m_nameImage;
    }
}
