package fr.ensibs.robots.logic;

/**
 * The battles default properties
 *
 * @author Pascale Launay
 */
public interface BattleSetup
{
    /**
     * The width of the battlefield in pixels
     */
    int FIELD_WIDTH = 1280;

    /**
     * The width of the battlefield in pixels
     */
    int FIELD_HEIGHT = 960;

    /**
     * The droids and robots' radius
     */
    int ROBOT_RADIUS = 10;

    /**
     * The droids' initial energy
     */
    int DROID_INITIAL_ENERGY = 2500;

    /**
     * The robots' initial energy
     */
    int ROBOT_INITIAL_ENERGY = 1500;

    /**
     * The number of teammates in a team, team leader excluded
     */
    int NB_TEAMMATES = 4;

    /**
     * The maximum distance for a fire in pixels
     */
    int MAX_FIRE_SCOPE = 500;

    /**
     * The maximal bullet power
     */
    int MAX_FIRE_POWER = 50;

    /**
     * The gun heat amount dropped each turn
     */
    int GUN_COOLING = 7;

    /**
     * The energy spent for a radar scanning
     */
    int SCAN_ENERGY = 1;

    /**
     * The radars' field of vision (45° on both sides of the radar heading)
     */
    int VISION_FIELD = 90;

    /**
     * The minimum distance for a robot's movement
     */
    int MIN_DISTANCE_MOVE = -40;

    /**
     * The maximum distance for a robot's movement
     */
    int MAX_DISTANCE_MOVE = 50;

    /**
     * The energy spent for a robot's movement
     */
    int MOTION_ENERGY = 0;

    /**
     * The energy lost when a collision occurs (for both robots) or when
     * the robot goes out the battlefield
     */
    int COLLISION_DAMAGE = 3;
}
