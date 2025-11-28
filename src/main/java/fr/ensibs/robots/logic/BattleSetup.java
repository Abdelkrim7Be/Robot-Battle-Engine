package fr.ensibs.robots.logic;

public interface BattleSetup
{
    int FIELD_WIDTH = 1280;
    int FIELD_HEIGHT = 960;
    int ROBOT_RADIUS = 10;
    int DROID_INITIAL_ENERGY = 2500;
    int ROBOT_INITIAL_ENERGY = 1500;
    int NB_TEAMMATES = 4;
    int MAX_FIRE_SCOPE = 500;
    int MAX_FIRE_POWER = 50;
    int GUN_COOLING = 7;
    int SCAN_ENERGY = 1;
    int VISION_FIELD = 90;
    int MIN_DISTANCE_MOVE = -40;
    int MAX_DISTANCE_MOVE = 50;
    int MOTION_ENERGY = 5;
    int BODY_TURN_ENERGY = 1;
    int GUN_TURN_ENERGY = 1;
    int RADAR_TURN_ENERGY = 1;
    int COLLISION_DAMAGE = 3;
}
