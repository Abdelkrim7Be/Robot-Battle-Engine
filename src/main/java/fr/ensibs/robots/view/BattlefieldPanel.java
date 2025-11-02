package fr.ensibs.robots.view;

import fr.ensibs.robots.logic.Droid;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.FIELD_HEIGHT;
import static fr.ensibs.robots.logic.BattleSetup.FIELD_WIDTH;

/**
 * The panel that displays the robots engaged on the battlefield
 *
 * @author Pascale Launay
 */
public class BattlefieldPanel extends JPanel
{
    private final List<DroidView<? extends Droid>> views; // the views of the robots engaged in the battlefield

    /**
     * Constructor
     *
     * @param views the views of the robots engaged in the battlefield
     */
    public BattlefieldPanel(List<DroidView<? extends Droid>> views)
    {
        setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        this.views = views;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // the panel actual dimensions
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // compute the scales and margins to be applied
        double scale = Math.min(panelWidth * 1.0d / FIELD_WIDTH, panelHeight * 1.0d / FIELD_HEIGHT);
        double marginX = (panelWidth - FIELD_WIDTH * scale) / 2;
        double marginY = (panelHeight - FIELD_HEIGHT * scale) / 2;
        g2d.setTransform(new AffineTransform(scale, 0, 0, scale, marginX, marginY));

        // draw the background
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

        // draw the robots
        for (DroidView<? extends Droid> view : views) {
            view.draw(g2d);
        }
    }
}