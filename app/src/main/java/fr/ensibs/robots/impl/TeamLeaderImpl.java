package fr.ensibs.robots.impl;

import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.TeamLeader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple {@link TeamLeader} implementation.
 */
class TeamLeaderImpl extends RobotImpl implements TeamLeader
{
    private final List<Droid> teammates;

    TeamLeaderImpl(BattlefieldImpl battlefield, Location spawn, int energy, double heading, List<Droid> teammates)
    {
        super(battlefield, spawn, energy, heading);
        this.teammates = Collections.unmodifiableList(new ArrayList<>(teammates));
    }

    @Override
    public List<Droid> getTeammates()
    {
        return teammates;
    }
}


