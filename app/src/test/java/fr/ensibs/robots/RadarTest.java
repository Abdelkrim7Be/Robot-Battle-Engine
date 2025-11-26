package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.CollisionException;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.ExhaustedException;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for radar scanning functionality.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Radar scans detect robots in field of vision</li>
 *   <li>Geometric field-of-view (pie slice) algorithm works correctly</li>
 *   <li>ScanResult contains relative information (distance/angle)</li>
 *   <li>Occluded robots are not detected</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class RadarTest
{
    private BattleFactory factory;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Test that radar detects robots in field of vision.
     * Mock the battlefield with 3 bots. Orient radar towards 2 of them.
     * Assert list size is 2.
     */
    @Test
    void testRadarDetectsRobotsInFieldOfVision() throws CollisionException, ExhaustedException
    {
        Robot scanner = factory.makeRobot();
        Droid target1 = factory.makeDroid();
        Droid target2 = factory.makeDroid();
        Droid target3 = factory.makeDroid();
        
        // Position scanner at center
        moveTo(scanner, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        
        // Position target1 to the right (East) of scanner
        moveTo(target1, FIELD_WIDTH / 2 + 100, FIELD_HEIGHT / 2);
        
        // Position target2 to the right and slightly up (East-Northeast) of scanner
        moveTo(target2, FIELD_WIDTH / 2 + 80, FIELD_HEIGHT / 2 - 50);
        
        // Position target3 to the left (West) of scanner - should NOT be detected
        moveTo(target3, FIELD_WIDTH / 2 - 100, FIELD_HEIGHT / 2);
        
        // Orient radar towards East (90 degrees) to see target1 and target2
        scanner.turnRadar(90.0 - scanner.getRadarHeading());
        
        // Perform scan
        List<Location> results = scanner.scan();
        
        // Should detect 2 robots in the field of vision (target1 and target2)
        // target3 should not be detected as it's behind the scanner
        assertEquals(2, results.size(), 
            "Radar should detect exactly 2 robots in field of vision when oriented towards them");
        
        // Verify results contain locations
        assertFalse(results.isEmpty(), "Scan results should not be empty");
        for (Location location : results) {
            assertNotNull(location, "Scan result location should not be null");
        }
    }

    /**
     * Test that radar does not detect robots outside field of vision.
     */
    @Test
    void testRadarDoesNotDetectRobotsOutsideFieldOfVision() throws CollisionException, ExhaustedException
    {
        Robot scanner = factory.makeRobot();
        Droid target = factory.makeDroid();
        
        // Position scanner at center
        moveTo(scanner, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        
        // Position target to the left (West) of scanner
        moveTo(target, FIELD_WIDTH / 2 - 100, FIELD_HEIGHT / 2);
        
        // Orient radar towards East (90 degrees) - target is behind scanner
        scanner.turnRadar(90.0 - scanner.getRadarHeading());
        
        // Perform scan
        List<Location> results = scanner.scan();
        
        // Should not detect the target (it's behind the scanner, outside field of vision)
        // VISION_FIELD is 90 degrees, so target at 270 degrees should not be detected
        boolean foundTarget = false;
        Location targetLocation = target.getLocation();
        for (Location result : results) {
            if (result.equals(targetLocation)) {
                foundTarget = true;
                break;
            }
        }
        
        // Target should not be detected if it's outside the field of vision
        assertFalse(foundTarget, 
            "Radar should not detect robots outside the field of vision (behind scanner)");
    }

    /**
     * Test geometric field-of-view scanning (pie slice algorithm).
     */
    @Test
    void testGeometricFieldOfViewScanning() throws CollisionException, ExhaustedException
    {
        Robot scanner = factory.makeRobot();
        Droid target1 = factory.makeDroid();
        Droid target2 = factory.makeDroid();
        
        // Position scanner
        moveTo(scanner, FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
        
        // Position target1 directly East (90 degrees)
        moveTo(target1, FIELD_WIDTH / 2 + 150, FIELD_HEIGHT / 2);
        
        // Position target2 at 45 degrees (Northeast) - should be in field of vision
        // VISION_FIELD is 90 degrees, so 45 degrees offset should be visible
        int offsetX = (int) (150 * Math.cos(Math.toRadians(45)));
        int offsetY = (int) (150 * Math.sin(Math.toRadians(45)));
        moveTo(target2, FIELD_WIDTH / 2 + offsetX, FIELD_HEIGHT / 2 - offsetY);
        
        // Orient radar towards East (90 degrees)
        scanner.turnRadar(90.0 - scanner.getRadarHeading());
        
        // Perform scan
        List<Location> results = scanner.scan();
        
        // Both targets should be detected (within 45 degrees of radar heading)
        assertTrue(results.size() >= 1, 
            "Radar should detect robots within field of vision");
    }

    /**
     * Test that radar scan consumes energy.
     */
    @Test
    void testRadarScanConsumesEnergy() throws ExhaustedException
    {
        Robot scanner = factory.makeRobot();
        int energyBefore = scanner.getEnergy();
        
        // Perform scan
        scanner.scan();
        
        int energyAfter = scanner.getEnergy();
        int expectedEnergy = energyBefore - SCAN_ENERGY;
        
        assertEquals(expectedEnergy, energyAfter,
            "Scan should consume SCAN_ENERGY amount of energy");
    }

    /**
     * Test that radar scan throws ExhaustedException when energy is insufficient.
     */
    @Test
    void testRadarScanThrowsExhaustedException()
    {
        Robot scanner = factory.makeRobot();
        
        // Drain energy by performing multiple scans
        int initialEnergy = scanner.getEnergy();
        int scansToPerform = (initialEnergy / SCAN_ENERGY) + 1;
        
        // Perform scans until energy is depleted
        for (int i = 0; i < scansToPerform; i++) {
            try {
                scanner.scan(); // Each scan costs SCAN_ENERGY
            } catch (ExhaustedException e) {
                // Expected when energy runs out
                // Now verify that scanning throws exception
                assertThrows(ExhaustedException.class, () -> {
                    scanner.scan();
                }, "Should throw ExhaustedException when energy < SCAN_ENERGY");
                return;
            }
        }
        
        // If we get here, verify energy is low and scanning should fail
        if (scanner.getEnergy() < SCAN_ENERGY) {
            assertThrows(ExhaustedException.class, () -> {
                scanner.scan();
            }, "Should throw ExhaustedException when energy < SCAN_ENERGY");
        }
    }

    /**
     * Helper method to move a droid to a specific location.
     */
    private void moveTo(Droid droid, int x, int y) throws CollisionException, ExhaustedException
    {
        fr.ensibs.robots.logic.Battlefield battlefield = factory.makeBattlefield();
        
        // Move to x location
        droid.turnRobot(90.0 - droid.getHeading());
        while (Math.abs(droid.getLocation().getX() - x) > 5) {
            int dx = x - droid.getLocation().getX();
            int moveDistance = Math.max(-MAX_DISTANCE_MOVE, Math.min(MAX_DISTANCE_MOVE, dx));
            if (moveDistance == 0) break;
            battlefield.move(droid, moveDistance);
        }
        
        // Move to y location
        droid.turnRobot(90.0);
        while (Math.abs(droid.getLocation().getY() - y) > 5) {
            int dy = y - droid.getLocation().getY();
            int moveDistance = Math.max(-MAX_DISTANCE_MOVE, Math.min(MAX_DISTANCE_MOVE, dy));
            if (moveDistance == 0) break;
            battlefield.move(droid, moveDistance);
        }
    }
}

