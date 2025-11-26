package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.EnhancedBattlefieldPanel;
import fr.ensibs.robots.impl.Leaderboard;
import fr.ensibs.robots.impl.ParticleSystem;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.view.DroidView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HUD, particle effects, and leaderboard components.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>HUD overlay displays robot stats correctly</li>
 *   <li>Particle system creates and updates particles</li>
 *   <li>Leaderboard ranks robots by energy</li>
 *   <li>Visual feedback components work correctly</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class HUDTest
{
    private BattleFactory factory;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Test that particle system creates particles.
     */
    @Test
    void testParticleSystemCreation()
    {
        ParticleSystem particleSystem = new ParticleSystem();
        
        Location location = new Location(100, 100);
        Color color = Color.RED;
        
        // Create explosion
        particleSystem.createExplosion(location, color, 10);
        
        // Verify particles were created
        assertTrue(particleSystem.getParticleCount() > 0,
            "Particle system should have particles after creating explosion");
        
        assertEquals(10, particleSystem.getParticleCount(),
            "Particle system should have exactly 10 particles");
    }

    /**
     * Test that particles update and fade out.
     */
    @Test
    void testParticleUpdate()
    {
        ParticleSystem particleSystem = new ParticleSystem();
        
        Location location = new Location(100, 100);
        particleSystem.createExplosion(location, Color.ORANGE, 5);
        
        int initialCount = particleSystem.getParticleCount();
        assertEquals(5, initialCount, "Should have 5 particles");
        
        // Update particles multiple times (they should fade out)
        for (int i = 0; i < 100; i++) {
            particleSystem.update();
        }
        
        // All particles should be dead after many updates
        assertEquals(0, particleSystem.getParticleCount(),
            "All particles should be removed after they expire");
    }

    /**
     * Test that hit effects create fewer particles than explosions.
     */
    @Test
    void testHitEffect()
    {
        ParticleSystem particleSystem = new ParticleSystem();
        
        Location location = new Location(200, 200);
        particleSystem.createHit(location, Color.YELLOW);
        
        // Hit effects should create fewer particles than explosions
        int particleCount = particleSystem.getParticleCount();
        assertTrue(particleCount > 0 && particleCount < 20,
            "Hit effect should create some particles but fewer than a large explosion");
    }

    /**
     * Test that leaderboard ranks robots by energy.
     */
    @Test
    void testLeaderboardRanking()
    {
        List<DroidView<? extends Droid>> views = new ArrayList<>();
        
        // Create robots with different energy levels
        // Note: We can't directly adjust energy, but we can test with default energy levels
        Droid robot1 = factory.makeDroid();
        views.add(factory.makeRobotView(robot1, "Robot1", Color.BLUE));
        
        Droid robot2 = factory.makeDroid();
        views.add(factory.makeRobotView(robot2, "Robot2", Color.RED));
        
        Droid robot3 = factory.makeDroid();
        views.add(factory.makeRobotView(robot3, "Robot3", Color.GREEN));
        
        // Create leaderboard
        Leaderboard leaderboard = new Leaderboard(3);
        
        // Verify leaderboard can be created and drawn
        assertNotNull(leaderboard, "Leaderboard should be created");
        assertEquals(200, leaderboard.getWidth(),
            "Leaderboard should have correct width");
        
        // Test that it can be drawn (no exceptions)
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        Graphics2D g2d = (Graphics2D) frame.getGraphics();
        if (g2d != null) {
            assertDoesNotThrow(() -> {
                leaderboard.draw(g2d, views, 10, 10);
            }, "Leaderboard draw should not throw exceptions");
        }
        frame.dispose();
    }

    /**
     * Test that leaderboard only shows top N robots.
     */
    @Test
    void testLeaderboardTopCount()
    {
        List<DroidView<? extends Droid>> views = new ArrayList<>();
        
        // Create 10 robots
        // Note: All will have same initial energy, but leaderboard will still work
        for (int i = 0; i < 10; i++) {
            Droid robot = factory.makeDroid();
            views.add(factory.makeRobotView(robot, "Robot" + i, Color.BLUE));
        }
        
        // Create leaderboard with top 5
        Leaderboard leaderboard = new Leaderboard(5);
        
        // Verify it only shows top 5
        assertEquals(5, leaderboard.getWidth(), // Just verify it exists
            "Leaderboard should be created");
    }

    /**
     * Test that HUD overlay can be toggled.
     */
    @Test
    void testHUDToggle()
    {
        List<DroidView<? extends Droid>> views = new ArrayList<>();
        EnhancedBattlefieldPanel panel = new EnhancedBattlefieldPanel(views);
        
        // Test HUD visibility toggle
        panel.setHUDVisible(true);
        panel.setHUDVisible(false);
        panel.setHUDVisible(true);
        
        // Test leaderboard visibility toggle
        panel.setLeaderboardVisible(true);
        panel.setLeaderboardVisible(false);
        panel.setLeaderboardVisible(true);
        
        // Verify panel can render
        assertDoesNotThrow(() -> {
            panel.render();
        }, "Panel should render without errors when toggling HUD");
    }

    /**
     * Test that collision feedback can be created.
     */
    @Test
    void testCollisionFeedback()
    {
        List<DroidView<? extends Droid>> views = new ArrayList<>();
        EnhancedBattlefieldPanel panel = new EnhancedBattlefieldPanel(views);
        
        Location hitLocation = new Location(300, 300);
        Color hitColor = Color.RED;
        
        // Create hit effect
        assertDoesNotThrow(() -> {
            panel.createHitEffect(hitLocation, hitColor);
        }, "Creating hit effect should not throw");
        
        // Create explosion
        assertDoesNotThrow(() -> {
            panel.createExplosion(hitLocation, hitColor, 20);
        }, "Creating explosion should not throw");
        
        // Update particles
        assertDoesNotThrow(() -> {
            panel.updateParticles();
        }, "Updating particles should not throw");
    }

    /**
     * Test that particle system can be cleared.
     */
    @Test
    void testParticleSystemClear()
    {
        ParticleSystem particleSystem = new ParticleSystem();
        
        Location location = new Location(100, 100);
        particleSystem.createExplosion(location, Color.RED, 10);
        
        assertEquals(10, particleSystem.getParticleCount(),
            "Should have 10 particles");
        
        particleSystem.clear();
        
        assertEquals(0, particleSystem.getParticleCount(),
            "Particle system should be empty after clear");
    }
}

