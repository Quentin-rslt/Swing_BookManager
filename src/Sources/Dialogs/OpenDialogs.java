package Sources.Dialogs;

import Sources.BookManager.BookManager;
import Sources.MainWindow;
import Sources.Tag;

import static Sources.Common.getLogo;

public class OpenDialogs {
    public static void openParametersDlg(MainWindow parent){
        ParametersDlg diag = new ParametersDlg(parent);
        diag.setSize(650,505);
        diag.setIconImage(getLogo("param.png"));
        diag.setTitle("Paramètres");
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
    public static void openAboutDlg(){
        AboutDlg diag = new AboutDlg();
        diag.setSize(480,280);
        diag.setIconImage(getLogo("logo_BookManager.png"));
        diag.setResizable(false);
        diag.setTitle("A propos");
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
    public static void openManageTagsDlg(){
        ManageTagsDlg diag = new ManageTagsDlg();
        diag.setTitle("Gérer les tags");
        diag.setIconImage(getLogo("tag.png"));
        diag.setSize(550,230);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
    public static void openManageTagsDlg(String title, String author){
        ManageTagsDlg diag = new ManageTagsDlg(title,author);
        diag.setTitle("Gérer les tags du livre : " +title);
        diag.setIconImage(getLogo("tag.png"));
        diag.setSize(550,230);
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);
    }
    public static EditTagDlg openEditTagDlg(Tag tag){
        EditTagDlg diag = new EditTagDlg(tag);
        diag.setSize(780,490);
        diag.setResizable(false);
        diag.setIconImage(getLogo("edit.png"));
        diag.setTitle("Modification du tag : " +tag.getTextTag());
        diag.setLocationRelativeTo(null);
        diag.setVisible(true);

        return diag;
    }
}
