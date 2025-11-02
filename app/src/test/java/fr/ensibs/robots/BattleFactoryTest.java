package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.logic.Battlefield;
import fr.ensibs.robots.logic.Droid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fr.ensibs.robots.logic.BattleSetup.DROID_INITIAL_ENERGY;
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
        this.factory = null; // TODO create your own implementation
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
}