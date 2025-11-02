package fr.ensibs.robots.factories;

import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;
import fr.ensibs.robots.logic.TeamLeader;

import java.io.File;
import java.util.List;

/**
 * A factory to create robot tasks from class names using reflection
 *
 * @author Pascale Launay
 */
public interface RobotTaskFactory
{
    /**
     * Load the classes from the given jar file. Clear the classes previously
     * loaded by previous {@link #loadJar(File)} invocations.
     *
     * @param jarFile a JAR file
     */
    void loadJar(File jarFile);

    /**
     * List all classes loaded from the JAR file matching the
     * <code>Class<? extends RobotTask<Robot>></code> type
     *
     * @return the available classes
     */
    List<Class<? extends RobotTask<Robot>>> listRobotClasses();

    /**
     * List all classes loaded from the JAR file matching the
     * <code>Class<? extends RobotTask<TeamLeader>></code> type
     *
     * @return the available classes
     */
    List<Class<? extends RobotTask<TeamLeader>>> listLeaderClasses();

    /**
     * Create a {@link RobotTask} instance from the given class name
     *
     * @param clazz the concrete {@link RobotTask} class
     * @return {@link RobotTask} instance
     * @throws Exception if any exception occurs while instantiating the class
     */
    RobotTask<Robot> makeRobotTask(Class<? extends RobotTask<Robot>> clazz);

    /**
     * Create a {@link RobotTask} instance from the given class name
     *
     * @param clazz the concrete {@link RobotTask} class
     * @return {@link RobotTask} instance
     * @throws Exception if any exception occurs while instantiating the class
     */
    RobotTask<TeamLeader> makeLeaderTask(Class<? extends RobotTask<TeamLeader>> clazz);
}
