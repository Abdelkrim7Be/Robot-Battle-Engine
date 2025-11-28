package fr.ensibs.tasks.teams.abdelhak;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.teams.abdelhak.Utils.normalRelativeAngle;

/**
 * AbdelhakimS Team Droid - Defensive Sniper Strategy
 * 
 * <p>Behavior:
 * - Stays relatively close to leader (guardian behavior)
 * - Maintains defensive positioning
 * - Uses medium power shots
 * - Prioritizes survival over aggression
 */
public class AbdelhakimDroid implements RobotTask<Robot>
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
        
        // If we have a target, engage but maintain defensive positioning
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
            
            // Fire with medium power when aligned (defensive - not too aggressive)
            double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
            if (gunTurnDegrees < 15) {
                try {
                    robot.fire(2); // Medium power (defensive)
                } catch (GunOverheatedException | ExhaustedException e) {
                    // Ignore
                }
            }
            
            // Move toward target but maintain distance (defensive)
            if (distance > 200) { // Only move closer if far away
                double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(robot.getHeading()));
                try {
                    robot.turnRobot(Math.toDegrees(bodyTurn)); // Full turn
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
            }
        } else {
            Location current = robot.getLocation();
            moveCounter++;
            int margin = 50;
            
            if (current.getX() < margin || current.getX() > FIELD_WIDTH - margin ||
                current.getY() < margin || current.getY() > FIELD_HEIGHT - margin) {
                double centerX = FIELD_WIDTH / 2.0;
                double centerY = FIELD_HEIGHT / 2.0;
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

