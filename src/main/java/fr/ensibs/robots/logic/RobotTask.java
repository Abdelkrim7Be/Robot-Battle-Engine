package fr.ensibs.robots.logic;

/**
 * Interface providing a {@link Runnable#run} method implementing the actions of a robot
 * at each step in the battle. This method is invoked periodically by the
 * {@link BattlefieldEngine}
 *
 * @param <R> the type of robot associated to this task
 */
public interface RobotTask<R extends Robot> extends Runnable
{
    /**
     * Initialize the robot associated to this task. This method should be invoked
     * before the {@link Runnable#run} method first invocation
     *
     * @param robot the robot
     */
    void setRobot(R robot);

    /**
     * Give the robot associated to this task.
     *
     * @return the robot
     */
    R getRobot();
}
