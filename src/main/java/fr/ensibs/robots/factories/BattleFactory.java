package fr.ensibs.robots.factories;

import fr.ensibs.robots.logic.Battlefield;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.TeamLeader;
import fr.ensibs.robots.view.DroidView;

import java.awt.*;
import java.util.List;

/**
 * A factory to create related robots and battlefield
 *
 * @author Pascale Launay
 */
public interface BattleFactory
{
    /**
     * Give a battlefield to which all robots created by this factory will be added.
     * The instance of the battlefield should be unique for this factory, i.e.
     * multiple invocations of this method to a factory should return the same instance.
     *
     * @return the battlefield
     */
    Battlefield makeBattlefield();

    /**
     * Make a {@link Droid} instance and add it to the battlefield instance at an
     * initial location chosen randomly among empty locations on the battlefield.
     *
     * @return the new droid
     */
    Droid makeDroid();

    /**
     * Make a {@link Robot} instance and add it to the battlefield instance at an
     * initial location chosen randomly among empty locations on the battlefield.
     *
     * @return the new droid
     */
    Robot makeRobot();

    /**
     * Create a team leader with the given teammates, and add it to the battlefield
     * instance at an initial location chosen randomly among empty locations on the
     * battlefield. The teammates should have been created and added previously through
     * the {@link #makeDroid()} method.
     *
     * @param teammates the leader's teammates
     */
    TeamLeader makeTeamLeader(List<Droid> teammates);

    /**
     * Create a new {@link DroidView} instance with the given properties
     *
     * @param robot the droid/robot represented by the view
     * @param name  the name to be displayed
     * @param color the color of the robot
     * @return a new robot view
     */
    <R extends Droid> DroidView<?> makeRobotView(R robot, String name, Color color);
}
