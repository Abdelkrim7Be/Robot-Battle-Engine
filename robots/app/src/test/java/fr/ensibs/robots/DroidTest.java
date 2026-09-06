package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.Droid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for a {@link Droid} implementation.
 *
 * @author Pascale Launay
 */
public class DroidTest
{
    private BattleFactory factory;   // the battle factory instance initialized before each test
    private Droid droid;             // the droid instance initialized before each test

    /**
     * Initialize a new factory and battlefield before each test
     */
    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
        this.droid = factory.makeDroid();
    }

    /**
     * A simple test for the {@link Droid#turnRobot(double)} method: turn the robot by -90°
     */
    @Test
    void testTurnRobot90()
    {
        // initialize the expected result
        double expectedHeading = droid.getHeading() >= 90 ? droid.getHeading() - 90 : droid.getHeading() + 270;
        double expectedGunHeading = droid.getGunHeading() >= 90 ? droid.getGunHeading() - 90 : droid.getGunHeading() + 270;

        // invoke the tested method
        droid.turnRobot(-90);

        // check the result
        assertEquals(expectedHeading, droid.getHeading(), 0.01, "Invalid heading after turning robot -90°");
        assertEquals(expectedGunHeading, droid.getGunHeading(), 0.01, "Invalid gun heading after turning robot -90°");
    }

    /**
     * A simple test for the {@link Droid#turnRobot(double)} method: turn the robot by 360°
     */
    @Test
    void testTurnRobot360()
    {
        // initialize the expected result
        double expectedHeading = droid.getHeading();
        double expectedGunHeading = droid.getGunHeading();

        // invoke the tested method
        droid.turnRobot(360);

        // check the result
        assertEquals(expectedHeading, droid.getHeading(), 0.01, "Invalid heading after turning robot 360°");
        assertEquals(expectedGunHeading, droid.getGunHeading(), 0.01, "Invalid gun heading after turning robot 360°");
    }

    /**
     * A simple test for the {@link Droid#turnGun(double)} method: turn the gun by 180°
     */
    @Test
    void testTurnGun180()
    {
        // initialize the expected result
        double expectedHeading = droid.getHeading();
        double expectedGunHeading = droid.getGunHeading() < 180 ? droid.getGunHeading() + 180 : droid.getGunHeading() - 180;

        // invoke the tested method
        droid.turnGun(180);

        // check the result
        assertEquals(expectedHeading, droid.getHeading(), 0.01, "Invalid heading after turning gun 180°");
        assertEquals(expectedGunHeading, droid.getGunHeading(), 0.01, "Invalid gun heading after turning gun 180°");
    }
}
