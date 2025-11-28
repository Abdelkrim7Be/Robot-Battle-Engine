package fr.ensibs.robots.view;

import fr.ensibs.robots.logic.Droid;

import java.awt.*;

public abstract class DroidView<R extends Droid>
{
    private final R robot;
    private final String name;
    private final Color color;

    public DroidView(R robot, String name, Color color)
    {
        this.robot = robot;
        this.name = name;
        this.color = color;
    }

    public String getName()
    {
        return name;
    }

    public Color getColor()
    {
        return color;
    }

    public R getRobot()
    {
        return robot;
    }

    public void draw(Graphics2D g2d)
    {
        drawBody(g2d);
        drawGun(g2d);
    }

    protected abstract void drawBody(Graphics2D g2d);
    protected abstract void drawGun(Graphics2D g2d);
}
