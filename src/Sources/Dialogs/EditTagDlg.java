package Sources.Dialogs;

import javax.swing.*;

public class EditTagDlg extends JDialog {
    private JPanel contentPane;
    private JPanel TagBtnPanel;
    private JButton TagOkBtn;
    private JButton TagCancelBtn;
    private JTextField TagNameTextField;

    public EditTagDlg() {
        setContentPane(contentPane);
        setModal(true);
    }
}
