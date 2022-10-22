package Sources.Dialogs;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static Sources.Common.loadTags;

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
    private JRadioButton FiltersorderCrossingRB;
    private JRadioButton FiltersascendingOrderRB;
    private JComboBox FiltersSortCB;
    private JComboBox FiltersTagCB;

    private boolean m_isValid;

    public FiltersDlg() {
        setContentPane(contentPane);
        setModal(true);
        initComponents();
        FiltersOkBtn.addActionListener((ActionEvent e)-> {
            setIsValid(true);
            setVisible(false);
            dispose();
        });
        FiltersCancelBtn.addActionListener((ActionEvent e)-> {
            setIsValid(false);
            setVisible(false);
            dispose();
        });
    }

    public String getMTitle(){
        return FiltersTitleTextField.getText();
    }
    public String getAuthor(){
        return FiltersAuthorTextField.getText();
    }
    public String getFirstDatRelease(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy");//set the date format returned to have the day, month and year
        return format.format(FiltersFirstReleaseYearSpin.getValue());
    }
    public String getLastDateRelease(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy");//set the date format returned to have the day, month and year
        return format.format(FiltersLastReleaseYearSpin.getValue());
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
    public String getTextTag(){
        return Objects.requireNonNull(FiltersTagCB.getSelectedItem()).toString();
    }
    public String getTextSort(){
        String sort= "";
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Titre")){
            sort = "Title";
        }
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Auteur")){
            sort = "Author";
        }
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Année de sortie")){
            sort = "ReleaseYear";
        }
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Nombre de page")){
            sort = "NumberOP";
        }
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Note personelle")){
            sort = "NotePerso";
        }
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Note Babelio")){
            sort = "NoteBabelio";
        }
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Date de début de lecture")){
            sort = "StartReading";
        }
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Date de fin de lecture")){
            sort = "EndReading";
        }
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Temps moyen de lecture")){
            sort = "AvReadingTime";
        }
        if(Objects.requireNonNull(FiltersSortCB.getSelectedItem()).toString().equals("Nombre de lecture")){
            sort = "NumberReading";
        }
        return sort;
    }
    public boolean isAscending(){
        return FiltersorderCrossingRB.isSelected();
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

            fillSortCB();
            fillTagsCB();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void fillSortCB(){
        for (String s : Arrays.asList("Titre", "Auteur", "Année de sortie", "Nombre de page", "Note personelle", "Note Babelio", "Date de début de lecture", "Date de fin de lecture"
        ,"Temps moyen de lecture","Nombre de lecture")) {
            FiltersSortCB.addItem(s);
        }
    }
    public void fillTagsCB(){
        this.FiltersTagCB.addItem("");
        for (int i = 0; i<loadTags().getSizeTags(); i++){
            this.FiltersTagCB.addItem(loadTags().getTag(i).getTextTag());
        }
    }
}
