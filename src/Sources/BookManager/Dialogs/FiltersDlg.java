package Sources.BookManager.Dialogs;

import Sources.Tags;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static Sources.BookManager.CommonBookManager.*;
import static Sources.BookManager.CommonBookManagerSQL.*;

public class FiltersDlg extends JDialog {
    private JPanel contentPane;
    private JButton FiltersOkBtn;
    private JButton FiltersResetBtn;
    private JTextField FiltersTitleTextField;
    private JTextField FiltersAuthorTextField;
    private JSpinner FiltersFirstReleaseYearSpin;
    private JSpinner FiltersLastReleaseYearSpin;
    private JSpinner FiltersFirstNoteSpin;
    private JSpinner FiltersLastNoteSpin;
    private JRadioButton FiltersorderCrossingRB;
    private JComboBox FiltersSortCB;
    private JComboBox FiltersTagCB;
    private JSpinner FiltersFirstNumberOPSpin;
    private JSpinner FiltersSecNumberOPSpin;
    private JSpinner FiltersFirstNumberORSpin;
    private JSpinner FiltersSecNumberORSpin;
    private JSpinner FiltersFirstAvTSpin;
    private JSpinner FiltersSecAvTSpin;
    private JSpinner FiltersFirstNoteBBSpin;
    private JSpinner FiltersSecNoteBBSpin;
    private JSpinner FiltersFirstStartRSpin;
    private JSpinner FiltersSecStartRSpin;
    private JSpinner FiltersFirstEndRSpin;
    private JSpinner FiltersSecEndRSpin;
    private JCheckBox UnknownReadDateChecbox;
    private JCheckBox NotDoneReadChecbox;
    private JCheckBox IsFilteredCheckBox;
    private JLabel ReadDateLabel;
    private JPanel FiltersTagsPanel;

    private boolean m_isValid;
    private Tags m_tags;
    final JPopupMenu m_popup;

    public FiltersDlg() {
        this.m_tags = new Tags();
        setContentPane(contentPane);
        setModal(true);
        initComponents();

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getLogo("remove.png")));
        m_popup.add(cut);

        FiltersOkBtn.addActionListener((ActionEvent e)-> {
            setIsValid(true);
            setVisible(false);
            dispose();
        });
        FiltersResetBtn.addActionListener((ActionEvent e)-> resetFilters());
        UnknownReadDateChecbox.addActionListener((ActionEvent e) -> {
            if (UnknownReadDateChecbox.isSelected()){
                NotDoneReadChecbox.setSelected(false);
                FiltersFirstStartRSpin.setEnabled(false);
                FiltersSecStartRSpin.setEnabled(false);

                FiltersFirstEndRSpin.setEnabled(false);
                FiltersSecEndRSpin.setEnabled(false);
            }
            else{
                FiltersFirstStartRSpin.setEnabled(true);
                FiltersSecStartRSpin.setEnabled(true);

                FiltersFirstEndRSpin.setEnabled(true);
                FiltersSecEndRSpin.setEnabled(true);
            }
        });
        NotDoneReadChecbox.addActionListener((ActionEvent e) -> {
            if (NotDoneReadChecbox.isSelected()){
                UnknownReadDateChecbox.setSelected(false);
                FiltersFirstStartRSpin.setEnabled(true);
                FiltersSecStartRSpin.setEnabled(true);

                FiltersFirstEndRSpin.setEnabled(false);
                FiltersSecEndRSpin.setEnabled(false);
            }
            else{
                FiltersFirstEndRSpin.setEnabled(true);
                FiltersSecEndRSpin.setEnabled(true);
            }
        });
        IsFilteredCheckBox.addActionListener(e -> {
            if(isFiltered()){
                FiltersFirstStartRSpin.setEnabled(true);
                FiltersSecStartRSpin.setEnabled(true);
                FiltersFirstEndRSpin.setEnabled(true);
                FiltersSecEndRSpin.setEnabled(true);
                NotDoneReadChecbox.setEnabled(true);
                UnknownReadDateChecbox.setEnabled(true);
                ReadDateLabel.setEnabled(true);
                UnknownReadDateChecbox.setSelected(false);
                NotDoneReadChecbox.setSelected(false);
            }
            else{
                FiltersFirstStartRSpin.setEnabled(false);
                FiltersSecStartRSpin.setEnabled(false);
                FiltersFirstEndRSpin.setEnabled(false);
                FiltersSecEndRSpin.setEnabled(false);
                NotDoneReadChecbox.setEnabled(false);
                UnknownReadDateChecbox.setEnabled(false);
                ReadDateLabel.setEnabled(false);
                UnknownReadDateChecbox.setSelected(false);
                NotDoneReadChecbox.setSelected(false);
            }
        });
        FiltersTagCB.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
            if (!Objects.equals(FiltersTagCB.getEditor().getItem().toString(), "")) {
                if (evt.getKeyCode()== KeyEvent.VK_ENTER){
                    fillPaneTags(getTags(), FiltersTagsPanel, FiltersTagCB, false);
                } else{
                    FiltersTagCB.showPopup();
                    for (int i=0; i<FiltersTagCB.getItemCount();i++){
                        if(Objects.equals(FiltersTagCB.getEditor().getItem().toString(), FiltersTagCB.getItemAt(i))){
                            FiltersTagCB.setSelectedIndex(i);
                            FiltersTagCB.showPopup();
                        }
                    }
                }
            }
            initListenerTag(getTags(), m_popup, FiltersTagsPanel);
            FiltersTagsPanel.updateUI();
            }
        });
        FiltersTagCB.getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            FiltersTagCB.showPopup();
            FiltersTagCB.setSelectedIndex(0);
            }
        });
        FiltersTagsPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (!e.getComponent().getComponentAt(e.getX(), e.getY()).equals(FiltersTagsPanel)) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    m_popup.show(FiltersTagsPanel, e.getX(), e.getY());//show a popup to edit the reading
                    m_popup.setInvoker(e.getComponent().getComponentAt(e.getX(), e.getY()));
                }
                initListenerTag(getTags(), m_popup, FiltersTagsPanel);
                FiltersTagsPanel.updateUI();
            }
            }
        });
        cut.addActionListener((ActionEvent evt)-> {
            Component[] componentList = FiltersTagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    FiltersTagsPanel.remove(componentList[i]);
                    getTags().removeTag(i);

                    for(int j=0; j<getTags().getSizeTags();j++){
                        FiltersTagsPanel.add(getTags().getTag(j));
                    }
                    break;
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, FiltersTagsPanel);
            FiltersTagsPanel.updateUI();
        });
    }
    public Tags getTags(){
        return this.m_tags;
    }
    public String getFilterTitle(){
        return FiltersTitleTextField.getText();
    }
    public String getFilterAuthor(){
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
    public String getFirstNumberOP(){
        return FiltersFirstNumberOPSpin.getValue().toString();
    }
    public String getLastNumberOP(){
        return FiltersSecNumberOPSpin.getValue().toString();
    }
    public String getFirstNumberOR(){
        return FiltersFirstNumberORSpin.getValue().toString();
    }
    public String getLastNumberOR(){
        return FiltersSecNumberORSpin.getValue().toString();
    }
    public String getFirstAvTime(){
        return FiltersFirstAvTSpin.getValue().toString();
    }
    public String getLastAvTime(){
        return FiltersSecAvTSpin.getValue().toString();
    }
    public String getFirstNoteBB(){
        return FiltersFirstNoteBBSpin.getValue().toString();
    }
    public String getLastNoteBB(){
        return FiltersSecNoteBBSpin.getValue().toString();
    }
    public String getFirstNote(){
        return FiltersFirstNoteSpin.getValue().toString();
    }
    public String getLastNote(){
        return FiltersLastNoteSpin.getValue().toString();
    }
    public String getFirstStartDate(){
        String str;
        if(UnknownReadDateChecbox.isSelected())
            str = "Inconnu";
        else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");//set the date format returned to have the day, month and year
            str = format.format(FiltersFirstStartRSpin.getValue());
        }
        return str;
    }
    public String getLastStartDate(){
        String str;
        if(UnknownReadDateChecbox.isSelected())
            str = "Inconnu";
        else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");//set the date format returned to have the day, month and year
            str =  format.format(FiltersSecStartRSpin.getValue());
        }
        return str;
    }
    public String getFirstEndDate(){
        String str;
        if(UnknownReadDateChecbox.isSelected())
            str = "Inconnu";
        else if (NotDoneReadChecbox.isSelected()) {
            str = "Pas fini";
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");//set the date format returned to have the day, month and year
            str=  format.format(FiltersFirstEndRSpin.getValue());
        }
        return str;
    }
    public String getLastEndDate(){
        String str;
        if(UnknownReadDateChecbox.isSelected())
            str = "Inconnu";
        else if (NotDoneReadChecbox.isSelected()) {
            str = "Pas fini";
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");//set the date format returned to have the day, month and year
            str=  format.format(FiltersSecEndRSpin.getValue());
        }
        return str;
    }
    public boolean isFiltered(){
        return this.IsFilteredCheckBox.isSelected();
    }

    public boolean getIsValid() {
        return m_isValid;
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

    public void setTags(Tags tags){
        this.m_tags = tags;
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

            SpinnerModel FirstNoteBB = new SpinnerNumberModel(0, 0, 5, 0.01);
            SpinnerModel LastNoteBB = new SpinnerNumberModel(5, 0, 5, 0.01);
            FiltersFirstNoteBBSpin.setModel(FirstNoteBB);
            FiltersSecNoteBBSpin.setModel(LastNoteBB);

            Date FirstDateStartReading = new Date();
            SpinnerDateModel FirstStartReadSpinDate = new SpinnerDateModel(new SimpleDateFormat("yyyy/MM/dd").parse("0001/01/01"), null,FirstDateStartReading, Calendar.YEAR);
            FiltersFirstStartRSpin.setModel(FirstStartReadSpinDate);
            JSpinner.DateEditor FirstStart = new JSpinner.DateEditor(FiltersFirstStartRSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
            FiltersFirstStartRSpin.setEditor(FirstStart);

            Date LastDateStartReading = new Date();
            SpinnerDateModel LastStartReadSpinDate = new SpinnerDateModel(LastDateStartReading, null,LastDateStartReading, Calendar.YEAR);
            FiltersSecStartRSpin.setModel(LastStartReadSpinDate);
            JSpinner.DateEditor LastStart = new JSpinner.DateEditor(FiltersSecStartRSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
            FiltersSecStartRSpin.setEditor(LastStart);

            Date FirstDateEndReading = new Date();
            SpinnerDateModel FirstEndReadSpinDate = new SpinnerDateModel(new SimpleDateFormat("yyyy/MM/dd").parse("0001/01/01"), null,FirstDateEndReading, Calendar.YEAR);
            FiltersFirstEndRSpin.setModel(FirstEndReadSpinDate);
            JSpinner.DateEditor FirstEnd = new JSpinner.DateEditor(FiltersFirstEndRSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
            FiltersFirstEndRSpin.setEditor(FirstEnd);

            Date LastDateEndReading = new Date();
            SpinnerDateModel EndReadSpinDate = new SpinnerDateModel(LastDateEndReading, null,LastDateEndReading, Calendar.YEAR);
            FiltersSecEndRSpin.setModel(EndReadSpinDate);
            JSpinner.DateEditor LastEnd = new JSpinner.DateEditor(FiltersSecEndRSpin,"yyyy/MM/dd");//set the display of the JSpinner reading book date
            FiltersSecEndRSpin.setEditor(LastEnd);

            SpinnerModel LastNumberOP = new SpinnerNumberModel(9999999, 0,9999999 , 1);
            FiltersSecNumberOPSpin.setModel(LastNumberOP);
            SpinnerModel LastNumberOR = new SpinnerNumberModel(9999999, 0, 9999999, 1);
            FiltersSecNumberORSpin.setModel(LastNumberOR);
            SpinnerModel LastAvTime = new SpinnerNumberModel(9999999, 0, 9999999, 1);
            FiltersSecAvTSpin.setModel(LastAvTime);

            SpinnerModel FirstNumberOP = new SpinnerNumberModel(0, 0,9999999 , 1);
            FiltersFirstNumberOPSpin.setModel(FirstNumberOP);
            SpinnerModel FirstNumberOR = new SpinnerNumberModel(0, 0, 9999999, 1);
            FiltersFirstNumberORSpin.setModel(FirstNumberOR);
            SpinnerModel FirstAvTime = new SpinnerNumberModel(0, 0, 9999999, 1);
            FiltersFirstAvTSpin.setModel(FirstAvTime);

            IsFilteredCheckBox.setSelected(false);
            FiltersFirstStartRSpin.setEnabled(false);
            FiltersSecStartRSpin.setEnabled(false);
            FiltersFirstEndRSpin.setEnabled(false);
            FiltersSecEndRSpin.setEnabled(false);
            NotDoneReadChecbox.setEnabled(false);
            UnknownReadDateChecbox.setEnabled(false);
            ReadDateLabel.setEnabled(false);

            fillSortCB();
            fillTagsCB();
        }catch (Exception e){
            System.out.println(e.getMessage());
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement composants impossible", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void resetFilters(){
        initComponents();
        FiltersorderCrossingRB.setSelected(true);
        FiltersTagCB.setSelectedIndex(0);
        FiltersSortCB.setSelectedIndex(0);
        FiltersTitleTextField.setText("");
        FiltersAuthorTextField.setText("");
        UnknownReadDateChecbox.setSelected(false);
        NotDoneReadChecbox.setSelected(false);
        setTags(new Tags());
        FiltersTagsPanel.removeAll();
        contentPane.updateUI();
    }
    @SuppressWarnings("unchecked")
    public void fillSortCB(){
        FiltersSortCB.removeAllItems();
        for (String s : Arrays.asList("Titre", "Auteur", "Année de sortie", "Nombre de page","Nombre de lecture","Temps moyen de lecture", "Note Babelio", "Note personelle", "Date de début de lecture", "Date de fin de lecture")) {
            FiltersSortCB.addItem(s);
        }
    }
    @SuppressWarnings("unchecked")
    public void fillTagsCB(){
        FiltersTagCB.removeAllItems();
        this.FiltersTagCB.addItem("");
        for (int i = 0; i<loadTags().getSizeTags(); i++){
            this.FiltersTagCB.addItem(loadTags().getTag(i).getTextTag());
        }
    }
}
