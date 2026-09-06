package fr.ensibs.robots.impl;

public class GamePhaseManager {
    
    public enum Phase {
        DEPLOYMENT,
        SKIRMISH,
        PRESSURE,
        SUDDEN_DEATH
    }
    
    private static final int DEPLOYMENT_DURATION = 0;
    private static final int SKIRMISH_DURATION = 50 * 60;
    private static final int PRESSURE_DURATION = 30 * 60;
    
    private Phase currentPhase = Phase.SKIRMISH;
    private int ticksInPhase = 0;
    private int totalTicks = 0;
    
    private Runnable onDeploymentEnd;
    private Runnable onSkirmishEnd;
    private Runnable onPressureEnd;
    
    public GamePhaseManager() {
        this.currentPhase = Phase.SKIRMISH;
        this.ticksInPhase = 0;
        this.totalTicks = 0;
    }
    
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
                break;
        }
    }
    
    private void transitionTo(Phase newPhase) {
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
        return true;
    }
    
    public boolean isZoneShrinking() {
        return currentPhase == Phase.PRESSURE || currentPhase == Phase.SUDDEN_DEATH;
    }
    
    public double getZoneShrinkRate() {
        switch (currentPhase) {
            case PRESSURE:
                return 0.5;
            case SUDDEN_DEATH:
                return 1.5;
            default:
                return 0;
        }
    }
    
    public double getBleedDamagePerTick() {
        if (currentPhase == Phase.SUDDEN_DEATH) {
            return 0.5;
        }
        return 0;
    }
    
    public boolean isInvulnerable() {
        return false;
    }
    
    public void setOnDeploymentEnd(Runnable callback) { this.onDeploymentEnd = callback; }
    public void setOnSkirmishEnd(Runnable callback) { this.onSkirmishEnd = callback; }
    public void setOnPressureEnd(Runnable callback) { this.onPressureEnd = callback; }
    
    public String getPhaseDisplayName() {
        switch (currentPhase) {
            case DEPLOYMENT: return "COMBAT";
            case SKIRMISH: return "COMBAT";
            case PRESSURE: return "ZONE CLOSING";
            case SUDDEN_DEATH: return "SUDDEN DEATH";
            default: return "UNKNOWN";
        }
    }
    
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
                return -1;
        }
        return Math.max(0, remaining / 60);
    }
}
