package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.CollisionException;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.ExhaustedException;
import fr.ensibs.robots.logic.GunOverheatedException;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.TeamMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation shared by droids and robots.
 * 
 * <p>Manages core droid functionality including:
 * <ul>
 *   <li>Location and movement tracking (via Body component)</li>
 *   <li>Energy management (consumption and recovery)</li>
 *   <li>Gun heat tracking (via Gun component)</li>
 *   <li>Body and gun heading management (via Body and Gun components)</li>
 * </ul>
 * 
 * <p>When the body turns, the gun automatically follows. The gun can also
 * turn independently of the body. This is managed through the hierarchical
 * Body -> Gun component relationship.
 */
class BaseDroid implements Droid
{
    private final BattlefieldImpl battlefield;
    private final Body body;
    private final Gun gun;
    private int energy;
    private final List<TeamMessage> messageQueue; // Queue of received messages

    BaseDroid(BattlefieldImpl battlefield, Location spawn, int initialEnergy, double initialHeading)
    {
        this.battlefield = battlefield;
        this.body = new Body(spawn, initialHeading);
        this.gun = new Gun(body, initialHeading);
        this.energy = initialEnergy;
        this.messageQueue = new ArrayList<>();
    }

    BattlefieldImpl getBattlefield()
    {
        return battlefield;
    }
    
    Body getBody()
    {
        return body;
    }
    
    Gun getGun()
    {
        return gun;
    }

    Location getLocationInternal()
    {
        return body.getLocation();
    }

    void setLocation(Location location)
    {
        body.setLocation(location);
    }

    void adjustEnergy(int delta)
    {
        this.energy = Math.max(0, this.energy + delta);
    }

    void setGunHeat(int gunHeat)
    {
        gun.setHeat(gunHeat);
    }

    void increaseGunHeat(int delta)
    {
        gun.increaseHeat(delta);
    }

    void requireEnergy(int amount) throws ExhaustedException
    {
        if (energy < amount) {
            throw new ExhaustedException(energy);
        }
    }

    void consumeEnergy(int amount) throws ExhaustedException
    {
        requireEnergy(amount);
        adjustEnergy(-amount);
    }

    static double normalize(double value)
    {
        return Body.normalize(value);
    }

    @Override
    public Location getLocation()
    {
        return body.getLocation();
    }

    @Override
    public int getEnergy()
    {
        return energy;
    }

    @Override
    public int getGunHeat()
    {
        return gun.getHeat();
    }

    @Override
    public double getHeading()
    {
        return body.getHeading();
    }

    double getGunHeadingInternal()
    {
        return gun.getHeading();
    }

    @Override
    public double getGunHeading()
    {
        return gun.getHeading();
    }

    @Override
    public void fire(int firePower) throws GunOverheatedException, ExhaustedException
    {
        battlefield.fire(this, firePower);
    }

    @Override
    public void move(double distance) throws CollisionException, ExhaustedException
    {
        battlefield.move(this, distance);
    }

    @Override
    public void turnRobot(double degrees)
    {
        // Rotate body - this returns the delta that was applied
        double bodyDelta = body.rotate(degrees);
        // Gun automatically follows body rotation
        gun.onBodyRotated(bodyDelta);
    }

    @Override
    public void turnGun(double degrees)
    {
        // Gun rotates independently of body
        gun.rotate(degrees);
    }
    
    /**
     * Receive a message from the team leader.
     * Messages are queued and processed when processMessages() is called.
     * 
     * @param message the message to receive
     */
    void receiveMessage(TeamMessage message)
    {
        if (message != null && energy > 0) { // Only process messages if alive
            messageQueue.add(message);
        }
    }
    
    /**
     * Process all queued messages from the team leader.
     * This should be called each game tick to execute commands.
     * 
     * <p>Messages are processed in order and then cleared from the queue.
     */
    void processMessages()
    {
        if (energy <= 0) {
            messageQueue.clear(); // Dead droids don't process messages
            return;
        }
        
        for (TeamMessage message : messageQueue) {
            try {
                processMessage(message);
            } catch (Exception e) {
                // Silently ignore exceptions from message processing
                // (e.g., collision, exhausted, etc.)
            }
        }
        
        messageQueue.clear();
    }
    
    /**
     * Process a single message.
     * 
     * @param message the message to process
     */
    private void processMessage(TeamMessage message)
    {
        switch (message.getType()) {
            case MOVE:
                Double distance = message.getDataAsDouble();
                if (distance != null) {
                    move(distance);
                }
                break;
                
            case TURN_BODY:
                Double bodyAngle = message.getDataAsDouble();
                if (bodyAngle != null) {
                    turnRobot(bodyAngle);
                }
                break;
                
            case TURN_GUN:
                Double gunAngle = message.getDataAsDouble();
                if (gunAngle != null) {
                    turnGun(gunAngle);
                }
                break;
                
            case FIRE:
                Integer power = message.getDataAsInteger();
                if (power != null) {
                    fire(power);
                }
                break;
                
            case MOVE_TO:
                Location target = message.getDataAsLocation();
                if (target != null) {
                    moveTo(target);
                }
                break;
                
            case AIM_AT:
                Location aimTarget = message.getDataAsLocation();
                if (aimTarget != null) {
                    aimAt(aimTarget);
                }
                break;
                
            case BROADCAST:
                // Custom message - can be handled by subclasses
                break;
        }
    }
    
    /**
     * Move towards a target location.
     * 
     * @param target the target location
     */
    private void moveTo(Location target)
    {
        Location current = getLocation();
        double dx = target.getX() - current.getX();
        double dy = target.getY() - current.getY();
        double distance = Math.hypot(dx, dy);
        
        if (distance > 5) { // Only move if not already close
            // Calculate angle to target
            double angle = Math.toDegrees(Math.atan2(dx, -dy));
            if (angle < 0) {
                angle += 360;
            }
            
            // Turn towards target
            double currentHeading = getHeading();
            double turnAngle = angle - currentHeading;
            
            // Normalize turn angle to shortest path
            if (turnAngle > 180) {
                turnAngle -= 360;
            } else if (turnAngle < -180) {
                turnAngle += 360;
            }
            
            turnRobot(turnAngle);
            
            // Move forward
            double moveDistance = Math.min(distance, 50); // Max move per command
            try {
                move(moveDistance);
            } catch (CollisionException | ExhaustedException e) {
                // Ignore - can't move
            }
        }
    }
    
    /**
     * Aim gun at a target location.
     * 
     * @param target the target location
     */
    private void aimAt(Location target)
    {
        Location current = getLocation();
        double dx = target.getX() - current.getX();
        double dy = target.getY() - current.getY();
        
        // Calculate angle to target
        double angle = Math.toDegrees(Math.atan2(dx, -dy));
        if (angle < 0) {
            angle += 360;
        }
        
        // Turn gun towards target
        double currentGunHeading = getGunHeading();
        double turnAngle = angle - currentGunHeading;
        
        // Normalize turn angle to shortest path
        if (turnAngle > 180) {
            turnAngle -= 360;
        } else if (turnAngle < -180) {
            turnAngle += 360;
        }
        
        turnGun(turnAngle);
    }
    
    /**
     * Get the current message queue (for testing/debugging).
     * 
     * @return a copy of the message queue
     */
    List<TeamMessage> getMessageQueue()
    {
        return new ArrayList<>(messageQueue);
    }
}

