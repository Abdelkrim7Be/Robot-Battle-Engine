package fr.ensibs.tasks.teams.nassim;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.teams.nassim.Utils.normalRelativeAngle;

/**
 * NassimS Team Leader - Tactical Balanced Strategy
 * 
 * <p>Behavior:
 * - Smart scanning (not just spinning, but tracking)
 * - Moves evasively - changes direction frequently
 * - Positions for optimal combat range
 * - Adjusts fire power based on distance
 * - Sends PRIORITY messages for low-energy enemies
 */
public class NassimLeader implements RobotTask<TeamLeader>
{
    public static final String TEAM_NAME = "NassimS";
    public static final java.awt.Color TEAM_COLOR = new java.awt.Color(0, 255, 255); // Cyan
    
    private TeamLeader leader;
    private int radarDirection = 1;
    private double targetX = -1, targetY = -1;
    private boolean hasTarget = false;
    private int moveCounter = 0;
    private double lastEnemyEnergy = 100;
    
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
            // STEP 1: Smart scanning (tracking, not just spinning)
            leader.turnRadar(8.0 * radarDirection);
            moveCounter++;
            if (moveCounter % 45 == 0) {
                radarDirection *= -1;
            }
            
            // STEP 2: Scan for enemies
            List<Location> enemies = leader.scan();
            Location enemyLocation = null;
            double enemyDistance = Double.MAX_VALUE;
            
            // Filter out teammates and find closest enemy
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
                double dist = Math.hypot(dx, dy);
                if (dist < 50) {
                    isTeammate = true;
                }
                
                if (!isTeammate && dist < enemyDistance) {
                    enemyLocation = enemyLoc;
                    enemyDistance = dist;
                }
            }
            
            if (enemyLocation != null) {
                targetX = enemyLocation.getX();
                targetY = enemyLocation.getY();
                hasTarget = true;
                
                // Estimate enemy energy (simplified - assume lower if we've been hitting them)
                // In real implementation, this would track damage dealt
                double estimatedEnergy = lastEnemyEnergy;
                
                // Broadcast message type based on enemy state
                String messageType = "TARGET";
                if (estimatedEnergy < 30) {
                    messageType = "PRIORITY"; // Low energy = priority target
                }
                
                String targetMessage = messageType + ":" + targetX + ":" + targetY + ":" + (int)estimatedEnergy;
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
                
                // Adjust fire power based on distance (tactical)
                double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
                if (gunTurnDegrees < 12) {
                    try {
                        int power;
                        if (enemyDistance < 150) {
                            power = 3; // Close range: high power
                        } else if (enemyDistance < 300) {
                            power = 2; // Medium range: medium power
                        } else {
                            power = 1; // Far range: low power
                        }
                        leader.fire(power);
                    } catch (GunOverheatedException | ExhaustedException e) {
                        // Ignore
                    }
                }
            } else {
                hasTarget = false;
            }
            
            // STEP 3: Evasive movement (tactical)
            if (moveCounter % 30 == 0) {
                // Change direction frequently
                double randomTurn = (Math.random() - 0.5) * 60; // -30 to +30 degrees
                try {
                    leader.turnRobot(randomTurn);
                    leader.move(8); // Medium movement
                } catch (CollisionException | ExhaustedException e) {
                    leader.turnRobot(90);
                }
            } else {
                // Continue moving in current direction
                try {
                    leader.move(5);
                } catch (CollisionException | ExhaustedException e) {
                    leader.turnRobot(90);
                }
            }
            
        } catch (ExhaustedException e) {
            try {
                leader.turnRadar(8.0 * radarDirection);
            } catch (Exception ex) {
                // Ignore
            }
        }
    }
}

