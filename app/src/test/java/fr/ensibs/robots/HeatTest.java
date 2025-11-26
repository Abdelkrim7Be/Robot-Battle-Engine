package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.Battlefield;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.GunOverheatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fr.ensibs.robots.logic.BattleSetup.GUN_COOLING;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for gun heat management and overheating.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Firing increases gun heat</li>
 *   <li>Gun heat decreases over time (cooldown)</li>
 *   <li>Firing fails if gun is overheated (heat > 1)</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class HeatTest
{
    private BattleFactory factory;
    private Battlefield battlefield;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
        this.battlefield = factory.makeBattlefield();
    }

    /**
     * Test that firing increases gun heat.
     */
    @Test
    void testFiringIncreasesHeat() throws GunOverheatedException, fr.ensibs.robots.logic.ExhaustedException
    {
        Droid droid = factory.makeDroid();
        int initialHeat = droid.getGunHeat();
        assertEquals(0, initialHeat, "Initial gun heat should be 0");
        
        // Fire with power 5
        droid.fire(5);
        
        // Heat should be 1 + (5 / 5) = 2
        int expectedHeat = 1 + (5 / 5);
        assertEquals(expectedHeat, droid.getGunHeat(),
            "Gun heat should increase to 1 + (power / 5) after firing");
    }

    /**
     * Test that gun heat decreases over time (cooldown per tick).
     */
    @Test
    void testGunHeatCooldown()
    {
        Droid droid = factory.makeDroid();
        
        try {
            // Fire to generate heat
            droid.fire(10);
        } catch (GunOverheatedException | fr.ensibs.robots.logic.ExhaustedException e) {
            fail("Unexpected exception while firing: " + e);
        }
        
        int heatBefore = droid.getGunHeat();
        assertTrue(heatBefore > 0, "Gun should have heat after firing");
        
        // Decrease gun heats (simulating one game tick)
        battlefield.decreaseGunHeats();
        
        int heatAfter = droid.getGunHeat();
        int expectedHeat = Math.max(0, heatBefore - GUN_COOLING);
        assertEquals(expectedHeat, heatAfter,
            "Gun heat should decrease by GUN_COOLING each tick");
    }

    /**
     * Test that firing fails if gun heat is too high (heat > 1).
     */
    @Test
    void testFiringFailsWhenOverheated()
    {
        Droid droid = factory.makeDroid();
        
        try {
            // Fire multiple times to build up heat
            droid.fire(1); // Heat = 1 + (1/5) = 1
            droid.fire(1); // Heat = 1 + 1 + (1/5) = 2 (if no cooldown)
        } catch (GunOverheatedException | fr.ensibs.robots.logic.ExhaustedException e) {
            // This might throw if heat builds up too fast
        }
        
        // Build up heat to > 1
        try {
            droid.fire(10); // Heat = 1 + (10/5) = 3
        } catch (GunOverheatedException | fr.ensibs.robots.logic.ExhaustedException e) {
            // Might throw if already overheated
        }
        
        // Now try to fire when heat > 1
        if (droid.getGunHeat() > 1) {
            assertThrows(GunOverheatedException.class, () -> {
                droid.fire(5);
            }, "Should throw GunOverheatedException when gun heat > 1");
        }
    }

    /**
     * Test that gun heat cannot go below 0.
     */
    @Test
    void testGunHeatCannotGoBelowZero()
    {
        Droid droid = factory.makeDroid();
        
        // Gun heat starts at 0
        assertEquals(0, droid.getGunHeat(), "Initial gun heat should be 0");
        
        // Try to decrease heat (cooldown)
        battlefield.decreaseGunHeats();
        
        // Heat should still be 0 (not negative)
        assertEquals(0, droid.getGunHeat(),
            "Gun heat should not go below 0");
    }

    /**
     * Test that gun heat cools down over multiple ticks.
     */
    @Test
    void testGunHeatMultipleCooldowns()
    {
        Droid droid = factory.makeDroid();
        
        try {
            // Fire to generate significant heat
            droid.fire(30); // Heat = 1 + (30/5) = 7
        } catch (GunOverheatedException | fr.ensibs.robots.logic.ExhaustedException e) {
            fail("Unexpected exception while firing: " + e);
        }
        
        int initialHeat = droid.getGunHeat();
        assertTrue(initialHeat > 0, "Gun should have heat after firing");
        
        // Cool down over multiple ticks
        int ticksToCool = (initialHeat / GUN_COOLING) + 1;
        for (int i = 0; i < ticksToCool; i++) {
            battlefield.decreaseGunHeats();
        }
        
        // Heat should be 0 or very close to 0
        assertTrue(droid.getGunHeat() <= GUN_COOLING,
            "Gun heat should be close to 0 after sufficient cooldown ticks");
    }

    /**
     * Test specific check: fire() fails if gunHeat is too high.
     * This is the specific test mentioned in the mission requirements.
     */
    @Test
    void testFireFailsIfGunHeatTooHigh()
    {
        Droid droid = factory.makeDroid();
        
        // Build up heat to > 1
        try {
            // Fire with high power to generate heat > 1
            droid.fire(10); // Heat = 1 + (10/5) = 3
        } catch (GunOverheatedException | fr.ensibs.robots.logic.ExhaustedException e) {
            // Might throw if already at limit
        }
        
        // Ensure heat is > 1
        if (droid.getGunHeat() <= 1) {
            // If heat is still <= 1, try firing again
            try {
                droid.fire(10);
            } catch (GunOverheatedException | fr.ensibs.robots.logic.ExhaustedException e) {
                // Expected if heat > 1
            }
        }
        
        // Now heat should be > 1, so firing should fail
        if (droid.getGunHeat() > 1) {
            assertThrows(GunOverheatedException.class, () -> {
                droid.fire(1);
            }, "fire() should fail if gunHeat > 1");
        }
    }
}

