package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Location;
// import fr.ensibs.robots.view.IDrawable; // TODO: Uncomment when api module is built

import java.awt.*;
import java.awt.geom.Ellipse2D;

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
     * Draw the bullet on the battlefield with enhanced tracer trail.
     * 
     * @param g2d the graphics context
     */
    public void draw(Graphics2D g2d)
    {
        if (!isVisible()) {
            return;
        }
        
        Location location = bullet.getLocation();
        int power = bullet.getPower();
        
        // Bullet size scales with power
        double size = 3.0 + (power * 0.4);
        double radius = size / 2.0;
        
        double heading = Math.toRadians(bullet.getHeading());
        double trailLength = 15.0 + (power * 0.5); // Longer trail for higher power
        
        // Draw fading trail (multiple segments for smooth fade)
        for (int i = 3; i >= 0; i--) {
            double segmentLength = trailLength * (i + 1) / 4.0;
            double segDx = Math.sin(heading) * segmentLength;
            double segDy = -Math.cos(heading) * segmentLength;
            
            float alpha = 0.3f + (0.7f * i / 3.0f);
            Color trailColor = new Color(
                tracerColor.getRed(),
                tracerColor.getGreen(),
                tracerColor.getBlue(),
                (int) (alpha * 200)
            );
            
            g2d.setColor(trailColor);
            g2d.setStroke(new BasicStroke(2.0f - (i * 0.3f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(
                location.getX(),
                location.getY(),
                (int) Math.round(location.getX() - segDx),
                (int) Math.round(location.getY() - segDy)
            );
        }
        
        // Draw bright core (glowing circle)
        Shape bulletShape = new Ellipse2D.Double(
            location.getX() - radius,
            location.getY() - radius,
            size,
            size);
        
        // Outer glow
        g2d.setColor(new Color(tracerColor.getRed(), tracerColor.getGreen(), 
                              tracerColor.getBlue(), 100));
        g2d.fill(new Ellipse2D.Double(
            location.getX() - radius - 2,
            location.getY() - radius - 2,
            size + 4,
            size + 4));
        
        // Bright core
        g2d.setColor(tracerColor);
        g2d.fill(bulletShape);
        
        // White hot center
        g2d.setColor(Color.WHITE);
        g2d.fill(new Ellipse2D.Double(
            location.getX() - radius / 2,
            location.getY() - radius / 2,
            size / 2,
            size / 2));
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

