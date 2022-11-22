package Sources;

import Sources.Dialogs.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static Sources.Common.*;
import static Sources.MainWindow.getAuthor;
import static Sources.MainWindow.getMTitle;

public class CommonSQL {
    public static void deleteBook(MainWindow parent){
        JFrame jFrame = new JFrame();
        int n = JOptionPane.showConfirmDialog(//Open a optionPane to verify if the user really want to delete the book return 0 il they want and 1 if they refuse
                jFrame,
                "Etes-vous sûr de vouloir supprimer définitivement le livre ?\n"+"Cette acion sera irréversible !",
                "An Inane Question",
                JOptionPane.YES_NO_OPTION);
        if(n == 0){
            String boolQry = "DELETE FROM Book WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
            String ReadingQry = "DELETE FROM Reading WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
            String TaggingQry = "DELETE FROM Tagging WHERE IdBook='"+getIdBook(getMTitle(),getAuthor())+"'";

            deleteImageMainResource(getMTitle(), getAuthor());
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(boolQry); PreparedStatement pstmt2 = conn.prepareStatement(ReadingQry);
                 PreparedStatement taggingPstmt = conn.prepareStatement(TaggingQry)) {
                // execute the delete statement
                pstmt.executeUpdate();
                pstmt2.executeUpdate();
                taggingPstmt.executeUpdate();
                parent.fillBookTable(parent.isFiltered());
                isNotInFilteredBookList(parent, true);
                if(parent.isFastSearch()){
                    parent.fastSearchBook(parent.getBookFastSearch().getText());
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public static void addBook(AddBookDlg diag, MainWindow parent){
        if (diag.isValide()){
            String BookQry = "INSERT INTO Book (Title,Author,Image,NumberOP,NotePerso,NoteBabelio,ReleaseYear,Summary) " +
                    "VALUES (?,?,?,?,?,?,?,?);";
            String ReadingQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                    "VALUES (?,?,?,?,?);";
            String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                    "VALUES (?,?);";
            String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=?"+
                    "WHERE Title='"+diag.getNewBookTitle()+"' AND Author='"+diag.getNewBookAuthor()+"'";

            parent.getContentPanel().updateUI();
            try (Connection conn = connect(); PreparedStatement BookPstmt = conn.prepareStatement(BookQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry);
                 PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry)) {

                ReadingPstmt.setInt(1, getIdReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                ReadingPstmt.setString(2, diag.getNewBookTitle());
                ReadingPstmt.setString(3, diag.getNewBookAuthor());

                BookPstmt.setString(1, diag.getNewBookTitle());
                BookPstmt.setString(2, diag.getNewBookAuthor());
                BookPstmt.setString(3, getNameOfBook());
                BookPstmt.setString(4, diag.getNewBookNumberOP());
                BookPstmt.setString(5, diag.getNewBookPersonalNote());
                BookPstmt.setString(6, diag.getNewBookBBLNote());
                BookPstmt.setString(7, diag.getNewBookReleaseYear());
                BookPstmt.setString(8, diag.getNewBookSummary());

                if(diag.isDateUnknown() && !diag.isNotDOne()){
                    ReadingPstmt.setString(4, diag.getNewBookStartReading());
                    ReadingPstmt.setString(5, diag.getNewBookEndReading());

                } else if (diag.isDateUnknown() && diag.isNotDOne()) {
                    ReadingPstmt.setString(4, diag.getNewBookStartReading());
                    ReadingPstmt.setString(5, "Pas fini");
                } else {
                    ReadingPstmt.setInt(1, getIdReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                    ReadingPstmt.setString(4, "Inconnu");
                    ReadingPstmt.setString(5, "Inconnu");
                }
                BookPstmt.executeUpdate();//Insert the new Book in table Book
                ReadingPstmt.executeUpdate();//Insert the new reading

                AvNumPstmt.setInt(1, averageTime(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                AvNumPstmt.setInt(2, getNumberOfReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                AvNumPstmt.executeUpdate();

                for(int i=0; i<diag.getTags().getSizeTags(); i++){
                    if(diag.getTagIsUpdate()){
                        String TagsUpdateQry = "UPDATE Tags SET Color=?"+
                                "WHERE Tag='"+diag.getTags().getTag(i).getTextTag()+"'";
                        PreparedStatement TagsUpdatePstmt = conn.prepareStatement(TagsUpdateQry);
                        TagsUpdatePstmt.setInt(1, diag.getTags().getTag(i).getColor());
                        TagsUpdatePstmt.executeUpdate();
                    }

                    String TagsInsertQry = "INSERT INTO Tags (Tag,Color)" +
                            " SELECT '"+ diag.getTags().getTag(i).getTextTag() +"', '"+diag.getTags().getTag(i).getColor()+"'" +
                            " WHERE NOT EXISTS(SELECT * FROM Tags WHERE Tag='" + diag.getTags().getTag(i).getTextTag() + "' AND Color='" + diag.getTags().getTag(i).getColor() + "')";
                    PreparedStatement TagsInsertPstmt = conn.prepareStatement(TagsInsertQry);
                    TagsInsertPstmt.executeUpdate();

                    TaggingPstmt.setInt(1, getIdBook(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                    TaggingPstmt.setInt(2, getIdTag(diag.getTags().getTag(i).getTextTag(), diag.getTags().getTag(i).getColor()));
                    TaggingPstmt.executeUpdate();
                }
                if(parent.isFiltered() || parent.isFastSearch()) {
                    if (isInFilteredList(diag.getNewBookTitle(), diag.getNewBookAuthor(), parent.getBooksTable())) {
                        parent.fillBookTable(parent.isFiltered());
                        parent.setMTitle(diag.getNewBookTitle());
                        parent.setAuthor(diag.getNewBookAuthor());
                        parent.setRowReading(0);
                        parent.setRowSelected(parent.getRowSelectedByBook(getMTitle(), getAuthor()));
                        parent.loadComponents(diag.getNewBookTitle(), getAuthor());//reload changes made to the book
                    } else {
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "Le livre créé ne correspond pas aux critères appliqué", "WARNING", JOptionPane.WARNING_MESSAGE);
                    }
                }else{
                    parent.fillBookTable(parent.isFiltered());
                    parent.setMTitle(diag.getNewBookTitle());
                    parent.setAuthor(diag.getNewBookAuthor());
                    parent.setRowReading(0);
                    parent.setRowSelected(parent.getRowSelectedByBook(getMTitle(), getAuthor()));
                    parent.loadComponents(getMTitle(), getAuthor());//reload changes made to the book
                }
                resetApp(parent, true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public static void editBook(EditBookDlg diag, MainWindow parent){
        if (diag.isValid()){
            String BookQry = "UPDATE Book SET Title=?, Author=?, Image=?, NumberOP=?, NotePerso=?, NoteBabelio=?, ReleaseYear=?, Summary=?"+
                    "WHERE Title='"+diag.getOldTitle()+"' AND Author='"+diag.getOldAuthor()+"'";//Edit in bdd the book that we want to change
            String ReadingQry = "UPDATE Reading SET Title=?, Author=?"+
                    "WHERE Title='"+diag.getOldTitle()+"' AND Author='"+diag.getOldAuthor()+"'";//Edit in bdd the book that we want to change
            String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                    "VALUES (?,?);";
            String DeleteTaggingQry = "DELETE FROM Tagging WHERE IdBook='"+getIdBook(diag.getOldTitle(), diag.getOldAuthor())+"'";
            try (Connection conn = connect(); PreparedStatement BookPstmt = conn.prepareStatement(BookQry); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry);
                 PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry); PreparedStatement DeleteTaggingPstmt = conn.prepareStatement(DeleteTaggingQry)) {
                // execute the uptdate statement
                ReadingPstmt.setString(1, diag.getNewTitle());
                ReadingPstmt.setString(2, diag.getNewAuthor());
                BookPstmt.setString(1, diag.getNewTitle());
                BookPstmt.setString(2, diag.getNewAuthor());
                BookPstmt.setString(3, getNameOfBook());
                BookPstmt.setString(4, diag.getNewNumberPage());
                BookPstmt.setString(5, diag.getNewPersonnalNote());
                BookPstmt.setString(6, diag.getNewBBLNote());
                BookPstmt.setString(7, diag.getNewReleaseyear());
                BookPstmt.setString(8, diag.getNewSummary());
                BookPstmt.executeUpdate();
                ReadingPstmt.executeUpdate();
                DeleteTaggingPstmt.executeUpdate();

                for(int i=0; i<diag.getTags().getSizeTags(); i++){
                    if(diag.getTagIsUpdate()){
                        String TagsUpdateQry = "UPDATE Tags SET Color=?"+
                                "WHERE Tag='"+diag.getTags().getTag(i).getTextTag()+"'";
                        PreparedStatement TagsUpdatePstmt = conn.prepareStatement(TagsUpdateQry);
                        TagsUpdatePstmt.setInt(1, diag.getTags().getTag(i).getColor());
                        TagsUpdatePstmt.executeUpdate();
                    }

                    String TagsInsertQry = "INSERT INTO Tags (Tag,Color)" +
                            " SELECT '"+ diag.getTags().getTag(i).getTextTag() +"', '"+diag.getTags().getTag(i).getColor()+"'" +
                            " WHERE NOT EXISTS(SELECT * FROM Tags WHERE Tag='" + diag.getTags().getTag(i).getTextTag() + "' AND Color='" + diag.getTags().getTag(i).getColor() + "')";
                    PreparedStatement TagsInsertPstmt = conn.prepareStatement(TagsInsertQry);
                    TagsInsertPstmt.executeUpdate();

                    TaggingPstmt.setInt(1, getIdBook(diag.getNewTitle(), diag.getNewAuthor()));
                    TaggingPstmt.setInt(2, getIdTag(diag.getTags().getTag(i).getTextTag(), diag.getTags().getTag(i).getColor()));
                    TaggingPstmt.executeUpdate();
                }

                parent.getContentPanel().updateUI();
                parent.fillBookTable(parent.isFiltered());
                parent.setMTitle(diag.getNewTitle());
                parent.setAuthor(diag.getNewAuthor());
                isItInFilteredBookList(parent, false);

                if(parent.isFastSearch()){
                    parent.fastSearchBook(parent.getBookFastSearch().getText());
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public static void addReading(AddReading diag, MainWindow parent){
        if (diag.getIsValid()){
            String ReadingQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                    "VALUES (?,?,?,?,?);";
            String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=?"+
                    "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
            parent.getContentPanel().updateUI();
            try(Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)){
                ReadingPstmt.setInt(1, getIdReading(getMTitle(), getAuthor()));
                ReadingPstmt.setString(2, getMTitle());
                ReadingPstmt.setString(3, getAuthor());

                if(!diag.isDateUnknown()&& !diag.isNotDone()){
                    ReadingPstmt.setString(4, diag.getNewStartReading());
                    ReadingPstmt.setString(5, diag.getNewEndReading());
                }else if (!diag.isDateUnknown() && diag.isNotDone()) {
                    ReadingPstmt.setString(4, diag.getNewStartReading());
                    ReadingPstmt.setString(5, "Pas fini");
                }
                else {
                    ReadingPstmt.setString(4, "Inconnu");
                    ReadingPstmt.setString(5, "Inconnu");
                }
                ReadingPstmt.executeUpdate();//Insert the new reading

                AvNumPstmt.setInt(1, averageTime(getMTitle(), getAuthor()));
                AvNumPstmt.setInt(2, getNumberOfReading(getMTitle(), getAuthor()));
                AvNumPstmt.executeUpdate();

                parent.setMTitle(getMTitle());
                parent.setAuthor(getAuthor());
                parent.fillBookTable(parent.isFiltered());
                parent.setRowReading(parent.getManageReading().getRowCount());
                parent.loadComponents(getMTitle(), getAuthor());
                if(parent.isFastSearch()){
                    parent.fastSearchBook(parent.getBookFastSearch().getText());
                }
            }catch (SQLException e){
                System.out.println(e.getMessage());
            }
        }
    }
    public static void editReading(EditReadingDlg diag, MainWindow parent){
        if(diag.isValid()){
            String sql = "UPDATE Reading SET StartReading=?, EndReading=?" +
                    "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+parent.getRowReading()+"'";//Edit in bdd the item that we want to change the reading date
            String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=? WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)) {
                // execute the uptdate statement
                pstmt.setString(1, diag.getNewStartReading());
                pstmt.setString(2, diag.getNewEndReading());
                pstmt.executeUpdate();

                AvNumPstmt.setInt(1, averageTime(getMTitle(), getAuthor()));
                AvNumPstmt.setInt(2, getNumberOfReading(getMTitle(), getAuthor()));
                AvNumPstmt.executeUpdate();

                //if the book is no longer in the filters then load on the first line
                parent.fillBookTable(parent.isFiltered());
                isItInFilteredBookList(parent, false);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public static void filtersBook(FiltersDlg diag, MainWindow parent){
        if(diag.getIsValid()) {
            if(parent.isFiltered()){
                parent.fillBookTable(parent.isFiltered());
            }
            else{
                parent.fillBookTable(diag.getIsValid());
                parent.setIsFiltered(diag.getIsValid());
            }
            isItInFilteredBookList(parent,false);
        }else{
            if (parent.getBooksTable().getRowCount() > 0) {
                parent.loadComponents(getMTitle(), getAuthor());
                parent.getBooksTable().setRowSelectionInterval(parent.getRowSelectedByBook(getMTitle(), getAuthor()), parent.getRowSelectedByBook(getMTitle(), getAuthor()));
                parent.getReadingsTable().setRowSelectionInterval(parent.getRowReading(),parent.getRowReading());
            } else
                parent.initComponents();
        }
        if(parent.isFastSearch()){
            parent.fastSearchBook(parent.getBookFastSearch().getText());
        }
        parent.getContentPanel().updateUI();
    }
    public static void updateImageToExport(String path){
        String replaceImageQry = "UPDATE Book SET Image=?";
        try{
            String url = "jdbc:sqlite:"+path;
            Connection connection = DriverManager.getConnection(url);

            PreparedStatement replaceImagePstmt = connection.prepareStatement(replaceImageQry);
            replaceImagePstmt.setString(1, "Default.jpg");
            replaceImagePstmt.executeUpdate();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("unchecked")
    public static void fillAuthorCB(JComboBox authorCB){
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet authorRs = statement.executeQuery("SELECT Author FROM Book  GROUP BY Author");
            authorCB.addItem("");
            while (authorRs.next()){
                authorCB.addItem(authorRs.getString(1));
            }

            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public static Connection connect() {
        Connection connection = null;
        try {
            Path folder = Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath(),"BookManager");
            Files.createDirectories(folder);

            String url = "jdbc:sqlite:"+folder+"/BookManager.db";

            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        }
        return i;
    }
    public static String getImageBDD(String title, String author) {
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
        }
        return name;
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
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }

        return tags;
    }
    public static int averageTime(String title, String author) {
        if(title.contains("'")){
            title = title.replace("'","''");
        }
        String sql = "SELECT StartReading, EndReading FROM Reading WHERE Title='" + title + "' AND Author='" + author + "'";
        long days = 0;
        int dateValid = 0;
        int average = 0;
        try (Connection conn = connect()) {
            Statement statement =conn.createStatement();
            ResultSet qry = statement.executeQuery(sql);

            while (qry.next()) {
                boolean isOk = ((qry.getString(1).equals("Inconnu") && qry.getString(2).equals("Inconnu")) ||
                        (qry.getString(1).equals("Inconnu") && qry.getString(2).equals("Pas fini")))
                        || ((!qry.getString(1).equals("Inconnu") && qry.getString(2).equals("Inconnu")) ||
                        (!qry.getString(1).equals("Inconnu") && qry.getString(2).equals("Pas fini")))
                        || ((qry.getString(1).equals("Inconnu") && !qry.getString(2).equals("Inconnu")) ||
                        (qry.getString(1).equals("Inconnu") && !qry.getString(2).equals("Pas fini")));
                if (!isOk) {
                    dateValid++;
                    LocalDate start = LocalDate.parse(qry.getString(1));
                    LocalDate stop = LocalDate.parse(qry.getString(2));
                    days = days + ChronoUnit.DAYS.between(start, stop);
                    average= (int) (days/dateValid)+1;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return average;
    }
    public static int getNumberOfReading(String title, String author){
        int i;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet CountReadingQry = statement.executeQuery("SELECT COUNT(*) FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'");
            i = CountReadingQry.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return i;
    }
    public static int getNumberOfBook(){
        int i;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet CountReadingQry = statement.executeQuery("SELECT COUNT(*) FROM Book");
            i = CountReadingQry.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return i;
    }
}