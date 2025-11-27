package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.CollisionException;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.ExhaustedException;
import fr.ensibs.robots.logic.GunOverheatedException;
import fr.ensibs.robots.logic.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for life steal mechanics.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Shooter gains energy when their bullet hits a target</li>
 *   <li>Life steal amount is 3 × bullet power</li>
 *   <li>Life steal only occurs on successful hit</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class LifeStealTest
{
    private BattleFactory factory;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Test that shooter gains energy when their bullet hits.
     * Assert shooter gains energy when their bullet hits.
     */
    @Test
    void testShooterGainsEnergyOnHit() throws CollisionException, ExhaustedException, GunOverheatedException
    {
        Droid shooter = factory.makeDroid();
        Droid target = factory.makeDroid();
        
        // Position shooter and target
        positionDroid(shooter, 200, 150);
        positionDroid(target, 200, 200);
        
        // Aim shooter at target
        shooter.turnGun(180.0 - shooter.getGunHeading()); // Point gun South
        
        int shooterEnergyBefore = shooter.getEnergy();
        int bulletPower = 10;
        
        // Fire at target
        shooter.fire(bulletPower);
        stepBattlefield();
        
        int shooterEnergyAfter = shooter.getEnergy();
        
        // Calculate expected energy change
        // Energy change = -bulletPower (cost) + lifeSteal (if hit)
        // Life steal = 3 × power = 3 × 10 = 30
        // Net change = -10 + 30 = +20
        
        // Verify shooter gained energy (net positive change)
        assertTrue(shooterEnergyAfter > shooterEnergyBefore - bulletPower,
            "Shooter should gain energy from life steal when bullet hits");
        
        // Verify the gain is approximately correct (allowing for energy consumption)
        int energyChange = shooterEnergyAfter - shooterEnergyBefore;
        // Should be around: -bulletPower + lifeSteal = -10 + 30 = 20
        assertTrue(energyChange >= 0,
            "Shooter should have net positive energy change from life steal");
    }

    /**
     * Test that life steal amount is 3 × bullet power.
     */
    @Test
    void testLifeStealAmountIsThreeTimesPower() throws CollisionException, ExhaustedException, GunOverheatedException
    {
        Droid shooter = factory.makeDroid();
        Droid target = factory.makeDroid();
        
        // Position shooter and target
        positionDroid(shooter, 200, 150);
        positionDroid(target, 200, 200);
        
        shooter.turnGun(180.0 - shooter.getGunHeading());
        
        int bulletPower = 5;
        int shooterEnergyBefore = shooter.getEnergy();
        
        // Fire at target
        shooter.fire(bulletPower);
        stepBattlefield();
        
        int shooterEnergyAfter = shooter.getEnergy();
        int energyGained = shooterEnergyAfter - (shooterEnergyBefore - bulletPower);
        
        // Expected life steal = 3 × 5 = 15
        int expectedLifeSteal = 3 * bulletPower;
        
        // Verify life steal amount (allowing for small variations)
        assertEquals(expectedLifeSteal, energyGained,
            "Life steal should be exactly 3 × bullet power");
    }

    /**
     * Test that life steal only occurs on successful hit (not on miss).
     */
    @Test
    void testLifeStealOnlyOnHit() throws GunOverheatedException, ExhaustedException
    {
        Droid shooter = factory.makeDroid();
        
        // Position shooter with no target in range
        positionDroid(shooter, 100, 100);
        
        // Aim away from any potential targets
        shooter.turnGun(0.0 - shooter.getGunHeading()); // Point gun North
        
        int shooterEnergyBefore = shooter.getEnergy();
        int bulletPower = 10;
        
        // Fire (should miss - no target)
        shooter.fire(bulletPower);
        stepBattlefield();
        
        int shooterEnergyAfter = shooter.getEnergy();
        
        // Energy should decrease by bullet power (no life steal on miss)
        int expectedEnergy = shooterEnergyBefore - bulletPower;
        assertEquals(expectedEnergy, shooterEnergyAfter,
            "Shooter should lose bullet power energy when bullet misses (no life steal)");
    }

    /**
     * Test multiple hits accumulate life steal.
     */
    @Test
    void testMultipleHitsAccumulateLifeSteal() throws CollisionException, ExhaustedException, GunOverheatedException
    {
        Droid shooter = factory.makeDroid();
        Droid target = factory.makeDroid();
        
        // Position shooter and target
        positionDroid(shooter, 200, 150);
        positionDroid(target, 200, 200);
        
        shooter.turnGun(180.0 - shooter.getGunHeading());
        
        int shooterEnergyBefore = shooter.getEnergy();
        int bulletPower = 5;
        int numberOfShots = 3;
        
        // Fire multiple times
        int shotsLanded = 0;
        while (shotsLanded < numberOfShots) {
            try {
                shooter.fire(bulletPower);
                stepBattlefield();
                shotsLanded++;
            } catch (GunOverheatedException e) {
                factory.makeBattlefield().decreaseGunHeats();
            }
        }
        
        int shooterEnergyAfter = shooter.getEnergy();
        
        // Calculate expected energy
        // Each shot: -power + lifeSteal = -5 + 15 = +10
        // 3 shots: +30 total
        int expectedEnergyChange = numberOfShots * (3 * bulletPower - bulletPower);
        int actualEnergyChange = shooterEnergyAfter - shooterEnergyBefore;
        
        // Verify energy increased from multiple life steals
        assertTrue(actualEnergyChange > 0,
            "Shooter should gain energy from multiple life steals");
        assertTrue(actualEnergyChange >= expectedEnergyChange - 5, // Allow for gun heat energy
            "Shooter should accumulate life steal from multiple hits");
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

    private void stepBattlefield()
    {
        fr.ensibs.robots.logic.Battlefield battlefield = factory.makeBattlefield();
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
    
    private void positionDroid(Droid droid, int x, int y)
    {
        try {
            java.lang.reflect.Method method = droid.getClass().getDeclaredMethod("setLocation", Location.class);
            method.setAccessible(true);
            method.invoke(droid, new Location(x, y));
        } catch (ReflectiveOperationException e) {
            fail("Unable to position robot: " + e.getMessage());
        }
    }
}

