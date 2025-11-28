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
                int margin = 50;
                
                if (current.getX() < margin || current.getX() > FIELD_WIDTH - margin ||
                    current.getY() < margin || current.getY() > FIELD_HEIGHT - margin) {
                    double centerX = FIELD_WIDTH / 2.0;
                    double centerY = FIELD_HEIGHT / 2.0;
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
                        leader.turnRobot(90);
                    }
                } else {
                    if (moveCounter % 40 == 0) {
                        double randomTurn = (Math.random() - 0.5) * 60;
                        try {
                            leader.turnRobot(randomTurn);
                            leader.move(20);
                        } catch (CollisionException | ExhaustedException e) {
                            leader.turnRobot(90);
                        }
                    } else {
                        try {
                            leader.move(15);
                        } catch (CollisionException | ExhaustedException e) {
                            leader.turnRobot(90);
                        }
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

