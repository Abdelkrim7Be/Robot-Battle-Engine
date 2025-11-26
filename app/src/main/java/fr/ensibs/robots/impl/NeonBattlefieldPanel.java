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
 * Neon-themed battlefield panel with dark background, digital grid, and cyber-military aesthetic.
 * 
 * <p>Features:
 * <ul>
 *   <li>Dark theme with neon accents</li>
 *   <li>Digital grid overlay</li>
 *   <li>Glowing danger zone borders</li>
 *   <li>60 FPS smooth rendering with double buffering</li>
 *   <li>Threading separation for responsive UI</li>
 * </ul>
 * 
 * @author Robot Wars Team - Operation Neon Battlefield
 */
public class NeonBattlefieldPanel extends BattlefieldPanel
{
    // Dark theme colors
    private static final Color BACKGROUND_DARK = new Color(15, 15, 25); // Very dark blue-gray
    private static final Color GRID_NEON = new Color(0, 150, 150, 60); // Cyan grid (subtle)
    private static final Color BORDER_NEON = new Color(255, 50, 50, 200); // Red danger zone
    private static final Color BORDER_GLOW = new Color(255, 100, 100, 100); // Red glow
    
    private static final int GRID_SIZE = 50;
    
    // Visual effects
    private final List<Object> additionalDrawables;
    private final ParticleSystem particleSystem;
    private final MuzzleFlashSystem muzzleFlashSystem;
    private final DamageFlashSystem damageFlashSystem;
    
    // Rendering optimization
    private long lastFrameTime = System.nanoTime();
    private double currentFPS = 60.0;
    
    /**
     * Constructor
     */
    public NeonBattlefieldPanel(List<DroidView<? extends Droid>> views)
    {
        super(views);
        this.additionalDrawables = new ArrayList<>();
        this.particleSystem = new ParticleSystem();
        this.muzzleFlashSystem = new MuzzleFlashSystem();
        this.damageFlashSystem = new DamageFlashSystem();
        
        setDoubleBuffered(true);
        setBackground(BACKGROUND_DARK);
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        // Calculate FPS
        long currentTime = System.nanoTime();
        long deltaTime = currentTime - lastFrameTime;
        if (deltaTime > 0) {
            currentFPS = 1_000_000_000.0 / deltaTime;
        }
        lastFrameTime = currentTime;
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Fill with dark background
        g2d.setColor(BACKGROUND_DARK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Compute scale and margins
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double scale = Math.min(panelWidth * 1.0d / FIELD_WIDTH, panelHeight * 1.0d / FIELD_HEIGHT);
        double marginX = (panelWidth - FIELD_WIDTH * scale) / 2;
        double marginY = (panelHeight - FIELD_HEIGHT * scale) / 2;
        
        AffineTransform originalTransform = g2d.getTransform();
        g2d.setTransform(new AffineTransform(scale, 0, 0, scale, marginX, marginY));
        
        // Draw dark background for battlefield
        g2d.setColor(BACKGROUND_DARK);
        g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        
        // Draw digital grid
        drawDigitalGrid(g2d);
        
        // Draw glowing danger zone borders
        drawDangerZoneBorders(g2d);
        
        // Draw bullets and trails
        drawAdditionalEntities(g2d);
        
        // Draw robots (with damage flash overlay)
        drawRobots(g2d);
        
        // Draw muzzle flashes
        muzzleFlashSystem.draw(g2d);
        
        // Draw particle effects
        particleSystem.draw(g2d);
        
        // Restore transform for screen-space drawing
        g2d.setTransform(originalTransform);
        
        // Draw FPS counter (debug)
        drawFPS(g2d);
    }
    
    /**
     * Draw the digital grid overlay.
     */
    private void drawDigitalGrid(Graphics2D g2d)
    {
        g2d.setColor(GRID_NEON);
        g2d.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // Draw vertical lines
        for (int x = 0; x <= FIELD_WIDTH; x += GRID_SIZE) {
            g2d.drawLine(x, 0, x, FIELD_HEIGHT);
        }
        
        // Draw horizontal lines
        for (int y = 0; y <= FIELD_HEIGHT; y += GRID_SIZE) {
            g2d.drawLine(0, y, FIELD_WIDTH, y);
        }
    }
    
    /**
     * Draw glowing danger zone borders.
     */
    private void drawDangerZoneBorders(Graphics2D g2d)
    {
        // Outer glow
        g2d.setColor(BORDER_GLOW);
        g2d.setStroke(new BasicStroke(4.0f));
        g2d.drawRect(-2, -2, FIELD_WIDTH + 4, FIELD_HEIGHT + 4);
        
        // Main border
        g2d.setColor(BORDER_NEON);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
    }
    
    /**
     * Draw robots with damage flash overlay.
     */
    private void drawRobots(Graphics2D g2d)
    {
        List<DroidView<? extends Droid>> views = getViews();
        for (DroidView<? extends Droid> view : views) {
            if (view.getRobot().getEnergy() > 0) {
                view.draw(g2d);
                // Apply damage flash if needed
                damageFlashSystem.drawFlash(g2d, view);
            }
        }
    }
    
    /**
     * Draw additional entities (bullets, etc.).
     */
    private void drawAdditionalEntities(Graphics2D g2d)
    {
        for (Object drawable : new ArrayList<>(additionalDrawables)) {
            try {
                java.lang.reflect.Method drawMethod = drawable.getClass().getMethod("draw", Graphics2D.class);
                drawMethod.invoke(drawable, g2d);
            } catch (Exception e) {
                // Skip if draw method doesn't exist
            }
        }
    }
    
    /**
     * Draw FPS counter (debug).
     */
    private void drawFPS(Graphics2D g2d)
    {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2d.drawString(String.format("FPS: %.1f", currentFPS), 10, 20);
    }
    
    /**
     * Get views list.
     */
    @SuppressWarnings("unchecked")
    private List<DroidView<? extends Droid>> getViews()
    {
        try {
            java.lang.reflect.Field viewsField = BattlefieldPanel.class.getDeclaredField("views");
            viewsField.setAccessible(true);
            return (List<DroidView<? extends Droid>>) viewsField.get(this);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    // Public API methods
    
    public void addDrawable(Object drawable)
    {
        additionalDrawables.add(drawable);
    }
    
    public void removeDrawable(Object drawable)
    {
        additionalDrawables.remove(drawable);
    }
    
    public void clearDrawables()
    {
        additionalDrawables.clear();
    }
    
    public void syncBullets(fr.ensibs.robots.logic.Battlefield battlefield)
    {
        if (!(battlefield instanceof BattlefieldImpl)) {
            return;
        }
        BattlefieldImpl impl = (BattlefieldImpl) battlefield;
        
        additionalDrawables.removeIf(d -> d instanceof BulletView);
        
        List<Bullet> bullets = impl.getBullets();
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                BulletView bulletView = new BulletView(bullet);
                additionalDrawables.add(bulletView);
            }
        }
    }
    
    public void updateParticles()
    {
        particleSystem.update();
        muzzleFlashSystem.update();
        damageFlashSystem.update();
    }
    
    public void createMuzzleFlash(fr.ensibs.robots.logic.Location location, double heading)
    {
        muzzleFlashSystem.createFlash(location, heading);
    }
    
    public void createHitEffect(fr.ensibs.robots.logic.Location location, Color color)
    {
        particleSystem.createHit(location, color);
    }
    
    public void createExplosion(fr.ensibs.robots.logic.Location location, Color color, int intensity)
    {
        particleSystem.createExplosion(location, color, intensity);
    }
    
    public void registerDamageFlash(Droid droid)
    {
        damageFlashSystem.registerFlash(droid);
    }
    
    public ParticleSystem getParticleSystem()
    {
        return particleSystem;
    }
}

