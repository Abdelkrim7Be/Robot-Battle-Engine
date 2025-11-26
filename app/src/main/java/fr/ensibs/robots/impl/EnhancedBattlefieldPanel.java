package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.view.BattlefieldPanel;
import fr.ensibs.robots.view.DroidView;
// import fr.ensibs.robots.view.IDrawable; // TODO: Uncomment when api module is built

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
    
    // TODO: Use IDrawable when api module is built
    // private final List<IDrawable> additionalDrawables;
    private final List<Object> additionalDrawables; // Temporary: will be List<IDrawable>
    
    /**
     * Constructor
     * 
     * @param views the views of the robots engaged in the battlefield
     */
    public EnhancedBattlefieldPanel(List<DroidView<? extends Droid>> views)
    {
        super(views);
        this.additionalDrawables = new ArrayList<>();
        
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
        
        // Restore original transform
        g2d.setTransform(originalTransform);
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
        // Draw all additional entities that have a draw method
        // TODO: Use IDrawable interface when api module is built
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
     * Render method that can be called explicitly (separated from physics loop).
     * This follows the MVC pattern where rendering is independent of physics updates.
     */
    public void render()
    {
        repaint();
    }
}
