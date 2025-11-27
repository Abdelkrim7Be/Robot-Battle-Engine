package fr.ensibs.robots.impl;

import java.util.Random;

/**
 * Camera shake system for screen trauma effects.
 * 
 * <p>MISSION 2.1: Camera Trauma - Makes explosions and big hits feel heavy.
 * When a robot dies or takes >10 damage, the screen shakes for 10 frames
 * with a damping factor.
 * 
 * <p>Usage:
 * <ol>
 *   <li>Call {@link #triggerShake(int)} when damage occurs or robot dies</li>
 *   <li>Call {@link #update()} each frame to decay the shake</li>
 *   <li>Call {@link #getOffsetX()} and {@link #getOffsetY()} before drawing</li>
 *   <li>Apply offset to Graphics2D translation</li>
 * </ol>
 * 
 * @author Robot Wars Team
 */
public class CameraShaker
{
    private static final int MAX_SHAKE_DURATION = 10; // frames
    private static final double MIN_SHAKE_INTENSITY = 2.0; // pixels
    private static final double MAX_SHAKE_INTENSITY = 5.0; // pixels
    private static final double DAMPING_FACTOR = 0.85; // Decay per frame
    
    private final Random random;
    private double currentIntensity;
    private int framesRemaining;
    private double offsetX;
    private double offsetY;
    
    /**
     * Constructor
     */
    public CameraShaker()
    {
        this.random = new Random();
        this.currentIntensity = 0.0;
        this.framesRemaining = 0;
        this.offsetX = 0.0;
        this.offsetY = 0.0;
    }
    
    /**
     * Trigger a camera shake with the given intensity.
     * 
     * @param intensity the shake intensity (damage amount or death indicator)
     */
    public void triggerShake(int intensity)
    {
        // Map intensity to shake strength
        // Death = 100 intensity, >10 damage = damage amount
        double shakeStrength = Math.min(MAX_SHAKE_INTENSITY, 
            MIN_SHAKE_INTENSITY + (intensity / 20.0));
        
        // If already shaking, add to existing intensity (stacking)
        if (framesRemaining > 0) {
            currentIntensity = Math.min(MAX_SHAKE_INTENSITY, 
                currentIntensity + shakeStrength);
        } else {
            currentIntensity = shakeStrength;
        }
        
        // Reset duration
        framesRemaining = MAX_SHAKE_DURATION;
    }
    
    /**
     * Update the camera shake (call each frame).
     * Decays the intensity and generates new random offsets.
     */
    public void update()
    {
        if (framesRemaining <= 0) {
            currentIntensity = 0.0;
            offsetX = 0.0;
            offsetY = 0.0;
            return;
        }
        
        // Generate random offset based on current intensity
        double maxOffset = currentIntensity;
        offsetX = (random.nextDouble() * 2 - 1) * maxOffset; // -maxOffset to +maxOffset
        offsetY = (random.nextDouble() * 2 - 1) * maxOffset;
        
        // Decay intensity with damping factor
        currentIntensity *= DAMPING_FACTOR;
        
        // Decrease frames remaining
        framesRemaining--;
    }
    
    /**
     * Get the current X offset for camera shake.
     * 
     * @return the X offset in pixels
     */
    public double getOffsetX()
    {
        return offsetX;
    }
    
    /**
     * Get the current Y offset for camera shake.
     * 
     * @return the Y offset in pixels
     */
    public double getOffsetY()
    {
        return offsetY;
    }
    
    /**
     * Check if the camera is currently shaking.
     * 
     * @return true if shaking, false otherwise
     */
    public boolean isShaking()
    {
        return framesRemaining > 0;
    }
    
    /**
     * Reset the camera shake (stop shaking immediately).
     */
    public void reset()
    {
        currentIntensity = 0.0;
        framesRemaining = 0;
        offsetX = 0.0;
        offsetY = 0.0;
    }
}

