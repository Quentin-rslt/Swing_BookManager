package Sources.Dialogs;

import Sources.MainWindow;
import Sources.Tag;

import static Sources.Common.*;
import static Sources.MainWindow.getMTitle;

public class OpenDialog {
    public static AddBookDlg openAddBookDlg(){
        AddBookDlg diag = new AddBookDlg();
        diag.setTitle("Ajout d'un livre");
        diag.setIconImage(getImageAdd());
        diag.setSize(1000,730);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static EditBookDlg openEditBookDlg(){
        EditBookDlg diag = new EditBookDlg();
        diag.setTitle("Modification du livre : " +getMTitle());
        diag.setIconImage(getImageEdit());
        diag.setSize(1000,730);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static AddReading openAddReadingDlg(){
        AddReading diag = new AddReading();
        diag.setTitle("Ajout d'une lecture pour le livre : "+getMTitle());
        diag.setSize(550,265);
        diag.setIconImage(getImageAdd());
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static EditReadingDlg openEditReadingDlg(String startDate, String endDate){
        EditReadingDlg diag = new EditReadingDlg(startDate,endDate);
        diag.setIconImage(getImageEdit());
        diag.setTitle("Modification d'une lecture");
        diag.setSize(500,220);
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
        diag.setIconImage(getImageTag());
        diag.setSize(550,230);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
    public static void openManageTagsDlg(String title, String author){
        ManageTagsDlg diag = new ManageTagsDlg(title,author);
        diag.setTitle("Gérer les tags du livre : " +title);
        diag.setIconImage(getImageTag());
        diag.setSize(550,230);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
    public static EditTagDlg openEditTagDlg(Tag tag){
        EditTagDlg diag = new EditTagDlg(tag);
        diag.setSize(780,490);
        diag.setIconImage(getImageEdit());
        diag.setTitle("Modification du tag : " +tag.getTextTag());
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
    public static void openParametersDlg(MainWindow parent){
        ParametersDlg diag = new ParametersDlg(parent);
        diag.setSize(650,505);
        diag.setIconImage(getImageEdit());
        diag.setTitle("Paramètres");
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
    public static void openAboutDlg(){
        AboutDlg diag = new AboutDlg();
        diag.setSize(480,280);
        diag.setTitle("A propos");
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
}
