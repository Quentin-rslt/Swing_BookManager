package Sources.Dialogs;

import Sources.MainWindow;
import Sources.Tag;

import static Sources.Common.getImageAdd;
import static Sources.Common.getImageEdit;

public class OpenDialog {
    public static ManageReadingDlg openManageReadingDlg(MainWindow parent, String title, String author){
        ManageReadingDlg diag = new ManageReadingDlg(parent, title, author);
        diag.setTitle("Gérer les lectures");
        diag.setSize(500,570);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static AddBookDlg openAddBookDlg(){
        AddBookDlg diag = new AddBookDlg();
        diag.setTitle("Ajouter un livre");
        diag.setIconImage(getImageAdd());
        diag.setSize(840,610);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static EditBookDlg openEditBookDlg(String title, String author){
        EditBookDlg diag = new EditBookDlg(title, author);
        diag.setTitle("Modifier un livre");
        diag.setIconImage(getImageEdit());
        diag.setSize(840,610);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static AddReading openAddReadingDlg(String title, String author){
        AddReading diag = new AddReading(title, author);
        diag.setTitle("Ajouter une lecture");
        diag.setSize(550,250);
        diag.setIconImage(getImageAdd());
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static FiltersDlg openFilterDlg(){
        FiltersDlg diag = new FiltersDlg();
        diag.setTitle("Filter la liste");
        diag.setSize(730,640);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static void openManageTagsDlg(){
        ManageTagsDlg diag = new ManageTagsDlg();
        diag.setTitle("Gérer les tags");
        diag.setSize(500,300);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
    public static EditTagDlg openEditTagDlg(Tag tag){
        EditTagDlg diag = new EditTagDlg(tag);
        diag.setSize(780,490);
        diag.setTitle("Modifier le tag");
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
}
