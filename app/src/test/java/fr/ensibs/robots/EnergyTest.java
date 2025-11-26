package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.CollisionException;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.ExhaustedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for energy consumption during movement and actions.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Movement consumes energy</li>
 *   <li>Energy cannot go below 0</li>
 *   <li>ExhaustedException is thrown when energy is insufficient</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class EnergyTest
{
    private BattleFactory factory;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Test that energy decreases after a move command.
     */
    @Test
    void testEnergyDecreasesAfterMove() throws CollisionException, ExhaustedException
    {
        Droid droid = factory.makeDroid();
        int initialEnergy = droid.getEnergy();
        
        // Move the droid
        droid.move(20.0);
        
        // Verify energy decreased by MOTION_ENERGY
        int expectedEnergy = initialEnergy - MOTION_ENERGY;
        assertEquals(expectedEnergy, droid.getEnergy(),
            "Energy should decrease by MOTION_ENERGY after moving");
    }

    /**
     * Test that multiple moves consume energy for each move.
     */
    @Test
    void testMultipleMovesConsumeEnergy() throws CollisionException, ExhaustedException
    {
        Droid droid = factory.makeDroid();
        int initialEnergy = droid.getEnergy();
        int numberOfMoves = 5;
        
        // Move multiple times
        for (int i = 0; i < numberOfMoves; i++) {
            droid.move(10.0);
        }
        
        // Verify energy decreased by MOTION_ENERGY for each move
        int expectedEnergy = initialEnergy - (MOTION_ENERGY * numberOfMoves);
        assertEquals(expectedEnergy, droid.getEnergy(),
            "Energy should decrease by MOTION_ENERGY for each move");
    }

    /**
     * Test that ExhaustedException is thrown when energy is insufficient for movement.
     */
    @Test
    void testExhaustedExceptionOnInsufficientEnergy()
    {
        Droid droid = factory.makeDroid();
        int initialEnergy = droid.getEnergy();
        
        // Consume almost all energy (leave just 1 point)
        // We'll need to move many times to drain energy
        int movesToDrain = (initialEnergy - 1) / MOTION_ENERGY;
        
        try {
            for (int i = 0; i < movesToDrain; i++) {
                droid.move(1.0);
            }
        } catch (CollisionException | ExhaustedException e) {
            fail("Unexpected exception while draining energy: " + e);
        }
        
        // Now try to move when energy is insufficient
        assertThrows(ExhaustedException.class, () -> {
            droid.move(1.0);
        }, "Should throw ExhaustedException when energy is insufficient for movement");
    }

    /**
     * Test that energy cannot go below 0.
     */
    @Test
    void testEnergyCannotGoBelowZero() throws CollisionException, ExhaustedException
    {
        Droid droid = factory.makeDroid();
        int initialEnergy = droid.getEnergy();
        
        // Try to move many times (more than energy allows)
        int movesToAttempt = (initialEnergy / MOTION_ENERGY) + 10;
        
        for (int i = 0; i < movesToAttempt; i++) {
            try {
                droid.move(1.0);
            } catch (ExhaustedException e) {
                // Expected when energy runs out
                break;
            }
        }
        
        // Verify energy is 0 or positive (should be 0 after exhausting)
        assertTrue(droid.getEnergy() >= 0,
            "Energy should never go below 0");
        
        // Verify we can't move anymore
        assertThrows(ExhaustedException.class, () -> {
            droid.move(1.0);
        }, "Should throw ExhaustedException when energy is 0");
    }

    /**
     * Test that energy consumption is consistent regardless of movement distance
     * (within limits).
     */
    @Test
    void testEnergyConsumptionIndependentOfDistance() throws CollisionException, ExhaustedException
    {
        Droid droid1 = factory.makeDroid();
        Droid droid2 = factory.makeDroid();
        
        int energy1Before = droid1.getEnergy();
        int energy2Before = droid2.getEnergy();
        
        // Move droid1 a short distance
        droid1.move(10.0);
        
        // Move droid2 a longer distance (but within limits)
        droid2.move(MAX_DISTANCE_MOVE);
        
        // Both should consume the same amount of energy (MOTION_ENERGY per move)
        int energy1After = droid1.getEnergy();
        int energy2After = droid2.getEnergy();
        
        int consumed1 = energy1Before - energy1After;
        int consumed2 = energy2Before - energy2After;
        
        assertEquals(consumed1, consumed2, MOTION_ENERGY,
            "Energy consumption should be the same (MOTION_ENERGY) regardless of distance");
        assertEquals(MOTION_ENERGY, consumed1,
            "Energy consumption should be exactly MOTION_ENERGY");
    }
}

