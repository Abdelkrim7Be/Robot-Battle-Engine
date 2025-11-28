package fr.ensibs.robots.factories;

import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;
import fr.ensibs.robots.logic.TeamLeader;

import java.io.File;
import java.util.List;

public interface RobotTaskFactory
{
    void loadJar(File jarFile);
    List<Class<? extends RobotTask<Robot>>> listRobotClasses();
    List<Class<? extends RobotTask<TeamLeader>>> listLeaderClasses();
    RobotTask<Robot> makeRobotTask(Class<? extends RobotTask<Robot>> clazz);
    RobotTask<TeamLeader> makeLeaderTask(Class<? extends RobotTask<TeamLeader>> clazz);
}
