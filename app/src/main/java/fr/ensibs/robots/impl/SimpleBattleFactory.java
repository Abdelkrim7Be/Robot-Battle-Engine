package fr.ensibs.robots.impl;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.logic.BattleSetup;
import fr.ensibs.robots.logic.Battlefield;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.TeamLeader;
import fr.ensibs.robots.view.DroidView;

import java.awt.Color;
import java.util.List;

/**
 * Concrete {@link BattleFactory} that wires the application default implementations.
 */
public class SimpleBattleFactory implements BattleFactory
{
    private final BattlefieldImpl battlefield;

    public SimpleBattleFactory()
    {
        this.battlefield = new BattlefieldImpl();
    }

    @Override
    public Battlefield makeBattlefield()
    {
        return battlefield;
    }

    @Override
    public Droid makeDroid()
    {
        Location spawn = battlefield.nextSpawn();
        BaseDroid droid = new BaseDroid(battlefield, spawn, BattleSetup.DROID_INITIAL_ENERGY, battlefield.nextHeading());
        battlefield.register(droid);
        return droid;
    }

    @Override
    public Robot makeRobot()
    {
        Location spawn = battlefield.nextSpawn();
        RobotImpl robot = new RobotImpl(battlefield, spawn, BattleSetup.ROBOT_INITIAL_ENERGY, battlefield.nextHeading());
        battlefield.register(robot);
        return robot;
    }

    @Override
    public TeamLeader makeTeamLeader(List<Droid> teammates)
    {
        Location spawn = battlefield.nextSpawn();
        TeamLeaderImpl leader = new TeamLeaderImpl(battlefield, spawn, BattleSetup.ROBOT_INITIAL_ENERGY, battlefield.nextHeading(), teammates);
        battlefield.register(leader);
        return leader;
    }

    @Override
    public <R extends Droid> DroidView<?> makeRobotView(R robot, String name, Color color)
    {
        if (robot instanceof Robot robotImpl) {
            return new SimpleRobotView<>(robotImpl, name, color == null ? Color.BLUE : color);
        }
        return new SimpleDroidView<>(robot, name, color == null ? Color.GRAY : color);
    }
}


