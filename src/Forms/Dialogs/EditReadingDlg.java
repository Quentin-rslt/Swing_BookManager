package Forms.Dialogs;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private JSpinner BookNewDateRedingSpin;


    private String m_title;
    private String m_author;
    private String m_dateReading;
    private boolean m_isValid = false;

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
            public void actionPerformed(ActionEvent e) {
                if(!Objects.equals(getDateReading(), getBookNewDateReading())){
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                }
                else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Ne peut pas modifier la date de lecture avec une date déjà existante ! ");
                }
            }
        });
    }

    public String getDateReading() {
        return m_dateReading;
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
    public String getBookNewDateReading(){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
        return formater.format(BookNewDateRedingSpin.getValue());
    }

    public void setDateReading(String m_dateReading) {
        this.m_dateReading = m_dateReading;
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
        BookTitleLable.setText("Titre : "+getMtitle());
        BookAuthorLabel.setText("Auteur : "+getAuthor());
        BookDateReadingLabel.setText("Date de lecture : "+getDateReading());

        Date date = new Date();
        SpinnerDateModel NewBookDateReadingSpinModel = new SpinnerDateModel(date,null,null,Calendar.YEAR);//Create a spinner date, to correctly select a date
        BookNewDateRedingSpin.setModel(NewBookDateReadingSpinModel);
        JSpinner.DateEditor Year = new JSpinner.DateEditor(BookNewDateRedingSpin,"yyyy-MM-dd");//set the display of the JSpinner of release date
        BookNewDateRedingSpin.setEditor(Year);
    }
}
