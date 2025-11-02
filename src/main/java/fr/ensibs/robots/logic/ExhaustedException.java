package fr.ensibs.robots.logic;

/**
 * Exception thrown when a robot attempts to realize some action while
 * he does not have enough energy
 *
 * @author Pascale Launay
 */
public class ExhaustedException extends Exception
{
    private final int energy; // the amount of the robot's energy when the exception occurs

    /**
     * Creates an exception instance, and initializes the energy when the
     * exception occurred.
     *
     * @param energy the amount of the robot's energy
     */
    public ExhaustedException(int energy)
    {
        this.energy = energy;
    }

    /**
     * Give the amount of the robot's energy when the exception occurred
     *
     * @return the robot's energy
     */
    public int getEnergy()
    {
        return this.energy;
    }
}
