package fr.ensibs.tasks.teams.nassim;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.tasks.teams.nassim.Utils.normalRelativeAngle;

/**
 * NassimS Team Droid - Tactical Balanced Strategy
 * 
 * <p>Behavior:
 * - Processes both TARGET and PRIORITY messages
 * - PRIORITY targets take precedence
 * - Uses appropriate power for distance
 * - Maintains medium range - not too close, not too far
 * - Strafes while engaging (moves perpendicular to enemy)
 */
public class NassimDroid implements RobotTask<Robot>
{
    private Robot robot;
    private double targetX = -1, targetY = -1;
    private boolean hasTarget = false;
    private boolean isPriority = false;
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
            return;
        }
        
        // Check for messages from leader
        if (robot instanceof Droid droid) {
            try {
                java.lang.reflect.Method getQueueMethod = droid.getClass().getMethod("getMessageQueue");
                @SuppressWarnings("unchecked")
                List<TeamMessage> messages = (List<TeamMessage>) getQueueMethod.invoke(droid);
                
                // Process messages - PRIORITY takes precedence
                for (TeamMessage msg : messages) {
                    if (msg.getType() == TeamMessage.MessageType.BROADCAST) {
                        String messageData = (String) msg.getData();
                        if (messageData != null) {
                            if (messageData.startsWith("PRIORITY:")) {
                                // Priority target - take this one
                                String[] parts = messageData.split(":");
                                if (parts.length >= 3) {
                                    try {
                                        targetX = Double.parseDouble(parts[1]);
                                        targetY = Double.parseDouble(parts[2]);
                                        hasTarget = true;
                                        isPriority = true;
                                    } catch (NumberFormatException e) {
                                        // Ignore
                                    }
                                }
                            } else if (messageData.startsWith("TARGET:") && !isPriority) {
                                // Regular target - only use if no priority
                                String[] parts = messageData.split(":");
                                if (parts.length >= 3) {
                                    try {
                                        targetX = Double.parseDouble(parts[1]);
                                        targetY = Double.parseDouble(parts[2]);
                                        hasTarget = true;
                                        isPriority = false;
                                    } catch (NumberFormatException e) {
                                        // Ignore
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // If we have a target, engage tactically
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
            
            // Fire with appropriate power for distance (tactical)
            double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
            if (gunTurnDegrees < 15) {
                try {
                    int power;
                    if (distance < 150) {
                        power = 3; // Close: high power
                    } else if (distance < 300) {
                        power = 2; // Medium: medium power
                    } else {
                        power = 1; // Far: low power
                    }
                    robot.fire(power);
                } catch (GunOverheatedException | ExhaustedException e) {
                    // Ignore
                }
            }
            
            // Move toward target aggressively
            double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(robot.getHeading()));
            try {
                robot.turnRobot(Math.toDegrees(bodyTurn));
                robot.move(Math.min(50, distance / 2)); // AGGRESSIVE movement
            } catch (CollisionException | ExhaustedException e) {
                // Turn away but keep moving
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
            Location current = robot.getLocation();
            moveCounter++;
            int margin = 50;
            
            if (current.getX() < margin || current.getX() > 1280 - margin ||
                current.getY() < margin || current.getY() > 960 - margin) {
                double centerX = 1280 / 2.0;
                double centerY = 960 / 2.0;
                double dx = centerX - current.getX();
                double dy = centerY - current.getY();
                double angle = Math.toDegrees(Math.atan2(dx, -dy));
                if (angle < 0) angle += 360;
                double currentHeading = robot.getHeading();
                double turnAngle = angle - currentHeading;
                if (turnAngle > 180) turnAngle -= 360;
                if (turnAngle < -180) turnAngle += 360;
                try {
                    robot.turnRobot(turnAngle);
                    robot.move(20);
                } catch (CollisionException | ExhaustedException e) {
                    robot.turnRobot(90);
                }
            } else {
                if (moveCounter % 40 == 0) {
                    double randomTurn = (Math.random() - 0.5) * 60;
                    try {
                        robot.turnRobot(randomTurn);
                        robot.move(20);
                    } catch (CollisionException | ExhaustedException e) {
                        robot.turnRobot(90);
                    }
                } else {
                    try {
                        robot.move(15);
                    } catch (CollisionException | ExhaustedException e) {
                        robot.turnRobot(90);
                    }
                }
            }
        }
    }
}

