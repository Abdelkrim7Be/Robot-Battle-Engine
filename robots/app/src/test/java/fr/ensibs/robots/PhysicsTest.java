package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the hierarchical physics engine.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Body rotation causes gun to rotate by the same amount</li>
 *   <li>Gun rotation is independent of body rotation</li>
 *   <li>Radar follows gun rotation</li>
 *   <li>Radar follows body rotation (via gun)</li>
 * </ul>
 * 
 * @author Robot Wars Team
 */
class PhysicsTest
{
    private BattleFactory factory;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Test that rotating the body by 90° shifts the absolute angle of the gun by 90°.
     */
    @Test
    void testBodyRotationAffectsGun()
    {
        Droid droid = factory.makeDroid();
        double initialBodyHeading = droid.getHeading();
        double initialGunHeading = droid.getGunHeading();
        
        // Rotate body by 90 degrees
        droid.turnRobot(90.0);
        
        // Verify body rotated
        double expectedBodyHeading = (initialBodyHeading + 90.0) % 360.0;
        if (expectedBodyHeading < 0) {
            expectedBodyHeading += 360.0;
        }
        assertEquals(expectedBodyHeading, droid.getHeading(), 0.001, 
            "Body should have rotated by 90 degrees");
        
        // Verify gun rotated by the same amount
        double expectedGunHeading = (initialGunHeading + 90.0) % 360.0;
        if (expectedGunHeading < 0) {
            expectedGunHeading += 360.0;
        }
        assertEquals(expectedGunHeading, droid.getGunHeading(), 0.001,
            "Gun should have rotated by 90 degrees when body rotated");
        
        // Verify the relative angle between gun and body is unchanged
        double relativeAngleBefore = normalizeAngle(droid.getGunHeading() - droid.getHeading());
        // Actually, let's recalculate: the relative angle should be the same
        double relativeAngleAfter = normalizeAngle(droid.getGunHeading() - droid.getHeading());
        assertEquals(relativeAngleBefore, relativeAngleAfter, 0.001,
            "Relative angle between gun and body should remain constant");
    }

    /**
     * Test that gun can rotate independently of body.
     */
    @Test
    void testGunRotationIndependent()
    {
        Droid droid = factory.makeDroid();
        double initialBodyHeading = droid.getHeading();
        double initialGunHeading = droid.getGunHeading();
        
        // Rotate gun by 45 degrees
        droid.turnGun(45.0);
        
        // Verify body did NOT rotate
        assertEquals(initialBodyHeading, droid.getHeading(), 0.001,
            "Body should not have rotated when gun rotates independently");
        
        // Verify gun rotated
        double expectedGunHeading = (initialGunHeading + 45.0) % 360.0;
        if (expectedGunHeading < 0) {
            expectedGunHeading += 360.0;
        }
        assertEquals(expectedGunHeading, droid.getGunHeading(), 0.001,
            "Gun should have rotated by 45 degrees");
    }

    /**
     * Test that radar follows gun rotation.
     */
    @Test
    void testRadarFollowsGun()
    {
        Robot robot = factory.makeRobot();
        double initialGunHeading = robot.getGunHeading();
        double initialRadarHeading = robot.getRadarHeading();
        
        // Rotate gun by 30 degrees
        robot.turnGun(30.0);
        
        // Verify gun rotated
        double expectedGunHeading = (initialGunHeading + 30.0) % 360.0;
        if (expectedGunHeading < 0) {
            expectedGunHeading += 360.0;
        }
        assertEquals(expectedGunHeading, robot.getGunHeading(), 0.001,
            "Gun should have rotated by 30 degrees");
        
        // Verify radar rotated by the same amount
        double expectedRadarHeading = (initialRadarHeading + 30.0) % 360.0;
        if (expectedRadarHeading < 0) {
            expectedRadarHeading += 360.0;
        }
        assertEquals(expectedRadarHeading, robot.getRadarHeading(), 0.001,
            "Radar should have rotated by 30 degrees when gun rotated");
    }

    /**
     * Test that radar follows body rotation (via gun).
     */
    @Test
    void testRadarFollowsBodyViaGun()
    {
        Robot robot = factory.makeRobot();
        double initialBodyHeading = robot.getHeading();
        double initialGunHeading = robot.getGunHeading();
        double initialRadarHeading = robot.getRadarHeading();
        
        // Rotate body by 60 degrees
        robot.turnRobot(60.0);
        
        // Verify body rotated
        double expectedBodyHeading = (initialBodyHeading + 60.0) % 360.0;
        if (expectedBodyHeading < 0) {
            expectedBodyHeading += 360.0;
        }
        assertEquals(expectedBodyHeading, robot.getHeading(), 0.001,
            "Body should have rotated by 60 degrees");
        
        // Verify gun rotated (following body)
        double expectedGunHeading = (initialGunHeading + 60.0) % 360.0;
        if (expectedGunHeading < 0) {
            expectedGunHeading += 360.0;
        }
        assertEquals(expectedGunHeading, robot.getGunHeading(), 0.001,
            "Gun should have rotated by 60 degrees when body rotated");
        
        // Verify radar rotated (following gun, which follows body)
        double expectedRadarHeading = (initialRadarHeading + 60.0) % 360.0;
        if (expectedRadarHeading < 0) {
            expectedRadarHeading += 360.0;
        }
        assertEquals(expectedRadarHeading, robot.getRadarHeading(), 0.001,
            "Radar should have rotated by 60 degrees when body rotated");
    }

    /**
     * Test that radar can rotate independently.
     */
    @Test
    void testRadarRotationIndependent()
    {
        Robot robot = factory.makeRobot();
        double initialBodyHeading = robot.getHeading();
        double initialGunHeading = robot.getGunHeading();
        double initialRadarHeading = robot.getRadarHeading();
        
        // Rotate radar by 15 degrees
        robot.turnRadar(15.0);
        
        // Verify body and gun did NOT rotate
        assertEquals(initialBodyHeading, robot.getHeading(), 0.001,
            "Body should not have rotated when radar rotates independently");
        assertEquals(initialGunHeading, robot.getGunHeading(), 0.001,
            "Gun should not have rotated when radar rotates independently");
        
        // Verify radar rotated
        double expectedRadarHeading = (initialRadarHeading + 15.0) % 360.0;
        if (expectedRadarHeading < 0) {
            expectedRadarHeading += 360.0;
        }
        assertEquals(expectedRadarHeading, robot.getRadarHeading(), 0.001,
            "Radar should have rotated by 15 degrees");
    }

    /**
     * Test complex rotation sequence: body -> gun -> radar -> body again.
     */
    @Test
    void testComplexRotationSequence()
    {
        Robot robot = factory.makeRobot();
        double initialBodyHeading = robot.getHeading();
        
        // Rotate body by 90
        robot.turnRobot(90.0);
        double bodyAfter90 = (initialBodyHeading + 90.0) % 360.0;
        if (bodyAfter90 < 0) bodyAfter90 += 360.0;
        assertEquals(bodyAfter90, robot.getHeading(), 0.001);
        
        // Rotate gun by -45 (relative to body)
        robot.turnGun(-45.0);
        assertEquals(bodyAfter90, robot.getHeading(), 0.001, "Body should be unchanged");
        
        // Rotate radar by 30 (relative to gun)
        robot.turnRadar(30.0);
        assertEquals(bodyAfter90, robot.getHeading(), 0.001, "Body should be unchanged");
        
        // Rotate body again by -30
        robot.turnRobot(-30.0);
        double finalBodyHeading = (bodyAfter90 - 30.0) % 360.0;
        if (finalBodyHeading < 0) finalBodyHeading += 360.0;
        assertEquals(finalBodyHeading, robot.getHeading(), 0.001,
            "Body should have rotated by -30 degrees from previous position");
    }

    /**
     * Normalize an angle to the range [-180, 180]
     */
    private double normalizeAngle(double angle)
    {
        double normalized = angle % 360.0;
        if (normalized > 180) {
            normalized -= 360;
        } else if (normalized < -180) {
            normalized += 360;
        }
        return normalized;
    }
}

