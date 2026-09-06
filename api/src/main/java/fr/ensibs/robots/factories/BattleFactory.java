package fr.ensibs.robots.factories;

import fr.ensibs.robots.logic.Battlefield;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.TeamLeader;
import fr.ensibs.robots.view.DroidView;

import java.awt.*;
import java.util.List;

public interface BattleFactory
{
    Battlefield makeBattlefield();
    Droid makeDroid();
    Robot makeRobot();
    TeamLeader makeTeamLeader(List<Droid> teammates);
    <R extends Droid> DroidView<?> makeRobotView(R robot, String name, Color color);
}
