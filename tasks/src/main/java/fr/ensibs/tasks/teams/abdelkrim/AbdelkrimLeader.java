package fr.ensibs.tasks.teams.abdelkrim;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.teams.abdelkrim.Utils.normalRelativeAngle;

public class AbdelkrimLeader implements RobotTask<TeamLeader>
{
    public static final String TEAM_NAME = "AbdelkrimS";
    public static final java.awt.Color TEAM_COLOR = new java.awt.Color(255, 100, 0); // Orange
    
    private TeamLeader leader;
    private int radarDirection = 1; // 1 = right, -1 = left
    private double targetX = -1, targetY = -1;
    private boolean hasTarget = false;
    private int moveCounter = 0;
    private boolean reachedCenter = false;
    private double lastX = -1, lastY = -1;
    private int stuckCounter = 0;
    private double patrolAngle = Math.random() * 360; // Unique starting angle per robot
    
    @Override
    public void setRobot(TeamLeader leader)
    {
        this.leader = leader;
    }

    @Override
    public TeamLeader getRobot()
    {
        return this.leader;
    }

    @Override
    public void run()
    {
        if (leader.getEnergy() <= 0) {
            return;
        }
        
        try {
            leader.turnRadar(10.0 * radarDirection);
            moveCounter++;
            if (moveCounter % 36 == 0) {
                radarDirection *= -1;
            }
            
            List<Location> enemies = leader.scan();
            Location enemyLocation = null;
            double closestEnemyDist = Double.MAX_VALUE;
            
            for (Location enemyLoc : enemies) {
                boolean isTeammate = false;
                for (Droid teammate : leader.getTeammates()) {
                    double dx = enemyLoc.getX() - teammate.getLocation().getX();
                    double dy = enemyLoc.getY() - teammate.getLocation().getY();
                    double dist = Math.hypot(dx, dy);
                    if (dist < 50) {
                        isTeammate = true;
                        break;
                    }
                }
                double dx = enemyLoc.getX() - leader.getLocation().getX();
                double dy = enemyLoc.getY() - leader.getLocation().getY();
                double dist = Math.hypot(dx, dy);
                if (dist < 50) {
                    isTeammate = true;
                }
                
                if (!isTeammate && dist < closestEnemyDist) {
                    enemyLocation = enemyLoc;
                    closestEnemyDist = dist;
                }
            }
            
            if (enemyLocation != null) {
                targetX = enemyLocation.getX();
                targetY = enemyLocation.getY();
                hasTarget = true;
                
                String targetMessage = "TARGET:" + targetX + ":" + targetY;
                TeamMessage message = new TeamMessage(TeamMessage.MessageType.BROADCAST, targetMessage, leader);
                leader.broadcastMessage(message);
                
                Location current = leader.getLocation();
                double dx = targetX - current.getX();
                double dy = targetY - current.getY();
                double distance = Math.hypot(dx, dy);
                double absoluteBearingRad = Math.atan2(dx, -dy);
                
                double gunHeadingRad = Math.toRadians(leader.getGunHeading());
                double gunTurnRad = normalRelativeAngle(absoluteBearingRad - gunHeadingRad);
                leader.turnGun(Math.toDegrees(gunTurnRad));
                
                double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
                if (gunTurnDegrees < 15) {
                    try {
                        int power = distance < 150 ? 3 : (distance < 300 ? 2 : 1);
                        leader.fire(power);
                    } catch (GunOverheatedException | ExhaustedException e) {
                    }
                }
                
                double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(leader.getHeading()));
                double bodyTurnDegrees = Math.abs(Math.toDegrees(bodyTurn));
                
                if (distance > 200) {
                    try {
                        leader.turnRobot(Math.toDegrees(bodyTurn));
                        leader.move(Math.min(30, distance / 3));
                    } catch (CollisionException | ExhaustedException e) {
                        leader.turnRobot(90);
                    }
                } else if (distance < 100) {
                    try {
                        leader.turnRobot(Math.toDegrees(bodyTurn) + 90);
                        leader.move(20);
                    } catch (CollisionException | ExhaustedException e) {
                        leader.turnRobot(-90);
                    }
                } else {
                    try {
                        double strafeAngle = normalRelativeAngle(absoluteBearingRad - Math.toRadians(leader.getHeading()) + Math.PI / 2);
                        leader.turnRobot(Math.toDegrees(strafeAngle));
                        leader.move(15);
                    } catch (CollisionException | ExhaustedException e) {
                        leader.turnRobot(90);
                    }
                }
            } else {
                hasTarget = false;
                Location current = leader.getLocation();
                int edgeMargin = 50;
                int centerRadius = 250;
                
                double centerX = FIELD_WIDTH / 2.0;
                double centerY = FIELD_HEIGHT / 2.0;
                double distToCenter = Math.hypot(current.getX() - centerX, current.getY() - centerY);
                
                // Stuck detection
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
                                leader.turnRobot(scatterAngle - leader.getHeading());
                                leader.move(40);
                            } catch (CollisionException | ExhaustedException e) {
                                leader.turnRobot(180);
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
                
                if (current.getX() < edgeMargin || current.getX() > FIELD_WIDTH - edgeMargin ||
                    current.getY() < edgeMargin || current.getY() > FIELD_HEIGHT - edgeMargin) {
                    if (distToCenter > centerRadius + 50) {
                        double dx = centerX - current.getX();
                        double dy = centerY - current.getY();
                        double angle = Math.toDegrees(Math.atan2(dx, -dy));
                        if (angle < 0) angle += 360;
                        double currentHeading = leader.getHeading();
                        double turnAngle = angle - currentHeading;
                        if (turnAngle > 180) turnAngle -= 360;
                        if (turnAngle < -180) turnAngle += 360;
                        try {
                            leader.turnRobot(turnAngle);
                            leader.move(20);
                        } catch (CollisionException | ExhaustedException e) {
                            leader.turnRobot(120);
                        }
                    } else {
                        double angle = Math.toDegrees(Math.atan2(current.getY() - centerY, current.getX() - centerX));
                        angle += 5;
                        double patrolX = centerX + centerRadius * Math.cos(Math.toRadians(angle));
                        double patrolY = centerY + centerRadius * Math.sin(Math.toRadians(angle));
                        double dx = patrolX - current.getX();
                        double dy = patrolY - current.getY();
                        double moveAngle = Math.toDegrees(Math.atan2(dx, -dy));
                        if (moveAngle < 0) moveAngle += 360;
                        double currentHeading = leader.getHeading();
                        double turnAngle = moveAngle - currentHeading;
                        if (turnAngle > 180) turnAngle -= 360;
                        if (turnAngle < -180) turnAngle += 360;
                        try {
                            leader.turnRobot(turnAngle);
                            leader.move(20);
                        } catch (CollisionException | ExhaustedException e) {
                            leader.turnRobot(120);
                        }
                    }
                } else if (distToCenter < centerRadius) {
                    // SCATTER - move directly away from center
                    double dx = current.getX() - centerX;
                    double dy = current.getY() - centerY;
                    double dist = Math.max(1, Math.hypot(dx, dy));
                    double unitX = dx / dist;
                    double unitY = dy / dist;
                    // Add small random component to scatter
                    double randomX = (Math.random() - 0.5) * 0.3;
                    double randomY = (Math.random() - 0.5) * 0.3;
                    double moveX = unitX + randomX;
                    double moveY = unitY + randomY;
                    double angle = Math.toDegrees(Math.atan2(moveX, -moveY));
                    if (angle < 0) angle += 360;
                    double currentHeading = leader.getHeading();
                    double turnAngle = angle - currentHeading;
                    if (turnAngle > 180) turnAngle -= 360;
                    if (turnAngle < -180) turnAngle += 360;
                    try {
                        leader.turnRobot(turnAngle);
                        leader.move(35);
                    } catch (CollisionException | ExhaustedException e) {
                        double scatterDir = Math.random() * 360;
                        leader.turnRobot(scatterDir - currentHeading);
                    }
                } else {
                    // Patrol in a ring with unique angle per robot
                    patrolAngle += 2.0;
                    if (patrolAngle > 360) patrolAngle -= 360;
                    double desiredRadius = centerRadius + 100;
                    double targetX = centerX + desiredRadius * Math.cos(Math.toRadians(patrolAngle));
                    double targetY = centerY + desiredRadius * Math.sin(Math.toRadians(patrolAngle));
                    double dx = targetX - current.getX();
                    double dy = targetY - current.getY();
                    double angle = Math.toDegrees(Math.atan2(dx, -dy));
                    if (angle < 0) angle += 360;
                    double currentHeading = leader.getHeading();
                    double turnAngle = angle - currentHeading;
                    if (turnAngle > 180) turnAngle -= 360;
                    if (turnAngle < -180) turnAngle += 360;
                    try {
                        leader.turnRobot(turnAngle);
                        leader.move(20);
                    } catch (CollisionException | ExhaustedException e) {
                        patrolAngle = Math.random() * 360;
                        leader.turnRobot(90);
                    }
                }
            }
            
        } catch (ExhaustedException e) {
            try {
                leader.turnRadar(10.0 * radarDirection);
            } catch (Exception ex) {
            }
        }
    }
}

