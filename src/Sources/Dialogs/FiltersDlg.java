package Sources.Dialogs;

import Sources.RoundBorderCp;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FiltersDlg extends JDialog {
    private JPanel contentPane;
    private JButton FiltersOkBtn;
    private JButton FiltersCancelBtn;
    private JTextField FiltersTitleTextField;
    private JTextField FiltersAuthorTextField;
    private JSpinner FiltersFirstReleaseYearSpin;
    private JSpinner FiltersLastReleaseYearSpin;
    private JSpinner FiltersFirstNoteSpin;
    private JSpinner FiltersLastNoteSpin;

    private boolean m_isValid;

    public FiltersDlg() {
        setContentPane(contentPane);
        setModal(true);
        initComponents();
        FiltersOkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setIsValid(true);
                setVisible(false);
                dispose();
            }
        });
        FiltersCancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setIsValid(false);
                setVisible(false);
                dispose();
            }
        });
    }

    public String getMTitle(){
        return FiltersTitleTextField.getText();
    }
    public String getAuthor(){
        return FiltersAuthorTextField.getText();
    }
    public String getFirstDatRelease(){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy");//set the date format returned to have the day, month and year
        return formater.format(FiltersFirstReleaseYearSpin.getValue());
    }
    public String getLastDateRelease(){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy");//set the date format returned to have the day, month and year
        return formater.format(FiltersLastReleaseYearSpin.getValue());
    }
    public String getFirstNote(){
        return FiltersFirstNoteSpin.getValue().toString();
    }
    public String getLastNote(){
        return FiltersLastNoteSpin.getValue().toString();
    }
    public boolean getIsValid() {
        return m_isValid;
    }

    public void setIsValid(boolean m_isValid) {
        this.m_isValid = m_isValid;
    }
    public void initComponents(){
        try{
            Date FirstDateRelease = new Date();
            SpinnerDateModel FirstReleaseDateSpinModel = new SpinnerDateModel(new SimpleDateFormat("yyyy").parse("0001"),null,FirstDateRelease, Calendar.YEAR);//Create a spinner date, to correctly select a date
            FiltersFirstReleaseYearSpin.setModel(FirstReleaseDateSpinModel);
            JSpinner.DateEditor FirstRelease = new JSpinner.DateEditor(FiltersFirstReleaseYearSpin,"yyyy");//set the display of the JSpinner of release date
            FiltersFirstReleaseYearSpin.setEditor(FirstRelease);

            Date EndDateRelease = new Date();
            SpinnerDateModel EndReleaseDateSpinModel = new SpinnerDateModel(EndDateRelease,null,EndDateRelease, Calendar.YEAR);//Create a spinner date, to correctly select a date
            FiltersLastReleaseYearSpin.setModel(EndReleaseDateSpinModel);
            JSpinner.DateEditor EndRelease = new JSpinner.DateEditor(FiltersLastReleaseYearSpin,"yyyy");//set the display of the JSpinner of release date
            FiltersLastReleaseYearSpin.setEditor(EndRelease);

            SpinnerModel FirstNoteSM = new SpinnerNumberModel(0, 0, 5, 0.5);//Set a default and max value for spinner Note
            SpinnerModel EndNoteSM = new SpinnerNumberModel(5, 0, 5, 0.5);
            FiltersFirstNoteSpin.setModel(FirstNoteSM);
            FiltersLastNoteSpin.setModel(EndNoteSM);
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }
}
