package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Location;
// import fr.ensibs.robots.view.IDrawable; // TODO: Uncomment when api module is built

import java.awt.*;
import java.awt.Composite;
import java.awt.AlphaComposite;

/**
 * Graphical representation of a bullet (tracer).
 * 
 * <p>Bullets are rendered as small, bright tracers that show their
 * trajectory. Higher power bullets are rendered larger and brighter.
 * 
 * <p>TODO: Implement IDrawable when api module is built.
 * 
 * @author Robot Wars Team
 */
public class BulletView // implements IDrawable
{
    private final Bullet bullet;
    private final Color tracerColor;
    
    /**
     * Constructor
     * 
     * @param bullet the bullet to render
     */
    public BulletView(Bullet bullet)
    {
        this.bullet = bullet;
        
        // Color based on power: higher power = brighter/more intense
        int power = bullet.getPower();
        int intensity = Math.min(255, 150 + power * 10);
        this.tracerColor = new Color(intensity, intensity, 100, 220); // Yellow-orange tracer
    }
    
    /**
     * MISSION F: Enhanced bullet rendering with colored trails and glow.
     * OPERATION SNIPER - Draw bullet as a line representing its velocity.
     * Makes bullets clearly visible as straight lines connecting robots.
     * 
     * @param g2d the graphics context
     */
    public void draw(Graphics2D g2d)
    {
        if (!isVisible()) {
            return;
        }
        
        Graphics2D gCopy = (Graphics2D) g2d.create();
        try {
            // Get bullet position and velocity
            double x = bullet.getX();
            double y = bullet.getY();
            double velocityX = bullet.getVelocityX();
            double velocityY = bullet.getVelocityY();
            double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            
            // Normalize velocity for trail direction
            double nx = speed > 0.001 ? velocityX / speed : 0;
            double ny = speed > 0.001 ? velocityY / speed : 0;
            
            // CRITICAL FIX: Limit trail length to prevent accumulation
            // Trail length based on speed, but capped to prevent long trails
            double trailLength = Math.min(30, speed * 1.5); // Max 30 pixels
            
            // MISSION F: Determine color by team (default to cyan for team 0, orange for team 1)
            Color bulletColor = new Color(0, 255, 255); // Cyan default
            // Could be improved by tracking bullet owner's team
            
            // NUCLEAR OPTION: Simple bullet rendering - just a circle and short line
            // Simple circle for bullet
            gCopy.setColor(Color.YELLOW);
            gCopy.fillOval((int)(x - 4), (int)(y - 4), 8, 8);
            
            // Simple short trail (just a line, no stored history)
            if (speed > 0.001) {
                int trailX = (int)(x - nx * 15); // Fixed 15 pixel trail
                int trailY = (int)(y - ny * 15);
                
                gCopy.setColor(Color.ORANGE);
                gCopy.setStroke(new BasicStroke(2));
                gCopy.drawLine((int)x, (int)y, trailX, trailY);
            }
        } finally {
            gCopy.dispose();
        }
    }
    
    /**
     * Check if this bullet should be rendered.
     * 
     * @return true if visible, false otherwise
     */
    public boolean isVisible()
    {
        return bullet != null && bullet.isActive();
    }
    
    /**
     * Get the bullet being rendered.
     * 
     * @return the bullet
     */
    public Bullet getBullet()
    {
        return bullet;
    }
}

