package fr.ensibs.robots.view;

import java.awt.Graphics2D;

/**
 * Interface for entities that can be drawn on the battlefield.
 * 
 * <p>This interface provides a clean separation between the model (logic)
 * and view (rendering) layers, following the MVC pattern.
 * 
 * <p>All drawable entities (robots, bullets, particles, etc.) should
 * implement this interface to be rendered on the battlefield.
 * 
 * @author Robot Wars Team
 */
public interface IDrawable
{
    /**
     * Draw this entity on the battlefield using the provided graphics context.
     * 
     * <p>The graphics context is already transformed to the battlefield coordinate
     * system, so drawing should use battlefield coordinates (0,0 at top-left,
     * FIELD_WIDTH x FIELD_HEIGHT).
     * 
     * @param g2d the graphics context for drawing
     */
    void draw(Graphics2D g2d);
    
    /**
     * Check if this drawable entity should be rendered.
     * Dead or inactive entities should return false.
     * 
     * @return true if this entity should be drawn, false otherwise
     */
    default boolean isVisible()
    {
        return true;
    }
}

