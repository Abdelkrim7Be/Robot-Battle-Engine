package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.impl.EnhancedBattlefieldFrame;

import javax.swing.*;

/**
 * The application entry point
 *
 * @author Pascale Launay
 */
public class Launcher
{
    /**
     * Application entry point. Create a battlefield, a frame to display it, and display the frame
     *
     * @param args NONE
     */
    public static void main(String[] args)
    {
        BattleFactory factory = new fr.ensibs.robots.impl.SimpleBattleFactory();
        RobotTaskFactory taskFactory = new fr.ensibs.robots.impl.RobotTaskFactoryImpl();

        SwingUtilities.invokeLater(() -> {
            // create the enhanced frame and display it
            EnhancedBattlefieldFrame frame = new EnhancedBattlefieldFrame(factory, taskFactory);
            frame.setVisible(true);
        });
    }
}
