package fr.ensibs.robots.logic;

/**
 * Exception thrown when a robot attempts to fire with a too high gun heat
 *
 * @author Pascale Launay
 */
public class GunOverheatedException extends Exception
{
    private final int gunHeat; // The gun heat when the exception occurred.

    /**
     * Creates an exception instance, and initializes the gun heat when the
     * exception occurred.
     *
     * @param gunHeat the gun heat when the exception occurred
     */
    public GunOverheatedException(int gunHeat)
    {
        super("Gun overheated. Gun heat: " + gunHeat);
        this.gunHeat = gunHeat;
    }

    /**
     * Gives the gun heat when the exception occurred.
     *
     * @return the gun heat
     */
    public int getGunHeat()
    {
        return gunHeat;
    }
}
