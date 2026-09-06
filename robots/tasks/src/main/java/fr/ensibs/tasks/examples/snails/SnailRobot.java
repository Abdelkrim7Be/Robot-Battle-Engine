package fr.ensibs.tasks.examples.snails;

import fr.ensibs.robots.logic.*;

import java.util.Random;

/**
 * A task for a robot that acts like a snail
 *
 * @author Pascale Launay
 */
public class SnailRobot implements RobotTask<Robot>
{
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private int direction = RANDOM.nextInt(360);
    private Robot robot;

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

    // CRITICAL FIX: Track position to detect stuck state
    private double lastX = -1, lastY = -1;
    private int stuckCounter = 0;
    private int fireCooldown = 0; // Prevent firing every tick
    
    @Override
    public void run()
    {
        // CRITICAL FIX: Check if stuck
        Location current = robot.getLocation();
        double currentX = current.getX();
        double currentY = current.getY();
        
        if (lastX >= 0 && lastY >= 0) {
            double moved = Math.sqrt(Math.pow(currentX - lastX, 2) + Math.pow(currentY - lastY, 2));
            if (moved < 1) {
                stuckCounter++;
                if (stuckCounter > 30) {
                    // Emergency unstuck - turn away and move
                    direction = RANDOM.nextInt(360);
                    stuckCounter = 0;
                }
            } else {
                stuckCounter = 0;
            }
        }
        lastX = currentX;
        lastY = currentY;
        
        try {
            // CRITICAL FIX: Fix syntax error - RANDOM.nextInt(-5, 5) doesn't exist
            // Change direction slightly, not completely random
            direction += RANDOM.nextInt(11) - 5; // -5 to +5 degrees
            direction = direction % 360;
            if (direction < 0) direction += 360;
            
            robot.turnRobot(direction);
            robot.move(3); // Move forward 3 pixels
            
            // CRITICAL FIX: Don't fire every tick - use cooldown
            fireCooldown--;
            if (fireCooldown <= 0) {
                robot.turnGun(5);
                try {
                    robot.fire(1);
                    fireCooldown = 10; // Fire every 10 ticks max
                } catch (GunOverheatedException | ExhaustedException e) {
                    // Gun overheated - wait longer
                    fireCooldown = 30;
                }
            }
        } catch (CollisionException e) {
            // CRITICAL FIX: Turn away from collision, don't just turn 180
            direction = (direction + 90 + RANDOM.nextInt(180)) % 360;
            stuckCounter = 0; // Reset stuck counter
        } catch (ExhaustedException e) {
            // Out of energy - reduce activity
            fireCooldown = 60;
        }
    }
}
