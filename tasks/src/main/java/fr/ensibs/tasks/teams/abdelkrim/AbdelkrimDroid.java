package fr.ensibs.tasks.teams.abdelkrim;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.teams.abdelkrim.Utils.normalRelativeAngle;

/**
 * AbdelkrimS Team Droid - Aggressive Hunter Strategy
 * 
 * <p>Behavior:
 * - Receives target positions from leader
 * - Rushes toward targets aggressively
 * - Fires frequently when aligned
 * - Follows leader closely
 */
public class AbdelkrimDroid implements RobotTask<Robot>
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
        
        // STEP 2: If we have a target, attack it aggressively
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
            
            // Fire if reasonably aligned (within 20 degrees for aggressive firing)
            double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
            if (gunTurnDegrees < 20) {
                try {
                    int power = gunTurnDegrees < 5 ? 3 : 1;
                    robot.fire(power);
                } catch (GunOverheatedException | ExhaustedException e) {
                    // Ignore
                }
            }
            
            // Move toward target aggressively - ALWAYS MOVE
            double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(robot.getHeading()));
            try {
                robot.turnRobot(Math.toDegrees(bodyTurn)); // Full turn for faster response
                robot.move(Math.min(50, distance / 2)); // MUCH MORE AGGRESSIVE movement (50 max)
            } catch (CollisionException | ExhaustedException e) {
                // Turn away on collision but keep trying to move
                try {
                    robot.turnRobot(90);
                    robot.move(30); // Still move after turning
                } catch (Exception ex) {
                    // If still blocked, try different direction
                    try {
                        robot.turnRobot(-90);
                        robot.move(20);
                    } catch (Exception ex2) {
                        // Ignore
                    }
                }
            }
        } else {
            // STEP 3: No target - ALWAYS move toward center to engage
            Location current = robot.getLocation();
            double centerX = FIELD_WIDTH / 2.0;
            double centerY = FIELD_HEIGHT / 2.0;
            double dx = centerX - current.getX();
            double dy = centerY - current.getY();
            double distance = Math.hypot(dx, dy);
            
            if (distance > 30) { // Move toward center - ALWAYS MOVE
                double absoluteBearingRad = Math.atan2(dx, -dy);
                double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(robot.getHeading()));
                
                try {
                    robot.turnRobot(Math.toDegrees(bodyTurn)); // Full turn
                    robot.move(Math.min(50, distance / 2)); // MUCH MORE AGGRESSIVE (50 max distance)
                } catch (CollisionException | ExhaustedException e) {
                    // Turn away on collision but keep moving
                    try {
                        robot.turnRobot(90);
                        robot.move(40); // Still move aggressively
                    } catch (Exception ex) {
                        // Try opposite direction
                        try {
                            robot.turnRobot(-90);
                            robot.move(30);
                        } catch (Exception ex2) {
                            // Ignore
                        }
                    }
                }
            } else {
                // At center - patrol around
                moveCounter++;
                try {
                    robot.move(10); // Forward movement
                    
                    // Turn periodically to patrol
                    if (moveCounter % 20 == 0) {
                        double randomTurn = (Math.random() - 0.5) * 90; // -45 to +45 degrees
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
}

