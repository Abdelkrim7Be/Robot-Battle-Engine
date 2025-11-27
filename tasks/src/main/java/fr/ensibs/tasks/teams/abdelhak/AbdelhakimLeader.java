package fr.ensibs.tasks.teams.abdelhak;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.teams.abdelhak.Utils.normalRelativeAngle;

/**
 * AbdelhakimS Team Leader - Defensive Sniper Strategy
 * 
 * <p>Behavior:
 * - Moves to corner and holds position
 * - Precise long-range shots (only fires when within 3 degrees)
 * - Always uses maximum power (sniper shots)
 */
public class AbdelhakimLeader implements RobotTask<TeamLeader>
{
    public static final String TEAM_NAME = "AbdelhakimS";
    public static final java.awt.Color TEAM_COLOR = new java.awt.Color(255, 0, 255); // Magenta
    
    private TeamLeader leader;
    private int radarDirection = 1;
    private double targetX = -1, targetY = -1;
    private boolean hasTarget = false;
    private boolean inCorner = false;
    private int cornerX, cornerY;
    
    @Override
    public void setRobot(TeamLeader leader)
    {
        this.leader = leader;
        // Determine nearest corner
        Location current = leader.getLocation();
        int fieldWidth = FIELD_WIDTH;
        int fieldHeight = FIELD_HEIGHT;
        int margin = 100;
        
        // Find nearest corner
        double distToTopLeft = Math.hypot(current.getX() - margin, current.getY() - margin);
        double distToTopRight = Math.hypot(current.getX() - (fieldWidth - margin), current.getY() - margin);
        double distToBottomLeft = Math.hypot(current.getX() - margin, current.getY() - (fieldHeight - margin));
        double distToBottomRight = Math.hypot(current.getX() - (fieldWidth - margin), current.getY() - (fieldHeight - margin));
        
        if (distToTopLeft <= distToTopRight && distToTopLeft <= distToBottomLeft && distToTopLeft <= distToBottomRight) {
            cornerX = margin;
            cornerY = margin;
        } else if (distToTopRight <= distToBottomLeft && distToTopRight <= distToBottomRight) {
            cornerX = fieldWidth - margin;
            cornerY = margin;
        } else if (distToBottomLeft <= distToBottomRight) {
            cornerX = margin;
            cornerY = fieldHeight - margin;
        } else {
            cornerX = fieldWidth - margin;
            cornerY = fieldHeight - margin;
        }
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
            // STEP 1: Spin radar continuously
            leader.turnRadar(10.0 * radarDirection);
            radarDirection *= -1;
            
            // STEP 2: Move to corner if not there yet
            if (!inCorner) {
                Location current = leader.getLocation();
                double dx = cornerX - current.getX();
                double dy = cornerY - current.getY();
                double distance = Math.hypot(dx, dy);
                
                if (distance > 30) {
                    double angle = Math.toDegrees(Math.atan2(dx, -dy));
                    if (angle < 0) angle += 360;
                    
                    double currentHeading = leader.getHeading();
                    double turnAngle = angle - currentHeading;
                    if (turnAngle > 180) turnAngle -= 360;
                    if (turnAngle < -180) turnAngle += 360;
                    
                    try {
                        leader.turnRobot(turnAngle / 2);
                        leader.move(Math.min(5, distance / 10));
                    } catch (CollisionException | ExhaustedException e) {
                        leader.turnRobot(90);
                    }
                } else {
                    inCorner = true;
                }
            }
            
            // STEP 3: Scan for enemies
            List<Location> enemies = leader.scan();
            Location enemyLocation = null;
            
            // Filter out teammates
            for (Location enemyLoc : enemies) {
                boolean isTeammate = false;
                for (Droid teammate : leader.getTeammates()) {
                    double dx = enemyLoc.getX() - teammate.getLocation().getX();
                    double dy = enemyLoc.getY() - teammate.getLocation().getY();
                    if (Math.hypot(dx, dy) < 50) {
                        isTeammate = true;
                        break;
                    }
                }
                double dx = enemyLoc.getX() - leader.getLocation().getX();
                double dy = enemyLoc.getY() - leader.getLocation().getY();
                if (Math.hypot(dx, dy) < 50) {
                    isTeammate = true;
                }
                
                if (!isTeammate) {
                    enemyLocation = enemyLoc;
                    break;
                }
            }
            
            if (enemyLocation != null) {
                targetX = enemyLocation.getX();
                targetY = enemyLocation.getY();
                hasTarget = true;
                
                // Broadcast to team
                String targetMessage = "TARGET:" + targetX + ":" + targetY;
                TeamMessage message = new TeamMessage(TeamMessage.MessageType.BROADCAST, targetMessage, leader);
                leader.broadcastMessage(message);
                
                // Calculate angle to target
                Location current = leader.getLocation();
                double dx = targetX - current.getX();
                double dy = targetY - current.getY();
                double absoluteBearingRad = Math.atan2(dx, -dy);
                
                // Turn gun toward target
                double gunHeadingRad = Math.toRadians(leader.getGunHeading());
                double gunTurnRad = normalRelativeAngle(absoluteBearingRad - gunHeadingRad);
                leader.turnGun(Math.toDegrees(gunTurnRad));
                
                // Fire only when VERY precisely aligned (within 3 degrees) - sniper behavior
                double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
                if (gunTurnDegrees < 3) {
                    try {
                        leader.fire(3); // Always maximum power (sniper)
                    } catch (GunOverheatedException | ExhaustedException e) {
                        // Ignore
                    }
                }
            } else {
                hasTarget = false;
            }
            
            // Minimal movement when in corner (defensive positioning)
            if (inCorner) {
                // Just hold position, minimal adjustments
            }
            
        } catch (ExhaustedException e) {
            try {
                leader.turnRadar(10.0 * radarDirection);
            } catch (Exception ex) {
                // Ignore
            }
        }
    }
}

