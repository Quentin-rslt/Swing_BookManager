package Sources.Dialogs;

import Sources.RoundBorderCp;
import Sources.Tag;
import Sources.Tags;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static Sources.Common.*;
import static Sources.Common.getImageEdit;
import static Sources.Dialogs.OpenDialog.openEditTagDlg;

public class AddBookDlg extends JDialog {
    private JPanel contentPane;
    private JButton ValidateBtn;
    private JButton CancelBtn;
    private JPanel PreviewPhotoPanel;
    private JTextField BookNameTextField;
    private JTextField BookAuthorTextField;
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
    private JComboBox BookTagsCB;
    private JPanel BookTagsPanel;
    private JScrollPane JsPane;
    private boolean m_isValide = false;//Useful for determinate if the input are good
    private boolean m_tagIsUpdate = false;
    private Connection m_connection;
    private Statement m_statement;
    Tags m_tags;
    final JPopupMenu m_popup;


    public AddBookDlg() {
        this.m_tags = new Tags();

        setContentPane(contentPane);
        setModal(true);
        initComponents();
        initComponents(true);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
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
        CancelBtn.addActionListener((ActionEvent e) -> {//Quit dlg without taking into account the input
            m_isValide = false;
            setVisible(false);
            dispose();
        });
        ValidateBtn.addActionListener((ActionEvent evt) -> {
            String sql = "SELECT Title, Author, StartReading, EndReading FROM Reading";
            if(!Objects.equals(getNewBookAuthor(), "") && !Objects.equals(getNewBookTitle(), "") && !Objects.equals(getNewBookSummary(), "")){//Verif if the input are good to quit the dlg and recovered the data for bdd
                try{//Can add a new reading if the book already exist
                    Class.forName("org.sqlite.JDBC");
                    m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                    m_statement = m_connection.createStatement();
                    ResultSet bookQry = m_statement.executeQuery(sql);
                    Date enDate =new SimpleDateFormat("yyyy-MM-dd").parse(getNewBookEndReading());
                    Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(getNewBookStartReading());

                    boolean bookFind =false;
                    while (bookQry.next() && !bookFind){//We browse the database until we find a book that already exists, in relation to the book created
                        if (Objects.equals(bookQry.getString(1), getNewBookTitle()) && Objects.equals(bookQry.getString(2), getNewBookAuthor())){//If the created book is already in the database, we exit the loop by setting an error dialog
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "Le livre existe déjà !");
                            bookFind = true;//If you have found a book, you are out of the loop
                        }
                    }
                    if(getNameOfBook().equals("")){
                        setNameOfBook("Default.jpg");
                    }
                    if (!bookFind && isDateUnknown()&& !isNotDOne() && !Objects.equals(getNewBookAuthor(), getNewBookTitle())){
                        addImageToResource();
                        m_isValide=true;
                        setVisible(false);
                        dispose();
                    } else if (!bookFind && !isDateUnknown() && isNotDOne() && !Objects.equals(getNewBookAuthor(), getNewBookTitle())) {
                        addImageToResource();
                        m_isValide=true;
                        setVisible(false);
                        dispose();
                    } else if (!bookFind && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne()
                            && startDate.compareTo(enDate)<0 && !Objects.equals(getNewBookAuthor(), getNewBookTitle())) {
                        addImageToResource();
                        m_isValide=true;
                        setVisible(false);
                        dispose();
                    } else if(Objects.equals(getNewBookAuthor(), getNewBookTitle()) && !Objects.equals(getNewBookAuthor(), "")){
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "Le nom de l'auteur et le titre d'un livre ne peut pas être identique ! ");
                    } else if (!Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne() && startDate.compareTo(enDate)>0) {
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut pas être après à la fin de lecture !");
                    } else if(!bookFind && Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne()){
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut être identique à la fin de lecture !");
                    }
                    m_connection.close();
                    m_statement.close();
                } catch ( Exception e ) {
                    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                    System.exit(0);
                }
            }
            else{
                JFrame jFrame = new JFrame();
                JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !");
            }
        });
        BookBrowseBtn.addActionListener((ActionEvent e)->selectNameOfBook(PreviewPhotoPanel));
        BookTagsCB.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (!Objects.equals(BookTagsCB.getSelectedItem(), "")) {
                    if (evt.getKeyCode()==KeyEvent.VK_ENTER){
                        fillPaneTags(getTags(), BookTagsPanel, BookTagsCB);
                    }
                }
                initListenerTag(getTags(), m_popup, BookTagsPanel);
                BookTagsPanel.updateUI();
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
                RoundBorderCp roundBrd = new RoundBorderCp(JsPane.getBackground(),2,25,18,0,20);
                BookSummaryTextPane.setBorder(roundBrd);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                RoundBorderCp roundBrd = new RoundBorderCp(contentPane.getBackground(),2,25,18,0,20);
                BookSummaryTextPane.setBorder(roundBrd);
            }
        });
    }

    public String getNewBookTitle(){//Get the new book title from JtextField
        return BookNameTextField.getText();
    }
    public String getNewBookAuthor(){
        return BookAuthorTextField.getText();
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
    public boolean isDateUnknown(){
        return BookUnknownReadDateChecbox.isSelected();
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

        fillThemeCB();
    }
    public void initComponents(boolean bool){
        BookNameTextField.setEnabled(bool);
        BookAuthorTextField.setEnabled(bool);
        BookReleaseYearSpin.setEnabled(bool);
        BookNumberOPSpin.setEnabled(bool);
        BookPersonalNoteSpin.setEnabled(bool);
        BookNoteBblSpin.setEnabled(bool);
        BookSummaryTextPane.setEnabled(bool);
        BookReleaseYearSpin.setEnabled(bool);
        BookBrowseBtn.setEnabled(bool);
        PreviewPhotoPanel.updateUI();
        PreviewPhotoPanel.removeAll();
        AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),3,25,18,0,20);
        BookSummaryTextPane.setBorder(roundBrd);
        JsPane.setBorder(null);
    }
    @SuppressWarnings("unchecked")
    public void fillThemeCB(){
        this.BookTagsCB.addItem("");
        for (int i = 0; i<loadTags().getSizeTags(); i++){
            this.BookTagsCB.addItem(loadTags().getTag(i).getTextTag());
        }
    }
    public void fastSearchCB(String text){
        fillThemeCB();
        for(int row = 0 ; row < this.BookTagsCB.getItemCount(); row++) {
            boolean notSeam =false;
            String cellText = this.BookTagsCB.getItemAt(row).toString();
            if(!cellText.equals("")) {
                for (int filterIndex = 0; filterIndex < text.length(); filterIndex++) {
                    if (filterIndex < cellText.length()) {
                        if (cellText.charAt(filterIndex) != text.charAt(filterIndex)) {
                            notSeam=true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if(notSeam){
                    this.BookTagsCB.removeItemAt(row);
                    row--;
                }
            }
        }
        this.BookTagsCB.setSelectedIndex(0);
    }
}
