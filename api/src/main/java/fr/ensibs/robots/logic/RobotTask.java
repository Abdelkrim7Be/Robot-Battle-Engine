package fr.ensibs.robots.logic;

public interface RobotTask<R extends Robot> extends Runnable
{
    void setRobot(R robot);
    R getRobot();
}
