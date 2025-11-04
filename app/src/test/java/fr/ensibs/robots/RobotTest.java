package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for a {@link Robot} implementation.
 *
 * @author Pascale Launay
 */
public class RobotTest
{
    private BattleFactory factory;   // the battle factory instance initialized before each test
    private Robot robot;             // the robot instance initialized before each test

    /**
     * Initialize a new factory and battlefield before each test
     */
    @BeforeEach
    void initialize()
    {
        this.factory = null; // TODO create your own implementation
        this.robot = factory.makeRobot();
    }

    /**
     * A simple test for the {@link Robot#turnRobot(double)} method: turn the robot by 90°
     */
    @Test
    void testTurnRobot90()
    {
        // initialize the expected result
        double expectedHeading = robot.getHeading() > 270 ? robot.getHeading() - 270 : robot.getHeading() + 90;
        double expectedGunHeading = robot.getGunHeading() > 270 ? robot.getGunHeading() - 270 : robot.getGunHeading() + 90;
        double expectedRadarHeading = robot.getRadarHeading() > 270 ? robot.getRadarHeading() - 270 : robot.getRadarHeading() + 90;

        // invoke the tested method
        robot.turnRobot(90);

        // check the result
        assertEquals(expectedHeading, robot.getHeading(), 0.01, "Invalid heading after turning robot 90°");
        assertEquals(expectedGunHeading, robot.getGunHeading(), 0.01, "Invalid gun heading after turning robot 90°");
        assertEquals(expectedRadarHeading, robot.getRadarHeading(), 0.01, "Invalid radar heading after turning robot 90°");
    }

    /**
     * A simple test for the {@link Robot#turnRobot(double)} method: turn the robot by -45°
     */
    @Test
    void testTurnRadar45()
    {
        // initialize the expected result
        double expectedHeading = robot.getHeading();
        double expectedGunHeading = robot.getGunHeading();
        double expectedRadarHeading = robot.getRadarHeading() < 45 ? robot.getRadarHeading() + 315 : robot.getRadarHeading() - 45;

        // invoke the tested method
        robot.turnRadar(-45);

        // check the result
        assertEquals(expectedHeading, robot.getHeading(), 0.01, "Invalid heading after turning radar -45°");
        assertEquals(expectedGunHeading, robot.getGunHeading(), 0.01, "Invalid gun heading after turning radar -45°");
        assertEquals(expectedRadarHeading, robot.getRadarHeading(), 0.01, "Invalid radar heading after turning radar v°");
    }
}
