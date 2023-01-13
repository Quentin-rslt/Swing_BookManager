package Sources.BookManager;
import static Sources.BookManager.MenuBarBookManager.*;
import static Sources.Common.isInFilteredList;

public class CommonBookManager {
    public static void isNotInFilteredBookList(BookManager bookManager, boolean bookDelete){
        if(bookDelete) {
            if (bookManager.getBooksTable().getRowCount() > 0) {
                if (bookManager.getRowSelected() > 0) {
                    bookManager.setRowSelected(bookManager.getRowSelected() - 1);
                }
                bookManager.setRowReading(0);
                bookManager.setRowSelected(bookManager.getRowSelected());
                bookManager.setMTitle(bookManager.getBooksTable().getValueAt(bookManager.getRowSelected(), 0).toString());
                bookManager.setAuthor(bookManager.getBooksTable().getValueAt(bookManager.getRowSelected(), 1).toString());
                bookManager.loadComponents(bookManager.getMTitle(), bookManager.getAuthor());//reload changes made to the book
            } else {
                bookManager.initComponents();
            }
        }else {
            if (bookManager.getBooksTable().getRowCount() > 0) {
                bookManager.setMTitle(bookManager.getBooksTable().getValueAt(0, 0).toString());
                bookManager.setAuthor(bookManager.getBooksTable().getValueAt(0, 1).toString());
                bookManager.setRowSelected(0);
                bookManager.setRowReading(0);
                bookManager.loadComponents(bookManager.getMTitle(), bookManager.getAuthor());//reload changes made to the book
            } else {
                bookManager.initComponents();
            }
        }
    }
    public static void isItInFilteredBookList(BookManager bookManager, boolean bookDelete){
        if(isInFilteredList(bookManager.getMTitle(),bookManager.getAuthor(), bookManager.getBooksTable())){
            bookManager.setRowSelected(bookManager.getRowSelectedByBook(bookManager.getMTitle(), bookManager.getAuthor()));
            bookManager.loadComponents(bookManager.getMTitle(), bookManager.getAuthor());//reload changes made to the book
        }
        else{
            isNotInFilteredBookList(bookManager, bookDelete);
        }
    }

    public static void resetBookManager(BookManager bookManager, boolean reset){
        bookManager.getMainWindow().getJMenuBar().getMenu(0).getItem(0).setEnabled(reset);
        getSupprBookMenuItem().setEnabled(reset);
        getSupprReadingMenuItem().setEnabled(reset);
        getEditBookMenuItem().setEnabled(reset);
        getEditReadingMenuItem().setEnabled(reset);
        getAddReadingMenuItem().setEnabled(reset);
        getManageTagsMenuItem().setEnabled(reset);
        bookManager.getMainWindow().getJMenuBar().getMenu(1).getItem(6).setEnabled(reset);
        bookManager.getMainWindow().getJMenuBar().getMenu(1).getItem(7).setEnabled(false);
    }

}
