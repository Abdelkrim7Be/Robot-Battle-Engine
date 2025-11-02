package fr.ensibs.tasks.examples.ducks;

import fr.ensibs.robots.logic.*;

import java.util.Random;

import static fr.ensibs.robots.logic.BattleSetup.*;

/**
 * A task for a robot that acts like a duck
 *
 * @author Pascale Launay
 */
public class DuckRobot implements RobotTask<Robot>
{
    private static final Random RANDOM = new Random(System.currentTimeMillis());

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
        robot.turnRobot(RANDOM.nextInt(-180, 180));
        try {
            robot.move(RANDOM.nextInt(MIN_DISTANCE_MOVE, MAX_DISTANCE_MOVE));
            robot.turnGun(RANDOM.nextInt(-30, 30));
            robot.fire(RANDOM.nextInt(MAX_FIRE_POWER));
        } catch (CollisionException | GunOverheatedException | ExhaustedException e) {
            System.err.println("Quack quack");
        }
    }
}
