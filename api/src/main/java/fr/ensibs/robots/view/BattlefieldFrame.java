package fr.ensibs.robots.view;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.logic.Droid;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The application frame composed of a battlefield panel where the battle takes place and
 * a controls panel containing all buttons and battle status information
 *
 * 
 */
public class BattlefieldFrame extends JFrame
{
    /**
     * Constructor
     */
    public BattlefieldFrame(BattleFactory factory, RobotTaskFactory taskFactory)
    {
        super("BATTLEFIELD");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        setContentPane(panel);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // shared list of robots views and battlefield
        List<DroidView<? extends Droid>> views = new ArrayList<>();

        // the battlefield at the center of the frame
        BattlefieldPanel battlefieldPanel = new BattlefieldPanel(views);
        panel.add(battlefieldPanel, BorderLayout.CENTER);

        // the controls on the right
        JPanel controlsPanel = new ControlsPanel(views, factory, taskFactory);
        panel.add(controlsPanel, BorderLayout.EAST);
    }
}
