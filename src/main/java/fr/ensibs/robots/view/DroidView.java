package fr.ensibs.robots.view;

import fr.ensibs.robots.logic.Droid;

import java.awt.*;

public abstract class DroidView<R extends Droid>
{
    private final R robot;     // the robot to be displayed (contains the characteristics of the robot)
    private final String name; // the name of the robot (displayed in the controls panel)
    private final Color color; // the color of the robot (for the robot drawing)

    /**
     * Constructor
     *
     * @param name  the name of the robot
     * @param color the color of the robot
     * @param robot the robot to be displayed
     */
    public DroidView(R robot, String name, Color color)
    {
        this.robot = robot;
        this.name = name;
        this.color = color;
    }

    /**
     * Give the name of the robot (to be displayed in the controls panel)
     *
     * @return the name of the robot
     */
    public String getName()
    {
        return name;
    }

    /**
     * Give the color of the robot (for the robot drawing)
     *
     * @return the color of the robot
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Give the robot to be displayed (contains the characteristics of the robot)
     *
     * @return the robot to be displayed
     */
    public R getRobot()
    {
        return robot;
    }

    /**
     * Method used to draw the robot on the {@link BattlefieldPanel}
     *
     * @param g2d the graphics context of the battlefield
     */
    public void draw(Graphics2D g2d)
    {
        // draw the 2 parts of the robot
        drawBody(g2d);
        drawGun(g2d);
    }

    /**
     * Draw a robot body at the location and in the direction of the given robot
     *
     * @param g2d the graphics context of the battlefield
     */
    protected abstract void drawBody(Graphics2D g2d);

    /**
     * Draw a robot gun at the location and in the direction of the given robot
     *
     * @param g2d the graphics context of the battlefield
     */
    protected abstract void drawGun(Graphics2D g2d);
}
