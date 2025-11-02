package fr.ensibs.tasks.examples.snails;

import fr.ensibs.robots.logic.*;

import java.util.Random;

/**
 * A task for a robot that acts like a snail
 *
 * @author Pascale Launay
 */
public class SnailRobot implements RobotTask<Robot>
{
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private int direction = RANDOM.nextInt(360);
    private Robot robot;

    @Override
    public void setRobot(Robot robot)
    {
        this.robot = robot;
    }

    @Override
    public Robot getRobot()
    {
        return this.robot;
    }

    @Override
    public void run()
    {
        try {
            robot.turnRobot(direction);
            direction = RANDOM.nextInt(-5, 5);
            robot.move(1);
            robot.turnGun(5);
            robot.fire(1);
        } catch (CollisionException e) {
            robot.turnRobot(180);
        } catch (GunOverheatedException | ExhaustedException e) {
            System.out.println("Blllllllllll");
        }
    }
}
