package Sources.BookManager;
import Sources.MainWindow;

import static Sources.BookManager.BookManager.*;
import static Sources.BookManager.MenuBarBookManager.*;
import static Sources.Common.isInFilteredList;

public class CommonBookManager {
    public static void isNotInFilteredBookList(MainWindow parent, boolean bookDelete){
        if(bookDelete) {
            if (parent.getBooksTable().getRowCount() > 0) {
                if (parent.getM_bookManager().getRowSelected() > 0) {
                    parent.getM_bookManager().setRowSelected(parent.getM_bookManager().getRowSelected() - 1);
                }
                parent.getM_bookManager().setRowReading(0);
                parent.getM_bookManager().setRowSelected(parent.getM_bookManager().getRowSelected());
                parent.getM_bookManager().setMTitle(parent.getBooksTable().getValueAt(parent.getM_bookManager().getRowSelected(), 0).toString());
                parent.getM_bookManager().setAuthor(parent.getBooksTable().getValueAt(parent.getM_bookManager().getRowSelected(), 1).toString());
                parent.getM_bookManager().loadComponents(getMTitle(), getAuthor());//reload changes made to the book
            } else {
                parent.getM_bookManager().initComponents();
            }
        }else {
            if (parent.getBooksTable().getRowCount() > 0) {
                parent.getM_bookManager().setMTitle(parent.getBooksTable().getValueAt(0, 0).toString());
                parent.getM_bookManager().setAuthor(parent.getBooksTable().getValueAt(0, 1).toString());
                parent.getM_bookManager().setRowSelected(0);
                parent.getM_bookManager().setRowReading(0);
                parent.getM_bookManager().loadComponents( getMTitle(), getAuthor());//reload changes made to the book
            } else {
                parent.getM_bookManager().initComponents();
            }
        }
    }
    public static void isItInFilteredBookList(MainWindow parent, boolean bookDelete){
        if(isInFilteredList(getMTitle(),getAuthor(), parent.getBooksTable())){
            parent.getM_bookManager().setRowSelected(parent.getM_bookManager().getRowSelectedByBook(getMTitle(), getAuthor()));
            parent.getM_bookManager().loadComponents(getMTitle(), getAuthor());//reload changes made to the book
        }
        else{
            isNotInFilteredBookList(parent, bookDelete);
        }
    }

    public static void resetBookManager(MainWindow parent, boolean reset){
        parent.getM_bookManager().getMainWindow().getJMenuBar().getMenu(0).getItem(0).setEnabled(reset);
        getSupprBookMenuItem().setEnabled(reset);
        getSupprReadingMenuItem().setEnabled(reset);
        getEditBookMenuItem().setEnabled(reset);
        getEditReadingMenuItem().setEnabled(reset);
        getAddReadingMenuItem().setEnabled(reset);
        getManageTagsMenuItem().setEnabled(reset);
        parent.getM_bookManager().getMainWindow().getJMenuBar().getMenu(1).getItem(6).setEnabled(reset);
        parent.getM_bookManager().getMainWindow().getJMenuBar().getMenu(1).getItem(7).setEnabled(false);
    }

}
