package fr.ensibs.tasks.teams.abdelrazak;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.teams.abdelrazak.Utils.normalRelativeAngle;

/**
 * AbdelrazakS Team Leader - Berserker Rush Strategy
 * 
 * <p>Behavior:
 * - Maximum aggression, constant movement
 * - Never stops moving
 * - Fires even when not perfectly aligned (within 20 degrees)
 * - Turns body TOWARD enemy and rams them
 */
public class AbdelrazakLeader implements RobotTask<TeamLeader>
{
    public static final String TEAM_NAME = "AbdelrazakS";
    public static final java.awt.Color TEAM_COLOR = new java.awt.Color(255, 0, 0); // Red
    
    private TeamLeader leader;
    private int radarDirection = 1;
    private double targetX = -1, targetY = -1;
    private boolean hasTarget = false;
    
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
            // STEP 1: Always spin radar aggressively
            leader.turnRadar(15.0 * radarDirection);
            radarDirection *= -1;
            
            // STEP 2: Scan for enemies
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
                
                // Broadcast immediately
                String targetMessage = "TARGET:" + targetX + ":" + targetY;
                TeamMessage message = new TeamMessage(TeamMessage.MessageType.BROADCAST, targetMessage, leader);
                leader.broadcastMessage(message);
                
                // Calculate angle to target
                Location current = leader.getLocation();
                double dx = targetX - current.getX();
                double dy = targetY - current.getY();
                double absoluteBearingRad = Math.atan2(dx, -dy);
                
                // Turn BODY toward enemy (berserker rush)
                double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(leader.getHeading()));
                leader.turnRobot(Math.toDegrees(bodyTurn));
                
                // Turn gun toward target
                double gunHeadingRad = Math.toRadians(leader.getGunHeading());
                double gunTurnRad = normalRelativeAngle(absoluteBearingRad - gunHeadingRad);
                leader.turnGun(Math.toDegrees(gunTurnRad));
                
                // Fire even when not perfectly aligned (within 20 degrees) - reckless
                double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
                if (gunTurnDegrees < 20) {
                    try {
                        leader.fire(3); // Maximum power always
                    } catch (GunOverheatedException | ExhaustedException e) {
                        // Ignore
                    }
                }
                
                // RAM toward enemy (berserker behavior)
                try {
                    leader.move(15); // Large aggressive movement
                } catch (CollisionException | ExhaustedException e) {
                    // Keep moving anyway
                }
            } else {
                hasTarget = false;
            }
            
            // STEP 3: Always move (never stop) - berserker behavior
            if (!hasTarget) {
                try {
                    // Large random movements
                    double randomTurn = (Math.random() - 0.5) * 90; // -45 to +45 degrees
                    leader.turnRobot(randomTurn);
                    leader.move(15); // Large movement
                } catch (CollisionException | ExhaustedException e) {
                    // Turn and keep moving
                    try {
                        leader.turnRobot(90);
                        leader.move(10);
                    } catch (Exception ex) {
                        // Ignore
                    }
                }
            }
            
        } catch (ExhaustedException e) {
            // Even out of energy, keep trying to move (berserker)
            try {
                leader.turnRadar(15.0 * radarDirection);
            } catch (Exception ex) {
                // Ignore
            }
        }
    }
}

