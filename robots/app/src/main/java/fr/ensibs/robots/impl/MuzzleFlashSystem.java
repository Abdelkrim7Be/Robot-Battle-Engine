package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Location;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages muzzle flash effects when robots fire.
 */
class MuzzleFlashSystem
{
    private static class MuzzleFlash
    {
        Location location;
        double heading;
        int age; // frames since creation
        static final int MAX_AGE = 3; // Flash lasts 3 frames
    }
    
    private final List<MuzzleFlash> flashes = new ArrayList<>();
    
    void createFlash(Location location, double heading)
    {
        MuzzleFlash flash = new MuzzleFlash();
        flash.location = location;
        flash.heading = heading;
        flash.age = 0;
        flashes.add(flash);
    }
    
    void update()
    {
        Iterator<MuzzleFlash> it = flashes.iterator();
        while (it.hasNext()) {
            MuzzleFlash flash = it.next();
            flash.age++;
            if (flash.age >= MuzzleFlash.MAX_AGE) {
                it.remove();
            }
        }
    }
    
    void draw(Graphics2D g2d)
    {
        for (MuzzleFlash flash : flashes) {
            drawFlash(g2d, flash);
        }
    }
    
    private void drawFlash(Graphics2D g2d, MuzzleFlash flash)
    {
        AffineTransform original = g2d.getTransform();
        
        // Translate to location
        g2d.translate(flash.location.getX(), flash.location.getY());
        g2d.rotate(Math.toRadians(flash.heading));
        
        // Calculate fade (brightest at start, fades out)
        float alpha = 1.0f - (flash.age / (float) MuzzleFlash.MAX_AGE);
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        
        // Draw bright yellow-white flash
        int size = 8 - (flash.age * 2);
        if (size > 0) {
            // Core (bright white)
            g2d.setColor(new Color(255, 255, 200, (int) (255 * alpha)));
            g2d.fillOval(-size / 2, -size / 2, size, size);
            
            // Outer glow (yellow-orange)
            g2d.setColor(new Color(255, 200, 0, (int) (150 * alpha)));
            g2d.fillOval(-size, -size, size * 2, size * 2);
        }
        
        g2d.setTransform(original);
    }
}

