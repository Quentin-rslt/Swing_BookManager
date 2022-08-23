package Sources.Dialogs;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class EditReadingDlg extends JDialog {
    private JPanel contentPane;
    private JPanel BookPanel;
    private JPanel BookBtnPanel;
    private JButton OkBtn;
    private JButton CancelBtn;
    private JPanel BookEditReadingPanel;
    private JPanel BookInfoPanel;
    private JLabel BookTitleLable;
    private JLabel BookAuthorLabel;
    private JLabel BookDateReadingLabel;
    private JLabel NewBookDateRedingLabel;
    private JSpinner BookNewDateReadingSpin;
    private JCheckBox BookUnknownDateReadingCheckBox;


    private String m_title;
    private String m_author;
    private String m_dateReading;
    private String m_newDateReading;
    private boolean m_isValid = false;
    private Date m_date = new Date();

    public EditReadingDlg(String title, String author, String dateReading) {
        setContentPane(contentPane);
        setModal(true);
        setMtitle(title);
        setAuthor(author);
        setDateReading(dateReading);
        initComponent();
        CancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setIsValid(false);
                setVisible(false);
                dispose();
            }
        });
        OkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String sql = "SELECT Title, Author, DateReading FROM Reading WHERE Title='"+getMtitle()+"' AND Author='"+getAuthor()+"'";
                try {
                    Class.forName("org.sqlite.JDBC");
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                    Statement statement = connection.createStatement();
                    ResultSet qry = statement.executeQuery(sql);

                    boolean dateFind =false;
                    while (qry.next() && !dateFind){//check if the modified date already exists (unless it is Unknown)
                        if (Objects.equals(getNewDateReading() ,qry.getString(3)) &&//If there is a date then we do not modify and display an error message
                                !Objects.equals(qry.getString(3), "Inconnu") && !Objects.equals(getNewDateReading(), "Inconnu")){
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "La date de lecture existe déjà !");
                            dateFind = true;
                        }
                        else{
                            dateFind = false;
                        }
                    }
                    if (!dateFind){//If a date has not been found in the database when leaving the loop, then the date typed is valid
                        setIsValid(true);
                        setVisible(false);
                        dispose();
                    }
                    connection.close();
                    statement.close();
                }catch (Exception e){
                    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                    System.exit(0);
                }
            }
        });
        BookUnknownDateReadingCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDateReadingUnknown())
                    BookNewDateReadingSpin.setEnabled(false);
                else
                    BookNewDateReadingSpin.setEnabled(true);
            }
        });
    }

    public String getDateReading() {
        return m_dateReading;
    }
    public String getNewDateReading() {
        if (isDateReadingUnknown())
            return "Inconnu";
        else{
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
            return formater.format(BookNewDateReadingSpin.getValue());
        }
    }
    public String getAuthor() {
        return m_author;
    }
    public String getMtitle() {
        return m_title;
    }
    public boolean isValid() {
        return m_isValid;
    }
    public Date getDate() {
        return m_date;
    }
    public boolean isDateReadingUnknown(){
        if(BookUnknownDateReadingCheckBox.isSelected())
            return true;
        else
            return false;
    }

    public void setDateReading(String m_dateReading) {
        this.m_dateReading = m_dateReading;
    }
    public void setNewDateReading(String m_newDateReading) {
        this.m_newDateReading = m_newDateReading;
    }
    public void setAuthor(String m_author) {
        this.m_author = m_author;
    }
    public void setMtitle(String m_title) {
        this.m_title = m_title;
    }
    public void setIsValid(boolean m_isValid) {
        this.m_isValid = m_isValid;
    }
    public void initComponent(){
        //Retrieves the data entered as a parameter from the constructor, and therefore from the DB
        BookTitleLable.setText("Titre : "+getMtitle());
        BookAuthorLabel.setText("Auteur : "+getAuthor());
        BookDateReadingLabel.setText("Date de lecture : "+getDateReading());

        SpinnerDateModel NewBookDateReadingSpinModel = new SpinnerDateModel(getDate(),null,getDate(),Calendar.YEAR);//Create a spinner date, to correctly select a date
        BookNewDateReadingSpin.setModel(NewBookDateReadingSpinModel);
        JSpinner.DateEditor Year = new JSpinner.DateEditor(BookNewDateReadingSpin,"yyyy-MM-dd");//set the display of the JSpinner of release date
        BookNewDateReadingSpin.setEditor(Year);

        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
        setNewDateReading(formater.format(BookNewDateReadingSpin.getValue()));//set the new date
    }
}
