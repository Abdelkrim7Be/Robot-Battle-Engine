package fr.ensibs.tasks.examples.ducks;

import fr.ensibs.robots.logic.*;

import java.util.List;

import static fr.ensibs.tasks.examples.ducks.Utils.normalRelativeAngle;

/**
 * Simple sample AI for leader robots.
 * 
 * <p>Simple behavior:
 * - Always spin radar to find enemies
 * - Move forward constantly
 * - Turn randomly to avoid getting stuck
 * - Shoot at enemies when detected
 * - Broadcast targets to team
 */
public class DuckLeader implements RobotTask<TeamLeader>
{
    private TeamLeader leader;
    private int radarDirection = 1; // 1 = right, -1 = left
    private double targetX = -1, targetY = -1;
    private boolean hasTarget = false;
    private int moveCounter = 0;
    
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
            if (!enemies.isEmpty()) {
                // Found enemy!
                Location enemy = enemies.get(0);
                targetX = enemy.getX();
                targetY = enemy.getY();
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
                
                // Fire if roughly aligned (loose requirement for more shooting)
                double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
                if (gunTurnDegrees < 20) { // Fire if within 20 degrees
                    try {
                        int power = gunTurnDegrees < 5 ? 3 : 1; // Max power if accurate
                        leader.fire(power);
                    } catch (GunOverheatedException | ExhaustedException e) {
                        // Ignore
                    }
                }
            } else {
                hasTarget = false;
            }
            
            // STEP 3: Always move forward
            try {
                // Move forward
                leader.move(5); // Forward movement
                
                // Turn randomly to avoid getting stuck
                if (moveCounter % 30 == 0) {
                    double randomTurn = (Math.random() - 0.5) * 30; // -15 to +15 degrees
                    leader.turnRobot(randomTurn);
                }
            } catch (CollisionException | ExhaustedException e) {
                // Turn away on collision
                leader.turnRobot(90);
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
