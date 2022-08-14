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

    public AddBookDlg() {
        setContentPane(contentPane);
        FillBookCombobox();
        setModal(true);

        SpinnerModel BookPersonalNotelSM = new SpinnerNumberModel(5, 0, 5, 1);//Set a default and max value for spinner Note
        SpinnerModel BookNoteBbblSM = new SpinnerNumberModel(5, 0, 5, 1);
        BookPersonalNoteSpin.setModel(BookPersonalNotelSM);
        BookNoteBblSpin.setModel(BookNoteBbblSM);

        Date dateRelease = new Date();
        Date date = new Date();

        SpinnerDateModel BookReadDateSpinDate = new SpinnerDateModel(date,null,null,Calendar.YEAR);//Create a spinner date, to correctly select a date
        SpinnerDateModel BookReleaseDateSpinModel = new SpinnerDateModel(dateRelease,null,null,Calendar.YEAR);//Create a spinner date, to correctly select a date

        BookReleaseYearSpin.setModel(BookReleaseDateSpinModel);
        JSpinner.DateEditor Year = new JSpinner.DateEditor(BookReleaseYearSpin,"yyyy");//set the display of the JSpinner of release date
        BookReleaseYearSpin.setEditor(Year);

        BookDateReadSpin.setModel(BookReadDateSpinDate);
        JSpinner.DateEditor ded = new JSpinner.DateEditor(BookDateReadSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
        BookDateReadSpin.setEditor(ded);

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
                }
                else{
                    BookDateReadSpin.setEnabled(true);
                }
            }
        });
        ExitingBookComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {//set the title and the author, retrieved by the ComboBox
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
                AddImageToPanel(m_title, m_author);//add the image of the book, with the author and the title recovered on the combobox, in our panel
            }
        });
    }

    public String getTitle(){
        return m_title;
    }
    public String getAuthor(){
        return m_author;
    }

    public void FillBookCombobox(){
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
    public void AddImageToPanel(String title, String author){//
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            Statement statement = connection.createStatement();
            ResultSet ImageQry = statement.executeQuery("SELECT Image FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");//Retrieved from the bdd the URL of the book image,
                                                                                                                                            // in parameters the title and the author enter in parameters of this function

            Image img = Toolkit.getDefaultToolkit().getImage(ImageQry.getString(1));
            img=img.getScaledInstance(200, 300, Image.SCALE_DEFAULT);
            ImageIcon icon = new ImageIcon(img);
            JLabel imgLabel = new JLabel();
            imgLabel.setDisabledIcon(icon);
            imgLabel.setIcon(icon);

            PreviewPhotoPanel.updateUI();//reload the panel
            PreviewPhotoPanel.removeAll();
            PreviewPhotoPanel.add(imgLabel);

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        AddBookDlg dialog = new AddBookDlg();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
