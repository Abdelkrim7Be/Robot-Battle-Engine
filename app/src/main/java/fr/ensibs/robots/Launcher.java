package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.view.BattlefieldFrame;
import fr.ensibs.robots.view.GraphicEngine;

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
            // create the frame and display it
            BattlefieldFrame frame = new BattlefieldFrame(factory, taskFactory);
            frame.pack();
            frame.setVisible(true);

            // start the graphic engine to refresh the view periodically
            GraphicEngine graphicEngine = new GraphicEngine(frame.getContentPane());
            graphicEngine.start(50);
        });
    }
}
