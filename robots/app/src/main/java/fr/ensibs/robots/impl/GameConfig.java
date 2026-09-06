package fr.ensibs.robots.impl;

import java.io.InputStream;
import java.util.Properties;

/**
 * MISSION H: Game configuration manager.
 * Loads settings from game-config.properties for easy tuning.
 * 
 * @author Robot Wars Team
 */
public class GameConfig {
    
    private static GameConfig instance;
    private Properties props;
    
    private GameConfig() {
        props = new Properties();
        try {
            InputStream is = getClass().getClassLoader()
                .getResourceAsStream("game-config.properties");
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            System.err.println("Could not load config: " + e.getMessage());
        }
    }
    
    public static GameConfig get() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }
    
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(props.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        String val = props.getProperty(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }
    
    // Convenience methods
    public int getRobotInitialEnergy() {
        return getInt("robot.energy.initial", 1500);
    }
    
    public int getDroidInitialEnergy() {
        return getInt("droid.energy.initial", 2500);
    }
    
    public double getLifestealMultiplier() {
        return getDouble("lifesteal.multiplier", 1.67);
    }
    
    public double getMoveCostMultiplier() {
        return getDouble("cost.move.multiplier", 0.0);
    }
    
    public int getGunCooling() {
        return getInt("gun.cooling.rate", 7);
    }
    
    public double getPassiveHealingRate() {
        return getDouble("healing.passive.rate", 1.0);
    }
}

