package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.BattleSetup;
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
    private final int maxEnergy; // MISSION A.4: Store initial energy as max energy
    private final List<TeamMessage> messageQueue; // Queue of received messages
    
    // MISSION A.4: Track movement and firing for passive regeneration
    private boolean movedThisTick = false;
    private boolean firedThisTick = false;

    BaseDroid(BattlefieldImpl battlefield, Location spawn, int initialEnergy, double initialHeading)
    {
        this.battlefield = battlefield;
        this.body = new Body(spawn, initialHeading);
        this.gun = new Gun(body, initialHeading);
        this.energy = initialEnergy;
        this.maxEnergy = initialEnergy; // MISSION A.4: Store initial energy as max
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

    /**
     * Try to consume the given amount of energy without throwing.
     *
     * @param amount energy to consume
     * @return {@code true} if energy was consumed, {@code false} otherwise
     */
    protected boolean tryConsumeEnergy(int amount)
    {
        if (amount <= 0) {
            return true;
        }
        if (energy < amount) {
            return false;
        }
        adjustEnergy(-amount);
        return true;
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
        firedThisTick = true;
        battlefield.fire(this, firePower);
    }

    @Override
    public void move(double distance) throws CollisionException, ExhaustedException
    {
        movedThisTick = true;
        battlefield.move(this, distance);
    }

    @Override
    public void turnRobot(double degrees)
    {
        if (Math.abs(degrees) < 1e-9) {
            return;
        }
        if (!tryConsumeEnergy(BattleSetup.BODY_TURN_ENERGY)) {
            return;
        }
        double bodyDelta = body.rotate(degrees);
        gun.onBodyRotated(bodyDelta);
    }

    @Override
    public void turnGun(double degrees)
    {
        if (Math.abs(degrees) < 1e-9) {
            return;
        }
        if (!tryConsumeEnergy(BattleSetup.GUN_TURN_ENERGY)) {
            return;
        }
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
        try {
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
        } catch (CollisionException | ExhaustedException | GunOverheatedException e) {
            // Silently ignore exceptions from message processing
            // (e.g., collision, exhausted, overheated)
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
     * Aim gun predictively at a moving target using intercept calculation.
     * 
     * <p>This method uses predictive targeting to calculate where the target
     * will be when the bullet arrives, accounting for target velocity and
     * bullet travel time.
     * 
     * <p>MISSION 1.1: Predictive Targeting - eliminates "random turning bullshit"
     * by making robots aim where targets will be, not where they are.
     * 
     * @param targetPos the current position of the target
     * @param targetVelX the X component of target velocity (pixels per tick)
     * @param targetVelY the Y component of target velocity (pixels per tick)
     * @param bulletPower the power of the bullet to fire (affects bullet speed)
     */
    void aimPredictively(Location targetPos, double targetVelX, double targetVelY, int bulletPower)
    {
        // Calculate bullet speed based on power
        double bulletSpeed = PredictiveTargeting.calculateBulletSpeed(bulletPower);
        
        // Get gun barrel tip position (where bullet spawns)
        Location shooterPos = getLocation();
        double gunHeading = getGunHeading();
        double gunHeadingRad = Math.toRadians(gunHeading);
        
        // Calculate barrel tip position (matches BattlefieldImpl.fire logic)
        double barrelLength = BattleSetup.ROBOT_RADIUS + 25.0; // ROBOT_RADIUS + gun extension
        double barrelTipX = shooterPos.getX() + (barrelLength * Math.sin(gunHeadingRad));
        double barrelTipY = shooterPos.getY() - (barrelLength * Math.cos(gunHeadingRad));
        Location barrelTipPos = new Location((int) Math.round(barrelTipX), (int) Math.round(barrelTipY));
        
        // Calculate intercept heading
        Double interceptHeading = PredictiveTargeting.calculateIntercept(
            barrelTipPos,
            targetPos,
            targetVelX,
            targetVelY,
            bulletSpeed
        );
        
        if (interceptHeading == null) {
            // Fall back to simple aiming if intercept calculation fails
            aimAt(targetPos);
            return;
        }
        
        // Turn gun to intercept heading
        double currentGunHeading = getGunHeading();
        double turnAngle = interceptHeading - currentGunHeading;
        
        // Normalize turn angle to shortest path
        if (turnAngle > 180) {
            turnAngle -= 360;
        } else if (turnAngle < -180) {
            turnAngle += 360;
        }
        
        turnGun(turnAngle);
    }
    
    /**
     * Calculate the predicted intercept heading for a moving target.
     * 
     * <p>This is a utility method that RobotTask implementations can use
     * to calculate intercept headings without automatically turning the gun.
     * 
     * @param targetPos the current position of the target
     * @param targetVelX the X component of target velocity (pixels per tick)
     * @param targetVelY the Y component of target velocity (pixels per tick)
     * @param bulletPower the power of the bullet to fire (affects bullet speed)
     * @return the intercept heading in degrees (0-360), or null if no solution exists
     */
    Double calculateInterceptHeading(Location targetPos, double targetVelX, double targetVelY, int bulletPower)
    {
        // Calculate bullet speed based on power
        double bulletSpeed = PredictiveTargeting.calculateBulletSpeed(bulletPower);
        
        // Get gun barrel tip position (where bullet spawns)
        Location shooterPos = getLocation();
        double gunHeading = getGunHeading();
        double gunHeadingRad = Math.toRadians(gunHeading);
        
        // Calculate barrel tip position (matches BattlefieldImpl.fire logic)
        double barrelLength = BattleSetup.ROBOT_RADIUS + 25.0; // ROBOT_RADIUS + gun extension
        double barrelTipX = shooterPos.getX() + (barrelLength * Math.sin(gunHeadingRad));
        double barrelTipY = shooterPos.getY() - (barrelLength * Math.cos(gunHeadingRad));
        Location barrelTipPos = new Location((int) Math.round(barrelTipX), (int) Math.round(barrelTipY));
        
        // Calculate and return intercept heading
        return PredictiveTargeting.calculateIntercept(
            barrelTipPos,
            targetPos,
            targetVelX,
            targetVelY,
            bulletSpeed
        );
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
    
    /**
     * MISSION A.4: Check if robot moved this tick.
     * 
     * @return true if robot moved this tick
     */
    boolean isMoving()
    {
        return movedThisTick;
    }
    
    /**
     * MISSION A.4: Check if robot fired this tick.
     * 
     * @return true if robot fired this tick
     */
    boolean hasFiredThisTick()
    {
        return firedThisTick;
    }
    
    /**
     * MISSION A.4: Reset movement and firing flags for next tick.
     * Should be called at the start of each game tick.
     */
    void resetTickFlags()
    {
        movedThisTick = false;
        firedThisTick = false;
    }
    
    /**
     * MISSION A.4: Get maximum energy (starting energy).
     * 
     * @return the maximum energy value
     */
    int getMaxEnergy()
    {
        return maxEnergy;
    }
    
    /**
     * MISSION A.4: Add energy (for passive regeneration).
     * 
     * @param amount the amount of energy to add
     */
    void addEnergy(int amount)
    {
        int maxEnergy = getMaxEnergy();
        this.energy = Math.min(maxEnergy, this.energy + amount);
    }
}
