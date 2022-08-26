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
    private JPanel BookInfoPanel;
    private JLabel BookTitleLable;
    private JLabel BookAuthorLabel;
    private JLabel BookEndReadingLabel;
    private JLabel BookStartReadingLabel;
    private JSpinner BookNewEndReadingSpin;
    private JCheckBox BookUnknownDateReadingCheckBox;
    private JCheckBox BookNotDoneReadChecbox;
    private JSpinner BookNewStartReadingSpin;


    private String m_title;
    private String m_author;
    private String m_startReading;
    private String m_endReading;
    private String m_newEndReading;
    private String m_newStartReading;
    private boolean m_isValid = false;
    private Date m_date = new Date();

    public EditReadingDlg(String title, String author, String startReading, String endReading) {
        setContentPane(contentPane);
        setModal(true);
        setMtitle(title);
        setAuthor(author);
        setStartReading(startReading);
        setEndReading(endReading);
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
                String sql = "SELECT Title, Author, StartReading, EndReading FROM Reading WHERE Title='"+getMtitle()+"' AND Author='"+getAuthor()+"'";
                try {
                    Class.forName("org.sqlite.JDBC");
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                    Statement statement = connection.createStatement();
                    ResultSet qry = statement.executeQuery(sql);
                    Date enDate =new SimpleDateFormat("yyyy-MM-dd").parse(getNewEndReading());
                    Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(getNewStartReading());

                    boolean dateFind =false;
                    while (qry.next() && !dateFind){//check if the modified date already exists (unless it is Unknown)
                        //If there is a date then we do not modify and display an error message
                        if (!getNewStartReading().equals(getStartReading()) && !getNewEndReading().equals(getEndReading())){//if we don't edit the end date
                            if (getNewStartReading().equals(qry.getString(3)) && getNewEndReading().equals(qry.getString(4))
                                    && !isDateReadingUnknown() && !isNotDone()){
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de lecture existe déjà !");
                                dateFind = true;
                            } else if (!isDateReadingUnknown() && !isNotDone() && Objects.equals(qry.getString(3), getNewStartReading())) {
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de début de lecture existe déjà !");
                                dateFind = true;//
                            } else if (!isDateReadingUnknown() && !isNotDone() && Objects.equals(qry.getString(4), getNewEndReading())) {
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de fin de lecture existe déjà !");
                                dateFind = true;//
                            } else
                                dateFind = false;
                        } else if (getNewStartReading().equals(getStartReading()) && !getNewEndReading().equals(getEndReading())) {//if we don't edit the start date
                            if (getNewStartReading().equals(qry.getString(3)) && getNewEndReading().equals(qry.getString(4))
                                    && !isDateReadingUnknown() && !isNotDone()){
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de lecture existe déjà !");
                                dateFind = true;
                            } else if (!isDateReadingUnknown() && !isNotDone() && Objects.equals(qry.getString(4), getNewEndReading())) {
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de fin de lecture existe déjà !");
                                dateFind = true;
                            } else
                                dateFind = false;
                        } else if (!getNewStartReading().equals(getStartReading()) && getNewEndReading().equals(getEndReading())) {
                            if (getNewStartReading().equals(qry.getString(3)) && getNewEndReading().equals(qry.getString(4))
                                    && !isDateReadingUnknown() && !isNotDone()){
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de lecture existe déjà !");
                                dateFind = true;
                            } else if (!isDateReadingUnknown() && !isNotDone() && Objects.equals(qry.getString(3), getNewStartReading())) {
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de début de lecture existe déjà !");
                                dateFind = true;//
                            } else if (!isDateReadingUnknown() && isNotDone() && Objects.equals(qry.getString(3), getNewStartReading())) {
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "La date de début de lecture existe déjà !");
                                dateFind = true;//
                            } else
                                dateFind = false;
                        }
                    }
                    if (!dateFind && isDateReadingUnknown()){
                        setIsValid(true);
                        setVisible(false);
                        dispose();
                    } else if (!dateFind && !isDateReadingUnknown() && isNotDone()) {
                        setIsValid(true);
                        setVisible(false);
                        dispose();
                    } else if (!dateFind && !Objects.equals(getNewStartReading(), getNewEndReading()) && !isDateReadingUnknown() && !isNotDone() && startDate.compareTo(enDate)<0){
                        setIsValid(true);
                        setVisible(false);
                        dispose();
                    } else if (!dateFind && !Objects.equals(getNewStartReading(), getNewEndReading()) && !isDateReadingUnknown() && !isNotDone() && startDate.compareTo(enDate)>0){
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut pas être après à la fin de lecture !");
                    } else if(!dateFind && Objects.equals(getNewStartReading(), getNewEndReading())
                            && !isDateReadingUnknown() && !isNotDone()){
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut pas être identique à la fin de lecture !");
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
                if (isDateReadingUnknown()){
                    BookNewEndReadingSpin.setEnabled(false);
                    BookNewStartReadingSpin.setEnabled(false);
                    BookNotDoneReadChecbox.setEnabled(false);
                }
                else{
                    BookNewEndReadingSpin.setEnabled(true);
                    BookNewStartReadingSpin.setEnabled(true);
                    BookNotDoneReadChecbox.setEnabled(true);
                }
            }
        });
        BookNotDoneReadChecbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isNotDone()){
                    BookNewEndReadingSpin.setEnabled(false);
                }
                else{
                    BookNewEndReadingSpin.setEnabled(true);
                }
            }
        });
    }

    public String getEndReading() {
        return m_endReading;
    }
    public String getStartReading() {
        return m_startReading;
    }
    public String getNewEndReading() {
        if (isNotDone() && !isDateReadingUnknown())
            return "Pas fini";
        else if (isDateReadingUnknown()) {
            return "Inconnu";
        }
        else{
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
            return formater.format(BookNewEndReadingSpin.getValue());
        }
    }
    public String getNewStartReading() {
        if (isDateReadingUnknown())
            return "Inconnu";
        else{
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
            return formater.format(BookNewStartReadingSpin.getValue());
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
    public boolean isDateReadingUnknown(){
        if(BookUnknownDateReadingCheckBox.isSelected())
            return true;
        else
            return false;
    }
    public boolean isNotDone(){
        return BookNotDoneReadChecbox.isSelected();
    }

    public void setEndReading(String m_endReading) {
        this.m_endReading = m_endReading;
    }
    public void setStartReading(String m_dateReading) {
        this.m_startReading = m_dateReading;
    }
    public void setNewEndReading(String newEndReading) {
        this.m_newEndReading = newEndReading;
    }
    public void setNewStartReading (String m_newStartReading){
        this.m_newStartReading = m_newStartReading;
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
        try {
            //Retrieves the data entered as a parameter from the constructor, and therefore from the DB
            BookTitleLable.setText("Titre : "+getMtitle());
            BookAuthorLabel.setText("Auteur : "+getAuthor());

            Date endDate = new Date();
            SpinnerDateModel NewBookEndReadingSpinModel = new SpinnerDateModel();
            if(!getEndReading().equals("Pas fini") && !getEndReading().equals("Inconnu")){//if the end reading is not inconnu or pas fini, init the spinner end date with de old date
                 NewBookEndReadingSpinModel = new SpinnerDateModel(new SimpleDateFormat("yyyy-MM-dd").parse(getEndReading()) ,null,endDate, Calendar.YEAR);
            } else if(getEndReading().equals("Pas fini")){
                BookNotDoneReadChecbox.setSelected(true);
                BookNewEndReadingSpin.setEnabled(false);
                NewBookEndReadingSpinModel = new SpinnerDateModel(endDate ,null,endDate, Calendar.YEAR);//Create a spinner date, to correctly select a date
            } else if(getEndReading().equals("Inconnu")){
                BookNewEndReadingSpin.setEnabled(false);
                BookNewStartReadingSpin.setEnabled(false);
                BookNotDoneReadChecbox.setEnabled(false);
                BookUnknownDateReadingCheckBox.setSelected(true);
                NewBookEndReadingSpinModel = new SpinnerDateModel(endDate ,null,endDate, Calendar.YEAR);//Create a spinner date, to correctly select a date
            }
            BookNewEndReadingSpin.setModel(NewBookEndReadingSpinModel);
            JSpinner.DateEditor end = new JSpinner.DateEditor(BookNewEndReadingSpin,"yyyy-MM-dd");//set the display of the JSpinner of release date
            BookNewEndReadingSpin.setEditor(end);

            SimpleDateFormat EndFormater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
            setNewEndReading(EndFormater.format(BookNewEndReadingSpin.getValue()));//set the new date

            Date startDate = new Date();
            SpinnerDateModel NewBookStartReadingSpinModel;
             if(!getStartReading().equals("Inconnu")){//if the start reading is not inconnu, init the spinner start date with de old date
                NewBookStartReadingSpinModel = new SpinnerDateModel(new SimpleDateFormat("yyyy-MM-dd").parse(getStartReading()) ,null,endDate, Calendar.YEAR);//Create a spinner date, to correctly select a date
            } else {
                 NewBookStartReadingSpinModel = new SpinnerDateModel(startDate ,null,startDate, Calendar.YEAR);
             }
            BookNewStartReadingSpin.setModel(NewBookStartReadingSpinModel);
            JSpinner.DateEditor start = new JSpinner.DateEditor(BookNewStartReadingSpin,"yyyy-MM-dd");//set the display of the JSpinner of release date
            BookNewStartReadingSpin.setEditor(start);

            SimpleDateFormat StartFormater = new SimpleDateFormat("yyyy-MM-dd");//set the date format returned to have the day, month and year
            setNewStartReading(StartFormater.format(BookNewStartReadingSpin.getValue()));//set the new date
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
