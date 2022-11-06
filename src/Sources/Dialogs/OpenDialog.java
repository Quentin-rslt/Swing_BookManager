package Sources.Dialogs;

import Sources.Tag;

import static Sources.Common.getImageAdd;
import static Sources.Common.getImageEdit;

public class OpenDialog {
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
        diag.setTitle("Critères de recherche");
        diag.setSize(730,700);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static void openManageTagsDlg(){
        ManageTagsDlg diag = new ManageTagsDlg();
        diag.setTitle("Gérer les tags");
        diag.setSize(550,350);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
    public static void openManageTagsDlg(String title, String author){
        ManageTagsDlg diag = new ManageTagsDlg(title,author);
        diag.setTitle("Gérer les tags du livre " +title);
        diag.setSize(550,350);
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
