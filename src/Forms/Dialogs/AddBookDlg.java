package Forms.Dialogs;

import javax.swing.*;

public class AddBookDlg extends JDialog {
    private JPanel contentPane;
    private JButton ValidateBtn;
    private JButton CancelBtn;
    private JPanel BtnPanel;
    private JPanel PreviewPhotoPanel;
    private JLabel BookAlreadyReadLabel;
    private JComboBox ExitingBookComboBox;
    private JLabel NewBookLabel;
    private JLabel NameLabel;
    private JTextField BookNameTextField;
    private JTextField BookAuthorTextField;
    private JLabel AuthorLabel;
    private JLabel ReleaseYearLabel;
    private JSpinner BookReleaseYearSpin;
    private JLabel PersonalNoteLabel;
    private JSpinner BookPersonalNoteSpin;
    private JLabel ReadingDateLabel;
    private JSpinner spinner1;
    private JCheckBox UnknownReadDateChecbox;
    private JPanel DateReadPanel;
    private JPanel LeftPanel;
    private JLabel PhotoLabel;
    private JButton BrowseBtn;
    private JLabel NoteBblLabel;
    private JSpinner BookNoteBblSpin;
    private JLabel SummaryLabel;
    private JTextPane textPane1;

    public AddBookDlg() {
        setContentPane(contentPane);
        setModal(true);
    }

    public static void main(String[] args) {
        AddBookDlg dialog = new AddBookDlg();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
