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
            public void actionPerformed(ActionEvent e) {
                if(!Objects.equals(getDateReading(), getNewDateReading())){//If the new date and old date are not similar
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                } else if (!Objects.equals(getDateReading(), "Inconnu") && Objects.equals(getNewDateReading(), "Inconnu")) {//if the old date is not "Inconnu" and the new is "Inconnu"
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                } else{//else we can't change the reading date
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Ne peut pas modifier la date de lecture avec une date déjà existante ! ");
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
        else
            return m_newDateReading;
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
