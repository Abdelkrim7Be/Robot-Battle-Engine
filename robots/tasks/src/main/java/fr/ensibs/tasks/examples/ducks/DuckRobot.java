package fr.ensibs.tasks.examples.ducks;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.examples.ducks.Utils.normalRelativeAngle;

/**
 * NUCLEAR OPTION: Simple working AI for droid robot.
 * 
 * <p>Simple behavior:
 * - Check for messages from leader
 * - If target received, move toward it and shoot
 * - If no target, patrol (move forward and turn)
 */
public class DuckRobot implements RobotTask<Robot>
{
    private Robot robot;
    private double targetX = -1, targetY = -1;
    private boolean hasTarget = false;
    private int moveCounter = 0;

    @Override
    public void setRobot(Robot robot)
    {
        this.robot = robot;
    }

    @Override
    public Robot getRobot()
    {
        return this.robot;
    }

    @Override
    public void run()
    {
        // NUCLEAR OPTION: Simple working AI
        if (robot.getEnergy() <= 0) {
            return; // Dead
        }
        
        // STEP 1: Check for messages from leader
        if (robot instanceof Droid droid) {
            try {
                java.lang.reflect.Method getQueueMethod = droid.getClass().getMethod("getMessageQueue");
                @SuppressWarnings("unchecked")
                List<TeamMessage> messages = (List<TeamMessage>) getQueueMethod.invoke(droid);
                
                for (TeamMessage msg : messages) {
                    if (msg.getType() == TeamMessage.MessageType.BROADCAST) {
                        String messageData = (String) msg.getData();
                        if (messageData != null && messageData.startsWith("TARGET:")) {
                            // Parse "TARGET:x:y"
                            String[] parts = messageData.split(":");
                            if (parts.length >= 3) {
                                try {
                                    targetX = Double.parseDouble(parts[1]);
                                    targetY = Double.parseDouble(parts[2]);
                                    hasTarget = true;
                                    System.out.println("[DROID] Target received: (" + targetX + ", " + targetY + ")");
                                } catch (NumberFormatException e) {
                                    // Ignore
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Reflection failed - continue without message processing
            }
        }
        
        // STEP 2: If we have a target, attack it
        if (hasTarget && targetX >= 0 && targetY >= 0) {
            Location current = robot.getLocation();
            double dx = targetX - current.getX();
            double dy = targetY - current.getY();
            double distance = Math.hypot(dx, dy);
            
            // Calculate angle to target
            double absoluteBearingRad = Math.atan2(dx, -dy);
            
            // Turn gun toward target
            double gunHeadingRad = Math.toRadians(robot.getGunHeading());
            double gunTurnRad = normalRelativeAngle(absoluteBearingRad - gunHeadingRad);
            robot.turnGun(Math.toDegrees(gunTurnRad));
            
            // Fire if roughly aligned (loose requirement)
            double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
            if (gunTurnDegrees < 20) { // Fire if within 20 degrees
                try {
                    int power = gunTurnDegrees < 5 ? 3 : 1; // Max power if accurate
                    robot.fire(power);
                    System.out.println("[DROID] FIRED power " + power + " at distance " + (int)distance);
                } catch (GunOverheatedException | ExhaustedException e) {
                    // Ignore
                }
            }
            
            // Move toward target
            double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(robot.getHeading()));
            try {
                robot.turnRobot(Math.toDegrees(bodyTurn) / 2); // Gradual turn
                robot.move(Math.min(5, distance / 10)); // Move toward target
            } catch (CollisionException | ExhaustedException e) {
                // Turn away on collision
                try {
                    robot.turnRobot(90);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        } else {
            // STEP 3: No target - patrol (always move)
            moveCounter++;
            try {
                // Always move forward
                robot.move(5); // Forward movement
                
                // Turn periodically to patrol
                if (moveCounter % 40 == 0) {
                    double randomTurn = (Math.random() - 0.5) * 60; // -30 to +30 degrees
                    robot.turnRobot(randomTurn);
                }
            } catch (CollisionException | ExhaustedException e) {
                // Turn away on collision
                try {
                    robot.turnRobot(90);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
    }
}
