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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static Sources.BookManager.CommonBookManagerSQL.*;
import static Sources.Dialogs.OpenDialogs.*;
import static Sources.Common.*;
import static Sources.CommonSQL.*;

public class AddBookDlg extends JDialog {
    private JPanel contentPane;
    private JButton ValidateBtn;
    private JButton ResetBtn;
    private JPanel PreviewPhotoPanel;
    private JTextField BookNameTextField;
    private JSpinner BookPersonalNoteSpin;
    private JCheckBox BookUnknownReadDateChecbox;
    private JButton BookBrowseBtn;
    private JSpinner BookNoteBblSpin;
    private JTextPane BookSummaryTextPane;
    private JSpinner BookReleaseYearSpin;
    private JSpinner BookEndReadingSpin;
    private JSpinner BookNumberOPSpin;
    private JCheckBox BookNotDoneReadChecbox;
    private JSpinner BookStartReadingSpin;
    private final MyManagerComboBox BookTagsCB = new MyManagerComboBox(true);
    private JPanel BookTagsPanel;
    private JScrollPane JsPane;
    private final MyManagerComboBox BookAuthorCB = new MyManagerComboBox(true);
    private JPanel BooksCbTagsPanel;
    private JPanel BookAuthorCbPanel;
    private boolean m_isValide = false;//Useful for determinate if the input are good
    private boolean m_tagIsUpdate = false;
    private Statement m_statement;
    Tags m_tags;
    final JPopupMenu m_popup;


    public AddBookDlg() {
        this.m_tags = new Tags();
        setContentPane(contentPane);
        setModal(true);
        initComponents();

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getLogo("remove.png")));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getLogo("edit.png")));
        m_popup.add(cut);
        m_popup.add(edit);

        BookUnknownReadDateChecbox.addActionListener((ActionEvent e) -> {
            if (BookUnknownReadDateChecbox.isSelected()){
                BookNotDoneReadChecbox.setSelected(false);
                BookEndReadingSpin.setEnabled(false);
                BookStartReadingSpin.setEnabled(false);
            }
            else{
                BookEndReadingSpin.setEnabled(true);
                BookStartReadingSpin.setEnabled(true);
            }
        });
        BookNotDoneReadChecbox.addActionListener((ActionEvent e) -> {
            if (BookNotDoneReadChecbox.isSelected()){
                BookUnknownReadDateChecbox.setSelected(false);
                BookStartReadingSpin.setEnabled(true);
                BookEndReadingSpin.setEnabled(false);
            }
            else{
                BookEndReadingSpin.setEnabled(true);
            }
        });
        ResetBtn.addActionListener((ActionEvent e) -> {//Quit dlg without taking into account the input
            setTags(new Tags());
            BookTagsPanel.removeAll();
            resetComponents();
            contentPane.updateUI();
        });
        ValidateBtn.addActionListener((ActionEvent evt) -> {
            String sql = "SELECT Title, Author, StartReading, EndReading FROM Reading";
            if(!Objects.equals(getNewBookAuthor(), "") && !Objects.equals(getNewBookTitle(), "") && !Objects.equals(getNewBookSummary(), "")){//Verif if the input are good to quit the dlg and recovered the data for bdd
                try (Connection conn = connect()){//Can add a new reading if the book already exist
                    m_statement = conn.createStatement();
                    ResultSet bookQry = m_statement.executeQuery(sql);
                    Date enDate =new SimpleDateFormat("yyyy-MM-dd").parse(getNewBookEndReading());
                    Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(getNewBookStartReading());

                    boolean bookFind =false;
                    while (bookQry.next() && !bookFind){//We browse the database until we find a book that already exists, in relation to the book created
                        if (Objects.equals(bookQry.getString(1), getNewBookTitle()) && Objects.equals(bookQry.getString(2), getNewBookAuthor())){//If the created book is already in the database, we exit the loop by setting an error dialog
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "Le livre existe déjà !", "Livre saisie invalide", JOptionPane.ERROR_MESSAGE);
                            bookFind = true;//If you have found a book, you are out of the loop
                        }
                    }
                    if(getNameOfImage().equals("")){
                        setNameOfImage("Default.jpg");
                    }
                    if(!bookFind && Objects.equals(getNewBookAuthor(), getNewBookTitle()) && !Objects.equals(getNewBookAuthor(), "")){
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "Le nom de l'auteur et le titre d'un livre ne peut pas être identique ! ", "Livre saisie invalide", JOptionPane.ERROR_MESSAGE);
                    } else if (!bookFind && isDateKnown() && !isNotDOne() && startDate.compareTo(enDate)>0) {
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut pas être après à la fin de lecture !", "Livre saisie invalide", JOptionPane.ERROR_MESSAGE);
                    } else if (!bookFind ) {
                        addImageToResource();
                        m_isValide=true;
                        setVisible(false);
                        dispose();
                    }
                    conn.close();
                    m_statement.close();
                } catch (SQLException | ParseException e ) {
                    JFrame jf = new JFrame();
                    JOptionPane.showMessageDialog(jf, e.getMessage(), "Validation livre impossible", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(e.getMessage());
                }
            }
            else{
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !", "Livre saisie invalide", JOptionPane.ERROR_MESSAGE);
            }
        });
        BookBrowseBtn.addActionListener((ActionEvent e)-> selectImage(PreviewPhotoPanel));
        BookTagsCB.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
            if (!Objects.equals(BookTagsCB.getEditor().getItem().toString(), "")) {
                if (evt.getKeyCode() != KeyEvent.VK_DELETE && evt.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
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
        BookTagsPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if(!e.getComponent().getComponentAt(e.getX(),e.getY()).equals(BookTagsPanel)){
                if(e.getButton() == MouseEvent.BUTTON3) {
                    m_popup.show(BookTagsPanel, e.getX(), e.getY());//show a popup to edit the reading
                    m_popup.setInvoker(e.getComponent().getComponentAt(e.getX(),e.getY()));
                }
                initListenerTag(getTags(), m_popup, BookTagsPanel);
            }
            }
        });

        cut.addActionListener((ActionEvent evt)-> {
            Component[] componentList = BookTagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    BookTagsPanel.remove(componentList[i]);
                    getTags().removeTag(i);

                    for(int j=0; j<getTags().getSizeTags();j++){
                        BookTagsPanel.add(getTags().getTag(j));
                    }
                    break;
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, BookTagsPanel);
            BookTagsPanel.updateUI();
        });
        edit.addActionListener((ActionEvent evt)-> {
            int i = 0;
            while (i<getTags().getSizeTags()) {
                Component[] componentList = BookTagsPanel.getComponents();
                if(componentList[i]==m_popup.getInvoker()){
                    EditTagDlg diag = openEditTagDlg(getTags().getTag(i));

                    if(diag.isValide()){
                        setTagIsUpdate(diag.isUpdate());
                        Tag tag = new Tag(diag.getNewTextTag());
                        tag.setColor(diag.getNewColorTag().getRGB());

                        BookTagsPanel.remove(componentList[i]);
                        getTags().getTags().remove(i);
                        getTags().addTag(tag);

                        for(int j=0; j<getTags().getSizeTags();j++){
                            BookTagsPanel.add(getTags().getTag(j));
                        }
                        BookTagsPanel.updateUI();
                        break;
                    }
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, BookTagsPanel);
        });
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
    }

    public String getNewBookTitle(){//Get the new book title from JtextField
        return BookNameTextField.getText();
    }
    public String getNewBookAuthor(){
        return Objects.requireNonNull(BookAuthorCB.getSelectedItem()).toString();
    }
    public String getNewBookNumberOP(){
        return BookNumberOPSpin.getValue().toString();
    }
    public String getNewBookReleaseYear(){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy");//set the date format returned to have just the year release
        return formater.format(BookReleaseYearSpin.getValue());
    }
    public String getNewBookPersonalNote(){
        return BookPersonalNoteSpin.getValue().toString();
    }
    public String getNewBookBBLNote(){
        return BookNoteBblSpin.getValue().toString();
    }
    public String getNewBookSummary(){
        return BookSummaryTextPane.getText();
    }
    public String getNewBookEndReading(){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
        return formater.format(BookEndReadingSpin.getValue());
    }
    public String getNewBookStartReading(){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
        return formater.format(BookStartReadingSpin.getValue());
    }
    public boolean isDateKnown(){
        return !BookUnknownReadDateChecbox.isSelected();
    }
    public boolean isNotDOne(){
        return BookNotDoneReadChecbox.isSelected();
    }
    public boolean isValide(){
        return m_isValide;
    }
    public Tags getTags(){
        return this.m_tags;
    }
    public boolean getTagIsUpdate() {
        return this.m_tagIsUpdate;
    }

    public void setTags(Tags tags){
        this.m_tags = tags;
    }
    public void setTagIsUpdate(boolean update){
        this.m_tagIsUpdate = update;
    }
    public void initComponents(){
        SpinnerModel BookPersonalNotelSM = new SpinnerNumberModel(5, 0, 5, 0.5);//Set a default and max value for spinner Note
        SpinnerModel BookNoteBbblSM = new SpinnerNumberModel(5, 0, 5, 0.01);
        BookPersonalNoteSpin.setModel(BookPersonalNotelSM);
        BookNoteBblSpin.setModel(BookNoteBbblSM);

        Date dateRelease = new Date();
        SpinnerDateModel BookReleaseDateSpinModel = new SpinnerDateModel(dateRelease,null,dateRelease,Calendar.YEAR);//Create a spinner date, to correctly select a date
        BookReleaseYearSpin.setModel(BookReleaseDateSpinModel);
        JSpinner.DateEditor Year = new JSpinner.DateEditor(BookReleaseYearSpin,"yyyy");//set the display of the JSpinner of release date
        BookReleaseYearSpin.setEditor(Year);

        Date dateEnd = new Date();
        SpinnerDateModel BookEndReadSpinDate = new SpinnerDateModel(dateEnd, null,dateEnd, Calendar.YEAR);//Create a spinner date, to correctly select a date
        BookEndReadingSpin.setModel(BookEndReadSpinDate);
        JSpinner.DateEditor ded = new JSpinner.DateEditor(BookEndReadingSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
        BookEndReadingSpin.setEditor(ded);

        Date dateStart = new Date();
        SpinnerDateModel BookStartReadSpinDate = new SpinnerDateModel(dateStart, null,dateStart, Calendar.YEAR);
        BookStartReadingSpin.setModel(BookStartReadSpinDate);
        JSpinner.DateEditor start = new JSpinner.DateEditor(BookStartReadingSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
        BookStartReadingSpin.setEditor(start);

        SpinnerModel BookNumberOPSM = new SpinnerNumberModel(0, 0, 9999999, 1);
        BookNumberOPSpin.setModel(BookNumberOPSM);

        AbstractBorder roundBrd = new MyManagerRoundBorderComponents(contentPane.getBackground(),3,25,0,0,20);
        BookSummaryTextPane.setBorder(roundBrd);
        JsPane.setBorder(null);

        BooksCbTagsPanel.add(BookTagsCB);
        BookAuthorCbPanel.add(BookAuthorCB);

        fillTagsCB(BookTagsCB);
        fillAuthorCB(BookAuthorCB);
    }
    public void resetComponents(){
        BookNameTextField.setText("");
        BookAuthorCB.setSelectedIndex(0);
        BookTagsCB.setSelectedIndex(0);
        BookUnknownReadDateChecbox.setSelected(false);
        BookNotDoneReadChecbox.setSelected(false);
        BookSummaryTextPane.setText("");
        BookEndReadingSpin.setEnabled(true);
        BookStartReadingSpin.setEnabled(true);
        PreviewPhotoPanel.removeAll();
        initComponents();
    }
}
