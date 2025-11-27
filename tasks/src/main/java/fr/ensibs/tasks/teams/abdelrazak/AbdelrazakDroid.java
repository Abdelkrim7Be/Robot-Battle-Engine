package fr.ensibs.tasks.teams.abdelrazak;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.teams.abdelrazak.Utils.normalRelativeAngle;

/**
 * AbdelrazakS Team Droid - Berserker Rush Strategy
 * 
 * <p>Behavior:
 * - Rushes toward any target received
 * - Fires constantly while moving
 * - Closes distance as fast as possible
 * - No defensive behavior - pure offense
 */
public class AbdelrazakDroid implements RobotTask<Robot>
{
    private Robot robot;
    private double targetX = -1, targetY = -1;
    private boolean hasTarget = false;
    
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
            return;
        }
        
        // Check for messages from leader
        if (robot instanceof Droid droid) {
            try {
                java.lang.reflect.Method getQueueMethod = droid.getClass().getMethod("getMessageQueue");
                @SuppressWarnings("unchecked")
                List<TeamMessage> messages = (List<TeamMessage>) getQueueMethod.invoke(droid);
                
                for (TeamMessage msg : messages) {
                    if (msg.getType() == TeamMessage.MessageType.BROADCAST) {
                        String messageData = (String) msg.getData();
                        if (messageData != null && messageData.startsWith("TARGET:")) {
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
                // Ignore
            }
        }
        
        // If we have a target, rush toward it (berserker)
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
            
            // Fire constantly while moving (berserker - fires even when not perfectly aligned)
            double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
            if (gunTurnDegrees < 30) { // Very loose requirement
                try {
                    robot.fire(3); // Maximum power always
                } catch (GunOverheatedException | ExhaustedException e) {
                    // Ignore - keep trying
                }
            }
            
            // Rush toward target (close distance as fast as possible)
            double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(robot.getHeading()));
            try {
                robot.turnRobot(Math.toDegrees(bodyTurn));
                robot.move(Math.min(50, distance / 2)); // MUCH MORE AGGRESSIVE (50 max)
            } catch (CollisionException | ExhaustedException e) {
                // Keep moving anyway (berserker)
                try {
                    robot.turnRobot(90);
                    robot.move(40);
                } catch (Exception ex) {
                    try {
                        robot.turnRobot(-90);
                        robot.move(30);
                    } catch (Exception ex2) {
                        // Ignore
                    }
                }
            }
        } else {
            // No target - ALWAYS move toward center aggressively
            Location current = robot.getLocation();
            double centerX = FIELD_WIDTH / 2.0;
            double centerY = FIELD_HEIGHT / 2.0;
            double dx = centerX - current.getX();
            double dy = centerY - current.getY();
            double distance = Math.hypot(dx, dy);
            
            if (distance > 30) {
                double absoluteBearingRad = Math.atan2(dx, -dy);
                double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(robot.getHeading()));
                try {
                    robot.turnRobot(Math.toDegrees(bodyTurn));
                    robot.move(Math.min(50, distance / 2)); // AGGRESSIVE toward center
                } catch (CollisionException | ExhaustedException e) {
                    try {
                        robot.turnRobot(90);
                        robot.move(40);
                    } catch (Exception ex) {
                        try {
                            robot.turnRobot(-90);
                            robot.move(30);
                        } catch (Exception ex2) {
                            // Ignore
                        }
                    }
                }
            } else {
                // At center - move aggressively anyway (berserker)
                try {
                    double randomTurn = (Math.random() - 0.5) * 90;
                    robot.turnRobot(randomTurn);
                    robot.move(40); // Large movement
                } catch (CollisionException | ExhaustedException e) {
                    try {
                        robot.turnRobot(90);
                        robot.move(30);
                    } catch (Exception ex) {
                        // Ignore
                    }
                }
            }
        }
    }
}

