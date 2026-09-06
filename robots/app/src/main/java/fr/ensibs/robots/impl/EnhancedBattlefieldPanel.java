package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.view.BattlefieldPanel;
import fr.ensibs.robots.view.DroidView;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.FIELD_HEIGHT;
import static fr.ensibs.robots.logic.BattleSetup.FIELD_WIDTH;

/**
 * Enhanced battlefield panel with grid, borders, and double buffering.
 * 
 * <p>This panel extends the base BattlefieldPanel with:
 * <ul>
 *   <li>Grid background for better visual reference</li>
 *   <li>Battlefield borders</li>
 *   <li>Double buffering for smooth rendering</li>
 *   <li>Support for IDrawable interface (when available)</li>
 * </ul>
 * 
 * <p>The rendering loop is separated from the physics loop (MVC pattern).
 * 
 * @author Robot Wars Team
 */
public class EnhancedBattlefieldPanel extends BattlefieldPanel
{
    private static final int GRID_SIZE = 50; // Grid cell size in pixels
    private static final Color GRID_COLOR = new Color(240, 240, 240); // Light gray
    private static final Color BORDER_COLOR = new Color(100, 100, 100); // Dark gray
    
    private final List<Object> additionalDrawables; // Temporary: will be List<IDrawable>
    
    // HUD and effects components
    private final HUDOverlay hudOverlay;
    private final ParticleSystem particleSystem;
    private final Leaderboard leaderboard;
    private boolean showHUD = true;
    private boolean showLeaderboard = true;
    
    /**
     * Constructor
     * 
     * @param views the views of the robots engaged in the battlefield
     */
    public EnhancedBattlefieldPanel(List<DroidView<? extends Droid>> views)
    {
        super(views);
        this.additionalDrawables = new ArrayList<>();
        this.hudOverlay = new HUDOverlay();
        this.particleSystem = new ParticleSystem();
        this.leaderboard = new Leaderboard();
        
        // Enable double buffering for smooth rendering
        setDoubleBuffered(true);
    }
    
    /**
     * Add an additional drawable entity to be rendered (e.g., bullets, particles).
     * 
     * @param drawable the drawable entity to add (must have draw(Graphics2D) method)
     */
    public void addDrawable(Object drawable)
    {
        additionalDrawables.add(drawable);
    }
    
    /**
     * Remove a drawable entity.
     * 
     * @param drawable the drawable entity to remove
     */
    public void removeDrawable(Object drawable)
    {
        additionalDrawables.remove(drawable);
    }
    
    /**
     * Clear all additional drawables.
     */
    public void clearDrawables()
    {
        additionalDrawables.clear();
    }
    
    /**
     * Enhanced rendering method with grid and borders.
     * This method is called by the rendering loop (separate from physics loop).
     * 
     * <p>This follows the MVC pattern where rendering is independent of physics updates.
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g); // Draws white background and robots
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Save original transform
        AffineTransform originalTransform = g2d.getTransform();
        
        // Compute scale and margins (same as base class)
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double scale = Math.min(panelWidth * 1.0d / FIELD_WIDTH, panelHeight * 1.0d / FIELD_HEIGHT);
        double marginX = (panelWidth - FIELD_WIDTH * scale) / 2;
        double marginY = (panelHeight - FIELD_HEIGHT * scale) / 2;
        g2d.setTransform(new AffineTransform(scale, 0, 0, scale, marginX, marginY));
        
        // Draw grid over the white background
        drawGrid(g2d);
        
        // Draw borders
        drawBorders(g2d);
        
        // Draw additional drawable entities (bullets, particles, etc.)
        drawAdditionalEntities(g2d);
        
        // Particle effects are kept disabled until cleanup is stable.
        // particleSystem.draw(g2d);
        
        // Draw HUD overlay (in battlefield coordinates)
        if (showHUD) {
            hudOverlay.draw(g2d, getViews());
        }
        
        // Restore original transform for screen-space drawing
        g2d.setTransform(originalTransform);
        
        // Draw leaderboard (in screen coordinates)
        if (showLeaderboard) {
            int leaderboardX = 10;
            int leaderboardY = 10;
            leaderboard.draw(g2d, getViews(), leaderboardX, leaderboardY);
        }
    }
    
    /**
     * Get the views list (accessor for HUD/Leaderboard).
     */
    @SuppressWarnings("unchecked")
    private List<DroidView<? extends Droid>> getViews()
    {
        // Access views through reflection since it's private in parent
        try {
            java.lang.reflect.Field viewsField = BattlefieldPanel.class.getDeclaredField("views");
            viewsField.setAccessible(true);
            return (List<DroidView<? extends Droid>>) viewsField.get(this);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Draw the grid pattern for visual reference.
     */
    private void drawGrid(Graphics2D g2d)
    {
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(0.5f));
        
        // Draw vertical grid lines
        for (int x = 0; x <= FIELD_WIDTH; x += GRID_SIZE) {
            g2d.drawLine(x, 0, x, FIELD_HEIGHT);
        }
        
        // Draw horizontal grid lines
        for (int y = 0; y <= FIELD_HEIGHT; y += GRID_SIZE) {
            g2d.drawLine(0, y, FIELD_WIDTH, y);
        }
    }
    
    /**
     * Draw the battlefield borders.
     */
    private void drawBorders(Graphics2D g2d)
    {
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(2.0f));
        
        // Draw border rectangle
        g2d.drawRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
    }
    
    /**
     * Draw additional drawable entities (bullets, particles, etc.).
     */
    private void drawAdditionalEntities(Graphics2D g2d)
    {
        // Draw all additional entities that expose a draw(Graphics2D) method
        for (Object drawable : new ArrayList<>(additionalDrawables)) {
            try {
                // Use reflection to call draw method if it exists
                java.lang.reflect.Method drawMethod = drawable.getClass().getMethod("draw", Graphics2D.class);
                drawMethod.invoke(drawable, g2d);
            } catch (Exception e) {
                // Skip if draw method doesn't exist or fails
            }
        }
    }
    
    /**
     * Update bullets from the battlefield and render them.
     * This should be called each frame to sync bullets with the panel.
     * 
     * @param battlefield the battlefield (must be BattlefieldImpl)
     */
    public void syncBullets(fr.ensibs.robots.logic.Battlefield battlefield)
    {
        if (!(battlefield instanceof BattlefieldImpl)) {
            return; // Skip if not BattlefieldImpl
        }
        BattlefieldImpl impl = (BattlefieldImpl) battlefield;
        
        // Clear old bullet views
        additionalDrawables.removeIf(d -> d instanceof BulletView);
        
        // Add views for all active bullets
        List<Bullet> bullets = impl.getBullets();
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                BulletView bulletView = new BulletView(bullet);
                additionalDrawables.add(bulletView);
            }
        }
    }
    
    /**
     * Update particle system (should be called each frame).
     */
    public void updateParticles()
    {
        particleSystem.update();
    }
    
    /**
     * Create a hit effect at the given location.
     * 
     * @param location the location of the hit
     * @param color the color of the effect
     */
    public void createHitEffect(fr.ensibs.robots.logic.Location location, Color color)
    {
        particleSystem.createHit(location, color);
    }
    
    /**
     * Create an explosion effect at the given location.
     * 
     * @param location the location of the explosion
     * @param color the color of the explosion
     * @param intensity the intensity (number of particles)
     */
    public void createExplosion(fr.ensibs.robots.logic.Location location, Color color, int intensity)
    {
        particleSystem.createExplosion(location, color, intensity);
    }
    
    /**
     * Toggle HUD visibility.
     * 
     * @param visible true to show HUD, false to hide
     */
    public void setHUDVisible(boolean visible)
    {
        this.showHUD = visible;
    }
    
    /**
     * Toggle leaderboard visibility.
     * 
     * @param visible true to show leaderboard, false to hide
     */
    public void setLeaderboardVisible(boolean visible)
    {
        this.showLeaderboard = visible;
    }
    
    /**
     * Get the particle system (for external updates).
     * 
     * @return the particle system
     */
    public ParticleSystem getParticleSystem()
    {
        return particleSystem;
    }
    
    /**
     * Render method that can be called explicitly (separated from physics loop).
     * This follows the MVC pattern where rendering is independent of physics updates.
     */
    public void render()
    {
        repaint();
    }
}
