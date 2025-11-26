package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
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
    // Dark sci-fi theme colors
    private static final Color BACKGROUND_DARK = new Color(13, 13, 13); // #0D0D0D Near Black
    private static final Color GRID_NEON = new Color(0, 50, 0, 80); // Dark green radar grid
    private static final Color BORDER_NEON = new Color(51, 51, 51); // #333333 Dark gray border
    private static final Color BORDER_GLOW = new Color(0, 100, 0, 50); // Subtle green glow
    
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
        // CRITICAL FIX: Do NOT call super.paintComponent() - it draws robots on white background!
        // We handle everything ourselves with correct order
        
        Graphics2D g2d = (Graphics2D) g;
        
        // STEP 1: WIPE - Clear screen FIRST (fixes ghosting)
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        g2d.setColor(Color.BLACK);
        g2d.clearRect(0, 0, panelWidth, panelHeight);
        g2d.fillRect(0, 0, panelWidth, panelHeight);
        
        // Calculate FPS
        long currentTime = System.nanoTime();
        long deltaTime = currentTime - lastFrameTime;
        if (deltaTime > 0) {
            currentFPS = 1_000_000_000.0 / deltaTime;
        }
        lastFrameTime = currentTime;
        
        // Performance: Disable expensive rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        
        // Compute scale and margins for coordinate transformation
        double scale = Math.min(panelWidth * 1.0d / FIELD_WIDTH, panelHeight * 1.0d / FIELD_HEIGHT);
        double marginX = (panelWidth - FIELD_WIDTH * scale) / 2;
        double marginY = (panelHeight - FIELD_HEIGHT * scale) / 2;
        
        // Save original transform
        AffineTransform originalTransform = g2d.getTransform();
        
        // Apply battlefield coordinate transform
        g2d.setTransform(new AffineTransform(scale, 0, 0, scale, marginX, marginY));
        
        // STEP 2: Clear battlefield area (dark background)
        g2d.setColor(BACKGROUND_DARK);
        g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        
        // STEP 3: GRID - Draw grid lines (dark green)
        drawDigitalGrid(g2d);
        
        // STEP 4: BORDERS
        drawDangerZoneBorders(g2d);
        
        // STEP 5: ENTITIES - Draw robots (BEFORE bullets)
        // DEBUG: Log robot positions for diagnostic
        List<DroidView<? extends Droid>> debugViews = getViews();
        if (!debugViews.isEmpty()) {
            System.out.printf("[RENDER_DEBUG] Frame: %d robots, scale=%.3f, margin=(%.1f,%.1f)%n", 
                debugViews.size(), scale, marginX, marginY);
            for (DroidView<? extends Droid> view : debugViews) {
                Location loc = view.getRobot().getLocation();
                double screenX = loc.getX() * scale + marginX;
                double screenY = loc.getY() * scale + marginY;
                System.out.printf("[RENDER_DEBUG] %s: LogicPos(%d,%d) -> ScreenPos(%.1f,%.1f) | Energy=%d | Color=%s%n",
                    view.getName(), loc.getX(), loc.getY(), screenX, screenY, 
                    view.getRobot().getEnergy(), view.getColor());
            }
        }
        drawRobots(g2d);
        
        // STEP 6: Bullets
        drawAdditionalEntities(g2d);
        
        // Restore transform for screen-space drawing
        g2d.setTransform(originalTransform);
        
        // STEP 7: UI OVERLAY - Draw FPS counter (last)
        drawFPS(g2d);
    }
    
    /**
     * Draw the digital grid overlay (radar-style dark green).
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
     * Draw danger zone borders (dark gray).
     */
    private void drawDangerZoneBorders(Graphics2D g2d)
    {
        // Main border
        g2d.setColor(BORDER_NEON);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
    }
    
    /**
     * Draw robots - STRICT ORDER: Save transform, translate, rotate, draw, restore.
     * CRITICAL: Each robot must save/restore transform to prevent corruption.
     */
    private void drawRobots(Graphics2D g2d)
    {
        List<DroidView<? extends Droid>> views = getViews();
        
        // ALWAYS draw test robot at center for visibility check
        drawTestRobot(g2d, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        
        // Draw all robots
        for (DroidView<? extends Droid> view : views) {
            if (view.getRobot().getEnergy() > 0) {
                // CRITICAL: Save transform before drawing each robot
                AffineTransform savedTransform = g2d.getTransform();
                
                try {
                    // Draw robot (view handles its own transforms)
                    view.draw(g2d);
                } finally {
                    // ALWAYS restore transform to prevent corruption
                    g2d.setTransform(savedTransform);
                }
            }
        }
    }
    
    /**
     * Draw a test robot at specified coordinates to verify rendering pipeline.
     * ALWAYS draw this to ensure robots are visible.
     */
    private void drawTestRobot(Graphics2D g2d, int x, int y)
    {
        AffineTransform original = g2d.getTransform();
        g2d.translate(x, y);
        
        // Draw HUGE bright red test robot (high visibility)
        int size = 80; // Very large
        g2d.setColor(Color.RED);
        g2d.fillRect(-size/2, -size/2, size, size);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(4.0f));
        g2d.drawRect(-size/2, -size/2, size, size);
        
        // Draw a cross to mark center
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawLine(-size/2, 0, size/2, 0);
        g2d.drawLine(0, -size/2, 0, size/2);
        
        g2d.setTransform(original);
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

