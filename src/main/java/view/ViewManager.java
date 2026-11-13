package view;

import java.awt.CardLayout;

import javax.swing.JPanel;

import interface_adapter.ViewManagerModel;
import interface_adapter.ViewManagerModelListener;

/**
 * The View Manager for the program. It listens for changes to the
 * ViewManagerModel and updates which View should be visible in the main area.
 */
public class ViewManager implements ViewManagerModelListener {
    private final CardLayout cardLayout;
    private final JPanel views;
    private final ViewManagerModel viewManagerModel;

    public ViewManager(JPanel views, CardLayout cardLayout, ViewManagerModel viewManagerModel) {
        this.views = views;
        this.cardLayout = cardLayout;
        this.viewManagerModel = viewManagerModel;
        this.viewManagerModel.addListener(this);
    }

    @Override
    public void viewChanged() {
        cardLayout.show(views, viewManagerModel.getCurrentViewName());
    }
}
