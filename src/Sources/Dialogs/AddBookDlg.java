package Sources.Dialogs;

import Sources.Tag;
import Sources.Tags;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.plaf.basic.BasicTreeUI;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static java.awt.Event.ENTER;

public class AddBookDlg extends JDialog {
    private JPanel contentPane;
    private JButton ValidateBtn;
    private JButton CancelBtn;
    private JPanel BtnPanel;
    private JPanel PreviewPhotoPanel;
    private JLabel NewBookLabel;
    private JLabel NameLabel;
    private JTextField BookNameTextField;
    private JTextField BookAuthorTextField;
    private JLabel AuthorLabel;
    private JLabel ReleaseYearLabel;
    private JLabel PersonalNoteLabel;
    private JSpinner BookPersonalNoteSpin;
    private JLabel StartReadingLabel;
    private JCheckBox BookUnknownReadDateChecbox;
    private JPanel DateReadPanel;
    private JPanel RightPanel;
    private JLabel PhotoLabel;
    private JButton BookBrowseBtn;
    private JLabel NoteBblLabel;
    private JSpinner BookNoteBblSpin;
    private JLabel SummaryLabel;
    private JTextPane BookSummaryTextPane;
    private JSpinner BookReleaseYearSpin;
    private JSpinner BookEndReadingSpin;
    private JSpinner BookNumberOPSpin;
    private JLabel NumberOPLabel;
    private JPanel LeftPanel;
    private JCheckBox BookNotDoneReadChecbox;
    private JLabel EndReadingLabel;
    private JSpinner BookStartReadingSpin;
    private JComboBox BookTagsCB;
    private JLabel BookTagsLabel;
    private JPanel BookTagsPanel;
    private String m_URL="";
    private boolean m_isValide = false;//Useful for determinate if the input are good
    private Connection m_connection;
    private Statement m_statement;
    private Tags m_tags;


    public AddBookDlg() {
        this.m_tags = new Tags();

        setContentPane(contentPane);
        setModal(true);
        initComponents();
        initComponents(true);

        BookUnknownReadDateChecbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(BookUnknownReadDateChecbox.isSelected()){//Hide the possibility to add a reading date when we don't know when you read it
                    BookEndReadingSpin.setEnabled(false);
                    BookStartReadingSpin.setEnabled(false);
                    BookNotDoneReadChecbox.setEnabled(false);
                }
                else{
                    BookEndReadingSpin.setEnabled(true);
                    BookStartReadingSpin.setEnabled(true);
                    BookNotDoneReadChecbox.setEnabled(true);
                }
            }
        });
        BookNotDoneReadChecbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (BookNotDoneReadChecbox.isSelected())
                    BookEndReadingSpin.setEnabled(false);
                else
                    BookEndReadingSpin.setEnabled(true);
            }
        });
        CancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {//Quit dlg without taking into account the input
                m_isValide = false;
                setVisible(false);
                dispose();
            }
        });
        ValidateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String sql = "SELECT Title, Author, StartReading, EndReading FROM Reading";
                if(!getTags().isEmpty() && !Objects.equals(getNewBookAuthor(), "") && !Objects.equals(getNewBookTitle(), "") && !Objects.equals(getNewBookSummary(), "")){//Verif if the input are good to quit the dlg and recovered the data for bdd
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
                            else
                                bookFind = false;
                        }
                        if (!bookFind && Objects.equals(getURL(), "") && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne()
                                && startDate.compareTo(enDate)<0){
                            //If a book has not been found in the database when leaving the loop, then the book typed is valid
                            File file = new File("Ressource/Image/Default.jpg");
                            String path = file.getAbsolutePath();
                            setURL(path);//create default image if we did'nt choice an image
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && Objects.equals(getURL(), "") && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne()
                                && startDate.compareTo(enDate)>0) {
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut pas être après à la fin de lecture !");
                        } else if (!bookFind && Objects.equals(getURL(), "") && !isDateUnknown() && isNotDOne()) {
                            File file = new File("Ressource/Image/Default.jpg");
                            String path = file.getAbsolutePath();
                            setURL(path);
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && isDateUnknown()&& !isNotDOne() && Objects.equals(getURL(), "")) {
                            File file = new File("Ressource/Image/Default.jpg");
                            String path = file.getAbsolutePath();
                            setURL(path);
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && isDateUnknown()&& !isNotDOne() && !Objects.equals(getURL(), "")){
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && !Objects.equals(getURL(), "") && !isDateUnknown() && isNotDOne()) {
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && !Objects.equals(getURL(), "") && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne()
                                && startDate.compareTo(enDate)<0) {
                            m_isValide=true;
                            setVisible(false);
                            dispose();
                        } else if (!bookFind && !Objects.equals(getURL(), "") && !Objects.equals(getNewBookStartReading(), getNewBookEndReading()) && !isDateUnknown() && !isNotDOne()
                                && startDate.compareTo(enDate)>0) {
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
                else if(Objects.equals(getNewBookAuthor(), getNewBookTitle())){
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Le nom de l'auteur et le titre d'un livre ne peut pas être identique ! ");
                }
                else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !");
                }
            }
        });
        BookBrowseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf= new JFileChooser();
                if (JFileChooser.APPROVE_OPTION == jf.showOpenDialog(PreviewPhotoPanel)){ //Opens the file panel to select an image
                    String path = jf.getSelectedFile().getPath();//Sélection image
                    setURL(path);
                    addImageToPanel(path);
                }
            }
        });
        BookTagsCB.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (!Objects.equals(BookTagsCB.getSelectedItem(), "")) {
//                    System.out.println(evt.getKeyCode());
//                    if(evt.getKeyCode()<100 && evt.getKeyCode()>10) {
//                        for(int i = 0; i<BookTagsCB.getItemCount();i++){
//                            if(BookTagsCB.getEditor().getItem().toString().contains(BookTagsCB.getItemAt(i).toString())){
//                                BookTagsCB.showPopup();
//                            }
//                        }
//                    }
                    if (evt.getKeyCode()==KeyEvent.VK_ENTER){
                        boolean tagFind = false;
                        int i = 0;
                        while(!tagFind && i<getTags().getSizeTags()){
                            if(Objects.equals(BookTagsCB.getSelectedItem(), getTags().getTag(i).getTextTag())){
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "Vous avez déjà sélectionné ce tag !");
                                tagFind =true;
                            }
                            else i++;
                        }
                        if(!tagFind){
                            getTags().createTag(Objects.requireNonNull(BookTagsCB.getSelectedItem()).toString());
                            for (int j = 0; j<getTags().getSizeTags(); j++){
                                BookTagsPanel.add(getTags().getTag(j));
                            }
                        }
                    }
                }
                BookTagsPanel.updateUI();
            }
        });

        BookTagsPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Component[] componentList = BookTagsPanel.getComponents();
                for(int i = 0; i<componentList.length;i++){
                    if(componentList[i]==e.getComponent().getComponentAt(e.getX(),e.getY())){
                        BookTagsPanel.remove(componentList[i]);
                        getTags().getTags().remove(i);
                    }
                }
                System.out.println(getTags().getSizeTags());
                BookTagsPanel.updateUI();
            }
        });
        //Change the cursor when we focus a tag
        BookTagsPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                File fileRemove = new File("Ressource/Icons/remove.png");
                String pathRemove = fileRemove.getAbsolutePath();
                Image imgRemove = Toolkit.getDefaultToolkit().getImage(pathRemove);
                Point hotspot = new Point(15, 15);
                String cursorName = "Lightsaber Cursor";
                for (int i=0;i<getTags().getSizeTags();i++)
                    getTags().getTag(i).setCursor(getToolkit().createCustomCursor(imgRemove, hotspot, cursorName));
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
    public String getURL(){
        return m_URL;
    }
    public Tags getTags(){
        return this.m_tags;
    }
    public String listOfTags(){
        StringBuilder tags = new StringBuilder();
        for(int i=0; i<getTags().getSizeTags(); i++){
            tags.append(getTags().getTag(i).getTextTag()).append("/");
        }
        return tags.toString();
    }
    //Dynamically add tags to the combobox, retrieved from bdd
    public Tags findTags(){
        Tags tags = new Tags();
        String sql = "SELECT Tags FROM Book";
        try {
            Class.forName("org.sqlite.JDBC");
            m_connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            m_statement = m_connection.createStatement();
            ResultSet tagsQry = m_statement.executeQuery(sql);
            while (tagsQry.next()){
                String[] strTags = tagsQry.getString(1).split("/");
                boolean tagFind = false;
                for(int j=0; j<Arrays.stream(strTags).count();j++){
                    for(int i = 0; i<tags.getSizeTags();i++){
                        if(strTags[j].equals(tags.getTag(i).getTextTag())){
                            tagFind=true;
                        }
                    }
                    if(!tagFind){
                        tags.createTag(strTags[j]);
                    }
                }
            }
            m_connection.close();
            m_statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return tags;
    }
    public void setTags(Tags tags){
        this.m_tags=tags;
    }
    public void addImageToPanel(String path){//Apply to our panel an image with path
        Image img = Toolkit.getDefaultToolkit().getImage(path);
        img=img.getScaledInstance(200, 300, Image.SCALE_AREA_AVERAGING);
        ImageIcon icon = new ImageIcon(img);
        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(icon);

        PreviewPhotoPanel.updateUI();//reload the panel
        PreviewPhotoPanel.removeAll();
        PreviewPhotoPanel.add(imgLabel);
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
    }
    public void setURL(String url){
        m_URL= url;
    }
    public void fillThemeCB(){
        this.BookTagsCB.addItem("");
        for (int i = 0; i<findTags().getSizeTags(); i++){
            this.BookTagsCB.addItem(findTags().getTag(i).getTextTag());
        }
    }
}
