package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * A scrollable list of text items. Used for both the Pantry and Intolerances tabs,
 * since they look the same and only hold different data.
 */
public class ItemListPanel extends JPanel {

    // data held by model, JList displays the data from the model.
    private final DefaultListModel<String> items = new DefaultListModel<>();
    private final JList<String> list = new JList<>(items);

    public ItemListPanel(String[] initialItems) {
        super(new BorderLayout());
        for (String item : initialItems) {
            items.addElement(item);
        }
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(list), BorderLayout.CENTER);

        JButton addButton = new JButton("+");
        JButton removeButton = new JButton("-");
        removeButton.setEnabled(false); // nothing is selected yet, so "-" button should be disableda

        addButton.addActionListener(e -> addItem());
        removeButton.addActionListener(e -> removeSelectedItem());
        // Only allow "-" when there is something selected to remove.
        list.addListSelectionListener(e -> removeButton.setEnabled(!list.isSelectionEmpty()));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(addButton);
        buttons.add(removeButton);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addItem() {
        String input = JOptionPane.showInputDialog(this, "Add item:");
        if (input == null) {
            return; // user pressed Cancel
        }
        String item = input.trim();
        if (!item.isEmpty() && !items.contains(item)) {
            items.addElement(item);
        }
    }

    private void removeSelectedItem() {
        int index = list.getSelectedIndex();
        if (index != -1) {
            items.remove(index);
        }
    }
}
