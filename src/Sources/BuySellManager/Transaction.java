package Sources.BuySellManager;

import Sources.MyManagerTable;
import Sources.RoundBorderCp;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static Sources.CommonSQL.connect;

public class Transaction extends JDialog {
    private JPanel contentPane;
    private JTextField TransacFastSearchTxtF;
    private JLabel TransacCountLbl;
    private JButton TransacCriteriaBtn;
    private JButton TransacCancelCriteriaBtn;
    private JButton TransacManageTagsBtn;
    private JButton AddTransacBtn;
    private JScrollPane TransacsJSP;
    private JLabel TransacSellLbl;
    private JLabel TransacSellBuyLbl;
    private JLabel TransacNameLbl;
    private JPanel TransacPhotoPnl;
    private JLabel TransacReleaseLbl;
    private JLabel TransacDateLbl;
    private JLabel TransacPriceLbl;
    private JPanel BookTagsPanel;
    private JScrollPane TransacDescriptionJSP;
    private JTextPane TransacDescriptionTxtP;
    private JLabel TransacBuyLbl;
    private JLabel TransacTypeLbl;

    private MyManagerTable m_transacsTable;
    private String m_name;
    private String m_brand;
    private int m_rowSelected;

    public Transaction() {
        setContentPane(contentPane);
        setModal(true);
        iniComponents();
        initLoadComponents();
        initListeners();
    }

    /***************************** GETTER **************************************/
    public String getNameTransac(){
        return this.m_name;
    }
    public String getBrand(){
        return this.m_brand;
    }
    public int getRowSelected(){
        return  this.m_rowSelected;
    }

    /***************************** SETTER **************************************/
    public void setNameTransac(String name){
        this.m_name = name;
    }
    public void setBrand(String brand){
        this.m_brand = brand;
    }
    public void setRowSelected(int rowSelected){
        this.m_rowSelected = rowSelected;
    }

    public void iniComponents(){
        //Init transaction table
        initTransactionTable();
        //init transaction description
        AbstractBorder roundBrd = new RoundBorderCp(contentPane.getBackground(),3,30,0,0,20);
        this.TransacDescriptionTxtP.setBorder(roundBrd);
        this.TransacDescriptionTxtP.setFont(new Font("Arial", Font.BOLD, 13));
        this.TransacDescriptionJSP.setBorder(null);
    }
    public void initTransactionTable(){
        this.m_transacsTable = new MyManagerTable(500,623,30, 15, 1, contentPane.getBackground());
        this.TransacsJSP.getViewport().add(this.m_transacsTable);
        fillTransactionTable();
    }
    public void fillTransactionTable(){
        this.m_transacsTable.getTableModel().setColumnCount(0);
        try(Connection conn = connect()) {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Transactions;");

            while (rs.next()) {//Fill in the table of the list of transactions
                String name = rs.getString("Name");//Retrieve the name
                String brand = rs.getString("Brand");//Retrieve the brand

                String[] header = {"Nom","Marque"};
                Object[] data = {name, brand};

                this.m_transacsTable.getTableModel().setColumnIdentifiers(header);//Create the header
                this.m_transacsTable.getTableModel().addRow(data);//add to tablemodel the data
            }
            this.m_transacsTable.initTable();

            rs.close();
            conn.close();
            statement.close();
        } catch (Exception e){
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement tableau transaction impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public void initLoadComponents(){
        if(this.m_transacsTable.getRowCount() != 0) {//Vérif if the table is not empty; when starting the app, load and focus on the first transaction of the table
            setNameTransac(this.m_transacsTable.getValueAt(0, 0).toString());
            setBrand(this.m_transacsTable.getValueAt(0, 1).toString());
            setRowSelected(0);
            loadComponents(getNameTransac(), getBrand());
        }
    }
    public void loadComponents(String name, String brand){
        this.m_transacsTable.setRowSelectionInterval(getRowSelected(), getRowSelected());
        try(Connection conn = connect()){
            Statement statement = conn.createStatement();
            Statement sumSellStatement = conn.createStatement();
            Statement sumBuyStatement = conn.createStatement();

            ResultSet transactionsQry = statement.executeQuery("SELECT * FROM Transactions WHERE Name='"+name+"' AND Brand='"+brand+ "'");
            ResultSet sumSellQry = sumSellStatement.executeQuery("SELECT SUM(Price) FROM Transactions WHERE IsABuy = 'false'");
            ResultSet sumBuyQry = sumBuyStatement.executeQuery("SELECT SUM(Price) FROM Transactions WHERE IsABuy = 'true'");

            //Sum and ratio
            double sumSell = sumSellQry.getDouble(1);
            double sumBuy = sumBuyQry.getDouble(1);
            double ratioSellBuy = sumSell/sumBuy;

            //Name
            TransacNameLbl.setText(name);
            //Type of transaction
            boolean isABuy = transactionsQry.getBoolean(2);
            if(isABuy){
                TransacTypeLbl.setText("Type de la transaction: Achat");
            }else {
                TransacTypeLbl.setText("Type de la transaction: Vente");
            }
            //Release Year
            TransacReleaseLbl.setText("Année de sortie : "+transactionsQry.getString(6));
            //Date transaction
            TransacDateLbl.setText("Date de la transaction : "+transactionsQry.getString(7));
            //Price transaction
            TransacPriceLbl.setText("Prix de la transaction : "+transactionsQry.getString(8));
            //Description
            TransacDescriptionTxtP.setText(transactionsQry.getString(9));
            //Count transactions
            TransacCountLbl.setText("Transactions : "+this.m_transacsTable.getRowCount());
            //Sum price sell
            TransacSellLbl.setText("Vente total : "+sumSell);
            //Sum price buy
            TransacBuyLbl.setText("Achat total : "+sumBuy);
            //Ratio Sell/Buy
            TransacSellBuyLbl.setText("Vente/Achat : "+ratioSellBuy);

        }catch (Exception e ){
            JFrame jf = new JFrame();
            JOptionPane.showMessageDialog(jf, e.getMessage(), "Chargement composants impossible", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.getMessage());
        }
    }
    public void initListeners(){
        this.m_transacsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                super.mouseClicked(evt);
                m_transacsTable.requestFocusInWindow();
                int newLine= m_transacsTable.rowAtPoint(evt.getPoint());

                if(newLine != getRowSelected()) {
                    setRowSelected(m_transacsTable.rowAtPoint(evt.getPoint()));
                    setNameTransac(m_transacsTable.getValueAt(getRowSelected(), 0).toString());
                    setBrand(m_transacsTable.getValueAt(getRowSelected(), 1).toString());
                    loadComponents(getNameTransac(), getBrand());
                }
            }
        });
    }
}
