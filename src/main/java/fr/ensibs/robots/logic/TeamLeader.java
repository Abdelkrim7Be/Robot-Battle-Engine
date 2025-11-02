package fr.ensibs.robots.logic;

import java.util.List;

/**
 * An advanced robot that is able to give orders to other robots in its team.
 *
 * @author Pascale Launay
 */
public interface TeamLeader extends Robot
{
    /**
     * Gets the team leader's teammates
     *
     * @return the teammates
     */
    List<Droid> getTeammates();
}
