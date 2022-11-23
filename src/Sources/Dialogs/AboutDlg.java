package Sources.Dialogs;

import javax.swing.*;
import java.awt.*;

import static Sources.Common.getLogo;

public class AboutDlg extends JDialog {
    private JPanel contentPane;
    private JPanel LogoPanel;
    private JButton GitBtn;
    private JTextPane InfosAppTextPane;
    private JLabel nameAppLabel;

    public AboutDlg() {
        setContentPane(contentPane);
        setModal(true);

        Image img = getLogo(100,100);
        ImageIcon icon = new ImageIcon(img);
        JLabel imgLabel = new JLabel();
        imgLabel.setIcon(icon);
        LogoPanel.add(imgLabel);

        InfosAppTextPane.setBackground(contentPane.getBackground());
        infosApp();

        nameAppLabel.setFont(new Font("Arial", Font.BOLD, 14));
    }
    public void infosApp(){
        String text = "";
        text = text+"\n\n\n\n\n\n\n" +
                "Powered by : IntelliJ IDEA 2022.2 (Community Edition)\n" +
                "Copyright © 2001–2022 Tintin.corp.";
        InfosAppTextPane.setText(text);
    }
}
