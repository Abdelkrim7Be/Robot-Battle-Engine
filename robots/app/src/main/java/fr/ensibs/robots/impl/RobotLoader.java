package fr.ensibs.robots.impl;

import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;
import fr.ensibs.robots.logic.TeamLeader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading robot teams from JAR files.
 * 
 * <p>This class wraps RobotTaskFactory to provide a simpler interface
 * for loading teams and extracting team information.
 */
public class RobotLoader
{
    private final RobotTaskFactory taskFactory;
    
    /**
     * Constructor
     * 
     * @param taskFactory the robot task factory to use for loading
     */
    public RobotLoader(RobotTaskFactory taskFactory)
    {
        this.taskFactory = taskFactory;
    }
    
    /**
     * Load robots from a JAR file.
     * 
     * @param jarFile the JAR file to load
     * @return TeamInfo containing the loaded team information
     * @throws Exception if loading fails
     */
    public TeamInfo loadTeamFromJar(File jarFile) throws Exception
    {
        if (jarFile == null || !jarFile.exists()) {
            throw new IllegalArgumentException("JAR file does not exist: " + jarFile);
        }
        
        // Extract team name from filename (e.g., "AbdelkrimS.jar" -> "AbdelkrimS")
        String fileName = jarFile.getName();
        String teamName = fileName.replace(".jar", "");
        
        // Load the JAR
        taskFactory.loadJar(jarFile);
        
        // Find leader and droid classes
        List<Class<? extends RobotTask<TeamLeader>>> leaderClasses = taskFactory.listLeaderClasses();
        List<Class<? extends RobotTask<Robot>>> droidClasses = taskFactory.listRobotClasses();
        
        if (leaderClasses.isEmpty()) {
            throw new IllegalStateException("No leader class found in JAR: " + jarFile);
        }
        
        // Use first leader class found
        Class<? extends RobotTask<TeamLeader>> leaderClass = leaderClasses.get(0);
        
        // Filter droid classes (exclude leader classes that might be in robot list)
        List<Class<? extends RobotTask<Robot>>> filteredDroidClasses = new ArrayList<>();
        String leaderClassName = leaderClass.getSimpleName();
        
        for (Class<? extends RobotTask<Robot>> droidClass : droidClasses) {
            // Check if this is actually a droid (not a leader)
            // Leaders implement RobotTask<TeamLeader>, droids implement RobotTask<Robot>
            String droidClassName = droidClass.getSimpleName();
            
            // Exclude if it's the leader class or contains "Leader" in name
            if (!leaderClass.equals(droidClass) && 
                !droidClassName.contains("Leader") &&
                !droidClassName.equals(leaderClassName)) {
                filteredDroidClasses.add(droidClass);
            }
        }
        
        // If no droids found, try to find classes with "Droid" in the name
        if (filteredDroidClasses.isEmpty()) {
            for (Class<? extends RobotTask<Robot>> droidClass : droidClasses) {
                String className = droidClass.getSimpleName();
                if (className.contains("Droid") && !className.contains("Leader")) {
                    filteredDroidClasses.add(droidClass);
                }
            }
        }
        
        // Try to extract team color from leader class if it has TEAM_COLOR constant
        java.awt.Color teamColor = java.awt.Color.CYAN; // Default
        try {
            java.lang.reflect.Field colorField = leaderClass.getField("TEAM_COLOR");
            teamColor = (java.awt.Color) colorField.get(null);
            AppLog.debug("Loaded TEAM_COLOR from JAR: " + teamColor + " (RGB: "
                + teamColor.getRed() + "," + teamColor.getGreen() + "," + teamColor.getBlue() + ")");
        } catch (Exception e) {
            AppLog.debug("No TEAM_COLOR in JAR, using default CYAN");
        }
        
        // Try to extract team name from leader class if it has TEAM_NAME constant
        try {
            java.lang.reflect.Field nameField = leaderClass.getField("TEAM_NAME");
            teamName = (String) nameField.get(null);
        } catch (Exception e) {
            // No TEAM_NAME constant - use filename
        }
        
        return new TeamInfo(teamName, jarFile, teamColor, leaderClass, filteredDroidClasses);
    }
    
    /**
     * Check if a class is a robot class (implements RobotTask).
     * 
     * @param clazz the class to check
     * @return true if it's a robot class
     */
    public static boolean isRobotClass(Class<?> clazz)
    {
        return RobotTask.class.isAssignableFrom(clazz) && 
               !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers());
    }
    
    /**
     * Get all loaded robot class names.
     * 
     * @return list of class names
     */
    public List<String> getLoadedRobotNames()
    {
        List<String> names = new ArrayList<>();
        List<Class<? extends RobotTask<TeamLeader>>> leaders = taskFactory.listLeaderClasses();
        List<Class<? extends RobotTask<Robot>>> robots = taskFactory.listRobotClasses();
        
        for (Class<? extends RobotTask<TeamLeader>> leader : leaders) {
            names.add(leader.getSimpleName());
        }
        for (Class<? extends RobotTask<Robot>> robot : robots) {
            names.add(robot.getSimpleName());
        }
        
        return names;
    }
}
