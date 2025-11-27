package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for a {@link Battlefield} implementation.
 *
 * @author Pascale Launay
 */
public class BattlefieldTest
{
    private BattleFactory factory;   // the battle factory instance initialized before each test
    private Battlefield battlefield; // the battlefield instance initialized before each test

    /**
     * Initialize a new factory and battlefield before each test
     */
    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
        this.battlefield = factory.makeBattlefield();
    }

    //-------------------------------------------------------------------------
    // MOVE
    //-------------------------------------------------------------------------

    /**
     * A simple test for the {@link Battlefield#move(Droid, double)} method:
     * invoke the move method for a robot which direction is horizontal
     *
     * @throws CollisionException exception while moving, that should not occur
     * @throws ExhaustedException exception while moving, that should not occur
     */
    @Test
    void testMoveHorizontally() throws CollisionException, ExhaustedException
    {
        // initialize the droid to be tested
        Droid droid = factory.makeDroid();
        droid.turnRobot(90 - droid.getHeading()); // set robot heading to 90
        int energyBeforeMove = droid.getEnergy();
        int x = droid.getLocation().getX();
        int distance = x > FIELD_WIDTH / 2 ? -40 : +40; // right or left according to the droid location

        // initialize the expected result
        Location expected = new Location(x + distance, droid.getLocation().getY());
        int expectedEnergy = energyBeforeMove - MOTION_ENERGY;

        // invoke the tested method
        battlefield.move(droid, distance);

        // check the result
        assertEquals(expected, droid.getLocation(), "Unexpected location after moving horizontally");
        assertEquals(expectedEnergy, droid.getEnergy(), "Unexpected energy after moving");
    }

    /**
     * A simple test for the {@link Battlefield#move(Droid, double)} method:
     * invoke the move method for a robot which direction is vertical
     *
     * @throws CollisionException exception while moving, that should not occur
     * @throws ExhaustedException exception while moving, that should not occur
     */
    @Test
    void testMoveVertically() throws CollisionException, ExhaustedException
    {
        // initialize the droid to be tested
        Droid droid = factory.makeDroid();
        droid.turnRobot(180 - droid.getHeading()); // set robot heading to 180
        int energyBeforeMove = droid.getEnergy();
        int y = droid.getLocation().getY();
        int distance = y > FIELD_HEIGHT / 2 ? -40 : +40; // up or down according to the droid location

        // initialize the expected result
        Location expected = new Location(droid.getLocation().getX(), y + distance);
        int expectedEnergy = energyBeforeMove - MOTION_ENERGY;

        // invoke the tested method
        battlefield.move(droid, distance);

        // check the result
        assertEquals(expected, droid.getLocation(), "Unexpected location after moving vertically");
        assertEquals(expectedEnergy, droid.getEnergy(), "Unexpected energy after moving");
    }

    /**
     * A simple test for the {@link Battlefield#move(Droid, double)} method:
     * invoke the move method for a robot which direction is 60, 120, 240 or 300
     * according to its initial location (i.e. 30° from the x-axis)
     *
     * @throws CollisionException exception while moving, that should not occur
     * @throws ExhaustedException exception while moving, that should not occur
     */
    @Test
    void testMove30() throws CollisionException, ExhaustedException
    {
        // initialize the droid to be tested
        Droid droid = factory.makeDroid();
        int x = droid.getLocation().getX();
        int y = droid.getLocation().getY();
        // choose the direction according to the droid location
        double degrees = x > FIELD_WIDTH / 2.0 ? (y > FIELD_HEIGHT / 2.0 ? 300 : 240) : (y > FIELD_HEIGHT / 2.0 ? 60 : 120);
        droid.turnRobot(degrees - droid.getHeading());
        int energyBeforeMove = droid.getEnergy();

        // initialize the expected result
        int dx = (int) Math.round((x > FIELD_WIDTH / 2.0 ? -20 : +20) * Math.sqrt(3));
        int dy = y > FIELD_HEIGHT / 2.0 ? -20 : +20;
        Location expected = new Location(x + dx, y + dy);
        int expectedEnergy = energyBeforeMove - MOTION_ENERGY;

        // invoke the tested method
        battlefield.move(droid, 40);

        // check the result
        assertEquals(expected, droid.getLocation(), "Unexpected location after moving in a " + degrees + "° orientation from (" + x + "," + y + ")");
        assertEquals(expectedEnergy, droid.getEnergy(), "Unexpected energy after moving");
    }

    /**
     * A simple test for the {@link Battlefield#move(Droid, double)} method:
     * multiple invocations of the move method in order to go out off the field
     * and throw a CollisionException
     *
     * @throws ExhaustedException exception while moving, that should not occur
     */
    @Test
    void testMoveOut() throws ExhaustedException
    {
        // initialize the droid to be tested
        Droid droid = factory.makeDroid();
        droid.turnRobot(180 - droid.getHeading()); // turn robot to down

        // initialize the expected result
        Location expected = new Location(droid.getLocation().getX(), FIELD_HEIGHT - ROBOT_RADIUS); // expected location after the collision
        int nbMoves = (int)Math.ceil((FIELD_HEIGHT - droid.getLocation().getY())/MAX_DISTANCE_MOVE);     // nb of moves to move out of the field
        int expectedEnergy = droid.getEnergy();

        // move until a collision occurs (going out of the field)
        try {
            for (int i = 0; i < nbMoves; i++) {
                expectedEnergy -= MOTION_ENERGY;
                battlefield.move(droid, MAX_DISTANCE_MOVE);
            }
            fail("Expected a CollisionException to be thrown at " + expected + ". Location: " + droid.getLocation());
        } catch (CollisionException e) {
            expectedEnergy -= COLLISION_DAMAGE;
            assertEquals(expected, e.getLocation(), "Unexpected collision location");
            assertEquals(expected, droid.getLocation(), "Unexpected droid location after collision");
            assertEquals(expectedEnergy, droid.getEnergy(), "Unexpected energy after collision");
        }
    }

    /**
     * A simple test for the {@link Battlefield#move(Droid, double)} method:
     * multiple invocations of the move method in order to collide another robot
     * and throw a CollisionException
     *
     * @throws CollisionException exception while moving, that should not occur
     * @throws ExhaustedException exception while moving, that should not occur
     */
    @Test
    void testMoveCollision() throws CollisionException, ExhaustedException
    {
        // initialize the droid to be tested, and another droid to be collided
        Droid droid1 = factory.makeDroid();
        moveTo(droid1, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        Droid droid2 = addSameLine(droid1);

        // initialize the expected result (should collide on the left of robot2)
        int nbMoves = (int)Math.ceil((droid2.getLocation().getX() - droid1.getLocation().getX())/MAX_DISTANCE_MOVE);
        Location expected = new Location(droid2.getLocation().getX() - 2 * ROBOT_RADIUS, droid2.getLocation().getY());
        droid1.turnRobot(90 - droid1.getHeading()); // turn to right
        int expectedEnergy1 = droid1.getEnergy();
        int expectedEnergy2 = droid2.getEnergy();
        try {
            for (int i = 0; i < nbMoves; i++) {
                expectedEnergy1 -= MOTION_ENERGY;
                battlefield.move(droid1, MAX_DISTANCE_MOVE);
            }
            fail("Expected a CollisionException to be thrown at " + expected + ". Location: " + droid1.getLocation());
        } catch (CollisionException e) {
            expectedEnergy1 -= COLLISION_DAMAGE;
            expectedEnergy2 -= COLLISION_DAMAGE;
            assertEquals(expected, e.getLocation(), "Unexpected collision location");
            assertEquals(expected, droid1.getLocation(), "Unexpected droid location after collision");
            assertEquals(expectedEnergy1, droid1.getEnergy(), "Unexpected energy after collision for the 1st robot");
            assertEquals(expectedEnergy2, droid2.getEnergy(), "Unexpected energy after collision for the 2nd robot");
        }
    }

    //-------------------------------------------------------------------------
    // FIRE
    //-------------------------------------------------------------------------

    /**
     * Test for the {@link Battlefield#fire(Droid, int)} method: invoke the fire method, with only one droid on
     * the battlefield
     *
     * @throws GunOverheatedException exception while firing, that should not occur
     * @throws ExhaustedException     exception while firing, that should not occur
     */
    @Test
    void testFireMissed() throws GunOverheatedException, ExhaustedException
    {
        // initialize the droid to be tested
        Droid droid = factory.makeDroid();

        // initialize the expected result
        int expectedEnergy = DROID_INITIAL_ENERGY - 5;
        int expectedHeat = 2;

        // invoke the tested method
        battlefield.fire(droid, 5);

        // check the result
        assertEquals(expectedEnergy, droid.getEnergy(), "Unexpected energy after fire");
        assertEquals(expectedHeat, droid.getGunHeat(), "Unexpected gun heat after fire");
    }

    /**
     * Test for the {@link Battlefield#fire(Droid, int)} method: invoke the fire method, with another droid on
     * the battlefield that should be shot
     *
     * @throws GunOverheatedException exception while firing, that should not occur
     * @throws CollisionException exception while moving, that should not occur
     * @throws ExhaustedException exception while moving or firing, that should not occur
     */
    @Test
    void testFireShot() throws CollisionException, ExhaustedException, GunOverheatedException
    {
        // initialize the droid to be tested, and another droid to be collided
        Droid droid1 = factory.makeDroid();
        moveTo(droid1, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        Droid droid2 = addSameLine(droid1);

        // invoke the tested method
        droid1.turnGun(90 - droid1.getGunHeading());
        int shooterEnergyBeforeFire = droid1.getEnergy();
        int targetEnergyBeforeFire = droid2.getEnergy();
        int firePower = 5;
        battlefield.fire(droid1, firePower);
        stepBattlefield();

        // check the result
        int lifeSteal = 3 * firePower;
        int damage = 4 * firePower + 2 * (firePower - 1);
        assertEquals(shooterEnergyBeforeFire - firePower + lifeSteal, droid1.getEnergy(), "Unexpected energy after fire");
        assertEquals(targetEnergyBeforeFire - damage, droid2.getEnergy(), "Unexpected energy after fire");
    }

    //-------------------------------------------------------------------------
    // SCAN
    //-------------------------------------------------------------------------

    /**
     * Tests for the {@link Battlefield#scan(Robot)} method with another robot
     * in the radar field of vision
     */
    @Test
    void testScanSuccess() throws ExhaustedException, CollisionException
    {
        // initialize the robot to be tested and another droid to be scanned
        Robot robot = factory.makeRobot();
        moveTo(robot, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        Droid droid = addSameLine(robot);

        // initialize the expected result
        Location[] expected = { droid.getLocation() };

        // invoke the tested method
        robot.turnRadar(90 - robot.getRadarHeading());
        List<Location> result = battlefield.scan(robot);

        // check the result
        assertArrayEquals(expected, result.toArray(), "Unexpected scan result");
    }

    /**
     * Tests for the {@link Battlefield#scan(Robot)} method with another robot out of the
     * radar field of vision
     */
    @Test
    void testScanEmpty() throws ExhaustedException, CollisionException
    {
        // initialize the robot to be tested and another droid to be scanned
        Robot robot = factory.makeRobot();
        moveTo(robot, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        Droid droid = addSameLine(robot);

        // invoke the tested method
        robot.turnRadar(180 - robot.getRadarHeading());
        List<Location> result = battlefield.scan(robot);

        // check the result
        if (result != null) {
            assertEquals(0, result.size(), "Scan result should be empty: " + result);
        }
    }

    @Test
    void testScanHidden() throws ExhaustedException, CollisionException
    {
        // initialize the robot to be tested and another droid to be scanned
        Robot robot = factory.makeRobot();
        moveTo(robot, FIELD_WIDTH / 4, FIELD_HEIGHT / 2);
        Droid droid = addSameLine(robot);
        Droid hidden = addSameLine(droid);

        // initialize the expected result
        Location[] expected = { droid.getLocation() };

        // invoke the tested method
        robot.turnRadar(90 - robot.getRadarHeading());
        List<Location> result = battlefield.scan(robot);

        // check the result
        assertArrayEquals(expected, result.toArray(), "Unexpected scan result");
    }

    //-------------------------------------------------------------------------
    // HEAT
    //-------------------------------------------------------------------------

    /**
     * Tests for the {@link Battlefield#decreaseGunHeats()} method
     */
    @Test
    void testDecreaseGunHeats()
    {
        // initialize droids to be tested
        Droid droid1 = factory.makeDroid();
        Droid droid2 = factory.makeDroid();
        try {
            droid1.fire(1);
            droid2.fire(30);
        } catch (GunOverheatedException | ExhaustedException e) {
            fail("Unexpected exception while firing: " + e);
        }

        // initialize expected values
        int expected1 = Math.max(0, droid1.getGunHeat() - GUN_COOLING);
        int expected2 = Math.max(0, droid2.getGunHeat() - GUN_COOLING);

        // invoke the tested method
        battlefield.decreaseGunHeats();

        // check the result
        assertEquals(expected1, droid1.getGunHeat(), "Invalid heat after decreaseGunHeats invocation for the first robot");
        assertEquals(expected2, droid2.getGunHeat(), "Invalid heat after decreaseGunHeats invocation for the second robot");
    }

    //-------------------------------------------------------------------------
    // PRIVATE
    //-------------------------------------------------------------------------

    /**
     * Make a robot on the right of the given robot on the same line
     *
     * @param droid1 the first robot, to be on the left
     * @return the second robot, to be on the right
     *
     * @throws CollisionException exception while moving, that should not occur
     * @throws ExhaustedException exception while moving, that should not occur
     */
    private Droid addSameLine(Droid droid1) throws CollisionException, ExhaustedException
    {
        Droid droid2 = factory.makeDroid();
        // move droid2 up or down if on the left if needed (could collide when moving right)
        droid2.turnRobot(-droid2.getHeading());
        if (droid1.getLocation().getX() > droid2.getLocation().getX()) {
            int dy = droid1.getLocation().getY() - droid2.getLocation().getY();
            if (Math.abs(dy) <= 2 * ROBOT_RADIUS) {
                battlefield.move(droid2, dy > 0 ? -2*ROBOT_RADIUS : 2*ROBOT_RADIUS);
            }
        }
        moveTo(droid2, droid1.getLocation().getX() + FIELD_WIDTH / 4, droid1.getLocation().getY());
        return droid2;
    }

    private void moveTo(Droid droid, int x, int y) throws CollisionException, ExhaustedException
    {
        // move droid to the x location
        droid.turnRobot(90 - droid.getHeading());
        while (droid.getLocation().getX() != x) {
            battlefield.move(droid, x - droid.getLocation().getX());
        }
        // move droid to the y location
        droid.turnRobot(90);
        while (droid.getLocation().getY() != y) {
            battlefield.move(droid, y - droid.getLocation().getY());
        }
    }

    private void stepBattlefield()
    {
        Battlefield battlefield = factory.makeBattlefield();
        try {
            java.lang.reflect.Method method = battlefield.getClass().getDeclaredMethod("detectCollisions");
            method.setAccessible(true);
            for (int i = 0; i < 100; i++) {
                method.invoke(battlefield);
            }
        } catch (ReflectiveOperationException e) {
            fail("Unable to advance battlefield state: " + e.getMessage());
        }
    }
}
