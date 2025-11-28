package fr.ensibs.robots.logic;

public interface Droid
{
    Location getLocation();
    int getEnergy();
    int getGunHeat();
    double getHeading();
    double getGunHeading();
    void fire(int firePower) throws GunOverheatedException, ExhaustedException;
    void move(double distance) throws CollisionException, ExhaustedException;
    void turnRobot(double degrees);
    void turnGun(double degrees);
}

