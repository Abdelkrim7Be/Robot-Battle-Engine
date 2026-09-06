package fr.ensibs.robots.view;

import fr.ensibs.robots.logic.Robot;

import java.awt.*;

public abstract class RobotView<R extends Robot> extends DroidView<R>
{
    public RobotView(R robot, String name, Color color)
    {
        super(robot, name, color);
    }

    public void draw(Graphics2D g2d)
    {
        super.draw(g2d);
        drawRadar(g2d);
    }

    protected abstract void drawRadar(Graphics2D g2d);
}
