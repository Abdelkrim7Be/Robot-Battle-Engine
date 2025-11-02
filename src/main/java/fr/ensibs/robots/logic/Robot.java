package fr.ensibs.robots.logic;

import java.util.List;

/**
 * A robot is an advanced droid, with an autonomous behaviour defined in a
 * {@link RobotTask} object associated to this robot, and a radar used to scan
 * for other robots. The radar is carried by the gun. When scanning, it gives
 * all robots in his field of vision. The radar's field of vision is a battle setup.
 *
 * @author Pascale Launay
 */
public interface Robot extends Droid
{
    /**
     * Give the direction that the radar is facing, in degrees. The value
     * returned will be between 0 and 360 (excluded): 0 means North, 90 means
     * East, 180 means South, and 270 means West.
     *
     * @return the current heading of the robot's radar
     */
    double getRadarHeading();

    /**
     * Turns the robot's radar by the given angle in degrees without turning the robot's
     * body or gun. The angle of the robot radar determines the direction in which it scans.
     * The robot's radar being carried by the robots' gun, the robot's radar follows the gun
     * in its turn.
     * <p>
     * Degrees must be inside the interval ]-360; 360[. A positive value means that the
     * robot's radar is set to turn right, whereas a negative value means that it turns left.
     *
     * @param degrees the angle of the rotation in degrees
     */
    void turnRadar(double degrees);

    /**
     * Gives all locations where robots are visible by this robot's scanner,
     * following the rules defined in {@link Battlefield}
     *
     * @return the locations of the scanned robots
     * @throws ExhaustedException if the robot has not enough energy to scan
     * @see Battlefield#scan(Robot)
     */
    List<Location> scan() throws ExhaustedException;
}
