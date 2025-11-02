package fr.ensibs.robots.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Engine that displays a battlefield view by invoking its {@link BattlefieldPanel#repaint()}
 * method periodically
 *
 * @author Pascale Launay
 */
public class GraphicEngine
{
    private final Component view; // the battlefield view
    private Timer timer;          // the timer used to run the task periodically

    /**
     * Constructor
     *
     * @param view the battlefield view
     */
    public GraphicEngine(Component view)
    {
        this.view = view;
    }

    /**
     * Start refreshing the view periodically
     *
     * @param period the period in ms
     */
    public void start(int period)
    {
        if (timer == null) {
            timer = new Timer(period, (ActionEvent e) -> {
                view.repaint();
            });
            timer.start();
        }
    }

    /**
     * Stop refreshing the view
     */
    public void stop()
    {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }
}
