package Sources;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static Sources.Common.*;
import static Sources.CommonSQL.*;

public class ImportExportData {
    public static String escapeSpecialCharacters(String data) {
        return data.replaceAll("\\n", " ");
    }
    public static boolean exportCSV(){
        JFrame jFrame = new JFrame();
        JOptionPane.showMessageDialog(jFrame, "Les images ne seront pas exportées","WARNING", JOptionPane.WARNING_MESSAGE);
        boolean good = false;
        try(Connection conn = connect()){
            String qryBook = "SELECT * FROM Book; ";
            String qryReading= "SELECT * FROM Reading; ";
            String qryTagging = "SELECT * FROM Tagging; ";
            String qryTags = "SELECT * FROM Tags; ";

            Date today = new Date();
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Saves/CSV/"+formater.format(today));
            Files.createDirectories(folder);

            Statement statement = conn.createStatement();

            ResultSet rsBook = statement.executeQuery(qryBook);
            BufferedWriter fileBook = new BufferedWriter(new FileWriter(folder+"/Book.csv"));
            fileBook.write("ID_Book|Title|Author|Image|NumberOP|NotePerso|NoteBabelio|ReleaseYear|AvReadingTime|NumberReading|Summary");
            while(rsBook.next())
            {
                String ID_Book = rsBook.getString(1);
                String title = rsBook.getString(2);
                String author = rsBook.getString(3);
                String image = "Default.jpg";
                String numberOP = rsBook.getString(5);
                String notePerso = rsBook.getString(6);
                String noteBBL = rsBook.getString(7);
                String releaseYear = rsBook.getString(8);
                String avReadingTime = rsBook.getString(9);
                String numberReading = rsBook.getString(10);
                String summary = escapeSpecialCharacters(rsBook.getString(11));

                String line = String.format(
                        "%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                        ID_Book, title,author, image, numberOP, notePerso,noteBBL, releaseYear, avReadingTime,numberReading, summary);

                fileBook.newLine();
                fileBook.write(line);
            }
            ResultSet rsReading = statement.executeQuery(qryReading);
            BufferedWriter fileReading = new BufferedWriter(new FileWriter(folder+"/Reading.csv"));
            fileReading.write("ID,Title,Author,StartReading,EndReading");
            while(rsReading.next())
            {
                int ID = rsReading.getInt(1);
                String title = rsReading.getString(2);
                String author = rsReading.getString(3);
                String startReading = rsReading.getString(4);
                String endReading = rsReading.getString(5);

                String line = String.format(
                        "%d,%s,%s,%s,%s",
                        ID, title,author, startReading, endReading);

                fileReading.newLine();
                fileReading.write(line);
            }
            ResultSet rsTagging = statement.executeQuery(qryTagging);
            BufferedWriter fileTagging = new BufferedWriter(new FileWriter(folder+"/Tagging.csv"));
            fileTagging.write("ID_Tag,ID_Book");
            while(rsTagging.next())
            {
                int ID_Book = rsTagging.getInt(1);
                int ID_Tag = rsTagging.getInt(2);

                String line = String.format(
                        "%d,%d",
                        ID_Book, ID_Tag);

                fileTagging.newLine();
                fileTagging.write(line);
            }
            ResultSet rsTags = statement.executeQuery(qryTags);
            BufferedWriter fileTags = new BufferedWriter(new FileWriter(folder+"/Tags.csv"));
            fileTags.write("ID,Tag,Color");
            while(rsTags.next())
            {
                int ID = rsTags.getInt(1);
                String Tag =rsTags.getString(2);
                int Color = rsTags.getInt(3);

                String line = String.format(
                        "%d,%s,%d",
                        ID, Tag,Color);

                fileTags.newLine();
                fileTags.write(line);
            }

            statement.close();
            fileBook.close();
            fileReading.close();
            fileTagging.close();
            fileTags.close();
            good = true;
        }
        catch (SQLException e) {
            System.out.println("Datababse error:");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File IO error:");
            e.printStackTrace();
        }
        return good;
    }
    public static int importCSV(MainWindow panel){
        int good = 0;
        JFileChooser jf = new JFileChooser();
        jf.setPreferredSize(new Dimension(850,600));
        jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jf.setDialogTitle("Sélectionnner un dossier");
        int rVal = jf.showOpenDialog(panel);
        if (JFileChooser.APPROVE_OPTION == rVal){ //Opens the file panel to select an image
            String path = jf.getSelectedFile().getPath();

            String dltBookQry = "DELETE FROM BOOK;";
            String dltReadingQry = "DELETE FROM Reading;";
            String dltTaggingQry = "DELETE FROM Tagging;";
            String dltTagsQry = "DELETE FROM Tags;";
            String dltSqlite_sequenceQry = "DELETE FROM sqlite_sequence;";

            String BookQry = "REPLACE INTO Book (ID,Title,Author,Image,NumberOP,NotePerso,NoteBabelio,ReleaseYear,AvReadingTime,NumberReading,Summary) " +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?); ";

            String ReadingQry = "REPLACE INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                    "VALUES (?,?,?,?,?); ";

            String TaggingQry = "REPLACE INTO Tagging (IdBook,IdTag) " +
                    "VALUES (?,?); " ;

            String TagsQry = "REPLACE INTO Tags (ID, Tag,Color) " +
                    "VALUES (?,?,?);";

            try (Connection conn = connect();
                 PreparedStatement BookPstmt = conn.prepareStatement(BookQry);
                 PreparedStatement TagsPstmt = conn.prepareStatement(TagsQry);
                 PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry);
                 PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry);
                 PreparedStatement DltBookPstmt = conn.prepareStatement(dltBookQry);
                 PreparedStatement DltReadingPstmt = conn.prepareStatement(dltReadingQry);
                 PreparedStatement DltTaggingPstmt = conn.prepareStatement(dltTaggingQry);
                 PreparedStatement DltTagsPstmt = conn.prepareStatement(dltTagsQry);
                 PreparedStatement DltSqlite_sequencePstmt = conn.prepareStatement(dltSqlite_sequenceQry)){

                DltBookPstmt.executeUpdate();
                DltReadingPstmt.executeUpdate();
                DltTaggingPstmt.executeUpdate();
                DltTagsPstmt.executeUpdate();
                DltSqlite_sequencePstmt.executeUpdate();

                //Book
                BufferedReader lineReaderBook = new BufferedReader(new FileReader(path+"/Book.csv"));
                String lineTextBook;
                lineReaderBook.readLine();
                while ((lineTextBook = lineReaderBook.readLine()) != null) {
                    String[] data = lineTextBook.split("\\|");
                    String ID_Book = data[0];
                    String title = data[1];
                    String author = data[2];
                    String image = data[3];
                    String numberOP = data[4];
                    String notePerso = data[5];
                    String noteBBL = data[6];
                    String releaseYear = data[7];
                    String avReadingTime = data[8];
                    String numberReading = data[9];
                    String summary = data[10];

                    //Book
                    BookPstmt.setString(1, ID_Book);
                    BookPstmt.setString(2, title);
                    BookPstmt.setString(3, author);
                    BookPstmt.setString(4, image);
                    BookPstmt.setString(5, numberOP);
                    BookPstmt.setString(6, notePerso);
                    BookPstmt.setString(7, noteBBL);
                    BookPstmt.setString(8, releaseYear);
                    BookPstmt.setString(9, avReadingTime);
                    BookPstmt.setString(10, numberReading);
                    BookPstmt.setString(11, summary);

                    BookPstmt.executeUpdate();
                }

                //Reading
                BufferedReader lineReaderReading = new BufferedReader(new FileReader(path+"/Reading.csv"));
                String lineTextReading;
                lineReaderReading.readLine();
                while ((lineTextReading = lineReaderReading.readLine()) != null) {
                    String[] data = lineTextReading.split(",");
                    int ID_Reading = Integer.parseInt(data[0]);
                    String title = data[1];
                    String author = data[2];
                    String startReading = data[3];
                    String endReading = data[4];

                    ReadingPstmt.setInt(1, ID_Reading);
                    ReadingPstmt.setString(2, title);
                    ReadingPstmt.setString(3, author);
                    ReadingPstmt.setString(4, startReading);
                    ReadingPstmt.setString(5, endReading);

                    ReadingPstmt.executeUpdate();
                }
                //Tagging
                BufferedReader lineReaderTagging = new BufferedReader(new FileReader(path+"/Tagging.csv"));
                String lineTextTagging;
                lineReaderTagging.readLine();
                while ((lineTextTagging = lineReaderTagging.readLine()) != null) {
                    String[] data = lineTextTagging.split(",");
                    int ID_Book = Integer.parseInt(data[0]);
                    int ID_Tag = Integer.parseInt(data[1]);

                    TaggingPstmt.setInt(1, ID_Book);
                    TaggingPstmt.setInt(2, ID_Tag);

                    TaggingPstmt.executeUpdate();
                }
                //Tags
                BufferedReader lineReaderTags = new BufferedReader(new FileReader(path+"/Tags.csv"));
                String lineTextTags;
                lineReaderTags.readLine();
                while ((lineTextTags = lineReaderTags.readLine()) != null) {
                    String[] data = lineTextTags.split(",");
                    int ID_Tag = Integer.parseInt(data[0]);
                    String tag = data[1];
                    int color = Integer.parseInt(data[2]);

                    TagsPstmt.setInt(1, ID_Tag);
                    TagsPstmt.setString(2, tag);
                    TagsPstmt.setInt(3, color);

                    TagsPstmt.executeUpdate();
                }
                good =1;
            } catch (SQLException e) {
                System.out.println("Datababse error:");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("File IO error:");
                e.printStackTrace();
            }
        }
        if(rVal == JFileChooser.CANCEL_OPTION ) {
            good = 2;
        }
        return good;
    }

    public static void exportDB(){
        JFrame jFrame = new JFrame();
        JOptionPane.showMessageDialog(jFrame, "Les images ne seront pas exportées","WARNING", JOptionPane.WARNING_MESSAGE);
        try{
            Connection conn = connect();
            Date today = new Date();
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Saves/Database/");
            String path = folder+"/"+formater.format(today)+".db";
            Files.createDirectories(folder);

            conn.createStatement().executeUpdate("backup to "+path);
            conn.close();
            updateImageToExport(path);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static int importDB(MainWindow panel){
        int good = 0;
        JFileChooser jf = new JFileChooser();
        jf.setPreferredSize(new Dimension(850,600));
        FileFilter fileFilter = new FileNameExtensionFilter("DB files", "db");
        jf.setFileFilter(fileFilter);
        jf.setDialogTitle("Sélectionnner une database");
        int rVal = jf.showOpenDialog(panel);
        do {
            if (JFileChooser.APPROVE_OPTION == rVal) { //Opens the file panel to select an image
                Path src = Path.of(jf.getSelectedFile().getPath());
                Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager");
                Path dest = Paths.get(folder+"/"+"BookManager.db");
                if (acceptDB(jf.getSelectedFile())){
                    good=1;
                    try {
                        Files.delete(dest);
                        Files.copy(src, dest);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (rVal == JFileChooser.CANCEL_OPTION) {
                good = 2;
            }
        }while (!acceptDB(jf.getSelectedFile()) && rVal==0);
        return good;
    }
}
