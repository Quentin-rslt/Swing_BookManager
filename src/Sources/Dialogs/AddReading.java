package Sources.Dialogs;

import Sources.RoundBorderCp;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static Sources.Common.connect;

public class AddReading extends JDialog {
    private JPanel contentPane;
    private JButton ReadingOkBtn;
    private JButton ReadingConcelBtn;
    private JLabel ReadingTitleLabel;
    private JLabel ReadingAuthorLabel;
    private JSpinner ReadingNewStartDateSpin;
    private JSpinner ReadingNewEndDateSpin;
    private JCheckBox ReadingUnknownCheckBox;
    private JCheckBox ReadingNotDoneCheckBox;

    private String m_title;
    private String m_author;
    private String m_newEndReading;
    private String m_newStartReading;
    private boolean m_isValid = false;

    public AddReading(String title, String author) {
        setContentPane(contentPane);
        setModal(true);
        setMtitle(title);
        setAuthor(author);
        initComponents();
        ReadingUnknownCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDateUnknown()){
                    ReadingNotDoneCheckBox.setSelected(false);
                    ReadingNewStartDateSpin.setEnabled(false);
                    ReadingNewEndDateSpin.setEnabled(false);
                }
                else {
                    ReadingNewStartDateSpin.setEnabled(true);
                    ReadingNewEndDateSpin.setEnabled(true);
                }
            }
        });
        ReadingNotDoneCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isNotDone()) {
                    ReadingUnknownCheckBox.setSelected(false);
                    ReadingNewStartDateSpin.setEnabled(true);
                    ReadingNewEndDateSpin.setEnabled(false);
                }
                else{
                    ReadingNewEndDateSpin.setEnabled(true);
                }
            }
        });
        ReadingConcelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setIsValid(false);
                setVisible(false);
                dispose();
            }
        });
        ReadingOkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                //System.out.println(getNewStartReading()+" - "+getNewEndReading());
                String sql = "SELECT Title, Author, StartReading, EndReading FROM Reading";
                try (Connection conn = connect()){
                    Statement statement = conn.createStatement();
                    ResultSet qry = statement.executeQuery(sql);

                    Date enDate =new Date();
                    Date startDate = new Date();
                    if(!isNotDone() && !isDateUnknown()){
                        enDate =new SimpleDateFormat("yyyy-MM-dd").parse(getNewEndReading());
                        startDate = new SimpleDateFormat("yyyy-MM-dd").parse(getNewStartReading());
                    }
                    boolean readingFound =false;
                    while (qry.next() && !readingFound){
                        if (!isDateUnknown() && !isNotDone() && Objects.equals(qry.getString(3), getNewStartReading())
                                && Objects.equals(qry.getString(4), getNewEndReading())){
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "Les dates de lecture existent déjà !");
                            readingFound = true;//
                        } else if (!isDateUnknown() && isNotDone() && Objects.equals(qry.getString(3), getNewStartReading()) && !Objects.equals(getNewStartReading(), getNewEndReading())) {
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "La date de début de lecture existe déjà !");
                            readingFound = true;//
                        } else if (!isDateUnknown() && !isNotDone() && Objects.equals(qry.getString(3), getNewStartReading()) && !Objects.equals(getNewStartReading(), getNewEndReading())) {
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "La date de début de lecture existe déjà !");
                            readingFound = true;//
                        } else if (!isDateUnknown() && !isNotDone() && Objects.equals(qry.getString(4), getNewEndReading()) && !Objects.equals(getNewStartReading(), getNewEndReading())) {
                            JFrame jFrame = new JFrame();
                            JOptionPane.showMessageDialog(jFrame, "La date de fin de lecture existe déjà !");
                            readingFound = true;//
                        } else{
                            readingFound = false;
                        }
                    }
                    if (!readingFound && isDateUnknown()){
                        setIsValid(true);
                        setVisible(false);
                        dispose();
                    } else if (!readingFound && isNotDone()) {
                        setIsValid(true);
                        setVisible(false);
                        dispose();
                    } else if (!readingFound && !Objects.equals(getNewStartReading(), getNewEndReading()) && !isDateUnknown() && !isNotDone() && startDate.compareTo(enDate)<0){
                        setIsValid(true);
                        setVisible(false);
                        dispose();
                    } else if (!readingFound && !Objects.equals(getNewStartReading(), getNewEndReading()) && !isDateUnknown() && !isNotDone() && startDate.compareTo(enDate)>0) {
                        setIsValid(false);
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut pas être après à la fin de lecture !");
                    } else if(!readingFound && Objects.equals(getNewStartReading(), getNewEndReading())
                            && !isDateUnknown() && !isNotDone()){
                        setIsValid(false);
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut pas être identique à la fin de lecture !");
                    }
                    conn.close();
                    statement.close();
                }catch (Exception e){
                    System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                    System.exit(0);
                }
            }
        });
    }

    public String getMtitle() {
        return m_title;
    }
    public String getAuthor() {
        return m_author;
    }
    public String getNewEndReading() {
        String end = "";
        if(!isDateUnknown() && !isNotDone()){
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
            end =formater.format(ReadingNewEndDateSpin.getValue());
        }
        else if (isDateUnknown())
            end="Inconnu";
        else if (isNotDone())
            end="Pas fini";
        return end;
    }
    public String getNewStartReading() {
        String start = "";
        if(!isDateUnknown() && !isNotDone()){
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
            start =formater.format(ReadingNewStartDateSpin.getValue());
        }
        else if (!isDateUnknown() && isNotDone()){
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
            start =formater.format(ReadingNewStartDateSpin.getValue());
        }
        else if (isDateUnknown())
            start="Inconnu";
        return start;
    }
    public boolean getIsValid() {
        return m_isValid;
    }
    public boolean isDateUnknown(){
        if (ReadingUnknownCheckBox.isSelected())
            return true;
        else
            return false;
    }
    public boolean isNotDone(){
        if (ReadingNotDoneCheckBox.isSelected())
            return true;
        else
            return false;
    }

    public void setMtitle(String m_title) {
        this.m_title = m_title;
    }
    public void setAuthor(String m_author) {
        this.m_author = m_author;
    }
    public void setNewEndReading(String m_newEndReading) {
        this.m_newEndReading = m_newEndReading;
    }
    public void setNewStartReading(String m_newStartReading) {
        this.m_newStartReading = m_newStartReading;
    }
    public void setIsValid(boolean bool){
        this.m_isValid = bool;
    }
    public void initComponents(){
        ReadingTitleLabel.setText("Nom du livre : "+getMtitle());
        ReadingAuthorLabel.setText("Nom de l'auteur : "+getAuthor());

        Date startDate = new Date();
        SpinnerDateModel NewBookStartReadingSpinModel = new SpinnerDateModel(startDate, null, startDate, Calendar.YEAR);
        ReadingNewStartDateSpin.setModel(NewBookStartReadingSpinModel);
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(ReadingNewStartDateSpin,"yyyy-MM-dd");//set the display of the JSpinner of release date
        ReadingNewStartDateSpin.setEditor(startEditor);

        Date endDate = new Date();
        SpinnerDateModel NewBookEndReadingSpinModel = new SpinnerDateModel(endDate, null, endDate, Calendar.YEAR);
        ReadingNewEndDateSpin.setModel(NewBookEndReadingSpinModel);
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(ReadingNewEndDateSpin,"yyyy-MM-dd");//set the display of the JSpinner of release date
        ReadingNewEndDateSpin.setEditor(endEditor);
    }
}
