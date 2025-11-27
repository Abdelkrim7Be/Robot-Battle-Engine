package fr.ensibs.robots.impl;

/**
 * Manages game phases for structured gameplay.
 * 
 * <p>MISSION B: Game Structure & Phases
 * Creates distinct game phases that create pacing and strategy:
 * <ul>
 *   <li>DEPLOYMENT: Spawn, no combat (10 seconds)</li>
 *   <li>SKIRMISH: Normal combat (50 seconds)</li>
 *   <li>PRESSURE: Zone shrinking (30 seconds)</li>
 *   <li>SUDDEN_DEATH: Fast zone, bleed damage (until game ends)</li>
 * </ul>
 */
public class GamePhaseManager {
    
    public enum Phase {
        DEPLOYMENT,    // Spawn, no combat
        SKIRMISH,      // Normal combat
        PRESSURE,      // Zone shrinking
        SUDDEN_DEATH   // Fast zone, bleed damage
    }
    
    // Phase timing (in game ticks, assuming ~60 ticks per second)
    private static final int DEPLOYMENT_DURATION = 10 * 60;    // 10 seconds
    private static final int SKIRMISH_DURATION = 50 * 60;      // 50 seconds (total 60)
    private static final int PRESSURE_DURATION = 30 * 60;      // 30 seconds (total 90)
    // SUDDEN_DEATH continues until game ends
    
    private Phase currentPhase = Phase.DEPLOYMENT;
    private int ticksInPhase = 0;
    private int totalTicks = 0;
    
    // Phase transition listeners
    private Runnable onDeploymentEnd;
    private Runnable onSkirmishEnd;
    private Runnable onPressureEnd;
    
    public GamePhaseManager() {
        this.currentPhase = Phase.DEPLOYMENT;
        this.ticksInPhase = 0;
        this.totalTicks = 0;
    }
    
    /**
     * Call this every game tick
     */
    public void update() {
        totalTicks++;
        ticksInPhase++;
        
        switch (currentPhase) {
            case DEPLOYMENT:
                if (ticksInPhase >= DEPLOYMENT_DURATION) {
                    transitionTo(Phase.SKIRMISH);
                    if (onDeploymentEnd != null) onDeploymentEnd.run();
                }
                break;
                
            case SKIRMISH:
                if (ticksInPhase >= SKIRMISH_DURATION) {
                    transitionTo(Phase.PRESSURE);
                    if (onSkirmishEnd != null) onSkirmishEnd.run();
                }
                break;
                
            case PRESSURE:
                if (ticksInPhase >= PRESSURE_DURATION) {
                    transitionTo(Phase.SUDDEN_DEATH);
                    if (onPressureEnd != null) onPressureEnd.run();
                }
                break;
                
            case SUDDEN_DEATH:
                // Continues until game ends
                break;
        }
    }
    
    private void transitionTo(Phase newPhase) {
        System.out.println("[PHASE] Transitioning from " + currentPhase + " to " + newPhase);
        currentPhase = newPhase;
        ticksInPhase = 0;
    }
    
    public Phase getCurrentPhase() {
        return currentPhase;
    }
    
    public int getTotalTicks() {
        return totalTicks;
    }
    
    public int getTicksInPhase() {
        return ticksInPhase;
    }
    
    public double getTotalTimeSeconds() {
        return totalTicks / 60.0;
    }
    
    public boolean canRobotsFire() {
        // No firing during deployment
        return currentPhase != Phase.DEPLOYMENT;
    }
    
    public boolean isZoneShrinking() {
        return currentPhase == Phase.PRESSURE || currentPhase == Phase.SUDDEN_DEATH;
    }
    
    public double getZoneShrinkRate() {
        switch (currentPhase) {
            case PRESSURE:
                return 0.5; // pixels per tick
            case SUDDEN_DEATH:
                return 1.5; // faster in sudden death
            default:
                return 0;
        }
    }
    
    public double getBleedDamagePerTick() {
        // Only in sudden death
        if (currentPhase == Phase.SUDDEN_DEATH) {
            return 0.5; // 0.5 damage per tick = 30 damage per second
        }
        return 0;
    }
    
    public boolean isInvulnerable() {
        // First 3 seconds of deployment = invulnerable
        return currentPhase == Phase.DEPLOYMENT && ticksInPhase < (3 * 60);
    }
    
    // Setters for callbacks
    public void setOnDeploymentEnd(Runnable callback) { this.onDeploymentEnd = callback; }
    public void setOnSkirmishEnd(Runnable callback) { this.onSkirmishEnd = callback; }
    public void setOnPressureEnd(Runnable callback) { this.onPressureEnd = callback; }
    
    /**
     * Get phase display name for UI
     */
    public String getPhaseDisplayName() {
        switch (currentPhase) {
            case DEPLOYMENT: return "DEPLOYMENT";
            case SKIRMISH: return "COMBAT";
            case PRESSURE: return "ZONE CLOSING";
            case SUDDEN_DEATH: return "SUDDEN DEATH";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * Get remaining time in current phase (for UI)
     */
    public int getRemainingSecondsInPhase() {
        int remaining = 0;
        switch (currentPhase) {
            case DEPLOYMENT:
                remaining = DEPLOYMENT_DURATION - ticksInPhase;
                break;
            case SKIRMISH:
                remaining = SKIRMISH_DURATION - ticksInPhase;
                break;
            case PRESSURE:
                remaining = PRESSURE_DURATION - ticksInPhase;
                break;
            case SUDDEN_DEATH:
                return -1; // Infinite
        }
        return Math.max(0, remaining / 60);
    }
}

