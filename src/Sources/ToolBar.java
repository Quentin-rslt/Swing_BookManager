package Sources;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToolBar extends JToolBar {
    public static JToolBar createToolBar(JToolBar toolBar) {

        // La barre d'outils à proprement parler

        JButton btnNew = new JButton( "fdgdfgdfg" );
        btnNew.setToolTipText( "New File (CTRL+N)" );
        toolBar.add( btnNew );

        JButton btnSave = new JButton( "fdgdfgdfg" );
        btnSave.setToolTipText( "Save (CTRL+S)" );
        toolBar.add( btnSave );

        JButton btnSaveAs = new JButton( "fdgdfgdfg" );
        btnSaveAs.setToolTipText( "Save As..." );
        toolBar.add( btnSaveAs );

        toolBar.addSeparator();

        JButton btnCopy = new JButton( new ImageIcon( "icons/copy.png") );
        btnCopy.setToolTipText( "Copy (CTRL+C)" );
        toolBar.add( btnCopy );

        JButton btnCut = new JButton( new ImageIcon( "icons/cut.png") );
        btnCut.setToolTipText( "Cut (CTRL+X)" );
        toolBar.add( btnCut );

        JButton btnPaste = new JButton( new ImageIcon( "icons/paste.png") );
        btnPaste.setToolTipText( "Paste (CTRL+V)" );
        toolBar.add( btnPaste );

        toolBar.addSeparator();

        JButton btnExit = new JButton( new ImageIcon( "icons/exit.png") );
        btnExit.setToolTipText( "Exit (ALT+F4)" );
        toolBar.add( btnExit );

        toolBar.addSeparator();

        return toolBar;
    }
}
