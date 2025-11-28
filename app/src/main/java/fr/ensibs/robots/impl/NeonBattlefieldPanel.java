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
    private static final Color BACKGROUND_DARK = new Color(0, 0, 0); // Pure black for maximum contrast
    private static final Color GRID_NEON = new Color(0, 100, 0, 120); // Bright green grid
    private static final Color BORDER_NEON = new Color(0, 150, 0); // Bright green border
    
    private static final int GRID_SIZE = 50;
    
    // CRITICAL: Store direct reference to views list (shared with ControlsPanel)
    private final List<DroidView<? extends Droid>> viewsList;
    
    // Visual effects
    private final List<Object> additionalDrawables;
    private final MuzzleFlashSystem muzzleFlashSystem;
    private final DamageFlashSystem damageFlashSystem;
    private final CameraShaker cameraShaker; // MISSION 2.1: Screen shake
    private final AutoDirector autoDirector; // MISSION 4.1: Smart camera
    
    // Battlefield reference for wreckage access
    private fr.ensibs.robots.logic.Battlefield battlefieldRef; // MISSION 2.3: For wreckage access
    
    // MISSION 4.2: Kill feed announcements
    private String lastKillAnnouncement; // Last kill streak announcement
    
    // Winner announcement - removed, only shown in kill feed
    
    // Rendering optimization
    private long lastFrameTime = System.nanoTime();
    private double currentFPS = 60.0;
    
    /**
     * Constructor
     */
    public NeonBattlefieldPanel(List<DroidView<? extends Droid>> views)
    {
        super(views);
        // CRITICAL: Store direct reference to the shared views list
        this.viewsList = views;
        this.additionalDrawables = new ArrayList<>();
        this.muzzleFlashSystem = new MuzzleFlashSystem();
        this.damageFlashSystem = new DamageFlashSystem();
        this.cameraShaker = new CameraShaker(); // MISSION 2.1: Screen shake
        this.autoDirector = new AutoDirector(); // MISSION 4.1: Smart camera
        
        setDoubleBuffered(true);
        setBackground(BACKGROUND_DARK);
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        // PART 1: THE RENDERING ENGINE - Clean, centered view with simple geometry
        Graphics2D g2d = (Graphics2D) g;
        
        // 1. CLEAR THE SCREEN
        int width = getWidth();
        int height = getHeight();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);
        
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
        
        // Calculate center offsets (0.9 scale for padding)
        double scale = Math.min(width * 1.0 / FIELD_WIDTH, height * 1.0 / FIELD_HEIGHT) * 0.9;
        int xOffset = (int) ((width - (FIELD_WIDTH * scale)) / 2);
        int yOffset = (int) ((height - (FIELD_HEIGHT * scale)) / 2);
        
        // Apply global transform: Translate then Scale
        Graphics2D battlefieldG = (Graphics2D) g2d.create();
        try {
            battlefieldG.translate(xOffset, yOffset);
            battlefieldG.scale(scale, scale);
            
            // Draw Green Grid border
            battlefieldG.setColor(new Color(0, 150, 0)); // Bright green
            battlefieldG.setStroke(new BasicStroke(2.0f));
            battlefieldG.drawRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
            
            // Draw grid lines
            battlefieldG.setColor(new Color(0, 50, 0)); // Dark green
            battlefieldG.setStroke(new BasicStroke(1.0f));
            for (int i = 0; i <= FIELD_WIDTH; i += 50) {
                battlefieldG.drawLine(i, 0, i, FIELD_HEIGHT);
            }
            for (int i = 0; i <= FIELD_HEIGHT; i += 50) {
                battlefieldG.drawLine(0, i, FIELD_WIDTH, i);
            }
            
            // Draw entities with strict isolation
            drawRobotsClean(battlefieldG);
            drawBulletsClean(battlefieldG);
            
            // MISSION F: Draw damage numbers
            if (battlefieldRef instanceof BattlefieldImpl) {
                BattlefieldImpl impl = (BattlefieldImpl) battlefieldRef;
                DamageNumberManager dnm = impl.getDamageNumberManager();
                if (dnm != null) {
                    dnm.update();
                    dnm.draw(battlefieldG);
                }
            }
        } finally {
            battlefieldG.dispose();
        }
        
        // Draw FPS counter (screen coordinates)
        drawFPS(g2d);
        
        // MISSION B: Draw phase indicator
        drawPhaseIndicator(g2d);
        
        // Winner announcement removed - only shown in kill feed
    }
    
    /**
     * Draw the digital grid overlay (radar-style bright green).
     */
    private void drawDigitalGrid(Graphics2D g2d)
    {
        g2d.setColor(GRID_NEON);
        g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
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
     * Draw danger zone borders (bright green).
     */
    private void drawDangerZoneBorders(Graphics2D g2d)
    {
        // Main border - bright green for visibility
        g2d.setColor(BORDER_NEON);
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
    }
    
    /**
     * MISSION 3.1: Draw the shrinking battle zone (red border).
     * 
     * @param g2d the graphics context
     */
    private void drawBattleZone(Graphics2D g2d)
    {
        if (battlefieldRef == null || !(battlefieldRef instanceof BattlefieldImpl)) {
            return;
        }
        BattlefieldImpl impl = (BattlefieldImpl) battlefieldRef;
        BattleZone zone = impl.getBattleZone();
        
        if (!zone.isZoneActive()) {
            return; // Zone not active yet
        }
        
        Location center = zone.getZoneCenter();
        double radius = zone.getZoneRadius();
        
        // Draw red border circle (danger zone)
        g2d.setColor(new Color(255, 0, 0, 200)); // Red, semi-transparent
        g2d.setStroke(new BasicStroke(4.0f));
        g2d.drawOval(
            (int)(center.getX() - radius),
            (int)(center.getY() - radius),
            (int)(radius * 2),
            (int)(radius * 2)
        );
        
        // Draw pulsing inner glow for extra visibility
        g2d.setColor(new Color(255, 0, 0, 50)); // Very transparent red
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawOval(
            (int)(center.getX() - radius - 5),
            (int)(center.getY() - radius - 5),
            (int)((radius + 5) * 2),
            (int)((radius + 5) * 2)
        );
    }
    
    /**
     * ---------------------------------------------------------
     * START OF HARDCODED RENDERER
     * ---------------------------------------------------------
     * HARD RESET: Vector graphics renderer - no glow, no particles, pure geometry
     * COMPLETELY REPLACES view.draw() system
     */
    /**
     * PART 1: Draw robots with clean, simple geometry (squares and rectangles).
     * No glows, no effects - just crisp shapes for debugging.
     */
    private void drawRobotsClean(Graphics2D battlefieldG)
    {
        List<DroidView<? extends Droid>> views = getViews();
        
        for (DroidView<? extends Droid> view : views) {
            Droid robot = view.getRobot();
            
            // Skip dead robots
            if (robot.getEnergy() <= 0) {
                continue;
            }
            
            // Use the color assigned to the view (from TeamInfo)
            Color teamColor = view.getColor();
            
            Location loc = robot.getLocation();
            int x = loc.getX();
            int y = loc.getY();
            
            // Create isolated Graphics copy for THIS robot
            Graphics2D gRobot = (Graphics2D) battlefieldG.create();
            try {
                // Translate to robot position
                gRobot.translate(x, y);
                
                // Rotate by body heading (convert North->East)
                double bodyHeading = robot.getHeading();
                gRobot.rotate(Math.toRadians(bodyHeading - 90));
                
                // Check if this is a Leader - Leaders are larger and have special styling
                boolean isLeader = robot instanceof fr.ensibs.robots.logic.TeamLeader;
                int bodySize = isLeader ? 50 : 40; // Leaders are 25% larger
                int halfSize = bodySize / 2;
                
                // Draw Body: Filled Rectangle at (0,0) after translate
                gRobot.setColor(teamColor);
                gRobot.fillRect(-halfSize, -halfSize, bodySize, bodySize); // Body
                gRobot.setColor(Color.BLACK);
                gRobot.drawRect(-halfSize, -halfSize, bodySize, bodySize); // Outline
                
                // Leaders get a thicker outline and inner highlight
                if (isLeader) {
                    gRobot.setStroke(new BasicStroke(3.0f));
                    gRobot.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 150));
                    gRobot.drawRect(-halfSize + 3, -halfSize + 3, bodySize - 6, bodySize - 6); // Inner highlight
                    gRobot.setStroke(new BasicStroke(1.0f));
                }
                
                // Reset rotation for gun
                gRobot.setTransform(battlefieldG.getTransform());
                gRobot.translate(x, y);
                double gunHeading = robot.getGunHeading();
                gRobot.rotate(Math.toRadians(gunHeading - 90));
                
                // Draw Gun: Rectangle at (0,0) after translate
                gRobot.setColor(Color.WHITE);
                gRobot.setStroke(new BasicStroke(3.0f));
                int gunLength = isLeader ? 45 : 35; // Leaders have longer gun
                gRobot.drawLine(0, 0, gunLength, 0); // Gun barrel
                
                // Draw Radar for Leaders only - Enhanced visualization
                if (isLeader) {
                    // Reset transform for radar (radar is mounted on gun, so use gun heading)
                    gRobot.setTransform(battlefieldG.getTransform());
                    gRobot.translate(x, y);
                    double radarHeading = robot.getGunHeading(); // Radar is mounted on gun
                    gRobot.rotate(Math.toRadians(radarHeading - 90));
                    
                    // Draw radar sweep arc (animated effect)
                    int radarRadius = 30;
                    int radarSweepAngle = 45; // 45 degrees sweep on each side
                    gRobot.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gRobot.setStroke(new BasicStroke(2.0f));
                    gRobot.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 100)); // Semi-transparent
                    // Draw sweep arc
                    gRobot.drawArc(-radarRadius, -radarRadius, radarRadius * 2, radarRadius * 2, 
                                   -radarSweepAngle, radarSweepAngle * 2);
                    
                    // Draw radar dish as a larger triangle pointing in scan direction
                    gRobot.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 220)); // Semi-transparent
                    gRobot.setStroke(new BasicStroke(2.5f));
                    // Larger triangle for Leaders
                    int[] xPoints = {0, -12, 12};
                    int[] yPoints = {-halfSize - 5, -halfSize + 5, -halfSize + 5}; // Positioned above robot body
                    gRobot.fillPolygon(xPoints, yPoints, 3);
                    gRobot.setColor(teamColor);
                    gRobot.drawPolygon(xPoints, yPoints, 3);
                    
                    // Draw radar center dot
                    gRobot.setColor(Color.CYAN);
                    gRobot.fillOval(-3, -halfSize - 5, 6, 6);
                    
                    // Draw crown/star icon above Leader
                    gRobot.setTransform(battlefieldG.getTransform());
                    gRobot.translate(x, y - halfSize - 20);
                    gRobot.setColor(new Color(255, 215, 0)); // Gold color for crown
                    gRobot.setFont(new Font("Monospaced", Font.BOLD, 16));
                    FontMetrics fm = gRobot.getFontMetrics();
                    String crown = "★"; // Star symbol for leader
                    int textWidth = fm.stringWidth(crown);
                    gRobot.drawString(crown, -textWidth / 2, 0);
                    
                    // Draw "LEADER" label below crown
                    gRobot.translate(0, 15);
                    gRobot.setColor(Color.WHITE);
                    gRobot.setFont(new Font("Monospaced", Font.BOLD, 10));
                    fm = gRobot.getFontMetrics();
                    String label = "LEADER";
                    textWidth = fm.stringWidth(label);
                    gRobot.drawString(label, -textWidth / 2, 0);
                }
            } finally {
                gRobot.dispose();
            }
            
            // MISSION F: Draw health bar above robot
            drawHealthBar(battlefieldG, robot, x, y);
        }
    }
    
    /**
     * MISSION F: Draw health bar above robot
     */
    private void drawHealthBar(Graphics2D g, Droid robot, int x, int y) {
        // Use proper BattleSetup constants
        double maxEnergy = (robot instanceof fr.ensibs.robots.logic.TeamLeader) 
            ? fr.ensibs.robots.logic.BattleSetup.ROBOT_INITIAL_ENERGY 
            : fr.ensibs.robots.logic.BattleSetup.DROID_INITIAL_ENERGY;
        
        double healthPercent = Math.max(0.0, Math.min(1.0, robot.getEnergy() / maxEnergy));
        
        int barWidth = 40;
        int barHeight = 4;
        int barX = x - barWidth / 2;
        int barY = y - 35; // Above robot
        
        // Background
        g.setColor(new Color(50, 50, 50));
        g.fillRect(barX, barY, barWidth, barHeight);
        
        // Health portion
        Color healthColor;
        if (healthPercent > 0.6) {
            healthColor = Color.GREEN;
        } else if (healthPercent > 0.3) {
            healthColor = Color.YELLOW;
        } else {
            healthColor = Color.RED;
        }
        
        g.setColor(healthColor);
        g.fillRect(barX, barY, (int)(barWidth * healthPercent), barHeight);
        
        // Border
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barWidth, barHeight);
    }
    
    /**
     * PART 1: Draw bullets with clean, simple geometry (circles only).
     * No trails, no glows - just yellow circles.
     */
    private void drawBulletsClean(Graphics2D battlefieldG)
    {
        if (battlefieldRef == null || !(battlefieldRef instanceof BattlefieldImpl)) {
            return;
        }
        BattlefieldImpl impl = (BattlefieldImpl) battlefieldRef;
        List<Bullet> bullets = impl.getBullets();
        
        for (Bullet bullet : bullets) {
            if (!bullet.isActive()) {
                continue;
            }
            
            Location loc = bullet.getLocation();
            int x = loc.getX();
            int y = loc.getY();
            
            // Create isolated Graphics copy for THIS bullet
            Graphics2D gBullet = (Graphics2D) battlefieldG.create();
            try {
                // Translate to bullet position
                gBullet.translate(x, y);
                
                // Draw simple yellow circle at (0,0) after translate
                gBullet.setColor(Color.YELLOW);
                gBullet.fillOval(-2, -2, 4, 4); // Radius 4px circle
            } finally {
                gBullet.dispose();
            }
        }
    }
    
    /**
     * SNAPSHOT PATTERN: Draw robots with strict Graphics isolation.
     * Each robot gets its own Graphics copy from the camera Graphics.
     */
    private void drawRobots(Graphics2D cameraG)
    {
        List<DroidView<? extends Droid>> views = getViews();
        
        // SNAPSHOT PATTERN: Each robot gets its own Graphics copy
        for (DroidView<? extends Droid> view : views) {
            Droid robot = view.getRobot();
            
            // Skip dead robots
            if (robot.getEnergy() <= 0) {
                continue;
            }
            
            // Use the color assigned to the view (from TeamInfo)
            Color teamColor = view.getColor();
            // DEBUG: Verify color is correct (can be removed later)
            if (teamColor == null) {
                System.err.println("WARNING: view.getColor() returned null for " + view.getName());
                teamColor = Color.YELLOW; // Fallback
            }
            
            // Check if this is a TeamLeader (larger, different shape)
            boolean isLeader = robot instanceof fr.ensibs.robots.logic.TeamLeader;
            
            Location loc = robot.getLocation();
            int x = loc.getX();
            int y = loc.getY();
            
            // SNAPSHOT PATTERN: Create isolated Graphics copy for THIS robot only
            Graphics2D gRobot = (Graphics2D) cameraG.create();
            try {
                // Move ONLY the copy to robot position
                gRobot.translate(x, y);
                
                // Draw body (at 0,0 because we translated the context)
                double bodyHeading = robot.getHeading();
                gRobot.rotate(Math.toRadians(bodyHeading - 90)); // Convert North->East
                
                if (isLeader) {
                    // LEADER: Larger hexagon shape to distinguish from droids
                    gRobot.setColor(teamColor);
                    gRobot.setStroke(new BasicStroke(4)); // Thicker outline for leader
                    
                    // Draw hexagon (6-sided shape)
                    int size = 28; // Larger than droids
                    java.awt.Polygon hexagon = new java.awt.Polygon();
                    for (int i = 0; i < 6; i++) {
                        double angle = Math.PI / 3 * i;
                        hexagon.addPoint((int)(size * Math.cos(angle)), (int)(size * Math.sin(angle)));
                    }
                    gRobot.fillPolygon(hexagon);
                    gRobot.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 255));
                    gRobot.drawPolygon(hexagon);
                    
                    // Draw "L" indicator for Leader
                    gRobot.setTransform(cameraG.getTransform());
                    gRobot.translate(x, y);
                    gRobot.setColor(Color.WHITE);
                    gRobot.setFont(new Font("Monospaced", Font.BOLD, 12));
                    gRobot.drawString("L", -5, 5);
                    gRobot.setTransform(cameraG.getTransform());
                    gRobot.translate(x, y);
                    gRobot.rotate(Math.toRadians(bodyHeading - 90));
                } else {
                    // DROID: Standard square shape
                gRobot.setColor(teamColor);
                gRobot.setStroke(new BasicStroke(3));
                gRobot.drawRect(-20, -20, 40, 40); // Outline at (0,0) relative to robot
                gRobot.setColor(new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 200));
                gRobot.fillRect(-20, -20, 40, 40); // Fill at (0,0)
                }
                
                // Reset rotation for gun
                gRobot.setTransform(cameraG.getTransform());
                gRobot.translate(x, y);
                
                // Draw gun (at 0,0 because we translated)
                double gunHeading = robot.getGunHeading();
                gRobot.rotate(Math.toRadians(gunHeading - 90));
                gRobot.setColor(Color.WHITE);
                gRobot.setStroke(new BasicStroke(3));
                gRobot.drawLine(0, 0, 35, 0); // Draw at (0,0) after translate
                
                // Draw radar if applicable
                if (robot instanceof fr.ensibs.robots.logic.Robot robotWithRadar) {
                    gRobot.setTransform(cameraG.getTransform());
                    gRobot.translate(x, y);
                    double radarHeading = robotWithRadar.getRadarHeading();
                    gRobot.rotate(Math.toRadians(radarHeading - 90));
                    gRobot.setColor(Color.GREEN);
                    gRobot.setStroke(new BasicStroke(2));
                    gRobot.drawOval(-10, -10, 20, 20); // Draw at (0,0) after translate
                }
                
                // Draw glow (at 0,0 because we translated)
                gRobot.setTransform(cameraG.getTransform());
                gRobot.translate(x, y);
                int glowRadius = 30;
                float[] fractions = {0.0f, 0.5f, 1.0f};
                Color[] glowColors = {
                    new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 30),
                    new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 15),
                    new Color(teamColor.getRed(), teamColor.getGreen(), teamColor.getBlue(), 0)
                };
                java.awt.geom.Point2D glowCenter = new java.awt.geom.Point2D.Float(0, 0);
                java.awt.RadialGradientPaint robotGlow = new java.awt.RadialGradientPaint(
                    glowCenter, glowRadius, fractions, glowColors
                );
                gRobot.setPaint(robotGlow);
                gRobot.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                gRobot.fillOval(-glowRadius, -glowRadius, glowRadius * 2, glowRadius * 2);
            } finally {
                // CRITICAL: Dispose copy - the 'pen' resets to origin
                gRobot.dispose();
            }
        }
    }
    
    /**
     * SNAPSHOT PATTERN: Draw additional entities (bullets) with strict Graphics isolation.
     * Each bullet gets its own Graphics copy from the camera Graphics.
     */
    private void drawAdditionalEntities(Graphics2D cameraG)
    {
        // CRITICAL: Remove inactive bullets immediately to prevent accumulation
        additionalDrawables.removeIf(drawable -> {
            if (drawable instanceof BulletView) {
                BulletView bulletView = (BulletView) drawable;
                return !bulletView.isVisible(); // Remove if not visible
            }
            return false;
        });
        
        // SNAPSHOT PATTERN: Each bullet gets its own Graphics copy
        for (Object drawable : new ArrayList<>(additionalDrawables)) {
            if (drawable instanceof BulletView) {
                BulletView bulletView = (BulletView) drawable;
                // SNAPSHOT PATTERN: Create isolated Graphics copy for THIS bullet
                Graphics2D gBullet = (Graphics2D) cameraG.create();
                try {
                    // BulletView.draw() will translate to bullet position and draw at (0,0)
                    bulletView.draw(gBullet);
                } finally {
                    // CRITICAL: Dispose copy
                    gBullet.dispose();
                }
            } else {
                // Fallback for other drawables
                try {
                    java.lang.reflect.Method drawMethod = drawable.getClass().getMethod("draw", Graphics2D.class);
                    Graphics2D gDrawable = (Graphics2D) cameraG.create();
                    try {
                        drawMethod.invoke(drawable, gDrawable);
                    } finally {
                        gDrawable.dispose();
                    }
                } catch (Exception e) {
                    // Skip if draw method doesn't exist
                }
            }
        }
    }
    
    /**
     * Draw FPS counter (bright green terminal style).
     */
    private void drawFPS(Graphics2D g2d)
    {
        g2d.setColor(new Color(0, 255, 0)); // Bright green
        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2d.drawString(String.format("FPS: %.1f", currentFPS), 10, 20);
    }
    
    /**
     * MISSION B: Draw phase indicator at top center of screen.
     */
    private void drawPhaseIndicator(Graphics2D g2d)
    {
        if (battlefieldRef == null || !(battlefieldRef instanceof BattlefieldImpl)) {
            return;
        }
        
        BattlefieldImpl impl = (BattlefieldImpl) battlefieldRef;
        GamePhaseManager pm = impl.getPhaseManager();
        if (pm == null) {
            return;
        }
        
        String phaseText = pm.getPhaseDisplayName();
        int remaining = pm.getRemainingSecondsInPhase();
        
        // Choose color based on phase
        Color phaseColor;
        switch (pm.getCurrentPhase()) {
            case DEPLOYMENT:
                phaseColor = Color.CYAN;
                break;
            case SKIRMISH:
                phaseColor = Color.GREEN;
                break;
            case PRESSURE:
                phaseColor = Color.ORANGE;
                break;
            case SUDDEN_DEATH:
                phaseColor = Color.RED;
                break;
            default:
                phaseColor = Color.WHITE;
        }
        
        // Draw phase name
        g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
        
        String displayText = phaseText;
        if (remaining > 0) {
            displayText += " - " + remaining + "s";
        }
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(displayText);
        int x = (getWidth() - textWidth) / 2;
        int y = 40;
        
        // Draw background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(x - 10, y - 20, textWidth + 20, 30);
        
        // Draw text
        g2d.setColor(phaseColor);
        g2d.drawString(displayText, x, y);
        
        // If deployment, show countdown
        if (pm.getCurrentPhase() == GamePhaseManager.Phase.DEPLOYMENT) {
            String subText = "Combat begins in " + remaining + " seconds";
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
            fm = g2d.getFontMetrics();
            int subWidth = fm.stringWidth(subText);
            g2d.setColor(Color.WHITE);
            g2d.drawString(subText, (getWidth() - subWidth) / 2, y + 25);
        }
    }
    
    // Winner announcement methods removed - only shown in kill feed
    
    /**
     * Get views list - use direct reference instead of reflection.
     */
    private List<DroidView<? extends Droid>> getViews()
    {
        // Use direct reference stored in constructor
        return viewsList != null ? viewsList : new ArrayList<>();
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
        // MISSION 2.3: Store battlefield reference for wreckage access
        this.battlefieldRef = battlefield;
        
        if (!(battlefield instanceof BattlefieldImpl)) {
            return;
        }
        BattlefieldImpl impl = (BattlefieldImpl) battlefield;
        
        additionalDrawables.removeIf(d -> d instanceof BulletView);
        
        // PHASE 4: Process visual effects events
        // Muzzle flash
        Object[] muzzleFlash = impl.getAndClearLastMuzzleFlash();
        if (muzzleFlash != null) {
            Location flashLoc = (Location) muzzleFlash[0];
            Double flashHeading = (Double) muzzleFlash[1];
            createMuzzleFlash(flashLoc, flashHeading);
        }
        
        
        // MISSION 2.1: Process damage events for camera shake
        // MISSION 4.2: Also check for kill streaks for announcer
        List<BattlefieldImpl.DamageEvent> damageEvents = impl.getAndClearRecentDamageEvents();
        for (BattlefieldImpl.DamageEvent event : damageEvents) {
            int intensity = event.isDeath ? 100 : event.damage;
            triggerCameraShake(intensity);
            
            // MISSION 4.2: Kill streak announcements (stored for dashboard access)
            if (event.isDeath && event.killer != null) {
                int killStreak = impl.getKillStreak(event.killer);
                String announcement = getKillStreakAnnouncement(killStreak);
                if (announcement != null) {
                    // Store for dashboard to retrieve
                    lastKillAnnouncement = announcement;
                }
            }
        }
        
        // CRITICAL FIX: Only add ACTIVE bullets to prevent accumulation
        List<Bullet> bullets = impl.getBullets();
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                BulletView bulletView = new BulletView(bullet);
                additionalDrawables.add(bulletView);
            }
        }
        
        // CRITICAL: Clean up inactive bullets immediately
        additionalDrawables.removeIf(d -> {
            if (d instanceof BulletView) {
                return !((BulletView) d).isVisible();
            }
            return false;
        });
        
        // MISSION 2.3: Update wreckage (for spark timing)
        impl.updateWreckages();
    }
    
    /**
     * SNAPSHOT PATTERN: Draw all wreckage with strict Graphics isolation.
     * 
     * @param cameraG the camera Graphics context
     */
    private void drawWreckages(Graphics2D cameraG)
    {
        if (battlefieldRef == null || !(battlefieldRef instanceof BattlefieldImpl)) {
            return;
        }
        BattlefieldImpl impl = (BattlefieldImpl) battlefieldRef;
        List<RobotWreckage> wreckages = impl.getWreckages();
        
        // SNAPSHOT PATTERN: Each wreckage gets its own Graphics copy
        for (RobotWreckage wreckage : wreckages) {
            Graphics2D gWreckage = (Graphics2D) cameraG.create();
            try {
                wreckage.draw(gWreckage);
            } finally {
                gWreckage.dispose();
            }
        }
    }
    
    /**
     * SNAPSHOT PATTERN: Draw all energy capsules with strict Graphics isolation.
     * 
     * @param cameraG the camera Graphics context
     */
    private void drawEnergyCapsules(Graphics2D cameraG)
    {
        if (battlefieldRef == null || !(battlefieldRef instanceof BattlefieldImpl)) {
            return;
        }
        BattlefieldImpl impl = (BattlefieldImpl) battlefieldRef;
        List<EnergyCapsule> capsules = impl.getEnergyCapsules();
        
        // SNAPSHOT PATTERN: Each capsule gets its own Graphics copy
        for (EnergyCapsule capsule : capsules) {
            Graphics2D gCapsule = (Graphics2D) cameraG.create();
            try {
                capsule.draw(gCapsule);
            } finally {
                gCapsule.dispose();
            }
        }
    }
    
    /**
     * Update particles and visual effects.
     * CRITICAL: Must be called every frame to prevent accumulation.
     */
    public void updateParticles()
    {
        muzzleFlashSystem.update();
        damageFlashSystem.update();
        // Camera shake is updated in paintComponent() for frame-perfect timing
    }
    
    /**
     * MISSION 2.1: Trigger camera shake for damage or death.
     * 
     * @param intensity the damage amount (use 100 for death)
     */
    public void triggerCameraShake(int intensity)
    {
        cameraShaker.triggerShake(intensity);
    }
    
    /**
     * MISSION 4.2: Get kill streak announcement text.
     * 
     * @param killStreak the kill streak count
     * @return announcement text or null if no special announcement
     */
    private String getKillStreakAnnouncement(int killStreak)
    {
        if (killStreak == 2) {
            return "DOUBLE KILL";
        } else if (killStreak == 3) {
            return "TRIPLE KILL";
        } else if (killStreak == 4) {
            return "QUADRA KILL";
        } else if (killStreak >= 5) {
            return "PENTA KILL";
        }
        return null;
    }
    
    /**
     * MISSION 4.2: Get and clear the last kill announcement.
     * 
     * @return the announcement text or null
     */
    public String getAndClearLastKillAnnouncement()
    {
        String announcement = lastKillAnnouncement;
        lastKillAnnouncement = null;
        return announcement;
    }
    
    public void createMuzzleFlash(fr.ensibs.robots.logic.Location location, double heading)
    {
        muzzleFlashSystem.createFlash(location, heading);
    }
    
    public void createHitEffect(fr.ensibs.robots.logic.Location location, Color color)
    {
        // Visual hit effects disabled in neon renderer to keep rendering stable
    }
    
    public void createExplosion(fr.ensibs.robots.logic.Location location, Color color, int intensity)
    {
        // Visual explosion effects disabled in neon renderer to keep rendering stable
    }
    
    public void registerDamageFlash(Droid droid)
    {
        damageFlashSystem.registerFlash(droid);
    }
    
}

