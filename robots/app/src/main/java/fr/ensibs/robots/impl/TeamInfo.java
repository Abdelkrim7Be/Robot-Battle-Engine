package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;
import fr.ensibs.robots.logic.TeamLeader;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Data class holding information about a loaded team.
 * 
 * <p>Contains team metadata, JAR file reference, color, and class references
 * for leader and droids.
 */
public class TeamInfo
{
    private final String name;
    private final File jarFile;
    private Color color;
    private final Class<? extends RobotTask<TeamLeader>> leaderClass;
    private final List<Class<? extends RobotTask<Robot>>> droidClasses;
    
    // Runtime instances (set after battle starts)
    private TeamLeader leaderInstance;
    private List<Robot> droidInstances;
    
    /**
     * Constructor
     * 
     * @param name the team name
     * @param jarFile the JAR file containing the team
     * @param color the team color
     * @param leaderClass the leader class
     * @param droidClasses the droid classes
     */
    public TeamInfo(String name, File jarFile, Color color,
                    Class<? extends RobotTask<TeamLeader>> leaderClass,
                    List<Class<? extends RobotTask<Robot>>> droidClasses)
    {
        this.name = name;
        this.jarFile = jarFile;
        this.color = color;
        this.leaderClass = leaderClass;
        this.droidClasses = new ArrayList<>(droidClasses);
        this.droidInstances = new ArrayList<>();
    }
    
    /**
     * Get the team name
     * 
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Get the JAR file
     * 
     * @return the JAR file
     */
    public File getJarFile()
    {
        return jarFile;
    }
    
    /**
     * Get the team color
     * 
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }
    
    /**
     * Set the team color
     * 
     * @param color the new color
     */
    public void setColor(Color color)
    {
        this.color = color;
    }
    
    /**
     * Get the leader class
     * 
     * @return the leader class
     */
    public Class<? extends RobotTask<TeamLeader>> getLeaderClass()
    {
        return leaderClass;
    }
    
    /**
     * Get the droid classes
     * 
     * @return list of droid classes
     */
    public List<Class<? extends RobotTask<Robot>>> getDroidClasses()
    {
        return new ArrayList<>(droidClasses);
    }
    
    /**
     * Get the leader instance (set after battle starts)
     * 
     * @return the leader instance, or null if not yet instantiated
     */
    public TeamLeader getLeaderInstance()
    {
        return leaderInstance;
    }
    
    /**
     * Set the leader instance
     * 
     * @param leaderInstance the leader instance
     */
    public void setLeaderInstance(TeamLeader leaderInstance)
    {
        this.leaderInstance = leaderInstance;
    }
    
    /**
     * Get the droid instances (set after battle starts)
     * 
     * @return list of droid instances
     */
    public List<Robot> getDroidInstances()
    {
        return new ArrayList<>(droidInstances);
    }
    
    /**
     * Add a droid instance
     * 
     * @param droid the droid instance
     */
    public void addDroidInstance(Robot droid)
    {
        droidInstances.add(droid);
    }
    
    @Override
    public String toString()
    {
        return name + " [" + color + "]";
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TeamInfo)) return false;
        TeamInfo teamInfo = (TeamInfo) o;
        return name.equals(teamInfo.name) && jarFile.equals(teamInfo.jarFile);
    }
    
    @Override
    public int hashCode()
    {
        return name.hashCode() * 31 + jarFile.hashCode();
    }
}

