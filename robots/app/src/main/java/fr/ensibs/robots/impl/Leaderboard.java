package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.view.DroidView;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Leaderboard component that displays top robots ranked by energy.
 * 
 * <p>Shows:
 * <ul>
 *   <li>Top N robots (default 5)</li>
 *   <li>Rank, name, and energy</li>
 *   <li>Color-coded by team</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
public class Leaderboard
{
    private static final int DEFAULT_TOP_COUNT = 5;
    private static final int LEADERBOARD_WIDTH = 200;
    private static final int ROW_HEIGHT = 25;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font ROW_FONT = new Font("Arial", Font.PLAIN, 11);
    
    private final int topCount;
    
    /**
     * Constructor with default top count (5).
     */
    public Leaderboard()
    {
        this(DEFAULT_TOP_COUNT);
    }
    
    /**
     * Constructor.
     * 
     * @param topCount number of top robots to display
     */
    public Leaderboard(int topCount)
    {
        this.topCount = topCount;
    }
    
    /**
     * Draw the leaderboard.
     * 
     * @param g2d the graphics context
     * @param views the robot views
     * @param x the X position
     * @param y the Y position
     */
    public void draw(Graphics2D g2d, List<DroidView<? extends Droid>> views, int x, int y)
    {
        // Get top robots sorted by energy
        List<DroidView<? extends Droid>> topRobots = getTopRobots(views);
        
        if (topRobots.isEmpty()) {
            return;
        }
        
        // Calculate height
        int height = 30 + (topRobots.size() * ROW_HEIGHT);
        
        // Draw background
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRoundRect(x, y, LEADERBOARD_WIDTH, height, 10, 10);
        
        // Draw border
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(x, y, LEADERBOARD_WIDTH, height, 10, 10);
        
        // Draw title
        g2d.setFont(TITLE_FONT);
        g2d.setColor(Color.WHITE);
        String title = "LEADERBOARD";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = x + (LEADERBOARD_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, y + 20);
        
        // Draw separator
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawLine(x + 5, y + 25, x + LEADERBOARD_WIDTH - 5, y + 25);
        
        // Draw rows
        int rowY = y + 35;
        for (int i = 0; i < topRobots.size(); i++) {
            DroidView<? extends Droid> view = topRobots.get(i);
            drawRow(g2d, x + 5, rowY, i + 1, view);
            rowY += ROW_HEIGHT;
        }
    }
    
    /**
     * Get top robots sorted by energy.
     */
    private List<DroidView<? extends Droid>> getTopRobots(List<DroidView<? extends Droid>> views)
    {
        return views.stream()
            .filter(view -> view.getRobot().getEnergy() > 0) // Only alive robots
            .sorted(Comparator.comparingInt((DroidView<? extends Droid> v) -> v.getRobot().getEnergy()).reversed())
            .limit(topCount)
            .collect(Collectors.toList());
    }
    
    /**
     * Draw a single leaderboard row.
     */
    private void drawRow(Graphics2D g2d, int x, int y, int rank, DroidView<? extends Droid> view)
    {
        Droid robot = view.getRobot();
        
        // Draw rank badge
        int badgeSize = 20;
        g2d.setColor(view.getColor());
        g2d.fillOval(x, y - badgeSize + 5, badgeSize, badgeSize);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String rankText = Integer.toString(rank);
        FontMetrics fm = g2d.getFontMetrics();
        int rankX = x + (badgeSize - fm.stringWidth(rankText)) / 2;
        g2d.drawString(rankText, rankX, y - 2);
        
        // Draw name
        g2d.setFont(ROW_FONT);
        g2d.setColor(Color.WHITE);
        String name = view.getName();
        if (name.length() > 15) {
            name = name.substring(0, 13) + "..";
        }
        g2d.drawString(name, x + badgeSize + 5, y);
        
        // Draw energy
        String energyText = Integer.toString(robot.getEnergy());
        int energyX = x + LEADERBOARD_WIDTH - 10 - fm.stringWidth(energyText);
        g2d.drawString(energyText, energyX, y);
    }
    
    /**
     * Get the preferred width of the leaderboard.
     * 
     * @return the width
     */
    public int getWidth()
    {
        return LEADERBOARD_WIDTH;
    }
}

