package Forms.Dialogs;

import javax.swing.*;

public class EditBookDlg extends JDialog {
    private JPanel contentPane;
    private JPanel LeftPanel;
    private JLabel NameLabel;
    private JTextField BookTitleTextField;
    private JLabel AuthorLabel;
    private JTextField BookAuthorTextField;
    private JLabel ReleaseYearLabel;
    private JLabel PersonalNoteLabel;
    private JSpinner BookPersonalNoteSpin;
    private JLabel PhotoLabel;
    private JButton BookBrowseBtn;
    private JLabel NoteBblLabel;
    private JSpinner BookNoteBblSpin;
    private JLabel SummaryLabel;
    private JTextPane BookSummaryTextPane;
    private JSpinner BookReleaseYearSpin;
    private JLabel NumberOPLabel;
    private JSpinner BookNumberOPSpin;
    private JPanel RightPanel;
    private JPanel BookBtnPanel;
    private JButton BookOkBtn;
    private JButton BookCancelBtn;
    private JPanel BookPhotoPanel;

    private String m_oldTitle;
    private String m_oldAuthor;
    private String m_newTitle;
    private String m_newAuthor;
    private String m_newReleaseyear;
    private String m_newNumberPage;
    private String m_newPersonnalNote;
    private String m_newBBLNote;
    private String m_newSummary;
    private String m_newURL;

    public EditBookDlg(String title, String author) {
        setContentPane(contentPane);
        setModal(true);
        setOldTitle(title);
        setOldAuthor(author);
        loadDB();
    }

    public String getOldTitle() {
        return m_oldTitle;
    }
    public String getOldAuthor() {
        return m_oldAuthor;
    }
    public String getNewTitle() {
        return m_newTitle;
    }
    public String getNewAuthor() {
        return m_newAuthor;
    }
    public String getNewReleaseyear() {
        return m_newReleaseyear;
    }
    public String getNewNumberPage() {
        return m_newNumberPage;
    }
    public String getNewPersonnalNote() {
        return m_newPersonnalNote;
    }
    public String getNewBBLNote() {
        return m_newBBLNote;
    }
    public String getNewSummary() {
        return m_newSummary;
    }
    public String getNewURL() {
        return m_newURL;
    }

    public void setOldTitle(String m_oldTitle) {
        this.m_oldTitle = m_oldTitle;
    }
    public void setOldAuthor(String m_oldAuthor) {
        this.m_oldAuthor = m_oldAuthor;
    }
    public void setNewTitle(String m_newTitle) {
        this.m_newTitle = m_newTitle;
    }
    public void setNewAuthor(String m_newAuthor) {
        this.m_newAuthor = m_newAuthor;
    }
    public void setNewReleaseyear(String m_newReleaseyear) {
        this.m_newReleaseyear = m_newReleaseyear;
    }
    public void setNewNumberPage(String m_newNumberPage) {
        this.m_newNumberPage = m_newNumberPage;
    }
    public void setNewPersonnalNote(String m_newPersonnalNote) {
        this.m_newPersonnalNote = m_newPersonnalNote;
    }
    public void setNewBBLNote(String m_newBBLNote) {
        this.m_newBBLNote = m_newBBLNote;
    }
    public void setNewSummary(String m_newSummary) {
        this.m_newSummary = m_newSummary;
    }
    public void setNewURL(String m_newURL) {
        this.m_newURL = m_newURL;
    }
    public void loadDB(){

    }
}
