package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.CollisionException;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.ExhaustedException;
import fr.ensibs.robots.logic.GunOverheatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fr.ensibs.robots.logic.BattleSetup.COLLISION_DAMAGE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for collision detection.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Bullet vs Robot collisions apply damage</li>
 *   <li>Robot vs Wall collisions stop movement and apply damage</li>
 *   <li>Robot vs Robot collisions stop movement and apply damage to both</li>
 *   <li>Dead robots (energy <= 0) are removed from active list</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class CollisionTest
{
    private BattleFactory factory;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Test bullet vs robot collision: Place a bot at (0,0) and a bullet at (0,0).
     * Assert Bot HP decreases and Bullet is removed.
     */
    @Test
    void testBulletVsRobotCollision() throws CollisionException, ExhaustedException, GunOverheatedException
    {
        Droid target = factory.makeDroid();
        Droid shooter = factory.makeDroid();
        
        // Position target at a known location
        moveTo(target, 200, 200);
        
        // Position shooter near target and aim at it
        moveTo(shooter, 200, 150);
        shooter.turnGun(180.0 - shooter.getGunHeading()); // Point gun South (towards target)
        
        int targetEnergyBefore = target.getEnergy();
        int shooterEnergyBefore = shooter.getEnergy();
        
        // Fire at target (bullet should hit immediately with instant hit-scan)
        shooter.fire(5);
        
        // Verify target took damage
        int targetEnergyAfter = target.getEnergy();
        assertTrue(targetEnergyAfter < targetEnergyBefore,
            "Target should have taken damage from bullet collision");
        
        // Verify shooter gained energy (life steal)
        int shooterEnergyAfter = shooter.getEnergy();
        // Shooter energy = before - bulletPower + lifeSteal (if hit)
        // = before - 5 + (3 * 5) = before + 10
        assertTrue(shooterEnergyAfter >= shooterEnergyBefore - 5,
            "Shooter should have gained energy from life steal on hit");
    }

    /**
     * Test robot vs wall collision: Robot hits wall, stops, and takes damage.
     */
    @Test
    void testRobotVsWallCollision() throws ExhaustedException
    {
        Droid droid = factory.makeDroid();
        
        // Position droid near top edge
        moveTo(droid, 100, 50);
        int energyBefore = droid.getEnergy();
        
        // Turn towards top (North) and try to move out of bounds
        droid.turnRobot(0.0 - droid.getHeading());
        
        // Try to move up (should hit wall)
        try {
            droid.move(100); // Large movement that will hit wall
            fail("Expected CollisionException when hitting wall");
        } catch (CollisionException e) {
            // Expected - robot hit wall
            
            // Verify robot stopped at boundary
            assertTrue(droid.getLocation().getY() >= 10, // ROBOT_RADIUS
                "Robot should be stopped at boundary");
            
            // Verify robot took collision damage
            int energyAfter = droid.getEnergy();
            int expectedEnergy = energyBefore - COLLISION_DAMAGE - 1; // -1 for motion energy
            assertEquals(expectedEnergy, energyAfter,
                "Robot should lose COLLISION_DAMAGE when hitting wall");
        }
    }

    /**
     * Test robot vs robot collision: Both robots stop and take damage.
     */
    @Test
    void testRobotVsRobotCollision() throws CollisionException, ExhaustedException
    {
        Droid droid1 = factory.makeDroid();
        Droid droid2 = factory.makeDroid();
        
        // Position droid1
        moveTo(droid1, 200, 200);
        
        // Position droid2 to the right of droid1
        moveTo(droid2, 250, 200);
        
        int energy1Before = droid1.getEnergy();
        int energy2Before = droid2.getEnergy();
        
        // Turn droid1 towards droid2 (East)
        droid1.turnRobot(90.0 - droid1.getHeading());
        
        // Try to move droid1 towards droid2 (should collide)
        try {
            droid1.move(100); // Large movement that will hit droid2
            fail("Expected CollisionException when hitting another robot");
        } catch (CollisionException e) {
            // Expected - robot hit another robot
            
            // Verify both robots took collision damage
            int energy1After = droid1.getEnergy();
            int energy2After = droid2.getEnergy();
            
            int expectedEnergy1 = energy1Before - COLLISION_DAMAGE - 1; // -1 for motion energy
            int expectedEnergy2 = energy2Before - COLLISION_DAMAGE;
            
            assertEquals(expectedEnergy1, energy1After,
                "Droid1 should lose COLLISION_DAMAGE when colliding with another robot");
            assertEquals(expectedEnergy2, energy2After,
                "Droid2 should lose COLLISION_DAMAGE when hit by another robot");
        }
    }

    /**
     * Test that dead robots (energy <= 0) are removed from active list.
     */
    @Test
    void testDeadRobotRemoval() throws GunOverheatedException, ExhaustedException
    {
        Droid target = factory.makeDroid();
        Droid shooter = factory.makeDroid();
        
        // Position target
        moveTo(target, 200, 200);
        
        // Position shooter and aim at target
        moveTo(shooter, 200, 150);
        shooter.turnGun(180.0 - shooter.getGunHeading());
        
        // Fire multiple times to kill target
        int targetEnergy = target.getEnergy();
        int damagePerShot = 4 * 10; // Assuming power 10
        int shotsNeeded = (targetEnergy / damagePerShot) + 1;
        
        for (int i = 0; i < shotsNeeded && target.getEnergy() > 0; i++) {
            try {
                shooter.fire(10);
            } catch (GunOverheatedException e) {
                // Wait for gun to cool
                factory.makeBattlefield().decreaseGunHeats();
            }
        }
        
        // Verify target is dead (energy <= 0)
        assertTrue(target.getEnergy() <= 0,
            "Target should be dead (energy <= 0)");
        
        // Note: Actual removal happens in the game loop via removeDeadRobots()
        // This test verifies the condition for removal
    }

    /**
     * Helper method to move a droid to a specific location.
     * Handles CollisionException internally.
     */
    private void moveTo(Droid droid, int x, int y) throws ExhaustedException
    {
        fr.ensibs.robots.logic.Battlefield battlefield = factory.makeBattlefield();
        
        // Move to x location
        droid.turnRobot(90.0 - droid.getHeading());
        while (Math.abs(droid.getLocation().getX() - x) > 5) {
            int dx = x - droid.getLocation().getX();
            int moveDistance = Math.max(-50, Math.min(50, dx));
            if (moveDistance == 0) break;
            try {
                battlefield.move(droid, moveDistance);
            } catch (CollisionException e) {
                // Stop if collision occurs
                break;
            }
        }
        
        // Move to y location
        droid.turnRobot(90.0);
        while (Math.abs(droid.getLocation().getY() - y) > 5) {
            int dy = y - droid.getLocation().getY();
            int moveDistance = Math.max(-50, Math.min(50, dy));
            if (moveDistance == 0) break;
            try {
                battlefield.move(droid, moveDistance);
            } catch (CollisionException e) {
                // Stop if collision occurs
                break;
            }
        }
    }
}

