package fr.ensibs.robots.view;

import fr.ensibs.robots.logic.Robot;

import java.awt.*;

/**
 * Abstract class used to display a robot on the battlefield. Encapsulates the robot it displays.
 * Subclasses should implement the methods used to draw the 3 parts of the robot (body, gun, radar)
 *
 * @author Pascale Launay
 */
public abstract class RobotView<R extends Robot> extends DroidView<R>
{
    /**
     * Constructor
     *
     * @param name  the name of the robot
     * @param color the color of the robot
     * @param robot the robot to be displayed
     */
    public RobotView(R robot, String name, Color color)
    {
        super(robot, name, color);
    }

    /**
     * Method used to draw the robot on the {@link BattlefieldPanel}
     *
     * @param g2d the graphics context of the battlefield
     */
    public void draw(Graphics2D g2d)
    {
        // draw the 3 parts of the robot
        super.draw(g2d);
        drawRadar(g2d);
    }

    /**
     * Draw a robot radar at the location and in the direction of the given robot
     *
     * @param g2d the graphics context of the battlefield
     */
    protected abstract void drawRadar(Graphics2D g2d);
}
