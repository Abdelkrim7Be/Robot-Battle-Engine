package fr.ensibs.robots.logic;

public class ExhaustedException extends Exception
{
    private final int energy;

    public ExhaustedException(int energy)
    {
        this.energy = energy;
    }

    public int getEnergy()
    {
        return this.energy;
    }
}
