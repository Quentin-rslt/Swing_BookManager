package Sources;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class CommonSQL {
    public static Connection connect() {
        Connection connection = null;
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
        int i =0;
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
        String sql = "SELECT Tag,Color FROM Tags";
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
}
