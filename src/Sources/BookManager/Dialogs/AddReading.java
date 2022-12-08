package Sources.BookManager.Dialogs;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static Sources.BookManager.BookManager.*;
import static Sources.CommonSQL.connect;

public class AddReading extends JDialog {
    private JPanel contentPane;
    private JButton ReadingOkBtn;
    private JButton ResetBtn;
    private JLabel ReadingTitleLabel;
    private JLabel ReadingAuthorLabel;
    private JSpinner ReadingNewStartDateSpin;
    private JSpinner ReadingNewEndDateSpin;
    private JCheckBox ReadingUnknownCheckBox;
    private JCheckBox ReadingNotDoneCheckBox;

    private final String m_title;
    private final String m_author;
    private boolean m_isValid = false;

    public AddReading() {
        setContentPane(contentPane);
        setModal(true);
        this.m_title = getMTitle();
        this.m_author = getAuthor();
        initComponents();
        ReadingUnknownCheckBox.addActionListener((ActionEvent e) ->{
                if (isDateUnknown()){
                    ReadingNotDoneCheckBox.setSelected(false);
                    ReadingNewStartDateSpin.setEnabled(false);
                    ReadingNewEndDateSpin.setEnabled(false);
                }
                else {
                    ReadingNewStartDateSpin.setEnabled(true);
                    ReadingNewEndDateSpin.setEnabled(true);
                }
            });
        ReadingNotDoneCheckBox.addActionListener((ActionEvent e)-> {
                if (isNotDone()) {
                    ReadingUnknownCheckBox.setSelected(false);
                    ReadingNewStartDateSpin.setEnabled(true);
                    ReadingNewEndDateSpin.setEnabled(false);
                }
                else{
                    ReadingNewEndDateSpin.setEnabled(true);
                }
            });
        ResetBtn.addActionListener((ActionEvent e)-> {
            initComponents();
            ReadingNewStartDateSpin.setEnabled(true);
            ReadingNewEndDateSpin.setEnabled(true);
            ReadingUnknownCheckBox.setSelected(false);
            ReadingNotDoneCheckBox.setSelected(false);
            contentPane.updateUI();
        });
        ReadingOkBtn.addActionListener((ActionEvent evt)-> {
            String sql = "SELECT Title, Author, StartReading, EndReading FROM Reading WHERE Title='"+this.m_title+"' AND Author='"+this.m_author+"'";
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
                        JOptionPane.showMessageDialog(jFrame, "Les dates de lecture existent déjà !", "Date saisie invalide", JOptionPane.ERROR_MESSAGE);
                        readingFound = true;//
                    } else if (!isDateUnknown() && isNotDone() && Objects.equals(qry.getString(3), getNewStartReading()) && !Objects.equals(getNewStartReading(), getNewEndReading())) {
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de début de lecture existe déjà !", "Date saisie invalide", JOptionPane.ERROR_MESSAGE);
                        readingFound = true;//
                    } else if (!isDateUnknown() && !isNotDone() && Objects.equals(qry.getString(3), getNewStartReading()) && !Objects.equals(getNewStartReading(), getNewEndReading())) {
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de début de lecture existe déjà !", "Date saisie invalide", JOptionPane.ERROR_MESSAGE);
                        readingFound = true;//
                    } else if (!isDateUnknown() && !isNotDone() && Objects.equals(qry.getString(4), getNewEndReading()) && !Objects.equals(getNewStartReading(), getNewEndReading())) {
                        JFrame jFrame = new JFrame();
                        JOptionPane.showMessageDialog(jFrame, "La date de fin de lecture existe déjà !", "Date saisie invalide", JOptionPane.ERROR_MESSAGE);
                        readingFound = true;//
                    }
                }
                if (!readingFound && !isDateUnknown() && !isNotDone() && startDate.compareTo(enDate)>0) {
                    setIsValid(false);
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "La date de début de lecture ne peut pas être après à la fin de lecture !", "Date saisie invalide", JOptionPane.ERROR_MESSAGE);
                }else if(!readingFound ){
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                }
                conn.close();
                statement.close();
            }catch (Exception e){
                System.out.println(e.getMessage());
                JFrame jf = new JFrame();
                JOptionPane.showMessageDialog(jf, e.getMessage(), "Validation lecture impossible", JOptionPane.ERROR_MESSAGE);
            }
        });
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
        return ReadingUnknownCheckBox.isSelected();
    }
    public boolean isNotDone(){
        return ReadingNotDoneCheckBox.isSelected();
    }

    public void setIsValid(boolean bool){
        this.m_isValid = bool;
    }
    public void initComponents(){
        ReadingTitleLabel.setText("Nom du livre : "+this.m_title);
        ReadingAuthorLabel.setText("Nom de l'auteur : "+this.m_author);

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
