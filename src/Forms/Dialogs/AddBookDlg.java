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
    private JSpinner BookReleaseYearSpin;
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
    private JPanel BookReadPanel;
    private JSpinner BookReadSpin;

    public AddBookDlg() {
        setContentPane(contentPane);
        FillBookCombobox();
        setModal(true);

        Date date = new Date();
        SpinnerDateModel BookReadDateSpinDate = new SpinnerDateModel(date,null,null,Calendar.YEAR);
        BookReadSpin = new JSpinner(BookReadDateSpinDate);
        JSpinner.DateEditor ded = new JSpinner.DateEditor(BookReadSpin,"yyyy/MM/dd");
        BookReadSpin.setEditor(ded);
        BookReadPanel.add(BookReadSpin);

        AlreadyReadChecbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                if(BookUnknownReadDateChecbox.isSelected()==true){
                    BookReadSpin.setEnabled(false);
                }
                else{
                    BookReadSpin.setEnabled(true);
                }
            }
        });
        ExitingBookComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String row = ExitingBookComboBox.getSelectedItem().toString();
                String author = "";
                String title = "";
                int i = 0;
                do {
                    title += row.charAt(i);
                    i++;
                } while (row.charAt(i+1)!= '-');
                author = row.replace(title, "");
                String RealAuthor = author.substring(3 , author.length());
                AddImageToPanel(title, RealAuthor);
            }
        });
    }
    public void FillBookCombobox(){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("SELECT Title, Author FROM BookManager GROUP BY Title, Author ORDER BY Title ASC;");
            while (rs.next()) {
                ExitingBookComboBox.addItem(rs.getString(1)+ " - " + rs.getString(2));
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void AddImageToPanel(String title, String author){
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            Statement statement = connection.createStatement();
            ResultSet ImageQry = statement.executeQuery("SELECT Image FROM BookManager WHERE Title='"+title+"' AND Author='"+author+ "'");

            Image img = Toolkit.getDefaultToolkit().getImage(ImageQry.getString(1));
            img=img.getScaledInstance(200, 300, Image.SCALE_DEFAULT);
            ImageIcon icon = new ImageIcon(img);
            JLabel imgLabel = new JLabel();
            imgLabel.setDisabledIcon(icon);
            imgLabel.setIcon(icon);

            PreviewPhotoPanel.updateUI();
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
