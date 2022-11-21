package Sources.Dialogs;

import javax.swing.*;

public class ParametersDlg extends JDialog {
    private JPanel contentPane;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JButton créerUnThèmeButton;
    private JComboBox comboBox3;

    public ParametersDlg() {
        setContentPane(contentPane);
        setModal(true);
    }
}
