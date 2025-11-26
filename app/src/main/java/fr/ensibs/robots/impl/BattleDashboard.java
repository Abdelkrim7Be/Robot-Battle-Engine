package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.view.DroidView;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Battle dashboard panel showing leaderboard, kill feed, and game status.
 */
public class BattleDashboard extends JPanel
{
    private final JList<String> leaderboardList;
    private final DefaultListModel<String> leaderboardModel;
    private final JTextArea killFeed;
    private final JLabel statusLabel;
    private final JLabel robotsAliveLabel;
    private long roundStartTime;
    private int initialRobotCount;
    
    public BattleDashboard()
    {
        setLayout(new BorderLayout(5, 5));
        setBackground(new Color(20, 20, 30));
        setForeground(Color.WHITE);
        
        // Leaderboard
        leaderboardModel = new DefaultListModel<>();
        leaderboardList = new JList<>(leaderboardModel);
        leaderboardList.setBackground(new Color(25, 25, 35));
        leaderboardList.setForeground(Color.WHITE);
        leaderboardList.setFont(new Font("Monospaced", Font.BOLD, 12));
        leaderboardList.setSelectionBackground(new Color(0, 150, 255));
        
        JScrollPane leaderboardScroll = new JScrollPane(leaderboardList);
        leaderboardScroll.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 255), 2),
            "LEADERBOARD",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 14),
            new Color(0, 200, 255)));
        leaderboardScroll.setBackground(new Color(20, 20, 30));
        
        // Kill feed
        killFeed = new JTextArea(8, 20);
        killFeed.setEditable(false);
        killFeed.setBackground(new Color(25, 25, 35));
        killFeed.setForeground(new Color(255, 100, 100));
        killFeed.setFont(new Font("Monospaced", Font.PLAIN, 11));
        killFeed.setLineWrap(true);
        killFeed.setWrapStyleWord(true);
        
        JScrollPane killFeedScroll = new JScrollPane(killFeed);
        killFeedScroll.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(255, 50, 50), 2),
            "KILL FEED",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 14),
            new Color(255, 100, 100)));
        killFeedScroll.setBackground(new Color(20, 20, 30));
        
        // Status panel
        JPanel statusPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        statusPanel.setBackground(new Color(20, 20, 30));
        
        statusLabel = new JLabel("Round Time: 0:00");
        statusLabel.setForeground(new Color(0, 255, 150));
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        
        robotsAliveLabel = new JLabel("Robots Alive: 0/0");
        robotsAliveLabel.setForeground(new Color(255, 200, 0));
        robotsAliveLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        
        statusPanel.add(statusLabel);
        statusPanel.add(robotsAliveLabel);
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(leaderboardScroll, BorderLayout.CENTER);
        topPanel.add(statusPanel, BorderLayout.SOUTH);
        topPanel.setBackground(new Color(20, 20, 30));
        
        add(topPanel, BorderLayout.CENTER);
        add(killFeedScroll, BorderLayout.SOUTH);
    }
    
    /**
     * Update the dashboard with current game state.
     */
    public void update(List<DroidView<? extends Droid>> views)
    {
        updateLeaderboard(views);
        updateStatus(views);
    }
    
    /**
     * Update leaderboard sorted by energy.
     */
    private void updateLeaderboard(List<DroidView<? extends Droid>> views)
    {
        leaderboardModel.clear();
        
        List<DroidView<? extends Droid>> alive = views.stream()
            .filter(v -> v.getRobot().getEnergy() > 0)
            .sorted(Comparator.comparingInt((DroidView<? extends Droid> v) -> v.getRobot().getEnergy()).reversed())
            .collect(Collectors.toList());
        
        int rank = 1;
        for (DroidView<? extends Droid> view : alive) {
            Droid droid = view.getRobot();
            String entry = String.format("#%d %s [%d]", rank++, view.getName(), droid.getEnergy());
            leaderboardModel.addElement(entry);
        }
    }
    
    /**
     * Update game status.
     */
    private void updateStatus(List<DroidView<? extends Droid>> views)
    {
        long currentTime = System.currentTimeMillis();
        if (roundStartTime == 0) {
            roundStartTime = currentTime;
            initialRobotCount = (int) views.stream().filter(v -> v.getRobot().getEnergy() > 0).count();
        }
        
        long elapsed = (currentTime - roundStartTime) / 1000;
        long minutes = elapsed / 60;
        long seconds = elapsed % 60;
        statusLabel.setText(String.format("Round Time: %d:%02d", minutes, seconds));
        
        int alive = (int) views.stream().filter(v -> v.getRobot().getEnergy() > 0).count();
        robotsAliveLabel.setText(String.format("Robots Alive: %d/%d", alive, initialRobotCount));
    }
    
    /**
     * Add a kill feed entry.
     */
    public void addKillFeed(String message)
    {
        SwingUtilities.invokeLater(() -> {
            killFeed.append(message + "\n");
            // Auto-scroll to bottom
            killFeed.setCaretPosition(killFeed.getDocument().getLength());
            // Limit to last 50 entries
            String text = killFeed.getText();
            String[] lines = text.split("\n");
            if (lines.length > 50) {
                StringBuilder sb = new StringBuilder();
                for (int i = lines.length - 50; i < lines.length; i++) {
                    sb.append(lines[i]).append("\n");
                }
                killFeed.setText(sb.toString());
            }
        });
    }
    
    /**
     * Reset dashboard for new round.
     */
    public void reset()
    {
        roundStartTime = 0;
        initialRobotCount = 0;
        killFeed.setText("");
        leaderboardModel.clear();
    }
}

