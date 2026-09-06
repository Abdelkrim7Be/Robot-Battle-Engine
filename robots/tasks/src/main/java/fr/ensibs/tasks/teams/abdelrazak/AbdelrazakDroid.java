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
    private double lastX = -1, lastY = -1;
    private int stuckCounter = 0;
    private double patrolAngle = Math.random() * 360; // Unique starting angle per robot
    
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
            Location current = robot.getLocation();
            int edgeMargin = 50;
            int centerRadius = 250; // Increased radius to avoid clustering
            
            double centerX = FIELD_WIDTH / 2.0;
            double centerY = FIELD_HEIGHT / 2.0;
            double distToCenter = Math.hypot(current.getX() - centerX, current.getY() - centerY);
            
            // Stuck detection: if we haven't moved much, we're stuck
            if (lastX >= 0 && lastY >= 0) {
                double moved = Math.hypot(current.getX() - lastX, current.getY() - lastY);
                if (moved < 3) {
                    stuckCounter++;
                    if (stuckCounter > 5) { // More aggressive - trigger after 5 ticks
                        // Scatter away from center aggressively
                        double scatterAngle = Math.random() * 360;
                        if (distToCenter < centerRadius) {
                            // If stuck in center, move directly away
                            double dx = current.getX() - centerX;
                            double dy = current.getY() - centerY;
                            scatterAngle = Math.toDegrees(Math.atan2(dx, dy));
                        }
                        try {
                            robot.turnRobot(scatterAngle - robot.getHeading());
                            robot.move(40);
                        } catch (CollisionException | ExhaustedException e) {
                            robot.turnRobot(180);
                        }
                        stuckCounter = 0;
                        patrolAngle = Math.random() * 360; // Reset patrol angle
                        lastX = current.getX();
                        lastY = current.getY();
                        return;
                    }
                } else {
                    stuckCounter = 0;
                }
            }
            lastX = current.getX();
            lastY = current.getY();
            
            // Near edges: move toward center (but stop before getting too close)
            if (current.getX() < edgeMargin || current.getX() > FIELD_WIDTH - edgeMargin ||
                current.getY() < edgeMargin || current.getY() > FIELD_HEIGHT - edgeMargin) {
                // Only move toward center if we're not already too close
                if (distToCenter > centerRadius + 50) {
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
                        robot.move(25);
                    } catch (CollisionException | ExhaustedException e) {
                        robot.turnRobot(120);
                    }
                } else {
                    // Already close enough to center, patrol around it
                    double angle = Math.toDegrees(Math.atan2(current.getY() - centerY, current.getX() - centerX));
                    angle += 5; // Move in circle
                    double patrolX = centerX + centerRadius * Math.cos(Math.toRadians(angle));
                    double patrolY = centerY + centerRadius * Math.sin(Math.toRadians(angle));
                    double dx = patrolX - current.getX();
                    double dy = patrolY - current.getY();
                    double moveAngle = Math.toDegrees(Math.atan2(dx, -dy));
                    if (moveAngle < 0) moveAngle += 360;
                    double currentHeading = robot.getHeading();
                    double turnAngle = moveAngle - currentHeading;
                    if (turnAngle > 180) turnAngle -= 360;
                    if (turnAngle < -180) turnAngle += 360;
                    try {
                        robot.turnRobot(turnAngle);
                        robot.move(25);
                    } catch (CollisionException | ExhaustedException e) {
                        robot.turnRobot(120);
                    }
                }
            } 
            // Too close to center: SCATTER - move directly away from center
            else if (distToCenter < centerRadius) {
                // Move directly away from center (no circular component to avoid convergence)
                double dx = current.getX() - centerX;
                double dy = current.getY() - centerY;
                // Normalize
                double dist = Math.max(1, Math.hypot(dx, dy));
                double unitX = dx / dist;
                double unitY = dy / dist;
                // Add small random component to scatter robots
                double randomX = (Math.random() - 0.5) * 0.3;
                double randomY = (Math.random() - 0.5) * 0.3;
                double moveX = unitX + randomX;
                double moveY = unitY + randomY;
                double angle = Math.toDegrees(Math.atan2(moveX, -moveY));
                if (angle < 0) angle += 360;
                double currentHeading = robot.getHeading();
                double turnAngle = angle - currentHeading;
                if (turnAngle > 180) turnAngle -= 360;
                if (turnAngle < -180) turnAngle += 360;
                try {
                    robot.turnRobot(turnAngle);
                    robot.move(40); // Fast escape for berserker
                } catch (CollisionException | ExhaustedException e) {
                    // Scatter in random direction when colliding
                    double scatterDir = Math.random() * 360;
                    robot.turnRobot(scatterDir - currentHeading);
                }
            } 
            // Middle area: patrol in a ring with unique angle per robot
            else {
                // Each robot has unique patrol angle to avoid convergence
                patrolAngle += 2.0; // Angular velocity (unique per robot)
                if (patrolAngle > 360) patrolAngle -= 360;
                
                double desiredRadius = centerRadius + 100; // Wider ring
                double targetX = centerX + desiredRadius * Math.cos(Math.toRadians(patrolAngle));
                double targetY = centerY + desiredRadius * Math.sin(Math.toRadians(patrolAngle));
                double dx = targetX - current.getX();
                double dy = targetY - current.getY();
                double angle = Math.toDegrees(Math.atan2(dx, -dy));
                if (angle < 0) angle += 360;
                double currentHeading = robot.getHeading();
                double turnAngle = angle - currentHeading;
                if (turnAngle > 180) turnAngle -= 360;
                if (turnAngle < -180) turnAngle += 360;
                try {
                    robot.turnRobot(turnAngle);
                    robot.move(25);
                } catch (CollisionException | ExhaustedException e) {
                    // If collision, change patrol angle to scatter
                    patrolAngle = Math.random() * 360;
                    robot.turnRobot(90);
                }
            }
        }
    }
}

