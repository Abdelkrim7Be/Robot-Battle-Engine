package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Droid;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks damage dealt, kills, and DPS for each robot.
 * 
 * <p>MISSION 4.2: Real-time DPS & Kill Feed
 * Tracks damage dealt in real-time and kill streaks for announcer.
 * 
 * @author Robot Wars Team
 */
public class DamageTracker
{
    private static class RobotStats
    {
        int totalDamageDealt;
        int kills;
        int killStreak;
        long lastKillTime;
        long battleStartTime;
        
        RobotStats()
        {
            this.totalDamageDealt = 0;
            this.kills = 0;
            this.killStreak = 0;
            this.lastKillTime = 0;
            this.battleStartTime = System.currentTimeMillis();
        }
        
        double getDPS(long currentTime)
        {
            long elapsedSeconds = Math.max(1, (currentTime - battleStartTime) / 1000);
            return totalDamageDealt / (double) elapsedSeconds;
        }
    }
    
    private final Map<Droid, RobotStats> stats = new HashMap<>();
    private long battleStartTime = System.currentTimeMillis();
    
    /**
     * Record damage dealt by a robot.
     * 
     * @param attacker the robot that dealt damage
     * @param damage the amount of damage dealt
     */
    public void recordDamage(Droid attacker, int damage)
    {
        if (attacker == null || damage <= 0) {
            return;
        }
        
        RobotStats stat = stats.computeIfAbsent(attacker, k -> new RobotStats());
        stat.totalDamageDealt += damage;
    }
    
    /**
     * Record a kill by a robot.
     * 
     * @param killer the robot that got the kill
     * @return the kill streak count (for announcer)
     */
    public int recordKill(Droid killer)
    {
        if (killer == null) {
            return 0;
        }
        
        RobotStats stat = stats.computeIfAbsent(killer, k -> new RobotStats());
        stat.kills++;
        stat.killStreak++;
        stat.lastKillTime = System.currentTimeMillis();
        
        return stat.killStreak;
    }
    
    /**
     * Reset kill streak for a robot (when they die).
     * 
     * @param robot the robot that died
     */
    public void resetKillStreak(Droid robot)
    {
        if (robot == null) {
            return;
        }
        
        RobotStats stat = stats.get(robot);
        if (stat != null) {
            stat.killStreak = 0;
        }
    }
    
    /**
     * Get total damage dealt by a robot.
     * 
     * @param robot the robot
     * @return total damage dealt
     */
    public int getTotalDamage(Droid robot)
    {
        RobotStats stat = stats.get(robot);
        return stat != null ? stat.totalDamageDealt : 0;
    }
    
    /**
     * Get DPS (damage per second) for a robot.
     * 
     * @param robot the robot
     * @return DPS value
     */
    public double getDPS(Droid robot)
    {
        RobotStats stat = stats.get(robot);
        if (stat == null) {
            return 0.0;
        }
        return stat.getDPS(System.currentTimeMillis());
    }
    
    /**
     * Get kill count for a robot.
     * 
     * @param robot the robot
     * @return kill count
     */
    public int getKills(Droid robot)
    {
        RobotStats stat = stats.get(robot);
        return stat != null ? stat.kills : 0;
    }
    
    /**
     * Get current kill streak for a robot.
     * 
     * @param robot the robot
     * @return kill streak count
     */
    public int getKillStreak(Droid robot)
    {
        RobotStats stat = stats.get(robot);
        return stat != null ? stat.killStreak : 0;
    }
    
    /**
     * Reset all stats for a new battle.
     */
    public void reset()
    {
        stats.clear();
        battleStartTime = System.currentTimeMillis();
    }
}

