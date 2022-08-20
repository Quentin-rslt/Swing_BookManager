package Forms.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

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
    private String m_newURL;
    private boolean isValid = false;

    public EditBookDlg(String title, String author) {
        setContentPane(contentPane);
        setModal(true);
        setOldTitle(title);
        setOldAuthor(author);
        loadDB(getOldTitle(), getOldAuthor());
        BookOkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Objects.equals(getNewTitle(), "") && !Objects.equals(getNewAuthor(), "") && !Objects.equals(getNewSummary(), "") && !Objects.equals(getNewURL(), "")){
                    setIsValid(true);
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
                if (JFileChooser.APPROVE_OPTION == jf.showOpenDialog(BookPhotoPanel)){ //Opens the file panel to select an image
                    String path = jf.getSelectedFile().getPath();//Sélection image
                    setNewURL(path);
                    addImageToPanel(path);
                }
            }
        });
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
        return BookAuthorTextField.getText();
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
    public String getNewURL() {
        return m_newURL;
    }
    public boolean isValid() {
        return isValid;
    }
    private Connection connect() {
        Connection connection = null;
        String url = "jdbc:sqlite:BookManager.db";
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public void setOldTitle(String oldTitle) {
        this.m_oldTitle = oldTitle;
    }
    public void setOldAuthor(String oldAuthor) {
        this.m_oldAuthor = oldAuthor;
    }
    public void setNewURL(String newURL) {
        this.m_newURL = newURL;
    }
    public void setIsValid(boolean valid) {
        isValid = valid;
    }
    public void loadDB(String title, String author){
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();
            //Title
            ResultSet titleQry = statement.executeQuery("SELECT Title FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
            BookTitleTextField.setText(titleQry.getString(1));

            //Author
            ResultSet authorQry = statement.executeQuery("SELECT Author FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
            BookAuthorTextField.setText(authorQry.getString(1));

            //Release year
            ResultSet NumberOPQry = statement.executeQuery("SELECT ReleaseYear FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
            Date dateRelease = new Date();
            SpinnerDateModel BookReleaseDateSpinModel = new SpinnerDateModel(new SimpleDateFormat("yyyy").parse(NumberOPQry.getString(1)),null,dateRelease,Calendar.YEAR);//Create a spinner date, to correctly select a date
            BookReleaseYearSpin.setModel(BookReleaseDateSpinModel);
            JSpinner.DateEditor Year = new JSpinner.DateEditor(BookReleaseYearSpin,"yyyy");//set the display of the JSpinner of release date
            BookReleaseYearSpin.setEditor(Year);

            //Number of page
            ResultSet ReleaseYearQry = statement.executeQuery("SELECT NumberOP FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
            SpinnerModel BookNumberSM = new SpinnerNumberModel(ReleaseYearQry.getInt(1), 0, 3248, 1);
            BookNumberOPSpin.setModel(BookNumberSM);

            //Note on babelio
            ResultSet NoteBBQry = statement.executeQuery("SELECT NoteBabelio FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
            SpinnerModel BookNoteBbblSM = new SpinnerNumberModel(NoteBBQry.getDouble(1), 0, 5, 0.01);
            BookNoteBblSpin.setModel(BookNoteBbblSM);

            //Personal note
            ResultSet NotePersoQry = statement.executeQuery("SELECT NotePerso FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
            SpinnerModel BookPersonalNotelSM = new SpinnerNumberModel(NotePersoQry.getDouble(1), 0, 5, 0.5);//Set a default and max value for spinner Note
            BookPersonalNoteSpin.setModel(BookPersonalNotelSM);

            //Summary
            ResultSet SummaryQry = statement.executeQuery("SELECT Summary FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
            BookSummaryTextPane.setText(SummaryQry.getString(1));

            //Image
            ResultSet ImageQry = statement.executeQuery("SELECT Image FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");
            addImageToPanel(ImageQry.getString(1));
            setNewURL(ImageQry.getString(1));

            conn.close();
            statement.close();
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

        BookPhotoPanel.updateUI();//reload the panel
        BookPhotoPanel.removeAll();
        BookPhotoPanel.add(imgLabel);
    }
}
