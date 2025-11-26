package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Battlefield;
import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.CollisionException;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.ExhaustedException;
import fr.ensibs.robots.logic.GunOverheatedException;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.ScanResult;

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
    private final Random random = new Random();

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
        int maxColumn = (BattleSetup.FIELD_WIDTH - 2 * BattleSetup.ROBOT_RADIUS) / BattleSetup.MAX_DISTANCE_MOVE;
        int maxRow = (BattleSetup.FIELD_HEIGHT - 2 * BattleSetup.ROBOT_RADIUS) / BattleSetup.MAX_DISTANCE_MOVE;
        
        // Try random placement first (more natural distribution)
        int maxAttempts = 500;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = BattleSetup.ROBOT_RADIUS + BattleSetup.MAX_DISTANCE_MOVE * random.nextInt(Math.max(1, maxColumn + 1));
            int y = BattleSetup.ROBOT_RADIUS + BattleSetup.MAX_DISTANCE_MOVE * random.nextInt(Math.max(1, maxRow + 1));
            Location candidate = new Location(x, y);
            if (isFree(candidate)) {
                return candidate;
            }
        }
        
        // Deterministic fallback: place robots on a grid to ensure spawnability
        int index = robots.size();
        int columns = Math.max(1, maxColumn + 1);
        int row = index / columns;
        int column = index % columns;
        int x = BattleSetup.ROBOT_RADIUS + column * BattleSetup.MAX_DISTANCE_MOVE;
        int y = BattleSetup.ROBOT_RADIUS + row * BattleSetup.MAX_DISTANCE_MOVE;
        x = Math.min(x, BattleSetup.FIELD_WIDTH - BattleSetup.ROBOT_RADIUS);
        y = Math.min(y, BattleSetup.FIELD_HEIGHT - BattleSetup.ROBOT_RADIUS);
        return new Location(x, y);
    }

    double nextHeading()
    {
        return random.nextDouble() * 360.0;
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
        
        // Create bullet entity
        Location startLocation = shooter.getLocation();
        double gunHeading = shooter.getGunHeading();
        Bullet bullet = new Bullet(shooter, power, startLocation, gunHeading);
        bullets.add(bullet);
        
        // Check for immediate hit (instant hit-scan for now)
        // In future missions, bullets will travel over time
        BaseDroid target = findTarget(shooter);
        if (target != null) {
            int damage = calculateDamage(power);
            target.adjustEnergy(-damage);
            shooter.adjustEnergy(calculateLifeSteal(power));
            bullet.deactivate(); // Bullet hit, deactivate it
        }
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
     * Update all bullets by one game tick.
     * This should be called each game loop iteration.
     */
    void updateBullets()
    {
        bullets.removeIf(bullet -> {
            if (!bullet.isActive()) {
                return true; // Remove inactive bullets
            }
            boolean stillActive = bullet.update();
            if (!stillActive) {
                return true; // Remove bullets that went out of bounds or exceeded range
            }
            // Check for collisions with robots
            Location bulletLoc = bullet.getLocation();
            for (BaseDroid robot : robots) {
                if (robot == bullet.getOwner() || robot.getEnergy() <= 0) {
                    continue;
                }
                double distSq = distanceSquared(bulletLoc, robot.getLocation());
                double collisionDistSq = BattleSetup.ROBOT_RADIUS * BattleSetup.ROBOT_RADIUS;
                if (distSq <= collisionDistSq) {
                    // Bullet hit robot
                    int damage = calculateDamage(bullet.getPower());
                    robot.adjustEnergy(-damage);
                    bullet.getOwner().adjustEnergy(calculateLifeSteal(bullet.getPower()));
                    bullet.deactivate();
                    return true; // Remove bullet
                }
            }
            return false; // Keep bullet active
        });
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

    /**
     * Perform a radar scan and return ScanResult objects with relative information.
     * This method uses geometric field-of-view scanning to detect robots in a
     * pie-slice shaped area centered on the radar's heading.
     * 
     * @param robot the robot performing the scan
     * @return a list of ScanResult objects containing distance and bearing for each detected robot
     * @throws ExhaustedException if the robot has insufficient energy to scan
     */
    public List<ScanResult> scanWithResults(Robot robot) throws ExhaustedException
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
        List<ScanResult> results = new ArrayList<>();
        for (ScanTarget target : targets) {
            if (blockedBuckets.add(target.bucket())) {
                // Create ScanResult with relative information only (prevents cheating)
                results.add(new ScanResult(target.distance(), target.bearing()));
            }
        }
        
        return Collections.unmodifiableList(results);
    }
    
    @Override
    public List<Location> scan(Robot robot) throws ExhaustedException
    {
        // For backward compatibility, convert ScanResult to Location
        // Note: This loses the relative information benefit, but maintains API compatibility
        List<ScanResult> scanResults = scanWithResults(robot);
        List<Location> locations = new ArrayList<>();
        Location source = robot.getLocation();
        double heading = robot.getRadarHeading();
        
        for (ScanResult result : scanResults) {
            // Reconstruct approximate location from relative info
            // This is approximate and may not be exact, but maintains API compatibility
            double bearingRad = Math.toRadians(result.getBearing());
            double distance = result.getDistance();
            int x = (int) Math.round(source.getX() + distance * Math.sin(bearingRad));
            int y = (int) Math.round(source.getY() - distance * Math.cos(bearingRad));
            locations.add(new Location(x, y));
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

