package Sources;

import javax.imageio.ImageIO;
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

import static Sources.Common.*;

public class ImportExportData {

    public static void exportCSV(){
        try(Connection conn = connect()){
            String qry = "SELECT * FROM Book, Reading, Tagging, Tags "+
                    "WHERE Book.ID=Tagging.IdBook AND Tagging.IdTag=Tags.ID AND Book.Title=Reading.Title AND Book.Author=Reading.Author";

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(qry);

            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager/Export");
            Files.createDirectories(folder);
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(folder+"/BookManager.csv"));
            fileWriter.write("ID_Book,Title,Author,Image,NumberOP,NotePerso,NoteBabelio,ReleaseYear,AvReadingTime,NumberReading,Summary,ID_Reading" +
                    ",StartReading,EndReading,ID_Tag,Tag,Color");

            while(rs.next())
            {
                int ID_Book = rs.getInt(1);
                String title = rs.getString(2);
                String author = rs.getString(3);
                String image = rs.getString(4);
                int numberOP = rs.getInt(5);
                float notePerso = rs.getFloat(6);
                float noteBBL = rs.getFloat(7);
                int releaseYear = rs.getInt(8);
                int avReadingTime = rs.getInt(9);
                int numberReading = rs.getInt(10);
                String summary = rs.getString(11);
                int ID_Reading = rs.getInt(12);
                String startReading = rs.getString(15);
                String endReading = rs.getString(16);
                int idTag = rs.getInt(18);
                String tag = rs.getString(20);
                int color = rs.getInt(21);


                String line = String.format(
                        "%d,%s,%s,%s,%d,%.1f,%.2f,%d,%d,%d,%s,%d,%s,%s,%d,%s,%d",
                        ID_Book, title,author, image, numberOP, notePerso,noteBBL, releaseYear, avReadingTime,numberReading, summary, ID_Reading,
                        startReading, endReading, idTag, tag,color);

                fileWriter.newLine();
                fileWriter.write(line);
            }
            statement.close();
            fileWriter.close();
        }
        catch (SQLException e) {
            System.out.println("Datababse error:");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File IO error:");
            e.printStackTrace();
        }
    }
    public static void importCSV(MainWindow panel){
        FileFilter imageFilter = new FileNameExtensionFilter("CSV files", "csv");
        JFileChooser jf = new JFileChooser();
        jf.setFileFilter(imageFilter);
        jf.setPreferredSize(new Dimension(850,600));
        int rVal;
        do{
            rVal = jf.showOpenDialog(panel);
            if (JFileChooser.APPROVE_OPTION == rVal){ //Opens the file panel to select an image
                String path = jf.getSelectedFile().getPath();
                if (acceptCSV(jf.getSelectedFile())){

                    String BookQry = "REPLACE INTO Book (ID,Title,Author,Image,NumberOP,NotePerso,NoteBabelio,ReleaseYear,AvReadingTime,NumberReading,Summary) " +
                            " VALUES (?,?,?,?,?,?,?,?,?,?,?); ";

                    String ReadingQry = "REPLACE INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                            "SELECT ?,?,?,?,? " +
                            "WHERE NOT EXISTS " +
                            "(SELECT * FROM Reading WHERE Title = ? AND Author = ? AND StartReading = ? AND EndReading = ?); ";

                    String TaggingQry = "REPLACE INTO Tagging (IdBook,IdTag) " +
                            "SELECT ?,? " +
                            "WHERE NOT EXISTS" +
                            "(SELECT * FROM Tagging WHERE IdBook = ? AND IdTag = ?); ";

                    String TagsQry = "REPLACE INTO Tags (ID, Tag,Color) " +
                            "VALUES (?,?,?);";

                    try (Connection conn = connect();
                         PreparedStatement BookPstmt = conn.prepareStatement(BookQry);
                         PreparedStatement TagsPstmt = conn.prepareStatement(TagsQry);
                         PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry);
                         PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry)){

                        BufferedReader lineReader = new BufferedReader(new FileReader(path));
                        String lineText = null;
                        lineReader.readLine(); // skip header line

                        while ((lineText = lineReader.readLine()) != null) {
                            String[] data = lineText.split(",");
                            int ID_Book = Integer.parseInt(data[0]);
                            String title = data[1];
                            String author = data[2];
                            String image = data[3];
                            int numberOP = Integer.parseInt(data[4]);
                            float notePerso = Float.parseFloat(data[5]);
                            float noteBBL = Float.parseFloat(data[6]);
                            int releaseYear = Integer.parseInt(data[7]);
                            int avReadingTime = Integer.parseInt(data[8]);
                            int numberReading = Integer.parseInt(data[9]);
                            String summary = data[10];
                            int ID_Reading = Integer.parseInt(data[11]);
                            String startReading = data[12];
                            String endReading = data[13];
                            int idTag = Integer.parseInt(data[14]);
                            String tag = data[15];
                            int color = Integer.parseInt(data[16]);

                            //Book
                            BookPstmt.setInt(1,ID_Book);
                            BookPstmt.setString(2,title);
                            BookPstmt.setString(3,author);
                            BookPstmt.setString(4,image);
                            BookPstmt.setInt(5,numberOP);
                            BookPstmt.setFloat(6,notePerso);
                            BookPstmt.setFloat(7,noteBBL);
                            BookPstmt.setInt(8,releaseYear);
                            BookPstmt.setInt(9,avReadingTime);
                            BookPstmt.setInt(10,numberReading);
                            BookPstmt.setString(11,summary);

                            //Reading
                            ReadingPstmt.setInt(1,ID_Reading);
                            ReadingPstmt.setString(2,title);
                            ReadingPstmt.setString(3,author);
                            ReadingPstmt.setString(4,startReading);
                            ReadingPstmt.setString(5,endReading);

                            //Tagging
                            TaggingPstmt.setInt(1,ID_Book);
                            TaggingPstmt.setInt(2,idTag);

                            //Tags
                            TagsPstmt.setInt(1,idTag);
                            TagsPstmt.setString(2,tag);
                            TagsPstmt.setInt(3,color);

                            BookPstmt.executeUpdate();
                            ReadingPstmt.executeUpdate();
                            TaggingPstmt.executeUpdate();
                            TagsPstmt.executeUpdate();
                        }
                        conn.close();
                    } catch (SQLException e) {
                        System.out.println("Datababse error:");
                        e.printStackTrace();
                    } catch (IOException e) {
                        System.out.println("File IO error:");
                        e.printStackTrace();
                    }
                }else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Veuillez choisir un format csv !");
                }
            }
        } while (!acceptCSV(jf.getSelectedFile()) && rVal==0);
    }
}
