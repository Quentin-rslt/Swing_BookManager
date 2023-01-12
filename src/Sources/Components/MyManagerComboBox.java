package Sources.Components;

import javax.swing.*;

public class MyManagerComboBox extends JComboBox {
    public MyManagerComboBox(boolean isEditable){
        super();
        this.setEditable(isEditable);
    }

    public void searchItemCB() {
        this.showPopup();
        String editorText = this.getEditor().getItem().toString().toLowerCase();
        int i = 1;
        boolean itemIsFind = false;
        while (i < this.getItemCount() && !itemIsFind) {
            String itemText = this.getItemAt(i).toString().toLowerCase();
            StringBuilder testItemText = new StringBuilder();

            if (editorText.length() < itemText.length()) {
                for (int y = 0; y < editorText.length(); y++) {
                    testItemText.append(itemText.charAt(y));
                }
                if (editorText.equals(testItemText.toString())) {
                    this.setSelectedItem(this.getItemAt(i));
                    this.getEditor().setItem(this.getItemAt(i));
                    ((JTextField) this.getEditor().getEditorComponent()).moveCaretPosition(editorText.length());
                    itemIsFind = true;
                }
            }
            i++;
        }
    }
}
