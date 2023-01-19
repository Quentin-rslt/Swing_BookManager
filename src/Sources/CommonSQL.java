package Sources;

import Sources.Components.Tags;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

import static Sources.BookManager.CommonBookManagerSQL.getIdBook;

public class CommonSQL {
    public static Connection connect() {
        Connection connection;
        try {
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"MyManager");
            Files.createDirectories(folder);

            String url = "jdbc:sqlite:"+folder+"/MyManager.db";

            connection = DriverManager.getConnection(url);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return connection;
    }
    public static int getIdTag(String tag, int color) {
        int i;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet idBook = statement.executeQuery("SELECT ID FROM Tags WHERE Tag='"+tag+"' AND Color='"+color+ "'");
            i=idBook.getInt(1);
            idBook.close();
            conn.close();
            statement.close();
        }catch (Exception e){
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Récupération id tag impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
        return i;
    }
    public static Tags loadTags(){
        Tags tags = new Tags();
        String sql = "SELECT Tag,Color FROM Tags ORDER BY Tag ASC";
        try(Connection conn = connect()) {
            Class.forName("org.sqlite.JDBC");
            Statement statement = conn.createStatement();
            ResultSet tagsQry = statement.executeQuery(sql);
            while (tagsQry.next()){
                tags.createTag(tagsQry.getString(1));
                tags.getTag(tags.getSizeTags()-1).setColor(tagsQry.getInt(2));
            }
            conn.close();
            statement.close();
        }catch (Exception e){
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Récupération tag impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }

        return tags;
    }
    public static Tags loadTags(String title, String author, JPanel panel){
        Tags tags = new Tags();
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();

            //Tags
            ResultSet tagsQry = statement.executeQuery("SELECT Tag,Color FROM Tags JOIN Tagging on Tags.ID=Tagging.IdTag " +
                    "WHERE Tagging.IdBook='"+getIdBook(title, author)+"' ORDER BY Tag ASC");
            panel.removeAll();
            while (tagsQry.next()){
                tags.createTag(tagsQry.getString(1));
                tags.getTag(tagsQry.getRow()-1).setColor(tagsQry.getInt(2));
                panel.add(tags.getTag(tags.getSizeTags()-1));
            }
            panel.updateUI();

            conn.close();
            statement.close();
        } catch (Exception e ) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement du livre impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
        return tags;
    }
    public static Tags loadTags(JPanel panel){
        Tags tags = new Tags();
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();

            //Tags
            ResultSet tagsQry = statement.executeQuery("SELECT Tag, Color FROM Tags " +
                    "ORDER BY Tag ASC");
            panel.removeAll();
            while (tagsQry.next()){
                tags.createTag(tagsQry.getString(1));
                tags.getTag(tagsQry.getRow()-1).setColor(tagsQry.getInt(2));
                panel.add(tags.getTag(tags.getSizeTags()-1));
            }
            panel.updateUI();

            conn.close();
            statement.close();
        } catch (Exception e ) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement du livre impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
        return tags;
    }
}
