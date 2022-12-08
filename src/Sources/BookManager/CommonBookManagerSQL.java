package Sources.BookManager;

import Sources.BookManager.Dialogs.*;

import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static Sources.BookManager.CommonBookManager.*;
import static Sources.BookManager.BookManager.*;
import static Sources.Common.*;
import static Sources.CommonSQL.*;

public class CommonBookManagerSQL {
    public static void deleteBook(BookManager bookManager){
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
                bookManager.fillBookTable(bookManager.isFiltered());
                isNotInFilteredBookList(bookManager, true);
                if(bookManager.isFastSearch()){
                    bookManager.fastSearchBook(bookManager.getBookFastSearch().getText());
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Suppression impossible", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void addBook(AddBookDlg diag, BookManager bookManager){
        if (diag.isValide()){
            String BookQry = "INSERT INTO Book (Title,Author,Image,NumberOP,NotePerso,NoteBabelio,ReleaseYear,Summary) " +
                    "VALUES (?,?,?,?,?,?,?,?);";
            String ReadingQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                    "VALUES (?,?,?,?,?);";
            String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                    "VALUES (?,?);";
            String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=?"+
                    "WHERE Title='"+diag.getNewBookTitle()+"' AND Author='"+diag.getNewBookAuthor()+"'";

            bookManager.getContentPanel().updateUI();
            try (Connection conn = connect(); PreparedStatement BookPstmt = conn.prepareStatement(BookQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry);
                 PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement TaggingPstmt = conn.prepareStatement(TaggingQry)) {

                ReadingPstmt.setInt(1, getIdReading(diag.getNewBookTitle(), diag.getNewBookAuthor()));
                ReadingPstmt.setString(2, diag.getNewBookTitle());
                ReadingPstmt.setString(3, diag.getNewBookAuthor());

                BookPstmt.setString(1, diag.getNewBookTitle());
                BookPstmt.setString(2, diag.getNewBookAuthor());
                BookPstmt.setString(3, getNameOfImage());
                BookPstmt.setString(4, diag.getNewBookNumberOP());
                BookPstmt.setString(5, diag.getNewBookPersonalNote());
                BookPstmt.setString(6, diag.getNewBookBBLNote());
                BookPstmt.setString(7, diag.getNewBookReleaseYear());
                BookPstmt.setString(8, diag.getNewBookSummary());

                if(diag.isDateKnown() && !diag.isNotDOne()){
                    ReadingPstmt.setString(4, diag.getNewBookStartReading());
                    ReadingPstmt.setString(5, diag.getNewBookEndReading());

                } else if (diag.isDateKnown() && diag.isNotDOne()) {
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
                if(bookManager.isFiltered() || bookManager.isFastSearch()) {
                    if (isInFilteredList(diag.getNewBookTitle(), diag.getNewBookAuthor(), bookManager.getBooksTable())) {
                        bookManager.fillBookTable(bookManager.isFiltered());
                        bookManager.setMTitle(diag.getNewBookTitle());
                        bookManager.setAuthor(diag.getNewBookAuthor());
                        bookManager.setRowReading(0);
                        bookManager.setRowSelected(bookManager.getRowSelectedByBook(getMTitle(), getAuthor()));
                        bookManager.loadComponents(diag.getNewBookTitle(), getAuthor());//reload changes made to the book
                    } else {
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "Le livre créé ne correspond pas aux critères appliqué", "WARNING", JOptionPane.WARNING_MESSAGE);
                    }
                }else{
                    bookManager.fillBookTable(bookManager.isFiltered());
                    bookManager.setMTitle(diag.getNewBookTitle());
                    bookManager.setAuthor(diag.getNewBookAuthor());
                    bookManager.setRowReading(0);
                    bookManager.setRowSelected(bookManager.getRowSelectedByBook(getMTitle(), getAuthor()));
                    bookManager.loadComponents(getMTitle(), getAuthor());//reload changes made to the book
                }
                resetBookManager(bookManager, true);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Ajout impossible", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void editBook(EditBookDlg diag, BookManager bookManager){
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
                BookPstmt.setString(3, getNameOfImage());
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

                bookManager.getContentPanel().updateUI();
                bookManager.fillBookTable(bookManager.isFiltered());
                bookManager.setMTitle(diag.getNewTitle());
                bookManager.setAuthor(diag.getNewAuthor());
                isItInFilteredBookList(bookManager, false);

                if(bookManager.isFastSearch()){
                    bookManager.fastSearchBook(bookManager.getBookFastSearch().getText());
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Edition impossible", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void addReading(AddReading diag, BookManager bookManager){
        if (diag.getIsValid()){
            String ReadingQry = "INSERT INTO Reading (ID,Title,Author,StartReading, EndReading) " +
                    "VALUES (?,?,?,?,?);";
            String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=?"+
                    "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
            bookManager.getContentPanel().updateUI();
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

                bookManager.setMTitle(getMTitle());
                bookManager.setAuthor(getAuthor());
                bookManager.fillBookTable(bookManager.isFiltered());
                bookManager.setRowReading(bookManager.getManageReading().getRowCount());
                bookManager.loadComponents(getMTitle(), getAuthor());
                if(bookManager.isFastSearch()){
                    bookManager.fastSearchBook(bookManager.getBookFastSearch().getText());
                }
            }catch (SQLException e){
                System.out.println(e.getMessage());
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Ajout impossible", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void editReading(EditReadingDlg diag, BookManager bookManager){
        if(diag.isValid()){
            String sql = "UPDATE Reading SET StartReading=?, EndReading=?" +
                    "WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+bookManager.getRowReading()+"'";//Edit in bdd the item that we want to change the reading date
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
                bookManager.fillBookTable(bookManager.isFiltered());
                isItInFilteredBookList(bookManager, false);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Edtion impossible", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void deleteReading(BookManager bookManager){
        String ReadingQry = "DELETE FROM Reading WHERE Title='"+ getMTitle()+"' AND Author='"+getAuthor()+"' AND ID='"+bookManager.getRowReading()+"'";//Delete in bdd the item that we want delete
        String AvNumQry = "UPDATE Book SET AvReadingTime=?, NumberReading=? WHERE Title='"+getMTitle()+"' AND Author='"+getAuthor()+"'";
        if(bookManager.getReadingsTable().getRowCount()>1){//If there is more than one reading you don't need to know if the person really wants to delete the book
            try (Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)) {
                ReadingPstmt.executeUpdate();
                AvNumPstmt.setInt(1, averageTime(getMTitle(), getAuthor()));
                AvNumPstmt.setInt(2, getNumberOfReading(getMTitle(), getAuthor()));
                AvNumPstmt.executeUpdate();

                bookManager.getContentPanel().updateUI();
                if(bookManager.getRowReading()>0) {
                    bookManager.setRowReading(bookManager.getRowReading() - 1);
                }
                bookManager.getReadingsTable().setRowSelectionInterval(bookManager.getRowReading(), bookManager.getRowReading());
                //load bdd in MainWindow
                bookManager.fillBookTable(bookManager.isFiltered());
                isItInFilteredBookList(bookManager, true);
                bookManager.getManageReading().resetIdReading(bookManager.getManageReading().getRowCount());//refresh all ID in the table ReadingDate
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Supression impossible", JOptionPane.ERROR_MESSAGE);
            }
        }
        else{
            deleteBook(bookManager);
        }
    }
    public static void filtersBook(FiltersDlg diag, BookManager bookManager){
        if(diag.getIsValid()) {
            if(bookManager.isFiltered()){
                bookManager.fillBookTable(bookManager.isFiltered());
            }
            else{
                bookManager.fillBookTable(diag.getIsValid());
                bookManager.setIsFiltered(diag.getIsValid());
            }
            isItInFilteredBookList(bookManager,false);
        }else{
            if (bookManager.getBooksTable().getRowCount() > 0) {
                bookManager.loadComponents(getMTitle(), getAuthor());
                bookManager.getBooksTable().setRowSelectionInterval(bookManager.getRowSelectedByBook(getMTitle(), getAuthor()), bookManager.getRowSelectedByBook(getMTitle(), getAuthor()));
                bookManager.getReadingsTable().setRowSelectionInterval(bookManager.getRowReading(),bookManager.getRowReading());
            } else
                bookManager.initComponents();
        }
        if(bookManager.isFastSearch()){
            bookManager.fastSearchBook(bookManager.getBookFastSearch().getText());
        }
        bookManager.getContentPanel().updateUI();
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
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "MAJ image impossible", JOptionPane.ERROR_MESSAGE);
        }
    }
    @SuppressWarnings("unchecked")
    public static void fillAuthorCB(JComboBox authorCB){
        authorCB.removeAllItems();
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
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Remplissage combobox auteur impossible", JOptionPane.ERROR_MESSAGE);
        }
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
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Récupération id lecture impossible", JOptionPane.ERROR_MESSAGE);
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
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Récupération id livre impossible", JOptionPane.ERROR_MESSAGE);
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
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Récupération image impossible", JOptionPane.ERROR_MESSAGE);
        }
        return name;
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
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Temps moyens de lecture impossible", JOptionPane.ERROR_MESSAGE);
        }
        return average;
    }
    public static int getNumberOfReading(String title, String author){
        int i = 0;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet CountReadingQry = statement.executeQuery("SELECT COUNT(*) FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'");
            i = CountReadingQry.getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Récupération nombre lecture impossible", JOptionPane.ERROR_MESSAGE);
        }
        return i;
    }
    public static int getNumberOfBook(){
        int i = 0;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet CountReadingQry = statement.executeQuery("SELECT COUNT(*) FROM Book");
            i = CountReadingQry.getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Récupération nombre livre impossible", JOptionPane.ERROR_MESSAGE);
        }
        return i;
    }
}