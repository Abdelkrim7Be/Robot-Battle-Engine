package fr.ensibs.robots.impl;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.view.ControlsPanel;
import fr.ensibs.robots.view.DroidView;
import fr.ensibs.robots.view.GraphicEngine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced battlefield frame with improved GUI and UX.
 * 
 * <p>Features:
 * <ul>
 *   <li>Enhanced battlefield panel with grid, HUD, and effects</li>
 *   <li>Better visual styling</li>
 *   <li>Improved layout and controls</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
public class EnhancedBattlefieldFrame extends JFrame
{
    private NeonBattlefieldPanel neonPanel;
    private BattleDashboard dashboard;
    private GraphicEngine graphicEngine;
    
    /**
     * Constructor
     */
    public EnhancedBattlefieldFrame(BattleFactory factory, RobotTaskFactory taskFactory)
    {
        super("BATTLEFIELD_TERMINAL // LIVE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Global dark sci-fi theme
        getContentPane().setBackground(new Color(26, 26, 26)); // #1A1A1A
        
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        setContentPane(mainPanel);
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.setBackground(new Color(26, 26, 26)); // #1A1A1A Dark Grey

        // shared list of robots views and battlefield
        List<DroidView<? extends Droid>> views = new ArrayList<>();

        // the neon battlefield at the center
        neonPanel = new NeonBattlefieldPanel(views);
        mainPanel.add(neonPanel, BorderLayout.CENTER);

        // Dashboard on the left
        dashboard = new BattleDashboard();
        dashboard.setPreferredSize(new Dimension(250, 0));
        mainPanel.add(dashboard, BorderLayout.WEST);

        // the controls on the right
        ControlsPanel controlsPanel = new ControlsPanel(views, factory, taskFactory);
        mainPanel.add(controlsPanel, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        
        // Start graphic engine at 60 FPS (16ms period)
        graphicEngine = new EnhancedGraphicEngine(neonPanel, dashboard, factory.makeBattlefield(), views);
        graphicEngine.start(16); // ~60 FPS for smooth rendering
    }
    
    /**
     * Enhanced graphic engine that also updates particles, bullets, and dashboard.
     */
    private static class EnhancedGraphicEngine extends GraphicEngine
    {
        private final NeonBattlefieldPanel panel;
        private final BattleDashboard dashboard;
        private final fr.ensibs.robots.logic.Battlefield battlefield;
        private final List<DroidView<? extends Droid>> views;
        
        EnhancedGraphicEngine(NeonBattlefieldPanel panel, BattleDashboard dashboard,
                             fr.ensibs.robots.logic.Battlefield battlefield,
                             List<DroidView<? extends Droid>> views)
        {
            super(panel);
            this.panel = panel;
            this.dashboard = dashboard;
            this.battlefield = battlefield;
            this.views = views;
        }
        
        @Override
        public void start(int period)
        {
            if (getTimer() == null) {
                javax.swing.Timer timer = new javax.swing.Timer(period, (e) -> {
                    // Update visual effects
                    panel.syncBullets(battlefield);
                    panel.updateParticles();
                    
                    // Update dashboard
                    dashboard.update(views);
                    
                    // Repaint
                    panel.repaint();
                });
                setTimer(timer);
                timer.start();
            }
        }
        
        // Accessor methods using reflection since timer is private
        private javax.swing.Timer getTimer()
        {
            try {
                java.lang.reflect.Field field = GraphicEngine.class.getDeclaredField("timer");
                field.setAccessible(true);
                return (javax.swing.Timer) field.get(this);
            } catch (Exception e) {
                return null;
            }
        }
        
        private void setTimer(javax.swing.Timer timer)
        {
            try {
                java.lang.reflect.Field field = GraphicEngine.class.getDeclaredField("timer");
                field.setAccessible(true);
                field.set(this, timer);
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    /**
     * Get the neon panel for external access.
     * 
     * @return the neon panel
     */
    public NeonBattlefieldPanel getNeonPanel()
    {
        return neonPanel;
    }
    
    /**
     * Get the dashboard for external access.
     * 
     * @return the dashboard
     */
    public BattleDashboard getDashboard()
    {
        return dashboard;
    }
}

