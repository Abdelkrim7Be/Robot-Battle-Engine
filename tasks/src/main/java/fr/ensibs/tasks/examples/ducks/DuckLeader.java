package fr.ensibs.tasks.examples.ducks;

import fr.ensibs.robots.logic.*;

import java.util.Random;

import static fr.ensibs.robots.logic.BattleSetup.*;

/**
 * A task for a team leader that acts like a duck
 *
 * @author Pascale Launay
 */
public class DuckLeader implements RobotTask<TeamLeader>
{
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private TeamLeader leader;

    @Override

    public void run()
    {
        for (Droid mate : leader.getTeammates()) {
            mate.turnRobot(RANDOM.nextInt(180));
            try {
                mate.move(RANDOM.nextInt(MIN_DISTANCE_MOVE, MAX_DISTANCE_MOVE));
                mate.fire(RANDOM.nextInt(MAX_FIRE_POWER));
            } catch (CollisionException | GunOverheatedException | ExhaustedException e) {
                System.err.println("Quack quack");
            }
        }
    }

    @Override
    public void setRobot(TeamLeader leader)
    {
        this.leader = leader;
    }

    @Override
    public TeamLeader getRobot()
    {
        return this.leader;
    }
}
