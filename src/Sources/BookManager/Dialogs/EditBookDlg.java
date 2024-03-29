package Sources.BookManager.Dialogs;

import Sources.Components.MyManagerComboBox;
import Sources.Dialogs.EditTagDlg;
import Sources.Components.MyManagerRoundBorderComponents;
import Sources.Components.Tag;
import Sources.Components.Tags;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static Sources.BookManager.CommonBookManagerSQL.*;
import static Sources.Dialogs.OpenDialogs.*;
import static Sources.Common.*;
import static Sources.CommonSQL.*;

public class EditBookDlg extends JDialog {
    private JPanel contentPane;
    private JTextField BookTitleTextField;
    private JSpinner BookPersonalNoteSpin;
    private JButton BookBrowseBtn;
    private JSpinner BookNoteBblSpin;
    private JTextPane BookSummaryTextPane;
    private JSpinner BookReleaseYearSpin;
    private JSpinner BookNumberOPSpin;
    private JButton BookOkBtn;
    private JPanel BookPhotoPanel;
    private JScrollPane JsPane;
    private final MyManagerComboBox BookTagsCB = new MyManagerComboBox(true);
    private JPanel BookTagsPanel;
    private final MyManagerComboBox BookAuthorCB = new MyManagerComboBox(true);
    private JButton ResetBtn;
    private JPanel BookTagsCbPanel;
    private JPanel BookAuthorCbPanel;
    private boolean m_isUpdate;
    private String m_oldTitle;
    private String m_oldAuthor;
    private boolean isValid = false;
    private JPopupMenu m_popup;
    private Tags m_tags = new Tags();
    private JMenuItem m_cut;
    private JMenuItem m_edit;

    public EditBookDlg(String title, String author) {
        setContentPane(contentPane);
        setModal(true);

        setOldTitle(title);
        setOldAuthor(author);

        initComponents();
        initPopupMenu();
        initListener();
    }

    public String getOldTitle() {
        return m_oldTitle;
    }
    public String getOldAuthor() {
        return m_oldAuthor;
    }
    public String getNewTitle() {
        return BookTitleTextField.getText();
    }
    public String getNewAuthor() {
        return Objects.requireNonNull(BookAuthorCB.getSelectedItem()).toString();
    }
    public String getNewReleaseyear() {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy");//set the date format returned to have just the year release
        return formater.format(BookReleaseYearSpin.getValue());
    }
    public String getNewNumberPage() {
        return BookNumberOPSpin.getValue().toString();
    }
    public String getNewPersonnalNote() {
        return BookPersonalNoteSpin.getValue().toString();
    }
    public String getNewBBLNote() {
        return BookNoteBblSpin.getValue().toString();
    }
    public String getNewSummary() {
        return BookSummaryTextPane.getText();
    }
    public boolean isValid() {
        return isValid;
    }
    public Tags getTags(){
        return this.m_tags;
    }
    public boolean getTagIsUpdate() {
        return m_isUpdate;
    }

    public void setTags(Tags tags){
        this.m_tags = tags;
    }
    public void setTagIsUpdate(boolean m_isUpdate) {
        this.m_isUpdate = m_isUpdate;
    }
    public void setOldTitle(String oldTitle) {
        this.m_oldTitle = oldTitle;
    }
    public void setOldAuthor(String oldAuthor) {
        this.m_oldAuthor = oldAuthor;
    }
    public void setIsValid(boolean valid) {
        isValid = valid;
    }
    public void loadDB(String title, String author){
        loadBook(title, author);
        setTags(loadTags(title, author, BookTagsPanel));
        fillTagsCB(BookTagsCB);
    }
    public void loadBook(String title, String author){
        fillAuthorCB(BookAuthorCB);
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet bookQry = statement.executeQuery("SELECT * FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");

            //Title
            BookTitleTextField.setText(title);

            //Author
            BookAuthorCB.setSelectedItem(author);

            //Release year
            Date dateRelease = new Date();
            SpinnerDateModel BookReleaseDateSpinModel = new SpinnerDateModel(new SimpleDateFormat("yyyy").parse(bookQry.getString(8)),null,dateRelease,Calendar.YEAR);//Create a spinner date, to correctly select a date
            BookReleaseYearSpin.setModel(BookReleaseDateSpinModel);
            JSpinner.DateEditor Year = new JSpinner.DateEditor(BookReleaseYearSpin,"yyyy");//set the display of the JSpinner of release date
            BookReleaseYearSpin.setEditor(Year);

            //Number of page
            SpinnerModel BookNumberSM = new SpinnerNumberModel(bookQry.getInt(5), 0, 3248, 1);
            BookNumberOPSpin.setModel(BookNumberSM);

            //Note on babelio
            SpinnerModel BookNoteBbblSM = new SpinnerNumberModel(bookQry.getDouble(7), 0, 5, 0.01);
            BookNoteBblSpin.setModel(BookNoteBbblSM);

            //Personal note
            SpinnerModel BookPersonalNotelSM = new SpinnerNumberModel(bookQry.getDouble(6), 0, 5, 0.5);//Set a default and max value for spinner Note
            BookPersonalNoteSpin.setModel(BookPersonalNotelSM);

            //Summary
            BookSummaryTextPane.setText(bookQry.getString(11));

            //Image
            addImageToPanel(bookQry.getString(4),BookPhotoPanel);
            setNameOfImage(bookQry.getString(4));

            conn.close();
            statement.close();
        } catch (Exception e ) {
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement du livre impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public void initComponents(){
        //AddMyManagerComboBox to panel
        BookAuthorCbPanel.add(BookAuthorCB);
        BookTagsCbPanel.add(BookTagsCB);
        //BookSummary
        AbstractBorder roundBrd = new MyManagerRoundBorderComponents(contentPane.getBackground(),2,25,0,0,20);
        BookSummaryTextPane.setBorder(roundBrd);
        JsPane.setBorder(null);
        //Load Components
        loadDB(getOldTitle(), getOldAuthor());
    }
    public void initPopupMenu(){
        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        m_cut = new JMenuItem("Supprimer", new ImageIcon(getLogo("remove.png")));
        m_edit = new JMenuItem("Modifier", new ImageIcon(getLogo("edit.png")));
        m_popup.add(m_cut);
        m_popup.add(m_edit);

    }
    public void initListener(){
        BookOkBtn.addActionListener((ActionEvent e) ->{
            if (!Objects.equals(getNewTitle(), "") && !Objects.equals(getNewAuthor(), "") && !Objects.equals(getNewSummary(), "")){
                deleteImageResource(getOldTitle(), getOldAuthor());
                setIsValid(true);
                updateImageToResource(getOldTitle(), getOldAuthor());
                setVisible(false);
                dispose();
            }
            else{
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !", "Livre saisie invalide", JOptionPane.ERROR_MESSAGE);
            }
        });
        ResetBtn.addActionListener(e -> {
            setTags(new Tags());
            BookTagsCB.setSelectedIndex(0);
            BookTagsPanel.removeAll();
            loadDB(getOldTitle(),getOldAuthor());
            initListenerTag(getTags(), m_popup, BookTagsPanel);
        });
        BookBrowseBtn.addActionListener((ActionEvent e)-> selectImage(BookPhotoPanel));
        BookSummaryTextPane.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                MyManagerRoundBorderComponents roundBrd = new MyManagerRoundBorderComponents(new Color(197,62,62),2,25,0,0,20);
                BookSummaryTextPane.setBorder(roundBrd);
            }
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                MyManagerRoundBorderComponents roundBrd = new MyManagerRoundBorderComponents(contentPane.getBackground(),2,25,0,0,20);
                BookSummaryTextPane.setBorder(roundBrd);
            }
        });
        initListenerTag(getTags(), m_popup, BookTagsPanel);
        BookTagsCB.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (!Objects.equals(BookTagsCB.getEditor().getItem().toString(), "")) {
                    if (evt.getKeyCode() != KeyEvent.VK_DELETE && evt.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
                        if (evt.getKeyCode()== KeyEvent.VK_ENTER){
                            fillPaneTags(getTags(), BookTagsPanel, BookTagsCB, true);
                        }
                        else{
                            BookTagsCB.searchItemCB();
                        }
                    }
                }
                else{
                    BookTagsCB.setSelectedIndex(0);
                }
                initListenerTag(getTags(), m_popup, BookTagsPanel);
                BookTagsPanel.updateUI();
            }
        });
        BookAuthorCB.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (!Objects.equals(BookAuthorCB.getEditor().getItem().toString(), "")) {
                    if (evt.getKeyCode() != KeyEvent.VK_DELETE && evt.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
                        BookAuthorCB.searchItemCB();
                    }
                }
                else{
                    BookAuthorCB.setSelectedIndex(0);
                }
            }
        });
        BookTagsCB.getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                BookTagsCB.showPopup();
            }
        });
        BookAuthorCB.getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                BookAuthorCB.showPopup();
            }
        });

        this.m_cut.addActionListener((ActionEvent evt) ->{
            Component[] componentList = BookTagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    BookTagsPanel.remove(componentList[i]);
                    getTags().removeTag(i);
                    break;
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, BookTagsPanel);
            BookTagsPanel.updateUI();
        });
        this.m_edit.addActionListener((ActionEvent evt)-> {
            Component[] componentList = BookTagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    EditTagDlg diag = openEditTagDlg(getTags().getTag(i));

                    if(diag.isValide()){
                        setTagIsUpdate(diag.isUpdate());
                        Tag tag = new Tag(diag.getNewTextTag());
                        tag.setColor(diag.getNewColorTag().getRGB());

                        getTags().addTag(tag);
                        BookTagsPanel.remove(componentList[i]);
                        getTags().getTags().remove(i);

                        for(int j=0; j<getTags().getSizeTags();j++){
                            BookTagsPanel.add(getTags().getTag(j));
                        }
                        BookTagsPanel.updateUI();
                    }
                    break;
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, BookTagsPanel);
            BookTagsPanel.updateUI();
        });
    }
}
