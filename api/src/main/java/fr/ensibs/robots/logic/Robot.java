package fr.ensibs.robots.logic;

import java.util.List;

public interface Robot extends Droid
{
    double getRadarHeading();
    void turnRadar(double degrees);
    List<Location> scan() throws ExhaustedException;
}
