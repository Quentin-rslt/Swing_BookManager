package Sources.BuySellManager;

import Sources.RoundBorderCp;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

public class Transaction extends JDialog {
    private JPanel contentPane;
    private JTextField TransactionFastSearch;
    private JLabel CountTransactionLbl;
    private JTable TransactionTable;
    private JButton FiltersTransactionBtn;
    private JButton CancelFiltersBtn;
    private JButton BuySellManageTagsBtn;
    private JButton AddTransactionBtn;
    private JLabel NameLabel;
    private JPanel TransationPhotoPanel;
    private JPanel TransationTagsPanel;
    private JLabel TransactionReleaseLbl;
    private JLabel TransactionDateLbl;
    private JLabel TransactionPrice;
    private JScrollPane JSpane;
    private JTextPane TransactionDescriptionTxtP;
    private JLabel TotalSellLbl;
    private JLabel TotalBuyLbl;
    private JLabel RatioSellBuyLbl;

    public Transaction() {
        setContentPane(contentPane);
        setModal(true);
        iniComponents();
        fillTransactionTable();
        loadComponents();
    }

    public void iniComponents(){
        AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),3,30,0,0,20);
        TransactionDescriptionTxtP.setBorder(roundBrd);
        TransactionDescriptionTxtP.setFont(new Font("Arial", Font.BOLD, 13));
    }
    public void fillTransactionTable(){

    }
    public void loadComponents(){

    }
}
