package fr.ensibs.robots.view;

import java.awt.Graphics2D;

public interface IDrawable
{
    void draw(Graphics2D g2d);
    
    default boolean isVisible()
    {
        return true;
    }
}

