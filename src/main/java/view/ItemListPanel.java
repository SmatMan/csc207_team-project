package view;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A scrollable list of text items. Used for both the Pantry and Intolerances tabs,
 * since they look the same and only hold different data.
 */
public class ItemListPanel extends JPanel {

    // data held by model, JList displays the data from the model.
    private final DefaultListModel<String> items = new DefaultListModel<>();

    public ItemListPanel(String[] initialItems) {
        super(new BorderLayout());
        for (String item : initialItems) {
            items.addElement(item);
        }
        add(new JScrollPane(new JList<>(items)), BorderLayout.CENTER);
    }
}
