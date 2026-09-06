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
        setBackground(new Color(13, 13, 13)); // #0D0D0D Near Black
        setForeground(new Color(0, 255, 0)); // Terminal Green
        setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 2)); // Bright green border
        
        // Leaderboard
        leaderboardModel = new DefaultListModel<>();
        leaderboardList = new JList<>(leaderboardModel);
        leaderboardList.setBackground(new Color(0, 0, 0)); // Pure black
        leaderboardList.setForeground(new Color(0, 255, 0)); // Bright green
        leaderboardList.setFont(new Font("Monospaced", Font.BOLD, 12));
        leaderboardList.setSelectionBackground(new Color(0, 100, 0)); // Dark green selection
        // Fix: Ensure JList can display full text (no wrapping/truncation)
        leaderboardList.setPrototypeCellValue("UNIT_99: [|||||] 100% | DMG:9999 DPS:999.9 K:99"); // Set prototype for width calculation
        // Use custom renderer to ensure full text display
        leaderboardList.setCellRenderer(new javax.swing.ListCellRenderer<String>() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel(value);
                label.setFont(new Font("Monospaced", Font.BOLD, 12));
                label.setBackground(isSelected ? new Color(0, 100, 0) : new Color(0, 0, 0));
                label.setForeground(new Color(0, 255, 0));
                label.setOpaque(true);
                return label;
            }
        });
        
        JScrollPane leaderboardScroll = new JScrollPane(leaderboardList);
        leaderboardScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Allow horizontal scroll if needed
        leaderboardScroll.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 0), 2), // Bright green
            "BATTLE_LOG // LEADERBOARD",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 14),
            new Color(0, 255, 0))); // Bright green
        leaderboardScroll.setBackground(new Color(13, 13, 13));
        leaderboardScroll.getViewport().setBackground(new Color(0, 0, 0));
        
        // Kill feed
        killFeed = new JTextArea(8, 20);
        killFeed.setEditable(false);
        killFeed.setBackground(new Color(0, 0, 0)); // Pure black
        killFeed.setForeground(new Color(0, 255, 0)); // Bright green
        killFeed.setFont(new Font("Monospaced", Font.PLAIN, 11));
        killFeed.setLineWrap(true);
        killFeed.setWrapStyleWord(true);
        
        JScrollPane killFeedScroll = new JScrollPane(killFeed);
        killFeedScroll.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(0, 150, 0), 2), // Bright green
            "KILL_FEED // LIVE",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 14),
            new Color(0, 255, 0))); // Bright green
        killFeedScroll.setBackground(new Color(13, 13, 13));
        killFeedScroll.getViewport().setBackground(new Color(0, 0, 0));
        
        // Status panel
        JPanel statusPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        statusPanel.setBackground(new Color(0, 0, 0)); // Pure black
        statusPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 2)); // Bright green
        
        statusLabel = new JLabel("ROUND_TIME: 0:00");
        statusLabel.setForeground(new Color(0, 255, 0)); // Bright green
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        
        robotsAliveLabel = new JLabel("UNITS_ALIVE: 0/0");
        robotsAliveLabel.setForeground(new Color(0, 255, 0)); // Bright green
        robotsAliveLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        
        statusPanel.add(statusLabel);
        statusPanel.add(robotsAliveLabel);
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(leaderboardScroll, BorderLayout.CENTER);
        topPanel.add(statusPanel, BorderLayout.SOUTH);
        topPanel.setBackground(new Color(13, 13, 13));
        
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
     * Update leaderboard sorted by energy, with MISSION 4.2: DPS and damage stats.
     */
    private void updateLeaderboard(List<DroidView<? extends Droid>> views)
    {
        leaderboardModel.clear();
        
        // Get damage tracker if available
        DamageTracker damageTracker = null;
        if (!views.isEmpty()) {
            Droid firstDroid = views.get(0).getRobot();
            if (firstDroid instanceof BaseDroid) {
                BaseDroid baseDroid = (BaseDroid) firstDroid;
                if (baseDroid.getBattlefield() instanceof BattlefieldImpl) {
                    BattlefieldImpl impl = (BattlefieldImpl) baseDroid.getBattlefield();
                    damageTracker = impl.getDamageTracker();
                }
            }
        }
        
        List<DroidView<? extends Droid>> alive = views.stream()
            .filter(v -> v.getRobot().getEnergy() > 0)
            .sorted(Comparator.comparingInt((DroidView<? extends Droid> v) -> v.getRobot().getEnergy()).reversed())
            .collect(Collectors.toList());
        
        int rank = 1;
        for (DroidView<? extends Droid> view : alive) {
            Droid droid = view.getRobot();
            int maxEnergy = droid instanceof fr.ensibs.robots.logic.Robot 
                ? fr.ensibs.robots.logic.BattleSetup.ROBOT_INITIAL_ENERGY 
                : fr.ensibs.robots.logic.BattleSetup.DROID_INITIAL_ENERGY;
            double energyPercent = (droid.getEnergy() / (double) maxEnergy) * 100;
            int bars = (int) (energyPercent / 20); // 5 bars max
            String barStr = "|".repeat(bars) + " ".repeat(5 - bars);
            
            // MISSION 4.2: Add DPS and damage stats
            String entry;
            if (damageTracker != null) {
                double dps = damageTracker.getDPS(droid);
                int totalDamage = damageTracker.getTotalDamage(droid);
                int kills = damageTracker.getKills(droid);
                entry = String.format("UNIT_%02d: [%s] %3.0f%% | DMG:%d DPS:%.1f K:%d", 
                    rank++, barStr, energyPercent, totalDamage, dps, kills);
                // Ensure entry fits in display (truncate if too long, though shouldn't be with monospaced font)
                if (entry.length() > 60) {
                    entry = entry.substring(0, 57) + "...";
                }
            } else {
                entry = String.format("UNIT_%02d: [%s] %3.0f%% ENRG", rank++, barStr, energyPercent);
            }
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
        statusLabel.setText(String.format("ROUND_TIME: %d:%02d", minutes, seconds));
        
        int alive = (int) views.stream().filter(v -> v.getRobot().getEnergy() > 0).count();
        robotsAliveLabel.setText(String.format("UNITS_ALIVE: %d/%d", alive, initialRobotCount));
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
     * MISSION 4.2: Add kill streak announcement (e.g., "DOUBLE KILL", "PENTA KILL").
     * 
     * @param announcement the announcement text
     */
    public void addKillStreakAnnouncement(String announcement)
    {
        if (announcement != null && !announcement.isEmpty()) {
            addKillFeed(">>> " + announcement + " <<<");
        }
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

