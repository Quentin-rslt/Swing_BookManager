package Sources.BookManager.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static Sources.BookManager.CommonBookManager.getLogo;

public class AboutDlg extends JDialog {
    private JPanel contentPane;
    private JPanel LogoPanel;
    private JButton GitBtn;
    private JLabel nameAppLabel;
    private JLabel LinkFlatLaf;
    private JLabel LinkIntelIJ;
    private JTextPane InfosFlatLafTextPane;
    private JTextPane InfosIntelIJTextPane;


    public AboutDlg() {
        setContentPane(contentPane);
        setModal(true);
        String strFlatLafLink = "www.formdev.com";
        LinkFlatLaf.setText(strFlatLafLink);
        String strIntelIJLink = "www.jetbrains.com";
        LinkIntelIJ.setText(strIntelIJLink);
        Image img = getLogo(100,100);
        ImageIcon icon = new ImageIcon(img);
        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(icon);
        LogoPanel.add(imgLabel);

        InfosFlatLafTextPane.setBackground(contentPane.getBackground());
        InfosFlatLafTextPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        InfosIntelIJTextPane.setBackground(contentPane.getBackground());
        InfosIntelIJTextPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        infosApp();

        nameAppLabel.setFont(new Font("Arial", Font.BOLD, 20));

        LinkFlatLaf.setForeground(GitBtn.getBackground());
        LinkFlatLaf.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.formdev.com/flatlaf/"));
                }
                catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e){
                LinkFlatLaf.setForeground(new Color(197,62,62));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                LinkFlatLaf.setForeground(GitBtn.getBackground());
            }
        });
        LinkIntelIJ.setForeground(GitBtn.getBackground());
        LinkIntelIJ.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.jetbrains.com/fr-fr/idea/"));
                }
                catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e){
                LinkIntelIJ.setForeground(new Color(197,62,62));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                LinkIntelIJ.setForeground(GitBtn.getBackground());
            }
        });

        GitBtn.addActionListener( e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/Quentin-rslt/BookManager"));
            }
            catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        });
    }
    public void infosApp(){
        String textIntelIJ = "";
        textIntelIJ = textIntelIJ+"" +
                "\nPowered by : IntelliJ IDEA 2022.2 (Community Edition)\n" +
                "Copyright © 2000–2022 JetBrains s.r.o.";
        String textFlatLaf = "";
        textFlatLaf = textFlatLaf+"" +
                "\n" +
                "Edits theme with FlatLaf Theme Editor\n" +
                "Copyright © 2019-2022 FormDev Software GmbH";

        InfosIntelIJTextPane.setText(textIntelIJ);
        InfosFlatLafTextPane.setText(textFlatLaf);
    }
}
