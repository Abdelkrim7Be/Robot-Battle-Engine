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
    
    // MISSION 5.1: Object pooling for particles
    private final ObjectPool<Particle> particlePool;
    
    // CRITICAL FIX: Limit particle count to prevent accumulation
    private static final int MAX_PARTICLES = 200; // Reduced from 500 to prevent spiral
    
    /**
     * Constructor
     */
    public ParticleSystem()
    {
        this.particles = new ArrayList<>();
        this.random = new Random();
        
        // MISSION 5.1: Initialize particle pool (pre-allocate 200 particles, max 500)
        this.particlePool = new ObjectPool<>(() -> new Particle(), 200, 500);
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
        // CRITICAL FIX: Don't create particles if we're at the limit
        if (particles.size() >= MAX_PARTICLES) {
            return; // Skip creation to prevent accumulation
        }
        
        // CRITICAL FIX: Limit intensity to prevent too many particles at once
        int actualIntensity = Math.min(intensity, 20); // Max 20 particles per explosion
        
        for (int i = 0; i < actualIntensity && particles.size() < MAX_PARTICLES; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 2.0 + random.nextDouble() * 4.0;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            int lifetime = 20 + random.nextInt(30); // 20-50 frames
            
            // MISSION 5.1: Use object pool instead of new Particle()
            Particle particle = particlePool.acquire();
            particle.init(location.getX(), location.getY(), vx, vy, color, lifetime);
            particles.add(particle);
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
     * CRITICAL FIX: Enforce particle limit and aggressive cleanup.
     */
    public void update()
    {
        // CRITICAL: Remove dead particles first
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle particle = it.next();
            particle.update();
            if (!particle.isAlive()) {
                it.remove();
                // MISSION 5.1: Return particle to pool instead of discarding
                particlePool.release(particle);
            }
        }
        
        // CRITICAL FIX: If too many particles, aggressively remove oldest ones
        if (particles.size() > MAX_PARTICLES) {
            int toRemove = particles.size() - MAX_PARTICLES;
            // Remove more than needed to create buffer
            toRemove = Math.max(toRemove, particles.size() / 2); // Remove at least half if over limit
            for (int i = 0; i < toRemove && !particles.isEmpty(); i++) {
                Particle removed = particles.remove(0); // Remove oldest
                particlePool.release(removed);
            }
        }
    }
    
    /**
     * Draw all particles.
     * CRITICAL FIX: Ensure particles are drawn with isolated transforms.
     * 
     * @param g2d the graphics context
     */
    public void draw(Graphics2D g2d)
    {
        // CRITICAL: Only draw alive particles (filtered in update(), but double-check here)
        for (Particle particle : particles) {
            // CRITICAL FIX: Verify particle is alive before drawing
            if (!particle.isAlive()) {
                continue; // Skip dead particles (shouldn't happen, but safety check)
            }
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
    static class Particle
    {
        private double x, y;
        private double vx, vy;
        private Color color;
        private int lifetime;
        private int age;
        
        Particle()
        {
            // Default constructor for object pool
        }
        
        /**
         * Initialize particle with values (for reuse from pool).
         */
        void init(double x, double y, double vx, double vy, Color color, int lifetime)
        {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.lifetime = lifetime;
            this.age = 0;
        }
        
        Particle(double x, double y, double vx, double vy, Color color, int lifetime)
        {
            init(x, y, vx, vy, color, lifetime);
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
            // CRITICAL FIX: Use Graphics2D copy for isolated transforms
            Graphics2D gCopy = (Graphics2D) g2d.create();
            try {
                // Translate to particle position in the copy
                gCopy.translate((int) x, (int) y);
                
                float alpha = 1.0f - ((float) age / lifetime);
                alpha = Math.max(0.0f, Math.min(1.0f, alpha));
                
                Color drawColor = new Color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    (int) (alpha * 255));
                
                gCopy.setColor(drawColor);
                gCopy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER)); // Ensure proper blending
                gCopy.fillOval(-2, -2, 4, 4); // Relative to particle center (already translated)
            } finally {
                // CRITICAL: Reset composite and dispose copy
                gCopy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                gCopy.dispose();
            }
        }
    }
}

