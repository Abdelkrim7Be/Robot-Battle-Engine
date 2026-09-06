package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Battlefield;
import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.CollisionException;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.ExhaustedException;
import fr.ensibs.robots.logic.GunOverheatedException;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.TeamLeader;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Battlefield implementation that enforces the {@link BattleSetup} constants.
 * 
 * <p>This implementation manages all robots on the battlefield, handles movement,
 * firing, scanning, and collision detection. It ensures all battle rules are
 * properly enforced including energy consumption, gun heat management, and
 * boundary checking.
 * 
 * <p>Thread-safe operations are supported through the use of CopyOnWriteArrayList
 * for robot storage, allowing concurrent access during battle execution.
 */
class BattlefieldImpl implements Battlefield
{
    private final List<BaseDroid> robots = new CopyOnWriteArrayList<>();
    private final List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private final List<RobotWreckage> wreckages = new CopyOnWriteArrayList<>();
    private final List<EnergyCapsule> energyCapsules = new CopyOnWriteArrayList<>();
    private final BattleZone battleZone = new BattleZone();
    private final DamageTracker damageTracker = new DamageTracker();
    private final DamageNumberManager damageNumberManager = new DamageNumberManager();

    private GamePhaseManager phaseManager;

    private final ObjectPool<Bullet> bulletPool = new ObjectPool<>(() -> new Bullet(), 50, 200);
    
    private final Random random = new Random();
    
    static class HitEvent
    {
        final Location location;
        final Color color;
        HitEvent(Location loc, Color c) { location = loc; color = c; }
    }
    private final List<HitEvent> recentHits = new ArrayList<>();
    private Location lastMuzzleFlashLocation;
    private double lastMuzzleFlashHeading;
    
    static class DamageEvent
    {
        final int damage;
        final boolean isDeath;
        final BaseDroid killer;
        final BaseDroid victim;
        DamageEvent(int dmg, boolean death, BaseDroid killer, BaseDroid victim) { 
            damage = dmg; 
            isDeath = death;
            this.killer = killer;
            this.victim = victim;
        }
    }
    private final List<DamageEvent> recentDamageEvents = new ArrayList<>();

    void register(BaseDroid droid)
    {
        robots.add(droid);
    }

    /**
     * Finds a free spawn location for a new robot.
     * 
     * <p>First attempts random placement up to 500 times. If no free location
     * is found, falls back to a deterministic grid-based placement to ensure
     * robots can always be spawned.
     * 
     * @return a free location on the battlefield
     */
    Location nextSpawn()
    {
        int spawnMargin = BattleSetup.ROBOT_RADIUS + BattleSetup.MAX_DISTANCE_MOVE;
        int maxColumn = (BattleSetup.FIELD_WIDTH - 2 * spawnMargin) / BattleSetup.MAX_DISTANCE_MOVE;
        int maxRow = (BattleSetup.FIELD_HEIGHT - 2 * spawnMargin) / BattleSetup.MAX_DISTANCE_MOVE;
        
        int index = robots.size();
        int columns = Math.max(1, maxColumn + 1);
        int rows = Math.max(1, maxRow + 1);
        int slots = columns * rows;
        for (int offset = 0; offset < slots; offset++) {
            int slot = (index + offset) % slots;
            int column = slot / rows;
            int row = slot % rows;
            int x = spawnMargin + column * BattleSetup.MAX_DISTANCE_MOVE;
            int y = spawnMargin + row * BattleSetup.MAX_DISTANCE_MOVE;
            Location candidate = new Location(
                Math.min(x, BattleSetup.FIELD_WIDTH - spawnMargin),
                Math.min(y, BattleSetup.FIELD_HEIGHT - spawnMargin)
            );
            if (isFree(candidate)) {
                return candidate;
            }
        }

        int maxAttempts = 500;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = spawnMargin + BattleSetup.MAX_DISTANCE_MOVE * random.nextInt(Math.max(1, maxColumn + 1));
            int y = spawnMargin + BattleSetup.MAX_DISTANCE_MOVE * random.nextInt(Math.max(1, maxRow + 1));
            Location candidate = new Location(x, y);
            if (isFree(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("No free spawn location available");
    }

    double nextHeading()
    {
        return 90.0;
    }
    
    /**
     * 
     * @param teamRobots list of robots to spawn
     * @param side 0 for left side, 1 for right side
     */
    void spawnTeamOnSide(List<BaseDroid> teamRobots, int side)
    {
        int fieldWidth = BattleSetup.FIELD_WIDTH;
        int fieldHeight = BattleSetup.FIELD_HEIGHT;
        int margin = 100; // Stay away from edges
        
        if (teamRobots.isEmpty()) {
            return;
        }
        
        // Team spawns on LEFT side (side 0) or RIGHT side (side 1)
        int teamX;
        double heading;
        if (side == 0) {
            // Left side, face right (toward enemy)
            teamX = margin + 50;
            heading = 90; // Face right (East)
        } else {
            // Right side, face left (toward enemy)
            teamX = fieldWidth - margin - 50;
            heading = 270; // Face left (West)
        }
        
        // Distribute robots vertically
        int spacing = (fieldHeight - 2 * margin) / (teamRobots.size() + 1);
        for (int i = 0; i < teamRobots.size(); i++) {
            BaseDroid robot = teamRobots.get(i);
            int y = margin + spacing * (i + 1);
            Location spawnLoc = new Location(teamX, y);
            robot.setLocation(spawnLoc);
            // Set heading by rotating from current heading
            double currentHeading = robot.getHeading();
            double delta = heading - currentHeading;
            // Normalize to shortest rotation
            if (delta > 180) delta -= 360;
            if (delta < -180) delta += 360;
            robot.turnRobot(delta);
        }
    }

    private boolean isFree(Location candidate)
    {
        for (BaseDroid droid : robots) {
            if (distanceSquared(droid.getLocation(), candidate) < 4 * BattleSetup.ROBOT_RADIUS * BattleSetup.ROBOT_RADIUS) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if two robots are teammates.
     * 
     * @param robot1 first robot
     * @param robot2 second robot
     * @return true if they are teammates
     */
    private boolean areTeammates(BaseDroid robot1, BaseDroid robot2)
    {
        // Check if robot1 is a TeamLeader and robot2 is in its teammates list
        if (robot1 instanceof TeamLeader leader1) {
            for (Droid teammate : leader1.getTeammates()) {
                if (teammate == robot2) {
                    return true;
                }
            }
        }
        
        // Check if robot2 is a TeamLeader and robot1 is in its teammates list
        if (robot2 instanceof TeamLeader leader2) {
            for (Droid teammate : leader2.getTeammates()) {
                if (teammate == robot1) {
                    return true;
                }
            }
        }
        
        // Check if both are teammates of the same leader
        // (two droids from the same team)
        for (BaseDroid potentialLeader : robots) {
            if (potentialLeader instanceof TeamLeader leader) {
                boolean robot1IsTeammate = false;
                boolean robot2IsTeammate = false;
                
                for (Droid teammate : leader.getTeammates()) {
                    if (teammate == robot1) {
                        robot1IsTeammate = true;
                    }
                    if (teammate == robot2) {
                        robot2IsTeammate = true;
                    }
                }
                
                if (robot1IsTeammate && robot2IsTeammate) {
                    return true; // Both are teammates of the same leader
                }
            }
        }
        
        return false;
    }

    private static double distanceSquared(Location a, Location b)
    {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return dx * dx + dy * dy;
    }

    /**
     * Calculate the damage a bullet will inflict based on its power.
     * 
     * <p>Damage formula:
     * <ul>
     *   <li>Base damage: 4 × power</li>
     *   <li>If power > 1: add 2 × (power - 1)</li>
     * </ul>
     * 
     * @param power the bullet power
     * @return the total damage amount
     */
    public static int calculateDamage(int power)
    {
        int damage = 4 * power;
        if (power > 1) {
            damage += 2 * (power - 1);
        }
        return damage;
    }
    
    /**
     * Calculate the energy recovery (life steal) for the shooter when a bullet hits.
     * 
     * @param power the bullet power
     * @return the energy recovery amount (3 × power)
     */
    public static int calculateLifeSteal(int power)
    {
        return 3 * power;
    }
    
    /**
     * Calculate the gun heat generated when firing a bullet.
     * 
     * @param power the bullet power
     * @return the heat amount (1 + power / 5)
     */
    public static int calculateGunHeat(int power)
    {
        return 1 + power / 5;
    }
    
    /**
     * Fires a bullet from the given robot. Creates a Bullet entity that travels
     * in the gun's direction and hits the closest valid target if any.
     * 
     * <p>Damage calculation uses {@link #calculateDamage(int)}.
     * Energy recovery uses {@link #calculateLifeSteal(int)}.
     * 
     * @param robot the robot firing
     * @param firePower the bullet power (clamped to valid range)
     * @throws GunOverheatedException if gun heat > 1
     * @throws ExhaustedException if robot lacks energy
     */
    @Override
    public void fire(Droid robot, int firePower) throws GunOverheatedException, ExhaustedException
    {
        if (phaseManager != null && !phaseManager.canRobotsFire()) {
            throw new GunOverheatedException(999); // Use as "not allowed" signal
        }
        
        BaseDroid shooter = requireDroid(robot);
        int power = Math.max(0, Math.min(BattleSetup.MAX_FIRE_POWER, firePower));
        if (power == 0) {
            return; // No-op for zero power
        }
        if (shooter.getGunHeat() > 1) {
            throw new GunOverheatedException(shooter.getGunHeat());
        }
        shooter.consumeEnergy(power);
        shooter.increaseGunHeat(calculateGunHeat(power));
        
        // PHASE 1: Spawn bullet at gun barrel tip, not robot center
        // Calculate barrel tip position based on gun heading
        Location robotLocation = shooter.getLocation();
        double gunHeading = shooter.getGunHeading();
        double gunHeadingRad = Math.toRadians(gunHeading);
        
        // Gun barrel length: ROBOT_RADIUS + gun extension (35px total from center)
        double barrelLength = BattleSetup.ROBOT_RADIUS + 25.0; // 10 + 25 = 35px total
        double barrelTipX = robotLocation.getX() + (barrelLength * Math.sin(gunHeadingRad));
        double barrelTipY = robotLocation.getY() - (barrelLength * Math.cos(gunHeadingRad)); // Negative because Y increases downward
        
        Location barrelTipLocation = new Location((int) Math.round(barrelTipX), (int) Math.round(barrelTipY));
        
        Bullet bullet = bulletPool.acquire();
        bullet.init(shooter, power, barrelTipLocation, gunHeading);
        bullets.add(bullet);
        
        // PHASE 4: Track muzzle flash location for visual effects
        lastMuzzleFlashLocation = barrelTipLocation;
        lastMuzzleFlashHeading = gunHeading;
        
        // PHASE 2: Removed instant hit-scan - bullets now travel and collide naturally via updateBullets()
    }
    
    /**
     * Get all active bullets on the battlefield.
     * 
     * @return a list of active bullets
     */
    List<Bullet> getBullets()
    {
        return new ArrayList<>(bullets);
    }
    
    /**
     * Used for robot bullet detection.
     * 
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param radius the radius in pixels
     * @return list of nearby bullets
     */
    public List<Bullet> getBulletsNear(double x, double y, double radius) {
        List<Bullet> nearby = new ArrayList<>();
        for (Bullet bullet : bullets) {
            if (!bullet.isActive()) continue;
            Location bulletLoc = bullet.getLocation();
            double dist = Math.sqrt(Math.pow(bulletLoc.getX() - x, 2) + Math.pow(bulletLoc.getY() - y, 2));
            if (dist <= radius) {
                nearby.add(bullet);
            }
        }
        return nearby;
    }
    
    /**
     * PHASE 4: Get recent hit events for visual effects (impact particles).
     * Clears the list after returning.
     * 
     * @return list of hit events
     */
    List<HitEvent> getAndClearRecentHits()
    {
        List<HitEvent> hits = new ArrayList<>(recentHits);
        recentHits.clear();
        return hits;
    }
    
    /**
     * PHASE 4: Get last muzzle flash location and heading for visual effects.
     * Returns null if no flash occurred since last call.
     * 
     * @return array [location, heading] or null
     */
    Object[] getAndClearLastMuzzleFlash()
    {
        if (lastMuzzleFlashLocation == null) {
            return null;
        }
        Object[] result = new Object[]{lastMuzzleFlashLocation, lastMuzzleFlashHeading};
        lastMuzzleFlashLocation = null;
        return result;
    }
    
    /**
     * Detect and handle all collisions in the battlefield.
     * This includes:
     * <ul>
     *   <li>Bullet vs Robot collisions</li>
     *   <li>Robot vs Wall collisions (handled during movement)</li>
     *   <li>Robot vs Robot collisions (handled during movement)</li>
     * </ul>
     * 
     * <p>This method should be called each game loop iteration.
     */
    void detectCollisions()
    {
        // Update bullets and detect bullet vs robot collisions
        updateBullets();
        
        // Robot vs wall and robot vs robot collisions are handled
        // during the move() method, so no additional processing needed here
    }
    
    /**
     * Update all bullets by one game tick and detect bullet vs robot collisions.
     * This should be called each game loop iteration.
     */
    void updateBullets()
    {
        bullets.removeIf(bullet -> {
            if (!bullet.isActive()) {
                bulletPool.release(bullet);
                return true; // Remove inactive bullets
            }
            boolean stillActive = bullet.update();
            if (!stillActive) {
                bulletPool.release(bullet);
                return true; // Remove bullets that went out of bounds or exceeded range
            }
            // PHASE 2: Check for collisions with robots using bounding box intersection
            Location bulletLoc = bullet.getLocation();
            // Bullet bounding box: small square around bullet position
            int bulletSize = 3; // Bullet radius
            Rectangle bulletRect = new Rectangle(
                bulletLoc.getX() - bulletSize, 
                bulletLoc.getY() - bulletSize,
                bulletSize * 2, 
                bulletSize * 2
            );
            
            for (BaseDroid robot : robots) {
                if (robot == bullet.getOwner() || robot.getEnergy() <= 0) {
                    continue; // Skip owner and dead robots
                }
                
                // Check for friendly fire - skip if robots are teammates
                if (areTeammates(bullet.getOwner(), robot)) {
                    continue; // Skip teammates (no friendly fire)
                }
                
                // Robot bounding box: square centered on robot
                Location robotLoc = robot.getLocation();
                int robotSize = BattleSetup.ROBOT_RADIUS;
                Rectangle robotRect = new Rectangle(
                    robotLoc.getX() - robotSize,
                    robotLoc.getY() - robotSize,
                    robotSize * 2,
                    robotSize * 2
                );
                
                // PHASE 2: Use bounding box intersection for collision detection
                if (bulletRect.intersects(robotRect)) {
                    // Bullet hit robot - apply damage and life steal
                    int damage = calculateDamage(bullet.getPower());
                    int energyBefore = robot.getEnergy();
                    robot.adjustEnergy(-damage);
                    int energyAfter = robot.getEnergy();
                    boolean isDeath = energyAfter <= 0 && energyBefore > 0;
                    
                    BaseDroid attacker = bullet.getOwner();
                    attacker.adjustEnergy(calculateLifeSteal(bullet.getPower()));
                    
                    damageNumberManager.addDamage(bulletLoc.getX(), bulletLoc.getY(), damage);
                    damageTracker.recordDamage(attacker, damage);

                    if (isDeath) {
                        damageTracker.recordKill(attacker);
                    }

                    recentHits.add(new HitEvent(bulletLoc, Color.ORANGE));

                    if (isDeath) {
                        recentDamageEvents.add(new DamageEvent(damage, true, attacker, robot));
                    } else if (damage > 10) {
                        recentDamageEvents.add(new DamageEvent(damage, false, attacker, robot));
                    }
                    
                    bullet.deactivate();
                    bulletPool.release(bullet);
                    return true; // Remove bullet
                }
            }
            
            // PHASE 4: Check for wall hits (out of bounds) - spawn impact particles
            if (bulletLoc.getX() < 0 || bulletLoc.getX() > BattleSetup.FIELD_WIDTH ||
                bulletLoc.getY() < 0 || bulletLoc.getY() > BattleSetup.FIELD_HEIGHT) {
                // Bullet hit wall - create wall impact effect
                recentHits.add(new HitEvent(bulletLoc, Color.GRAY));
            }
            return false; // Keep bullet active
        });
    }
    
    /**
     * Remove all dead robots (energy <= 0) from the battlefield.
     * This should be called each game loop iteration after collision detection.
     * 
     * @return the number of robots removed
     */
    int removeDeadRobots()
    {
        int sizeBefore = robots.size();
                robots.removeIf(robot -> {
                    boolean isDead = robot.getEnergy() <= 0;
                    if (isDead) {
                        boolean alreadyHasKillerEvent = recentDamageEvents.stream()
                            .anyMatch(e -> e.isDeath && e.victim == robot && e.killer != null);
                        
                        if (!alreadyHasKillerEvent) {
                            recentDamageEvents.add(new DamageEvent(100, true, null, robot)); // 100 = death intensity
                        }
                
                damageTracker.resetKillStreak(robot);

                Location deathLocation = robot.getLocation();
                double bodyHeading = robot.getHeading();
                double gunHeading = robot.getGunHeading();
                
                // Determine team color (default to gray if unknown)
                Color teamColor = Color.GRAY;
                // Try to get color from view if available (this is a simplified approach)
                // In a full implementation, you'd pass the view/color info here
                
                RobotWreckage wreckage = new RobotWreckage(deathLocation, teamColor, bodyHeading, gunHeading);
                wreckages.add(wreckage);
                
                EnergyCapsule capsule = new EnergyCapsule(deathLocation);
                energyCapsules.add(capsule);
            }
            return isDead;
        });
        return sizeBefore - robots.size();
    }
    
    /**
     * @return list of wreckage (dead robots that remain on field)
     */
    List<RobotWreckage> getWreckages()
    {
        return new ArrayList<>(wreckages);
    }
    
    /**
     * Update all wreckage.
     */
    void updateWreckages()
    {
        for (RobotWreckage wreckage : wreckages) {
            wreckage.update();
        }
    }
    
    /**
     * @param phaseManager the phase manager
     */
    void setPhaseManager(GamePhaseManager phaseManager) {
        this.phaseManager = phaseManager;
    }
    
    /**
     * @return the phase manager, or null if not set
     */
    public GamePhaseManager getPhaseManager() {
        return phaseManager;
    }
    
    /**
     * @return the damage number manager
     */
    public DamageNumberManager getDamageNumberManager() {
        return damageNumberManager;
    }
    
    /**
     * Should be called each game tick.
     */
    void updateBattleZone()
    {
        if (phaseManager != null && phaseManager.isZoneShrinking()) {
            double shrinkRate = phaseManager.getZoneShrinkRate();
            battleZone.shrink(shrinkRate);
        } else {
            // Fallback to time-based (for compatibility)
            battleZone.update(System.currentTimeMillis());
        }
        
        for (BaseDroid robot : robots) {
            if (robot.getEnergy() > 0) {
                int zoneDamage = battleZone.getZoneDamage(robot.getLocation());
                if (zoneDamage > 0) {
                    robot.adjustEnergy(-zoneDamage);
                }
            }
        }
    }
    
    /**
     * @param damagePerTick the damage amount per tick
     */
    void applyBleedDamage(double damagePerTick) {
        for (BaseDroid robot : robots) {
            if (robot.getEnergy() > 0) {
                robot.adjustEnergy(-(int)Math.ceil(damagePerTick));
            }
        }
    }
    
    /**
     * @return the battle zone
     */
    BattleZone getBattleZone()
    {
        return battleZone;
    }
    
    /**
     * Should be called each game tick.
     */
    void updateEnergyCapsules()
    {
        for (EnergyCapsule capsule : energyCapsules) {
            capsule.update();
        }
        
        for (BaseDroid robot : robots) {
            if (robot.getEnergy() > 0) {
                Location robotLoc = robot.getLocation();
                for (EnergyCapsule capsule : energyCapsules) {
                    if (capsule.canBeCollected(robotLoc)) {
                        int energyGained = capsule.collect();
                        robot.adjustEnergy(energyGained);
                    }
                }
            }
        }
        
        energyCapsules.removeIf(EnergyCapsule::isCollected);
    }
    
    /**
     * @return list of energy capsules
     */
    List<EnergyCapsule> getEnergyCapsules()
    {
        return new ArrayList<>(energyCapsules);
    }
    
    /**
     * @return the damage tracker
     */
    DamageTracker getDamageTracker()
    {
        return damageTracker;
    }
    
    /**
     * Get the list of robots on the battlefield.
     * Used for matching robot instances between views and battlefield.
     * 
     * @return list of robots (copy for safety)
     */
    List<BaseDroid> getRobots()
    {
        return new ArrayList<>(robots); // Return a copy for safety
    }
    
    /**
     * Should be called when a kill occurs.
     * 
     * @param killer the robot that got the kill
     * @return kill streak count
     */
    int getKillStreak(Droid killer)
    {
        return damageTracker.getKillStreak(killer);
    }
    
    /**
     * This is a convenience method that delegates to DamageTracker.
     * 
     * @param robot the robot
     * @return kill count
     */
    int getKills(Droid robot)
    {
        return damageTracker.getKills(robot);
    }
    
    /**
     * @return list of damage events (damage amount and whether it was a death)
     */
    List<DamageEvent> getAndClearRecentDamageEvents()
    {
        List<DamageEvent> events = new ArrayList<>(recentDamageEvents);
        recentDamageEvents.clear();
        return events;
    }
    
    /**
     * Get recent damage events without clearing them (for sharing between UI components).
     * 
     * @return list of damage events (damage amount and whether it was a death)
     */
    List<DamageEvent> getRecentDamageEvents()
    {
        return new ArrayList<>(recentDamageEvents);
    }
    
    /**
     * Get the number of active (alive) robots on the battlefield.
     * A robot is considered "active" if it has enough energy to move (energy >= MOTION_ENERGY).
     * 
     * @return the count of robots with energy >= MOTION_ENERGY
     */
    int getActiveRobotCount()
    {
        int count = (int) robots.stream()
            .filter(robot -> robot.getEnergy() >= BattleSetup.MOTION_ENERGY)
            .count();
        return count;
    }
    
    /**
     * @return the number of active bullets
     */
    int getBulletCount()
    {
        return (int) bullets.stream()
            .filter(bullet -> bullet.isActive())
            .count();
    }
    
    /**
     * Process team messages for all droids.
     * This should be called each game loop iteration after leader tasks run
     * but before individual robot tasks.
     */
    void processTeamMessages()
    {
        for (BaseDroid droid : robots) {
            if (droid.getEnergy() > 0) {
                droid.processMessages();
            }
        }
    }
    
    /**
     * Robots gain +1 energy per tick when NOT moving and NOT firing.
     * Should be called at the start of each game tick (before robot actions).
     */
    void applyPassiveRegeneration()
    {
        for (BaseDroid robot : robots) {
            robot.resetTickFlags();
        }
        
    }
    
    /**
     * Should be called at the end of each game tick (after robot actions).
     */
    void processPassiveRegeneration()
    {
        for (BaseDroid robot : robots) {
            if (robot.getEnergy() > 0) {
                // Only regenerate if robot didn't move and didn't fire
                if (!robot.isMoving() && !robot.hasFiredThisTick()) {
                    robot.addEnergy(1);
                }
            }
        }
    }

    /**
     * Finds the closest valid target in the gun's line of fire.
     * A target is valid if it's within the fire scope, in the gun's direction,
     * and within the gun's width (2 * ROBOT_RADIUS).
     * 
     * @param shooter the robot firing
     * @return the closest target, or null if no valid target
     */
    private BaseDroid findTarget(BaseDroid shooter)
    {
        Location source = shooter.getLocation();
        double heading = shooter.getGunHeading();
        double rad = Math.toRadians(heading);
        double dirX = Math.sin(rad);
        double dirY = -Math.cos(rad);
        BaseDroid closest = null;
        double closestProjection = Double.POSITIVE_INFINITY;
        double maxLateralDistance = 2.0 * BattleSetup.ROBOT_RADIUS;
        
        for (BaseDroid candidate : robots) {
            if (candidate == shooter || candidate.getEnergy() <= 0) {
                continue; // Skip self and eliminated robots
            }
            double dx = candidate.getLocation().getX() - source.getX();
            double dy = candidate.getLocation().getY() - source.getY();
            double projection = dx * dirX + dy * dirY;
            if (projection <= 0 || projection > BattleSetup.MAX_FIRE_SCOPE) {
                continue; // Behind shooter or out of range
            }
            double cross = Math.abs(dx * dirY - dy * dirX);
            if (cross > maxLateralDistance) {
                continue; // Too far from the line of fire
            }
            if (projection < closestProjection) {
                closestProjection = projection;
                closest = candidate;
            }
        }
        return closest;
    }

    @Override
    public void move(Droid robot, double distance) throws CollisionException, ExhaustedException
    {
        BaseDroid mover = requireDroid(robot);
        double limitedDistance = Math.max(BattleSetup.MIN_DISTANCE_MOVE, Math.min(BattleSetup.MAX_DISTANCE_MOVE, distance));
        if (limitedDistance == 0) {
            return;
        }
        mover.consumeEnergy(BattleSetup.MOTION_ENERGY);
        Location start = mover.getLocation();
        double heading = mover.getHeading();
        double rad = Math.toRadians(heading);
        double dx = limitedDistance * Math.sin(rad);
        double dy = -limitedDistance * Math.cos(rad);
        double totalDistance = Math.hypot(dx, dy);
        double unitX = totalDistance == 0 ? 0 : dx / totalDistance;
        double unitY = totalDistance == 0 ? 0 : dy / totalDistance;

        int steps = (int) Math.max(1, Math.ceil(totalDistance));
        double stepX = dx / steps;
        double stepY = dy / steps;
        double currentX = start.getX();
        double currentY = start.getY();
        for (int i = 0; i < steps; i++) {
            currentX += stepX;
            currentY += stepY;
            Location candidate = new Location((int) Math.round(currentX), (int) Math.round(currentY));
            Location clamped = clamp(candidate);
            BaseDroid collided = collidedRobot(mover, clamped);
            boolean hitWall = !candidate.equals(clamped);
            if (hitWall || collided != null) {
                Location stopLocation = clamped;
                if (collided != null) {
                    stopLocation = new Location(
                        (int) Math.round(collided.getLocation().getX() - unitX * 2 * BattleSetup.ROBOT_RADIUS),
                        (int) Math.round(collided.getLocation().getY() - unitY * 2 * BattleSetup.ROBOT_RADIUS)
                    );
                    stopLocation = clamp(stopLocation);
                }
                mover.setLocation(stopLocation);
                mover.adjustEnergy(-BattleSetup.COLLISION_DAMAGE);
                if (collided != null) {
                    collided.adjustEnergy(-BattleSetup.COLLISION_DAMAGE);
                }
                throw new CollisionException(stopLocation);
            }
            mover.setLocation(clamped);
        }
    }

    private Location clamp(Location candidate)
    {
        int x = Math.max(BattleSetup.ROBOT_RADIUS, Math.min(BattleSetup.FIELD_WIDTH - BattleSetup.ROBOT_RADIUS, candidate.getX()));
        int y = Math.max(BattleSetup.ROBOT_RADIUS, Math.min(BattleSetup.FIELD_HEIGHT - BattleSetup.ROBOT_RADIUS, candidate.getY()));
        if (x == candidate.getX() && y == candidate.getY()) {
            return candidate;
        }
        return new Location(x, y);
    }

    /**
     * Checks if the given location collides with any robot on the battlefield.
     * Uses squared distance comparison for efficiency (avoids square root calculation).
     * 
     * @param mover the robot that is moving (excluded from collision check)
     * @param candidate the location to check for collisions
     * @return the collided robot, or null if no collision
     */
    private BaseDroid collidedRobot(BaseDroid mover, Location candidate)
    {
        double collisionDistanceSquared = 4.0 * BattleSetup.ROBOT_RADIUS * BattleSetup.ROBOT_RADIUS;
        for (BaseDroid droid : robots) {
            if (droid == mover || droid.getEnergy() <= 0) {
                continue; // Skip self and eliminated robots
            }
            if (distanceSquared(candidate, droid.getLocation()) <= collisionDistanceSquared) {
                return droid;
            }
        }
        return null;
    }

    @Override
    public List<Location> scan(Robot robot) throws ExhaustedException
    {
        RobotImpl scanner = requireRobot(robot);
        scanner.consumeEnergy(BattleSetup.SCAN_ENERGY);
        List<ScanTarget> targets = new ArrayList<>();
        Location source = scanner.getLocation();
        double heading = scanner.getRadarHeading();
        double halfField = BattleSetup.VISION_FIELD / 2.0;
        
        // Scan all robots in the field of vision (pie slice)
        for (BaseDroid candidate : robots) {
            if (candidate == robot || candidate.getEnergy() <= 0) {
                continue; // Skip self and eliminated robots
            }
            Location location = candidate.getLocation();
            double bearing = calculateBearing(source, location);
            double angleDiff = angleDifference(heading, bearing);
            
            // Check if robot is within the field of vision (pie slice)
            if (angleDiff > halfField) {
                continue; // Outside field of vision
            }
            
            // Calculate distance
            double distance = Math.hypot(location.getX() - source.getX(), location.getY() - source.getY());
            
            // Calculate lateral distance for occlusion checking
            double lateral = distance * Math.sin(Math.toRadians(angleDiff));
            int bucket = (int) Math.floor(Math.abs(lateral) / (2 * BattleSetup.ROBOT_RADIUS + 0.0001));
            
            targets.add(new ScanTarget(location, distance, bearing, bucket));
        }
        
        // Sort by distance (closest first)
        targets.sort(Comparator.comparingDouble(ScanTarget::distance));
        
        // Filter out occluded targets (only show closest in each lateral bucket)
        Set<Integer> blockedBuckets = new HashSet<>();
        List<Location> locations = new ArrayList<>();
        for (ScanTarget target : targets) {
            if (blockedBuckets.add(target.bucket())) {
                locations.add(target.location());
            }
        }
        
        return Collections.unmodifiableList(locations);
    }

    /**
     * Calculate the bearing angle from one location to another.
     * 
     * @param from the source location
     * @param to the target location
     * @return the bearing in degrees (0-360, where 0 = North)
     */
    private static double calculateBearing(Location from, Location to)
    {
        double dx = to.getX() - from.getX();
        double dy = from.getY() - to.getY();
        double angle = Math.toDegrees(Math.atan2(dx, dy));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    private static double angleDifference(double a, double b)
    {
        double diff = Math.abs(a - b) % 360.0;
        return diff > 180 ? 360 - diff : diff;
    }

    @Override
    public void decreaseGunHeats()
    {
        for (BaseDroid droid : robots) {
            // Use Gun component's decreaseHeat method for proper encapsulation
            droid.getGun().decreaseHeat(BattleSetup.GUN_COOLING);
        }
    }

    private BaseDroid requireDroid(Droid robot)
    {
        if (!(robot instanceof BaseDroid base) || !robots.contains(base)) {
            throw new IllegalArgumentException("Unknown robot instance");
        }
        return base;
    }
    
    /**
     * Check if a robot is still registered on the battlefield.
     * Used to validate robots before executing tasks.
     * 
     * @param robot the robot to check
     * @return true if the robot is still on the battlefield, false otherwise
     */
    boolean isRobotRegistered(Robot robot)
    {
        return robot instanceof BaseDroid base && robots.contains(base);
    }

    private RobotImpl requireRobot(Robot robot)
    {
        BaseDroid base = requireDroid(robot);
        if (!(base instanceof RobotImpl robotImpl)) {
            throw new IllegalArgumentException("Robot instance required");
        }
        return robotImpl;
    }

    /**
     * Internal record for tracking scan targets during field-of-view calculation.
     */
    private record ScanTarget(Location location, double distance, double bearing, int bucket) {}
}
