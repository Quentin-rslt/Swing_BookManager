package Sources.Components;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MyManagerTable extends JTable {
    private final int m_height;
    private final int m_borderRadius;
    private final int m_numberOfVisibleLines;
    private final int m_thickness;
    private final Color m_borderColor;
    private final DefaultTableModel m_tableModel = new DefaultTableModel() {
        public boolean isCellEditable(int rowIndex, int colIndex) {
            return false;
        }
    };

    public MyManagerTable(int width, int height, int borderRadius, int numberOfVisibleLines, int thickness, Color boderColor){
        this.m_height=height;
        this.m_borderRadius=borderRadius;
        this.m_numberOfVisibleLines=numberOfVisibleLines;
        this.m_thickness=thickness;
        this.m_borderColor=boderColor;

        setPreferredScrollableViewportSize(new Dimension(width, height));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setModel(m_tableModel);

        initTable();
    }

    public void initTable(){
        AbstractBorder roundBrdMax = new MyManagerRoundBorderComponents(this.m_borderColor,this.m_thickness,this.m_borderRadius, 0,0,0);
        AbstractBorder roundBrdMin = new MyManagerRoundBorderComponents(this.m_borderColor,this.m_thickness,this.m_borderRadius, this.m_height-(this.getRowCount()*this.getRowHeight()),0,0);
        if(this.getRowCount()>this.m_numberOfVisibleLines) {
            this.setBorder(roundBrdMax);
        }
        else {
            this.setBorder(roundBrdMin);
        }
    }
    public void scrollFolowRow(int row){
        Rectangle cellRect = this.getCellRect(row, 0, false);
        this.scrollRectToVisible(cellRect);
    }

    public DefaultTableModel getTableModel(){
        return this.m_tableModel;
    }
}
