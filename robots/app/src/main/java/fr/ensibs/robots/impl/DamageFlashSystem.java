package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.view.DroidView;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages damage flash effects when robots take damage.
 */
class DamageFlashSystem
{
    private static class DamageFlash
    {
        int age; // frames since damage
        static final int MAX_AGE = 2; // Flash lasts 2 frames
    }
    
    private final Map<Droid, DamageFlash> flashes = new HashMap<>();
    
    void registerFlash(Droid droid)
    {
        DamageFlash flash = new DamageFlash();
        flash.age = 0;
        flashes.put(droid, flash);
    }
    
    void update()
    {
        Iterator<Map.Entry<Droid, DamageFlash>> it = flashes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Droid, DamageFlash> entry = it.next();
            DamageFlash flash = entry.getValue();
            flash.age++;
            if (flash.age >= DamageFlash.MAX_AGE) {
                it.remove();
            }
        }
    }
    
    void drawFlash(Graphics2D g2d, DroidView<? extends Droid> view)
    {
        Droid droid = view.getRobot();
        DamageFlash flash = flashes.get(droid);
        if (flash == null) {
            return;
        }
        
        // Draw white flash overlay
        float alpha = 1.0f - (flash.age / (float) DamageFlash.MAX_AGE);
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        
        Location location = droid.getLocation();
        int radius = fr.ensibs.robots.logic.BattleSetup.ROBOT_RADIUS;
        
        AffineTransform original = g2d.getTransform();
        g2d.translate(location.getX(), location.getY());
        
        // Draw white flash overlay
        g2d.setColor(new Color(255, 255, 255, (int) (200 * alpha)));
        g2d.fillOval(-radius, -radius, radius * 2, radius * 2);
        
        g2d.setTransform(original);
    }
}

