package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.Battlefield;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.TeamLeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for a {@link fr.ensibs.robots.factories.BattleFactory} implementation
 *
 * @author Pascale Launay
 */
public class BattleFactoryTest
{
    private BattleFactory factory;   // the battle factory instance initialized before each test

    /**
     * Initialize a new factory before each test
     */
    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Tests for the {@link BattleFactory#makeBattlefield()} method
     */
    @Test
    void testMakeBattlefield()
    {
        // create a battlefield instance
        Battlefield battlefield = factory.makeBattlefield();

        // check the method result
        assertNotNull(battlefield, "The makeBattlefield method returned null");
        assertSame(this.factory.makeBattlefield(), battlefield, "The makeBattlefield method should always return the same instance");
    }

    /**
     * Tests for the {@link BattleFactory#makeDroid()} method
     */
    @Test
    void testMakeDroid()
    {
        // create a droid instance
        Droid droid = factory.makeDroid();

        // check the method result
        assertNotNull(droid, "The makeDroid method returned null");
        assertEquals(DROID_INITIAL_ENERGY, droid.getEnergy(), "The droid initial energy is not valid");
        assertEquals(0, droid.getGunHeat(), "The droid initial heat is not valid");
        assertNotEquals(factory.makeDroid().getLocation(), droid.getLocation(), "Different droids initial locations should not be the same");
    }

    /**
     * Tests for the {@link BattleFactory#makeRobot()} method
     */
    @Test
    void testMakeRobot()
    {
        // create a robot instance
        Robot robot = factory.makeRobot();

        // check the method result
        assertNotNull(robot, "The makeRobot method returned null");
        assertEquals(ROBOT_INITIAL_ENERGY, robot.getEnergy(), "The robot initial energy is not valid");
        assertEquals(0, robot.getGunHeat(), "The robot initial heat is not valid");
        assertNotEquals(factory.makeRobot().getLocation(), robot.getLocation(), "Different robots initial locations should not be the same");
    }

    /**
     * Tests for the {@link BattleFactory#makeTeamLeader(List)} method
     */
    @Test
    void testMakeTeamLeader()
    {
        // create a leader instance
        Droid[] droids = {factory.makeDroid(), factory.makeDroid(), factory.makeDroid(), factory.makeDroid()};
        TeamLeader leader = factory.makeTeamLeader(Arrays.asList(droids));

        // check the method result
        assertNotNull(leader, "The makeTeamLeader method returned null");
        assertEquals(ROBOT_INITIAL_ENERGY, leader.getEnergy(), "The leader initial energy is not valid");
        assertEquals(0, leader.getGunHeat(), "The leader initial heat is not valid");
        assertNotNull(leader.getTeammates(), "The leader teammates getter returned null");
        assertArrayEquals(droids, leader.getTeammates().toArray(), "The leader teammates list is not valid");
    }
}