package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Location;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Particle system for visual effects (explosions, hits, etc.).
 * 
 * <p>Manages particles that:
 * <ul>
 *   <li>Fade out over time</li>
 *   <li>Move with velocity</li>
 *   <li>Change color as they age</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
public class ParticleSystem
{
    private final List<Particle> particles;
    private final Random random;
    
    /**
     * Constructor
     */
    public ParticleSystem()
    {
        this.particles = new ArrayList<>();
        this.random = new Random();
    }
    
    /**
     * Create an explosion effect at the given location.
     * 
     * @param location the location of the explosion
     * @param color the color of the explosion
     * @param intensity the intensity (number of particles)
     */
    public void createExplosion(Location location, Color color, int intensity)
    {
        for (int i = 0; i < intensity; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 2.0 + random.nextDouble() * 4.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            int lifetime = 20 + random.nextInt(30); // 20-50 frames
            
            particles.add(new Particle(
                location.getX(),
                location.getY(),
                vx, vy,
                color,
                lifetime));
        }
    }
    
    /**
     * Create a hit effect (smaller explosion).
     * 
     * @param location the location of the hit
     * @param color the color of the hit
     */
    public void createHit(Location location, Color color)
    {
        createExplosion(location, color, 8);
    }
    
    /**
     * Update all particles (move, age, remove dead ones).
     */
    public void update()
    {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle particle = it.next();
            particle.update();
            if (!particle.isAlive()) {
                it.remove();
            }
        }
    }
    
    /**
     * Draw all particles.
     * 
     * @param g2d the graphics context
     */
    public void draw(Graphics2D g2d)
    {
        for (Particle particle : particles) {
            particle.draw(g2d);
        }
    }
    
    /**
     * Clear all particles.
     */
    public void clear()
    {
        particles.clear();
    }
    
    /**
     * Get the number of active particles.
     * 
     * @return the particle count
     */
    public int getParticleCount()
    {
        return particles.size();
    }
    
    /**
     * Represents a single particle.
     */
    private static class Particle
    {
        private double x, y;
        private double vx, vy;
        private Color color;
        private int lifetime;
        private int age;
        
        Particle(double x, double y, double vx, double vy, Color color, int lifetime)
        {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.lifetime = lifetime;
            this.age = 0;
        }
        
        void update()
        {
            x += vx;
            y += vy;
            vx *= 0.95; // Friction
            vy *= 0.95;
            age++;
        }
        
        boolean isAlive()
        {
            return age < lifetime;
        }
        
        void draw(Graphics2D g2d)
        {
            float alpha = 1.0f - ((float) age / lifetime);
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            
            Color drawColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int) (alpha * 255));
            
            g2d.setColor(drawColor);
            g2d.fillOval((int) x - 2, (int) y - 2, 4, 4);
        }
    }
}

