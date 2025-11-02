package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.logic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
        this.factory = null; // TODO create your own implementation
        this.battlefield = factory.makeBattlefield();
    }

    /**
     * A simple test for the {@link Battlefield#move(Droid, double)} method:
     * invoke the move method for a robot which direction is horizontal
     */
    @Test
    void testMoveHorizontally()
    {
        // initialize the droid to be tested
        Droid droid = factory.makeDroid();
        droid.turnRobot(90 - droid.getHeading()); // set robot heading to 90
        int x = droid.getLocation().getX();
        int distance = x > FIELD_WIDTH / 2 ? -40 : +40; // right or left according to the droid location

        // initialize the expected result
        Location expected = new Location(x + distance, droid.getLocation().getY());
        int expectedEnergy = DROID_INITIAL_ENERGY - MOTION_ENERGY;

        // invoke the tested method
        try {
            battlefield.move(droid, distance);
        } catch (CollisionException | ExhaustedException e) {
            fail("Unexpected exception while moving: " + e);
        }

        // check the result
        assertEquals(expected, droid.getLocation(), "Unexpected location after moving horizontally");
        assertEquals(expectedEnergy, droid.getEnergy(), "Unexpected energy after moving");
    }

    /**
     * A simple test for the {@link Battlefield#move(Droid, double)} method:
     * invoke the move method for a robot which direction is vertical
     */
    @Test
    void testMoveVertically()
    {
        // initialize the droid to be tested
        Droid droid = factory.makeDroid();
        droid.turnRobot(180 - droid.getHeading()); // set robot heading to 180
        int y = droid.getLocation().getY();
        int distance = y > FIELD_HEIGHT / 2 ? -40 : +40; // up or down according to the droid location

        // initialize the expected result
        Location expected = new Location(droid.getLocation().getX(), y + distance);
        int expectedEnergy = DROID_INITIAL_ENERGY - MOTION_ENERGY;

        // invoke the tested method
        try {
            battlefield.move(droid, distance);
        } catch (CollisionException | ExhaustedException e) {
            fail("Unexpected exception while moving: " + e);
        }

        // check the result
        assertEquals(expected, droid.getLocation(), "Unexpected location after moving vertically");
        assertEquals(expectedEnergy, droid.getEnergy(), "Unexpected energy after moving");
    }

    /**
     * A simple test for the {@link Battlefield#move(Droid, double)} method:
     * invoke the move method for a robot which direction is 60, 120, 240 or 300
     * according to its initial location (i.e. 30° from the x-axis)
     */
    @Test
    void testMove30()
    {
        // initialize the droid to be tested
        Droid droid = factory.makeDroid();
        int x = droid.getLocation().getX();
        int y = droid.getLocation().getY();
        // choose the direction according to the droid location
        double degrees = x > FIELD_WIDTH / 2.0 ? (y > FIELD_HEIGHT / 2.0 ? 300 : 240) : (y > FIELD_HEIGHT / 2.0 ? 60 : 120);
        droid.turnRobot(degrees - droid.getHeading());

        // initialize the expected result
        int dx = (int) Math.round((x > FIELD_WIDTH / 2.0 ? -20 : +20) * Math.sqrt(3));
        int dy = y > FIELD_HEIGHT / 2.0 ? -20 : +20;
        Location expected = new Location(x + dx, y + dy);
        int expectedEnergy = DROID_INITIAL_ENERGY - MOTION_ENERGY;

        // invoke the tested method
        try {
            battlefield.move(droid, 40);
        } catch (CollisionException | ExhaustedException e) {
            fail("Unexpected exception while moving: " + e);
        }

        // check the result
        assertEquals(expected, droid.getLocation(), "Unexpected location after moving in a " + degrees + "° orientation from (" + x + "," + y + ")");
        assertEquals(expectedEnergy, droid.getEnergy(), "Unexpected energy after moving");
    }

    /**
     * Tests for the {@link Battlefield#fire(Droid, int)} method
     */
    @Test
    void testFire()
    {
        // empty test method
    }

    /**
     * Tests for the {@link Battlefield#scan(Robot)} method
     */
    @Test
    void testScan()
    {
        // empty test method
    }

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
}
