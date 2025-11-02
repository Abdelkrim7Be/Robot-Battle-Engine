package fr.ensibs.robots.logic;

/**
 * A droid is a robot, consisting in two parts:
 *
 * <ul>
 * <li>the body, used for turning and moving the robot. The body carries the gun.</li>
 *
 * <li>the gun is mounted on the body and is used for firing energy bullets.
 * The gun can turn.</li>
 * </ul>
 * <p>
 * A droid has no scanner and is not autonomous, unlike a {@link Robot}, but it has
 * extra energy. Droids are intended for use in teams.
 * <p>
 * The size of the robot, used to compute the collisions and the shots results,
 * is a battle setup: for simplicity's sake, the robot is considered to be a circle
 * whose center is its location (see {@link #getLocation()})
 *
 * @author Pascale Launay
 */
public interface Droid
{
    /**
     * Give the location of the center of the robot on the battlefield in pixels,
     * according to the Java graphical coordinate system (the origin point is the
     * upper left point of the battlefield)
     *
     * @return the current location of the robot
     */
    Location getLocation();

    /**
     * Give the amount of energy of the robot. All droids have the same energy
     * at instantiation: this initial energy is a battle setup.
     *
     * @return the current energy of the robot
     */
    int getEnergy();

    /**
     * Give the heat of the gun. The initial gun heat is 0.
     *
     * @return the current heat of the robot's gun
     */
    int getGunHeat();

    /**
     * Give the direction that the robot's body is facing, in degrees.
     * The value returned must be between 0 and 360 (excluded): 0 means
     * North, 90 means East, 180 means South, and 270 means West.
     *
     * @return the current heading of the robot's body
     */
    double getHeading();

    /**
     * Give the direction that the gun is facing, in degrees. The value
     * returned must be between 0 and 360 (excluded): 0 means North, 90 means
     * East, 180 means South, and 270 means West.
     *
     * @return the current heading of the robot's gun
     */
    double getGunHeading();

    /**
     * Fires a bullet, following the rules defined in {@link Battlefield}
     *
     * @param firePower the bullet power
     * @throws GunOverheatedException if the gun heat is greater than 1
     * @throws ExhaustedException     if the robot has not enough energy to fire
     * @see Battlefield#fire(Droid, int)
     */
    void fire(int firePower) throws GunOverheatedException, ExhaustedException;

    /**
     * Moves the robot, following the rules defined in {@link Battlefield}
     *
     * @param distance the distance of the move pixels (greater than 0: forwards - less than 0: backwards)
     * @throws CollisionException if the robot goes out of the battlefield or collides with another robot
     * @throws ExhaustedException if the robot has not enough energy to move
     * @see Battlefield#move(Droid, double)
     */
    void move(double distance) throws CollisionException, ExhaustedException;

    /**
     * Turns the robot's body by the given angle in degrees. The robot's gun being
     * carried by the robot's body, the robot's gun follows the body in its turn.
     * The angle of the robot body determines the direction in which it moves.
     * <p>
     * Degrees must be inside the interval ]-360; 360[. A positive value means that the
     * robot's body is set to turn right, whereas a negative value means that it turns left.
     *
     * @param degrees the angle of the rotation in degrees
     */
    void turnRobot(double degrees);

    /**
     * Turns the robot's gun by the given angle in degrees without turning the robot's
     * body. The angle of the robot gun determines the direction in which it fires.
     * <p>
     * Degrees must be inside the interval ]-360; 360[. A positive value means that the
     * robot's gun is set to turn right, whereas a negative value means that it turns left.
     *
     * @param degrees the angle of the rotation in degrees
     */
    void turnGun(double degrees);
}

