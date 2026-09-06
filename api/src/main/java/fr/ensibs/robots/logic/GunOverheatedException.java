package fr.ensibs.robots.logic;

public class GunOverheatedException extends Exception
{
    private final int gunHeat;

    public GunOverheatedException(int gunHeat)
    {
        super("Gun overheated. Gun heat: " + gunHeat);
        this.gunHeat = gunHeat;
    }

    public int getGunHeat()
    {
        return gunHeat;
    }
}
