package fr.ensibs.robots.logic;

import java.util.List;

public interface Battlefield
{
    void fire(Droid robot, int firePower) throws GunOverheatedException, ExhaustedException;

    void move(Droid robot, double distance) throws CollisionException, ExhaustedException;

    List<Location> scan(Robot robot) throws ExhaustedException;
    void decreaseGunHeats();
}
