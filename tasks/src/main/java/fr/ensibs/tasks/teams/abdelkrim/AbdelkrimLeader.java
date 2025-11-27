package fr.ensibs.tasks.teams.abdelkrim;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.teams.abdelkrim.Utils.normalRelativeAngle;

/**
 * AbdelkrimS Team Leader - Aggressive Hunter Strategy
 * 
 * <p>Behavior:
 * - Actively hunts enemies, moves toward center
 * - Aggressive scanning with continuous radar spinning
 * - Broadcasts target positions to teammates
 * - Fires when reasonably aligned (within 10-15 degrees)
 */
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
        // DEBUG: Log that run() was called
        System.out.println("[AbdelkrimLeader] run() called - energy: " + leader.getEnergy());
        
        if (leader.getEnergy() <= 0) {
            System.out.println("[AbdelkrimLeader] Robot is dead, returning");
            return; // Dead
        }
        
        try {
            // STEP 1: Always spin radar to find enemies
            leader.turnRadar(10.0 * radarDirection);
            
            // Reverse radar direction periodically
            moveCounter++;
            if (moveCounter % 36 == 0) { // Every 36 ticks (360 degrees / 10)
                radarDirection *= -1;
            }
            
            // STEP 2: Scan for enemies
            List<Location> enemies = leader.scan();
            Location enemyLocation = null;
            
            // Filter out teammates from scan results
            for (Location enemyLoc : enemies) {
                boolean isTeammate = false;
                // Check if this location is close to any teammate
                for (Droid teammate : leader.getTeammates()) {
                    double dx = enemyLoc.getX() - teammate.getLocation().getX();
                    double dy = enemyLoc.getY() - teammate.getLocation().getY();
                    double dist = Math.hypot(dx, dy);
                    if (dist < 50) { // Within 50 pixels = likely teammate
                        isTeammate = true;
                        break;
                    }
                }
                // Also check if close to self
                double dx = enemyLoc.getX() - leader.getLocation().getX();
                double dy = enemyLoc.getY() - leader.getLocation().getY();
                double dist = Math.hypot(dx, dy);
                if (dist < 50) {
                    isTeammate = true;
                }
                
                if (!isTeammate) {
                    enemyLocation = enemyLoc;
                    break; // Attack first enemy found
                }
            }
            
            if (enemyLocation != null) {
                // Found enemy!
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
                
                // Fire if reasonably aligned (within 15 degrees)
                double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
                if (gunTurnDegrees < 15) {
                    try {
                        // Use power based on distance (more power when closer)
                        double distance = Math.hypot(dx, dy);
                        int power = distance < 150 ? 3 : (distance < 300 ? 2 : 1);
                        leader.fire(power);
                    } catch (GunOverheatedException | ExhaustedException e) {
                        // Ignore
                    }
                }
            } else {
                hasTarget = false;
            }
            
            // STEP 3: Move aggressively toward center
            Location current = leader.getLocation();
            int fieldWidth = FIELD_WIDTH;
            int fieldHeight = FIELD_HEIGHT;
            int centerX = fieldWidth / 2;
            int centerY = fieldHeight / 2;
            
            double dx = centerX - current.getX();
            double dy = centerY - current.getY();
            double distanceToCenter = Math.hypot(dx, dy);
            
            if (distanceToCenter > 50) {
                // Move toward center
                double angleToCenter = Math.toDegrees(Math.atan2(dx, -dy));
                if (angleToCenter < 0) angleToCenter += 360;
                
                double currentHeading = leader.getHeading();
                double turnAngle = angleToCenter - currentHeading;
                if (turnAngle > 180) turnAngle -= 360;
                if (turnAngle < -180) turnAngle += 360;
                
                try {
                    leader.turnRobot(turnAngle); // Full turn for faster response
                    leader.move(Math.min(50, distanceToCenter / 2)); // MUCH MORE AGGRESSIVE (50 max)
                } catch (CollisionException | ExhaustedException e) {
                    // Turn away on collision but keep moving aggressively
                    try {
                        leader.turnRobot(90);
                        leader.move(40); // Still move aggressively
                    } catch (Exception ex) {
                        // Try opposite direction
                        try {
                            leader.turnRobot(-90);
                            leader.move(30);
                        } catch (Exception ex2) {
                            // Ignore
                        }
                    }
                }
            } else {
                reachedCenter = true;
                // Once at center, move in hunting pattern
                if (moveCounter % 60 == 0) {
                    double randomTurn = (Math.random() - 0.5) * 60; // -30 to +30 degrees
                    try {
                        leader.turnRobot(randomTurn);
                        leader.move(10);
                    } catch (CollisionException | ExhaustedException e) {
                        leader.turnRobot(90);
                    }
                }
            }
            
        } catch (ExhaustedException e) {
            // Out of energy - just turn radar
            try {
                leader.turnRadar(10.0 * radarDirection);
            } catch (Exception ex) {
                // Ignore
            }
        }
    }
}

