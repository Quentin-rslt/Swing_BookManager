package Forms.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

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
    private JLabel PersonalNoteLabel;
    private JSpinner BookPersonalNoteSpin;
    private JLabel ReadingDateLabel;
    private JCheckBox BookUnknownReadDateChecbox;
    private JPanel DateReadPanel;
    private JPanel LeftPanel;
    private JLabel PhotoLabel;
    private JButton BookBrowseBtn;
    private JLabel NoteBblLabel;
    private JSpinner BookNoteBblSpin;
    private JLabel SummaryLabel;
    private JTextPane BookSummaryTextPane;
    private JCheckBox AlreadyReadChecbox;
    private JSpinner BookReleaseYearSpin;
    private JSpinner BookDateReadSpin;

    private String m_author;
    private String m_title;
    private String m_URL;
    private boolean m_isValide = false;//Useful for determinate if the input are good

    public AddBookDlg() {
        setContentPane(contentPane);
        fillBookCombobox();
        setModal(true);
        initComponents();

        AlreadyReadChecbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {//If we have already entered the book in the database, hide the way to create a reading, just add a reading date
                if(AlreadyReadChecbox.isSelected()==true){
                    ExitingBookComboBox.setEnabled(true);
                    BookNameTextField.setEnabled(false);
                    BookAuthorTextField.setEnabled(false);
                    BookReleaseYearSpin.setEnabled(false);
                    BookPersonalNoteSpin.setEnabled(false);
                    BookNoteBblSpin.setEnabled(false);
                    BookSummaryTextPane.setEnabled(false);
                    BookReleaseYearSpin.setEnabled(false);
                    BookBrowseBtn.setEnabled(false);
                    PreviewPhotoPanel.updateUI();
                    PreviewPhotoPanel.removeAll();
                }
                else{
                    ExitingBookComboBox.setEnabled(false);
                    BookNameTextField.setEnabled(true);
                    BookAuthorTextField.setEnabled(true);
                    BookReleaseYearSpin.setEnabled(true);
                    BookPersonalNoteSpin.setEnabled(true);
                    BookNoteBblSpin.setEnabled(true);
                    BookSummaryTextPane.setEnabled(true);
                    BookReleaseYearSpin.setEnabled(true);
                    BookBrowseBtn.setEnabled(true);
                    PreviewPhotoPanel.updateUI();
                    PreviewPhotoPanel.removeAll();
                }
            }
        });
        BookUnknownReadDateChecbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(BookUnknownReadDateChecbox.isSelected()==true){//Hide the possibility to add a reading date when we don't know when you read it
                    BookDateReadSpin.setEnabled(false);
                    System.out.println(BookPersonalNoteSpin.getValue().toString());
                }
                else{
                    BookDateReadSpin.setEnabled(true);
                }
            }
        });
        ExitingBookComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {//set the title and the author, retrieved by the ComboBox
                String row = ExitingBookComboBox.getSelectedItem().toString();//get the item that we chose
                String author = "";
                m_title = "";
                int i = 0;
                do {//As long as the character in the string is not '-', we add to our title variable the letter
                    m_title += row.charAt(i);//at the end we have juste the title
                    i++;
                } while (row.charAt(i+1)!= '-');
                author = row.replace(m_title, "");//We recover only the end of the line to keep only the author
                m_author = author.substring(3 , author.length());
                try{
                    Class.forName("org.sqlite.JDBC");
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                    Statement statement = connection.createStatement();
                    ResultSet ImageQry = statement.executeQuery("SELECT Image FROM BookManager WHERE Title='"+m_title+"' AND Author='"+m_author+ "'");//Retrieved from the bdd the URL of the book image
                                                                                                                                                    // in parameters the title and the author enter in parameters of this function
                    addImageToPanel(ImageQry.getString(1));//add the image of the book, with the author and the title recovered on the combobox, in our panel
                } catch ( Exception e ) {
                    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                    System.exit(0);
                }

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
            public void actionPerformed(ActionEvent e) {
                if(AlreadyReadChecbox.isSelected()==true){
                    m_isValide=true;
                    setVisible(false);
                    dispose();
                }
                else if(getNewBookAuthor()!="" && getNewBookAuthor()!="" && getNewBookReleaseYear()!="" && getNewBookPersonalNote()!="" &&
                        getNewBookBBLNote()!="" && getNewBookSummary()!="" && getURL()!=""){//Verif if the input are good to quit the dlg and recovered the data for bdd
                    m_isValide=true;
                    setVisible(false);
                    dispose();
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
    }

    public String getTitle(){
        return m_title;
    }
    public String getAuthor(){
        return m_author;
    }
    public String getNewBookTitle(){//Get the new book title from JtextField
        return BookNameTextField.getText();
    }
    public String getNewBookAuthor(){
        return BookAuthorTextField.getText();
    }
    public String getNewBookReleaseYear(){
        return BookReleaseYearSpin.getValue().toString();
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
    public String getNewBookDateReading(){
        return BookDateReadSpin.getValue().toString();
    }
    public boolean isDateUnknown(){
        return BookUnknownReadDateChecbox.isSelected();
    }
    public boolean isValide(){
        return m_isValide;
    }
    public String getURL(){
        return m_URL;
    }

    public void fillBookCombobox(){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("SELECT Title, Author FROM BookManager GROUP BY Title, Author ORDER BY Title ASC;");
            while (rs.next()) {
                ExitingBookComboBox.addItem(rs.getString(1)+ " - " + rs.getString(2));//Filled the combobox with the data Recovered from the database
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void addImageToPanel(String path){//Apply to our panel an image with path
        Image img = Toolkit.getDefaultToolkit().getImage(path);
        img=img.getScaledInstance(200, 300, Image.SCALE_DEFAULT);
        ImageIcon icon = new ImageIcon(img);
        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(icon);

        PreviewPhotoPanel.updateUI();//reload the panel
        PreviewPhotoPanel.removeAll();
        PreviewPhotoPanel.add(imgLabel);
    }
    public void initComponents(){
        SpinnerModel BookPersonalNotelSM = new SpinnerNumberModel(5, 0, 5, 1);//Set a default and max value for spinner Note
        SpinnerModel BookNoteBbblSM = new SpinnerNumberModel(5, 0, 5, 1);
        BookPersonalNoteSpin.setModel(BookPersonalNotelSM);
        BookNoteBblSpin.setModel(BookNoteBbblSM);

        Date dateRelease = new Date();
        Date date = new Date();

        SpinnerDateModel BookReadDateSpinDate = new SpinnerDateModel(date,null,null, Calendar.YEAR);//Create a spinner date, to correctly select a date
        SpinnerDateModel BookReleaseDateSpinModel = new SpinnerDateModel(dateRelease,null,null,Calendar.YEAR);//Create a spinner date, to correctly select a date

        BookReleaseYearSpin.setModel(BookReleaseDateSpinModel);
        JSpinner.DateEditor Year = new JSpinner.DateEditor(BookReleaseYearSpin,"yyyy");//set the display of the JSpinner of release date
        BookReleaseYearSpin.setEditor(Year);

        BookDateReadSpin.setModel(BookReadDateSpinDate);
        JSpinner.DateEditor ded = new JSpinner.DateEditor(BookDateReadSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
        BookDateReadSpin.setEditor(ded);
    }
    public void setURL(String url){
        m_URL= url;
    }

    public static void main(String[] args) {
        AddBookDlg dialog = new AddBookDlg();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
