package fr.ensibs.robots.logic;

import java.util.List;

/**
 * The battlefield where robots fight between each others. Implementations of this class should
 * ensure that all the battle rules are respected
 *
 * @author Pascale Launay
 */
public interface Battlefield
{
    /**
     * Fires a bullet from the given robot. The source location of the shot is the robot's
     * location, the direction is given by the gun's direction and the distance of the shot
     * is {@link BattleSetup#MAX_FIRE_SCOPE}.
     * <p>
     * The gun cannot fire if its heat is greater than 1. The initial gun heat is 0.
     * The amount of gun heat generated when the gun is fired is 1 + (firePower / 5).
     * At each battle step, the gun's heat decreases by {@link BattleSetup#GUN_COOLING}
     * <p>
     * The firePower is limited by the maximal power defined in the battle setup
     * ({@link BattleSetup#MAX_FIRE_POWER}. If the given power is not valid,
     * it will be reduced to conform this limit.
     * <p>
     * The given bullet power is an amount of energy that is removed from the source
     * robot's energy. If the robot has less energy than the given power, the bullet
     * is not fired and an exception is thrown.
     * <p>
     * If the bullet hits another robot, do (4 * power) damage to the target robot.
     * If power is greater than 5, it will do an additional 2 * (power - 1) damage.
     * The source robot will get (3 * power) back if the bullet hits the other robot.
     * <p>
     * The energies of the source and target robots must be updated according to
     * the fire result.
     *
     * @param robot     the source robot
     * @param firePower the bullet power
     * @throws GunOverheatedException if the gun's heat is greater than 1
     * @throws ExhaustedException     if the robot has not enough energy to fire
     */
    void fire(Droid robot, int firePower) throws GunOverheatedException, ExhaustedException;

    /**
     * Move a robot forwards or backwards by the given distance, measured in pixels.
     * The source location and direction are those of the robot's body. Both positive
     * and negative values can be given as input, where negative values means that
     * the robot moves backwards instead of forwards.
     * <p>
     * One motion (i.e. one invocation of this method) costs {@link BattleSetup#MOTION_ENERGY}
     * points of energy, removed from the robot's energy. If the robot has less energy
     * than {@link BattleSetup#MOTION_ENERGY}, the robot's location is unchanged and an
     * exception is thrown.
     * <p>
     * If the robot goes out of the battlefield or if it collides with another robot,
     * the move is complete, meaning that the robot will not move any further. The robot
     * loses {@link BattleSetup#COLLISION_DAMAGE} points of energy. The robot that was hit
     * loses the same amount of energy in case of collision.
     * <p>
     * The distance is limited by the minimal and maximal distances defined in the battle setup
     * ({@link BattleSetup#MIN_DISTANCE_MOVE} and {@link BattleSetup#MAX_DISTANCE_MOVE}).
     * If the given distance is not valid, it will be reduced to conform the limit.
     *
     * @param robot    the robot to be moved
     * @param distance the distance of the move
     * @throws CollisionException if the robot goes out of the battlefield or if it
     *                            collides with another robot. Handles the location where the robot is blocked
     *                            after the collision
     * @throws ExhaustedException if the robot has not enough energy to move
     */
    void move(Droid robot, double distance) throws CollisionException, ExhaustedException;

    /**
     * Gives all locations where robots are visible by the given robot's scanner.
     * The source location of the scan is the robot's location. The direction and
     * field of vision are given by its radar. The field of vision is centered
     * relatively to the scanner's direction.
     * <p>
     * One scan (i.e. one invocation of this method) costs {@link BattleSetup#SCAN_ENERGY}
     * points of energy. If the robot has less energy than {@link BattleSetup#SCAN_ENERGY},
     * the scan is not executed and an exception is thrown.
     *
     * @param robot the source robot
     * @return the locations of the scanned robots
     * @throws ExhaustedException if the robot has not enough energy to scan
     */
    List<Location> scan(Robot robot) throws ExhaustedException;

    /**
     * Decrease the gun heats of all the robots engaged in the battle by the
     * {@link BattleSetup#GUN_COOLING} amount. This method is invoked at each
     * battle step by the {@link BattlefieldEngine}.
     */
    void decreaseGunHeats();
}
