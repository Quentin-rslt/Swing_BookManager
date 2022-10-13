package Sources.Dialogs;

import Sources.RoundBorderCp;
import Sources.Tag;
import Sources.Tags;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class EditBookDlg extends JDialog {
    private JPanel contentPane;
    private JTextField BookTitleTextField;
    private JTextField BookAuthorTextField;
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
    private boolean m_isUpdate;
    private String m_oldTitle;
    private String m_oldAuthor;
    private String m_newURL;
    private boolean isValid = false;
    private JPopupMenu m_popup;
    private Tags m_tags = new Tags();

    public EditBookDlg(String title, String author) {
        setContentPane(contentPane);
        setModal(true);
        setOldTitle(title);
        setOldAuthor(author);

        m_popup = new JPopupMenu();//Create a popup menu to delete a reading an edit this reading
        File fileRemove = new File("Ressource/Icons/remove.png");
        String pathRemove = fileRemove.getAbsolutePath();
        Image imgRemove = Toolkit.getDefaultToolkit().getImage(pathRemove);
        imgRemove = imgRemove.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);
        JMenuItem cut = new JMenuItem("Supprimer", new ImageIcon(imgRemove));

        File fileEdit = new File("Ressource/Icons/edit.png");
        String pathEdit = fileEdit.getAbsolutePath();
        Image imgEdit = Toolkit.getDefaultToolkit().getImage(pathEdit);
        imgEdit = imgEdit.getScaledInstance(18,18,Image.SCALE_AREA_AVERAGING);
        JMenuItem edit = new JMenuItem("Modifier", new ImageIcon(imgEdit));
        m_popup.add(cut);
        m_popup.add(edit);

        loadDB(getOldTitle(), getOldAuthor());
        AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),2,25,0,0,3);
        BookSummaryTextPane.setBorder(roundBrd);
        JsPane.setBorder(null);

        BookOkBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Objects.equals(getNewTitle(), "") && !Objects.equals(getNewAuthor(), "") && !Objects.equals(getNewSummary(), "") && !Objects.equals(getNewURL(), "")){
                    setIsValid(true);
                    setVisible(false);
                    dispose();
                }
                else{
                    JFrame jFrame = new JFrame();
                    JOptionPane.showMessageDialog(jFrame, "Veuillez remplir tous les champs !");
                }
            }
        });
        BookBrowseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf= new JFileChooser();
                if (JFileChooser.APPROVE_OPTION == jf.showOpenDialog(BookPhotoPanel)){ //Opens the file panel to select an image
                    String path = jf.getSelectedFile().getPath();//Sélection image
                    setNewURL(path);
                    addImageToPanel(path);
                }
            }
        });
        BookSummaryTextPane.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                RoundBorderCp roundBrd = new RoundBorderCp(JsPane.getBackground(),2,25,0,0,3);
                BookSummaryTextPane.setBorder(roundBrd);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                RoundBorderCp roundBrd = new RoundBorderCp(contentPane.getBackground(),2,25,0,0,3);
                BookSummaryTextPane.setBorder(roundBrd);
            }
        });
        BookTagsCB.getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (!Objects.equals(BookTagsCB.getSelectedItem(), "")) {
                    if (evt.getKeyCode()== KeyEvent.VK_ENTER){
                        boolean tagFind = false;
                        int i = 0;
                        while(!tagFind && i<getTags().getSizeTags()){
                            if(Objects.equals(BookTagsCB.getSelectedItem(), getTags().getTag(i).getTextTag())){
                                JFrame jFrame = new JFrame();
                                JOptionPane.showMessageDialog(jFrame, "Vous avez déjà sélectionné ce tag !");
                                tagFind =true;
                            }
                            else i++;
                        }
                        if(!tagFind){
                            boolean isInCB = false;
                            ArrayList<String> listOfCb = new ArrayList<>();
                            for(int j = 0; j<BookTagsCB.getItemCount();j++){
                                listOfCb.add(BookTagsCB.getItemAt(j).toString());
                                if(BookTagsCB.getSelectedItem().toString().equals(BookTagsCB.getItemAt(j).toString())){
                                    isInCB=true;
                                }
                            }
                            //if the tag don't exist open editTagTagWindow to configure the color or the text
                            if(!isInCB){
                                EditTagDlg diag = new EditTagDlg(BookTagsCB.getSelectedItem().toString(), listOfCb, getTags());
                                diag.setTitle("Créer un tag");
                                diag.setLocationRelativeTo(null);
                                diag.setVisible(true);

                                if(diag.isValide()){
                                    Tag tag = new Tag(diag.getNewTextTag());
                                    tag.setColor(diag.getNewColorTag().getRGB());
                                    getTags().addTag(tag);

                                    for(int j=0; j<getTags().getSizeTags();j++){
                                        BookTagsPanel.add(getTags().getTag(j));
                                    }
                                    BookTagsPanel.updateUI();
                                }
                            }
                            else{
                                String sql = "SELECT Color FROM Tags WHERE Tag='"+BookTagsCB.getSelectedItem().toString()+ "'";
                                try {
                                    Class.forName("org.sqlite.JDBC");
                                    Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
                                    Statement statement = connection.createStatement();
                                    ResultSet tagsQry = statement.executeQuery(sql);

                                    getTags().createTag(Objects.requireNonNull(BookTagsCB.getSelectedItem()).toString());
                                    getTags().getTag(getTags().getSizeTags() - 1).setColor(tagsQry.getInt(1));
                                    for (int j = 0; j < getTags().getSizeTags(); j++) {
                                        BookTagsPanel.add(getTags().getTag(j));
                                        BookTagsCB.setSelectedIndex(0);
                                    }
                                    connection.close();
                                    statement.close();
                                }catch (Exception e ){
                                    System.exit(0);
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                    }
                }
                BookTagsPanel.updateUI();
            }
        });

        BookTagsPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(!e.getComponent().getComponentAt(e.getX(),e.getY()).equals(BookTagsPanel)){
                    if(e.getButton() == MouseEvent.BUTTON3) {
                        m_popup.show(BookTagsPanel, e.getX(), e.getY());//show a popup to edit the reading
                        m_popup.setInvoker(e.getComponent().getComponentAt(e.getX(),e.getY()));
                    }
                }
            }
        });
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Component[] componentList = BookTagsPanel.getComponents();
                for(int i = 0; i<getTags().getSizeTags();i++){
                    if(componentList[i]==m_popup.getInvoker()){
                        BookTagsPanel.remove(componentList[i]);
                        getTags().getTags().remove(i);
                    }
                }
                BookTagsPanel.updateUI();
            }
        });
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Component[] componentList = BookTagsPanel.getComponents();
                //j is the index of tags where we wan't to edit
                int j = 0;
                for(int i = 0; i<getTags().getSizeTags();i++){
                    if(componentList[i]==m_popup.getInvoker()){
                        j = i;
                    }
                }

                EditTagDlg diag = new EditTagDlg(getTags().getTag(j));
                diag.setTitle("Modifier le tag");
                diag.setLocationRelativeTo(null);
                diag.setVisible(true);

                if(diag.isValide()){
                    setTagIsUpdate(diag.isUpdate());
                    Tag tag = new Tag(diag.getNewTextTag());
                    tag.setColor(diag.getNewColorTag().getRGB());

                    getTags().addTag(tag);
                    BookTagsPanel.remove(componentList[j]);
                    getTags().getTags().remove(j);

                    for(int i=0; i<getTags().getSizeTags();i++){
                        BookTagsPanel.add(getTags().getTag(i));
                    }
                    BookTagsPanel.updateUI();
                }
            }
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
        return BookAuthorTextField.getText();
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
    public String getNewURL() {
        return m_newURL;
    }
    public boolean isValid() {
        return isValid;
    }
    private Connection connect() {
        Connection connection = null;
        String url = "jdbc:sqlite:BookManager.db";
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }
    public Tags getTags(){
        return this.m_tags;
    }
    public Tags loadTags(){
        Tags tags = new Tags();
        String sql = "SELECT Tag,Color FROM Tags";
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:BookManager.db");
            Statement statement = connection.createStatement();
            ResultSet tagsQry = statement.executeQuery(sql);
            while (tagsQry.next()){
                tags.createTag(tagsQry.getString(1));
                tags.getTag(tags.getSizeTags()-1).setColor(tagsQry.getInt(2));
            }
            connection.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return tags;
    }
    public int getIdReading(String title, String author) {
        int i =0;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM Reading WHERE Title='"+title+"' AND Author='"+author+ "'");
            i=rs.getInt(1);
            rs.close();
            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return i;
    }
    public int getIdBook(String title, String author) {
        int i =0;
        try (Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet idBook = statement.executeQuery("SELECT ID FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            i=idBook.getInt(1);
            idBook.close();
            conn.close();
            statement.close();
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return i;
    }
    public boolean getTagIsUpdate() {
        return m_isUpdate;
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
    public void setNewURL(String newURL) {
        this.m_newURL = newURL;
    }
    public void setIsValid(boolean valid) {
        isValid = valid;
    }
    public void loadDB(String title, String author){
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();
            //Title
            ResultSet titleQry = statement.executeQuery("SELECT Title FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            BookTitleTextField.setText(titleQry.getString(1));

            //Author
            ResultSet authorQry = statement.executeQuery("SELECT Author FROM Book WHERE Title='"+title+"' AND Author='"+author+ "'");
            BookAuthorTextField.setText(authorQry.getString(1));

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
            addImageToPanel(ImageQry.getString(1));
            setNewURL(ImageQry.getString(1));
            fillThemeCB();
            conn.close();
            statement.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    public void addImageToPanel(String path){//Apply to our panel an image with path
        Image img = Toolkit.getDefaultToolkit().getImage(path);
        img=img.getScaledInstance(266, 400, Image.SCALE_AREA_AVERAGING);
        ImageIcon icon = new ImageIcon(img);
        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(icon);

        BookPhotoPanel.updateUI();//reload the panel
        BookPhotoPanel.removeAll();
        BookPhotoPanel.add(imgLabel);
    }
    public void fillThemeCB(){
        this.BookTagsCB.addItem("");
        for (int i = 0; i<loadTags().getSizeTags(); i++){
            this.BookTagsCB.addItem(loadTags().getTag(i).getTextTag());
        }
    }
}
