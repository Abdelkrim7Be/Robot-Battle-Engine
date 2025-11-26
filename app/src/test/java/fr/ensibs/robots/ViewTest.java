package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.EnhancedBattlefieldPanel;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.view.BattlefieldFrame;
import fr.ensibs.robots.view.DroidView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the graphical view components.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Window initializes without crashing</li>
 *   <li>Canvas dimensions match battlefield size</li>
 *   <li>Rendering loop works independently of physics</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class ViewTest
{
    private BattleFactory factory;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Functional test: Initialize the window and ensure it launches without crashing.
     * Check dimensions match battlefield size.
     */
    @Test
    void testWindowInitialization()
    {
        // Create a simple frame to test window setup
        JFrame frame = new JFrame("Test Battlefield");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create panel with empty view list
        List<DroidView<? extends Droid>> views = new ArrayList<>();
        EnhancedBattlefieldPanel panel = new EnhancedBattlefieldPanel(views);
        
        // Add panel to frame
        frame.getContentPane().add(panel);
        frame.pack();
        
        // Verify panel has correct preferred size
        Dimension preferredSize = panel.getPreferredSize();
        assertEquals(FIELD_WIDTH, preferredSize.width,
            "Panel width should match FIELD_WIDTH");
        assertEquals(FIELD_HEIGHT, preferredSize.height,
            "Panel height should match FIELD_HEIGHT");
        
        // Verify frame is not null and can be created
        assertNotNull(frame, "Frame should be created");
        assertNotNull(panel, "Panel should be created");
        
        // Clean up
        frame.dispose();
    }

    /**
     * Test that the panel can be rendered without errors.
     */
    @Test
    void testPanelRendering()
    {
        List<DroidView<? extends Droid>> views = new ArrayList<>();
        EnhancedBattlefieldPanel panel = new EnhancedBattlefieldPanel(views);
        
        // Create a frame to host the panel
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(false); // Don't actually show, just test setup
        
        // Test that render() method works
        assertDoesNotThrow(() -> {
            panel.render();
        }, "render() should not throw exceptions");
        
        // Verify panel is double buffered
        assertTrue(panel.isDoubleBuffered(),
            "Panel should have double buffering enabled");
        
        frame.dispose();
    }

    /**
     * Test that the panel dimensions are correct.
     */
    @Test
    void testPanelDimensions()
    {
        List<DroidView<? extends Droid>> views = new ArrayList<>();
        EnhancedBattlefieldPanel panel = new EnhancedBattlefieldPanel(views);
        
        Dimension preferredSize = panel.getPreferredSize();
        
        // Verify dimensions match battlefield size
        assertEquals(FIELD_WIDTH, preferredSize.width,
            "Panel preferred width should be FIELD_WIDTH");
        assertEquals(FIELD_HEIGHT, preferredSize.height,
            "Panel preferred height should be FIELD_HEIGHT");
    }

    /**
     * Test that the rendering loop can be called independently (MVC pattern).
     */
    @Test
    void testRenderingLoopSeparation()
    {
        List<DroidView<? extends Droid>> views = new ArrayList<>();
        EnhancedBattlefieldPanel panel = new EnhancedBattlefieldPanel(views);
        
        // Create frame
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(false);
        
        // Test that render() can be called multiple times independently
        // This simulates the rendering loop being separate from physics loop
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                panel.render();
            }
        }, "render() should be callable independently of physics updates");
        
        frame.dispose();
    }

    /**
     * Test that the panel can handle empty view list.
     */
    @Test
    void testEmptyViewList()
    {
        List<DroidView<? extends Droid>> views = new ArrayList<>();
        EnhancedBattlefieldPanel panel = new EnhancedBattlefieldPanel(views);
        
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(false);
        
        // Should render without errors even with no robots
        assertDoesNotThrow(() -> {
            panel.render();
        }, "Panel should render without errors even with empty view list");
        
        frame.dispose();
    }
}

