package Sources.Dialogs;

import Sources.RoundBorderCp;
import Sources.Tag;
import Sources.Tags;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static Sources.Common.*;
import static Sources.CommonSQL.*;
import static Sources.Dialogs.OpenDialog.openEditTagDlg;

public class EditBookDlg extends JDialog {
    private JPanel contentPane;
    private JTextField BookTitleTextField;
    private JSpinner BookPersonalNoteSpin;
    private JButton BookBrowseBtn;
    private JSpinner BookNoteBblSpin;
    private JTextPane BookSummaryTextPane;
    private JSpinner BookReleaseYearSpin;
    private JSpinner BookNumberOPSpin;
    private JButton BookOkBtn;
    private JPanel BookPhotoPanel;
    private JScrollPane JsPane;
    private JComboBox BookTagsCB;
    private JPanel BookTagsPanel;
    private JComboBox BookAuthorCB;
    private boolean m_isUpdate;
    private String m_oldTitle;
    private String m_oldAuthor;
    private boolean isValid = false;
    final JPopupMenu m_popup;
    Tags m_tags = new Tags();

    public EditBookDlg(String title, String author) {
        setContentPane(contentPane);
        setModal(true);
        setOldTitle(title);
        setOldAuthor(author);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(getImageCut()));
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(getImageEdit()));
        m_popup.add(cut);
        m_popup.add(edit);

        loadDB(getOldTitle(), getOldAuthor());
        AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),2,25,18,0,20);
        BookSummaryTextPane.setBorder(roundBrd);
        JsPane.setBorder(null);

        BookOkBtn.addActionListener((ActionEvent e) ->{
                if (!Objects.equals(getNewTitle(), "") && !Objects.equals(getNewAuthor(), "") && !Objects.equals(getNewSummary(), "")){
                    deleteImageResource(getOldTitle(), getOldAuthor());
                    setIsValid(true);
                    updateImageToResource(getOldTitle(), getOldAuthor());
                    setVisible(false);
                    dispose();
                }
                else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !");
                }
            });
        BookBrowseBtn.addActionListener((ActionEvent e)-> selectImageOfBook(BookPhotoPanel));
        BookSummaryTextPane.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                RoundBorderCp roundBrd = new RoundBorderCp(JsPane.getBackground(),2,25,18,0,20);
                BookSummaryTextPane.setBorder(roundBrd);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                RoundBorderCp roundBrd = new RoundBorderCp(contentPane.getBackground(),2,25,18,0,20);
                BookSummaryTextPane.setBorder(roundBrd);
            }
        });
        initListenerTag(getTags(), m_popup, BookTagsPanel);
        BookTagsCB.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
            if (!Objects.equals(BookTagsCB.getSelectedItem(), "")) {
                if (evt.getKeyCode()== KeyEvent.VK_ENTER){
                    fillPaneTags(getTags(), BookTagsPanel, BookTagsCB, true);
                }
            }
                initListenerTag(getTags(), m_popup, BookTagsPanel);
            BookTagsPanel.updateUI();
            }
        });
        BookTagsCB.getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            BookTagsCB.showPopup();
            BookTagsCB.setSelectedIndex(0);
            }
        });
        BookAuthorCB.getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            BookAuthorCB.showPopup();
            BookAuthorCB.setSelectedIndex(getRowAuthorCB(getOldAuthor()));
            }
        });

        cut.addActionListener((ActionEvent evt) ->{
            Component[] componentList = BookTagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    BookTagsPanel.remove(componentList[i]);
                    getTags().removeTag(i);
                    break;
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, BookTagsPanel);
            BookTagsPanel.updateUI();
        });
        edit.addActionListener((ActionEvent evt)-> {
            Component[] componentList = BookTagsPanel.getComponents();
            int i = 0;
            while (i<getTags().getSizeTags()) {
                if(componentList[i]==m_popup.getInvoker()){
                    EditTagDlg diag = openEditTagDlg(getTags().getTag(i));

                    if(diag.isValide()){
                        setTagIsUpdate(diag.isUpdate());
                        Tag tag = new Tag(diag.getNewTextTag());
                        tag.setColor(diag.getNewColorTag().getRGB());

                        getTags().addTag(tag);
                        BookTagsPanel.remove(componentList[i]);
                        getTags().getTags().remove(i);

                        for(int j=0; j<getTags().getSizeTags();j++){
                            BookTagsPanel.add(getTags().getTag(j));
                        }
                        BookTagsPanel.updateUI();
                    }
                    break;
                }
                i++;
            }
            initListenerTag(getTags(), m_popup, BookTagsPanel);
            BookTagsPanel.updateUI();
        });
    }

    public String getOldTitle() {
        return m_oldTitle;
    }
    public String getOldAuthor() {
        return m_oldAuthor;
    }
    public String getNewTitle() {
        return BookTitleTextField.getText();
    }
    public String getNewAuthor() {
        return Objects.requireNonNull(BookAuthorCB.getSelectedItem()).toString();
    }
    public String getNewReleaseyear() {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy");//set the date format returned to have just the year release
        return formater.format(BookReleaseYearSpin.getValue());
    }
    public String getNewNumberPage() {
        return BookNumberOPSpin.getValue().toString();
    }
    public String getNewPersonnalNote() {
        return BookPersonalNoteSpin.getValue().toString();
    }
    public String getNewBBLNote() {
        return BookNoteBblSpin.getValue().toString();
    }
    public String getNewSummary() {
        return BookSummaryTextPane.getText();
    }
    public boolean isValid() {
        return isValid;
    }
    public Tags getTags(){
        return this.m_tags;
    }
    public boolean getTagIsUpdate() {
        return m_isUpdate;
    }
    public int getRowAuthorCB(String author){
        int row = 0;

        while(row<BookAuthorCB.getItemCount()){
            if(author.equals(BookAuthorCB.getItemAt(row))) {
                break;
            }
            else{
                row++;
            }
        }
        return row;
    }

    public void setTagIsUpdate(boolean m_isUpdate) {
        this.m_isUpdate = m_isUpdate;
    }
    public void setOldTitle(String oldTitle) {
        this.m_oldTitle = oldTitle;
    }
    public void setOldAuthor(String oldAuthor) {
        this.m_oldAuthor = oldAuthor;
    }
    public void setIsValid(boolean valid) {
        isValid = valid;
    }
    public void loadDB(String title, String author){
        fillAuthorCB(BookAuthorCB);
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();
            //Title
            if(title.contains("'")){
                title= title.replace("'", "''");
            }
            if(title.contains("''''")){
                String newTitle = title.replace("''''", "'");
                BookTitleTextField.setText(newTitle);
            }
            else{
                BookTitleTextField.setText(title);
            }
            //Author
            ResultSet authorQry = statement.executeQuery("SELECT Author FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            BookAuthorCB.setSelectedItem(authorQry.getString(1));
            //Release year
            ResultSet NumberOPQry = statement.executeQuery("SELECT ReleaseYear FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            Date dateRelease = new Date();
            SpinnerDateModel BookReleaseDateSpinModel = new SpinnerDateModel(new SimpleDateFormat("yyyy").parse(NumberOPQry.getString(1)),null,dateRelease,Calendar.YEAR);//Create a spinner date, to correctly select a date
            BookReleaseYearSpin.setModel(BookReleaseDateSpinModel);
            JSpinner.DateEditor Year = new JSpinner.DateEditor(BookReleaseYearSpin,"yyyy");//set the display of the JSpinner of release date
            BookReleaseYearSpin.setEditor(Year);

            //Number of page
            ResultSet ReleaseYearQry = statement.executeQuery("SELECT NumberOP FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            SpinnerModel BookNumberSM = new SpinnerNumberModel(ReleaseYearQry.getInt(1), 0, 3248, 1);
            BookNumberOPSpin.setModel(BookNumberSM);

            //Note on babelio
            ResultSet NoteBBQry = statement.executeQuery("SELECT NoteBabelio FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            SpinnerModel BookNoteBbblSM = new SpinnerNumberModel(NoteBBQry.getDouble(1), 0, 5, 0.01);
            BookNoteBblSpin.setModel(BookNoteBbblSM);

            //Tags
            ResultSet tagsQry = statement.executeQuery("SELECT Tag,Color FROM Tags JOIN Tagging on Tags.ID=Tagging.IdTag " +
                    "WHERE Tagging.IdBook='"+getIdBook(title, author)+"'");
            BookTagsPanel.removeAll();
            while (tagsQry.next()){
                getTags().createTag(tagsQry.getString(1));
                getTags().getTag(tagsQry.getRow()-1).setColor(tagsQry.getInt(2));
                for(int i=0; i<getTags().getSizeTags();i++) {
                    BookTagsPanel.add(getTags().getTag(i));
                }
            }
            BookTagsPanel.updateUI();

            //Personal note
            ResultSet NotePersoQry = statement.executeQuery("SELECT NotePerso FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            SpinnerModel BookPersonalNotelSM = new SpinnerNumberModel(NotePersoQry.getDouble(1), 0, 5, 0.5);//Set a default and max value for spinner Note
            BookPersonalNoteSpin.setModel(BookPersonalNotelSM);

            //Summary
            ResultSet SummaryQry = statement.executeQuery("SELECT Summary FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            BookSummaryTextPane.setText(SummaryQry.getString(1));

            //Image
            ResultSet ImageQry = statement.executeQuery("SELECT Image FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            addImageToPanel(ImageQry.getString(1),BookPhotoPanel);
            setNameOfImage(ImageQry.getString(1));
            fillThemeCB();
            conn.close();
            statement.close();
        } catch ( SQLException e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public void fillThemeCB(){
        this.BookTagsCB.addItem("");
        for (int i = 0; i<loadTags().getSizeTags(); i++){
            this.BookTagsCB.addItem(loadTags().getTag(i).getTextTag());
        }
    }
}
