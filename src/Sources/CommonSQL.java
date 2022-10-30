package Sources;

import Sources.Dialogs.AddBookDlg;
import Sources.Dialogs.AddReading;
import Sources.Dialogs.EditBookDlg;
import Sources.Dialogs.FiltersDlg;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static Sources.Common.*;
import static Sources.MainWindow.getAuthor;
import static Sources.MainWindow.getMTitle;

public class CommonSQL {
    public static void deleteBook(String title, String  author, MainWindow parent){
        JFrame jFrame = new JFrame();
        int n = JOptionPane.showConfirmDialog(//Open a optionPane to verify if the user really want to delete the book return 0 il they want and 1 if they refuse
                jFrame,
                "Etes-vous sûr de vouloir supprimer définitivement le livre ?\n"+"Cette acion sera irréversible !",
                "An Inane Question",
                JOptionPane.YES_NO_OPTION);
        if(n == 0){
            String boolQry = "DELETE FROM Book WHERE Title='"+title+"' AND Author='"+author+"'";
            String ReadingQry = "DELETE FROM Reading WHERE Title='"+title+"' AND Author='"+author+"'";
            String TaggingQry = "DELETE FROM Tagging WHERE IdBook='"+getIdBook(title,author)+"'";

            deleteImageMainResource(title, author);
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(boolQry); PreparedStatement pstmt2 = conn.prepareStatement(ReadingQry);
                 PreparedStatement taggingPstmt = conn.prepareStatement(TaggingQry)) {
                // execute the delete statement
                pstmt.executeUpdate();
                pstmt2.executeUpdate();
                taggingPstmt.executeUpdate();
                parent.loadDB(parent.isFiltered());
                isNotInFilteredBookList(parent);
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
                Statement statement = conn.createStatement();

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

                if(!diag.isDateUnknown() && !diag.isNotDOne()){
                    ReadingPstmt.setString(4, diag.getNewBookStartReading());
                    ReadingPstmt.setString(5, diag.getNewBookEndReading());

                } else if (!diag.isDateUnknown() && diag.isNotDOne()) {
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

                parent.loadDB(parent.isFiltered());
                parent.setMTitle(diag.getNewBookTitle());
                parent.setAuthor(diag.getNewBookAuthor());
                isItInFilteredBookList(getMTitle(),getAuthor(),parent);
                if(parent.isFastSearch()){
                    parent.fastSearchBook(parent.getBookFastSearch().getText());
                }
                conn.close();
                statement.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public static void editBook(EditBookDlg diag, String title, String author, MainWindow parent){
        if (diag.isValid()){
            String BookQry = "UPDATE Book SET Title=?, Author=?, Image=?, NumberOP=?, NotePerso=?, NoteBabelio=?, ReleaseYear=?, Summary=?"+
                    "WHERE Title='"+title+"' AND Author='"+author+"'";//Edit in bdd the book that we want to change
            String ReadingQry = "UPDATE Reading SET Title=?, Author=?"+
                    "WHERE Title='"+title+"' AND Author='"+author+"'";//Edit in bdd the book that we want to change
            String TaggingQry = "INSERT INTO Tagging (IdBook,IdTag) " +
                    "VALUES (?,?);";
            String DeleteTaggingQry = "DELETE FROM Tagging WHERE IdBook='"+getIdBook(title,author)+"'";
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
                parent.loadDB(parent.isFiltered());
                parent.setMTitle(diag.getNewTitle());
                parent.setAuthor(diag.getNewAuthor());
                isItInFilteredBookList(getMTitle(),getAuthor(),parent);
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
                    "WHERE Title='"+diag.getMtitle()+"' AND Author='"+diag.getAuthor()+"'";
            parent.getContentPanel().updateUI();
            try(Connection conn = connect(); PreparedStatement ReadingPstmt = conn.prepareStatement(ReadingQry); PreparedStatement AvNumPstmt = conn.prepareStatement(AvNumQry)){
                Statement statement = conn.createStatement();
                ReadingPstmt.setInt(1, getIdReading(diag.getMtitle(), diag.getAuthor()));
                ReadingPstmt.setString(2, diag.getMtitle());
                ReadingPstmt.setString(3, diag.getAuthor());

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

                AvNumPstmt.setInt(1, averageTime(diag.getMtitle(), diag.getAuthor()));
                AvNumPstmt.setInt(2, getNumberOfReading(diag.getMtitle(), diag.getAuthor()));
                AvNumPstmt.executeUpdate();


                parent.setMTitle(diag.getMtitle());
                parent.setAuthor(diag.getAuthor());
                parent.loadComponents(diag.getMtitle(), diag.getAuthor());
                parent.loadDB(parent.isFiltered());
                //Focus in the jtable on a reading created from an existing book
                parent.getBooksTable().setRowSelectionInterval(parent.getRowSelected(diag.getMtitle(), diag.getAuthor()), parent.getRowSelected(diag.getMtitle(), diag.getAuthor()));
                if (parent.getCounterManageReading() > 0)
                    parent.getManageReadingDiag().fillBookList(getMTitle(), getAuthor());
                if(parent.isFastSearch()){
                    parent.fastSearchBook(parent.getBookFastSearch().getText());
                }
                conn.close();
                statement.close();
            }catch (SQLException e){
                System.out.println(e.getMessage());
                System.exit(0);
            }
        }
    }
    public static void filtersBook(FiltersDlg diag, String title , String author, MainWindow parent){
        if(diag.getIsValid()) {
            if(parent.isFiltered()){
                parent.loadDB(parent.isFiltered());
            }
            else{
                parent.loadDB(diag.getIsValid());
                parent.setIsFiltered(diag.getIsValid());
            }
            if (parent.getBooksTable().getRowCount() > 0) {
                parent.setMTitle(parent.getBooksTable().getValueAt(0, 0).toString());
                parent.setAuthor(parent.getBooksTable().getValueAt(0, 1).toString());
                parent.loadComponents(getMTitle(), getAuthor());
                parent.getBooksTable().setRowSelectionInterval(parent.getRowSelected(getMTitle(), getAuthor()), parent.getRowSelected(getMTitle(), getAuthor()));
                if (parent.getManageReadingDiag() != null) {
                    parent.getManageReadingDiag().fillBookList(getMTitle(), getAuthor());
                }
            } else
                parent.initComponents();
        }else{
            if (parent.getBooksTable().getRowCount() > 0) {
                parent.loadComponents(title, author);
                parent.getBooksTable().setRowSelectionInterval(parent.getRowSelected(title, author), parent.getRowSelected(title, author));
                if (parent.getManageReadingDiag() != null) {
                    parent.getManageReadingDiag().fillBookList(title, author);
                }
            } else
                parent.initComponents();
        }
        if(parent.isFastSearch()){
            parent.fastSearchBook(parent.getBookFastSearch().getText());
        }
        parent.getContentPanel().updateUI();
    }
}
