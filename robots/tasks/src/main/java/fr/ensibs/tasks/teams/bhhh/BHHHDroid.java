package fr.ensibs.tasks.teams.bhhh;

import fr.ensibs.robots.logic.*;
import java.util.List;
import static fr.ensibs.robots.logic.BattleSetup.*;
import static fr.ensibs.tasks.teams.bhhh.Utils.normalRelativeAngle;

public class BHHHDroid implements RobotTask<Robot>
{
    private Robot robot;
    private double targetX = -1, targetY = -1;
    private boolean hasTarget = false;
    private int moveCounter = 0;
    private double lastX = -1, lastY = -1;
    private int stuckCounter = 0;
    private double patrolAngle = Math.random() * 360;
    
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
        if (robot.getEnergy() <= 0) return;
        
        if (robot instanceof Droid droid) {
            try {
                java.lang.reflect.Method getQueueMethod = droid.getClass().getMethod("getMessageQueue");
                @SuppressWarnings("unchecked")
                List<TeamMessage> messages = (List<TeamMessage>) getQueueMethod.invoke(droid);
                
                for (TeamMessage msg : messages) {
                    if (msg.getType() == TeamMessage.MessageType.BROADCAST) {
                        String messageData = (String) msg.getData();
                        if (messageData != null && messageData.startsWith("TARGET:")) {
                            String[] parts = messageData.split(":");
                            if (parts.length >= 3) {
                                try {
                                    targetX = Double.parseDouble(parts[1]);
                                    targetY = Double.parseDouble(parts[2]);
                                    hasTarget = true;
                                } catch (NumberFormatException e) {}
                            }
                        }
                    }
                }
            } catch (Exception e) {}
        }
        
        if (hasTarget && targetX >= 0 && targetY >= 0) {
            Location current = robot.getLocation();
            double dx = targetX - current.getX();
            double dy = targetY - current.getY();
            double distance = Math.hypot(dx, dy);
            
            double absoluteBearingRad = Math.atan2(dx, -dy);
            
            double gunHeadingRad = Math.toRadians(robot.getGunHeading());
            double gunTurnRad = normalRelativeAngle(absoluteBearingRad - gunHeadingRad);
            robot.turnGun(Math.toDegrees(gunTurnRad));
            
            double gunTurnDegrees = Math.abs(Math.toDegrees(gunTurnRad));
            if (gunTurnDegrees < 20) {
                try {
                    int power = gunTurnDegrees < 5 ? 3 : 1;
                    robot.fire(power);
                } catch (GunOverheatedException | ExhaustedException e) {}
            }
            
            double bodyTurn = normalRelativeAngle(absoluteBearingRad - Math.toRadians(robot.getHeading()));
            
            if (distance > 200) {
                try {
                    robot.turnRobot(Math.toDegrees(bodyTurn));
                    robot.move(Math.min(30, distance / 3));
                } catch (CollisionException | ExhaustedException e) {
                    robot.turnRobot(90);
                }
            } else if (distance < 100) {
                try {
                    robot.turnRobot(Math.toDegrees(bodyTurn) + 90);
                    robot.move(20);
                } catch (CollisionException | ExhaustedException e) {
                    robot.turnRobot(-90);
                }
            } else {
                try {
                    double strafeAngle = normalRelativeAngle(absoluteBearingRad - Math.toRadians(robot.getHeading()) + Math.PI / 2);
                    robot.turnRobot(Math.toDegrees(strafeAngle));
                    robot.move(15);
                } catch (CollisionException | ExhaustedException e) {
                    robot.turnRobot(90);
                }
            }
        } else {
            Location current = robot.getLocation();
            moveCounter++;
            int edgeMargin = 50;
            int centerRadius = 250;
            
            double centerX = FIELD_WIDTH / 2.0;
            double centerY = FIELD_HEIGHT / 2.0;
            double distToCenter = Math.hypot(current.getX() - centerX, current.getY() - centerY);
            
            if (lastX >= 0 && lastY >= 0) {
                double moved = Math.hypot(current.getX() - lastX, current.getY() - lastY);
                if (moved < 3) {
                    stuckCounter++;
                    if (stuckCounter > 5) {
                        double scatterAngle = Math.random() * 360;
                        if (distToCenter < centerRadius) {
                            double dx = current.getX() - centerX;
                            double dy = current.getY() - centerY;
                            scatterAngle = Math.toDegrees(Math.atan2(dx, dy));
                        }
                        try {
                            robot.turnRobot(scatterAngle - robot.getHeading());
                            robot.move(40);
                        } catch (CollisionException | ExhaustedException e) {
                            robot.turnRobot(180);
                        }
                        stuckCounter = 0;
                        patrolAngle = Math.random() * 360;
                        lastX = current.getX();
                        lastY = current.getY();
                        return;
                    }
                } else {
                    stuckCounter = 0;
                }
            }
            lastX = current.getX();
            lastY = current.getY();
            
            if (current.getX() < edgeMargin || current.getX() > FIELD_WIDTH - edgeMargin ||
                current.getY() < edgeMargin || current.getY() > FIELD_HEIGHT - edgeMargin) {
                if (distToCenter > centerRadius + 50) {
                    double dx = centerX - current.getX();
                    double dy = centerY - current.getY();
                    double angle = Math.toDegrees(Math.atan2(dx, -dy));
                    if (angle < 0) angle += 360;
                    double currentHeading = robot.getHeading();
                    double turnAngle = angle - currentHeading;
                    if (turnAngle > 180) turnAngle -= 360;
                    if (turnAngle < -180) turnAngle += 360;
                    try {
                        robot.turnRobot(turnAngle);
                        robot.move(20);
                    } catch (CollisionException | ExhaustedException e) {
                        robot.turnRobot(120);
                    }
                } else {
                    double angle = Math.toDegrees(Math.atan2(current.getY() - centerY, current.getX() - centerX));
                    angle += 5;
                    double patrolX = centerX + centerRadius * Math.cos(Math.toRadians(angle));
                    double patrolY = centerY + centerRadius * Math.sin(Math.toRadians(angle));
                    double dx = patrolX - current.getX();
                    double dy = patrolY - current.getY();
                    double moveAngle = Math.toDegrees(Math.atan2(dx, -dy));
                    if (moveAngle < 0) moveAngle += 360;
                    double currentHeading = robot.getHeading();
                    double turnAngle = moveAngle - currentHeading;
                    if (turnAngle > 180) turnAngle -= 360;
                    if (turnAngle < -180) turnAngle += 360;
                    try {
                        robot.turnRobot(turnAngle);
                        robot.move(20);
                    } catch (CollisionException | ExhaustedException e) {
                        robot.turnRobot(120);
                    }
                }
            } else if (distToCenter < centerRadius) {
                double dx = current.getX() - centerX;
                double dy = current.getY() - centerY;
                double dist = Math.max(1, Math.hypot(dx, dy));
                double unitX = dx / dist;
                double unitY = dy / dist;
                double randomX = (Math.random() - 0.5) * 0.3;
                double randomY = (Math.random() - 0.5) * 0.3;
                double moveX = unitX + randomX;
                double moveY = unitY + randomY;
                double angle = Math.toDegrees(Math.atan2(moveX, -moveY));
                if (angle < 0) angle += 360;
                double currentHeading = robot.getHeading();
                double turnAngle = angle - currentHeading;
                if (turnAngle > 180) turnAngle -= 360;
                if (turnAngle < -180) turnAngle += 360;
                try {
                    robot.turnRobot(turnAngle);
                    robot.move(35);
                } catch (CollisionException | ExhaustedException e) {
                    double scatterDir = Math.random() * 360;
                    robot.turnRobot(scatterDir - currentHeading);
                }
            } else {
                patrolAngle += 2.0;
                if (patrolAngle > 360) patrolAngle -= 360;
                double desiredRadius = centerRadius + 100;
                double targetX = centerX + desiredRadius * Math.cos(Math.toRadians(patrolAngle));
                double targetY = centerY + desiredRadius * Math.sin(Math.toRadians(patrolAngle));
                double dx = targetX - current.getX();
                double dy = targetY - current.getY();
                double angle = Math.toDegrees(Math.atan2(dx, -dy));
                if (angle < 0) angle += 360;
                double currentHeading = robot.getHeading();
                double turnAngle = angle - currentHeading;
                if (turnAngle > 180) turnAngle -= 360;
                if (turnAngle < -180) turnAngle += 360;
                try {
                    robot.turnRobot(turnAngle);
                    robot.move(20);
                } catch (CollisionException | ExhaustedException e) {
                    patrolAngle = Math.random() * 360;
                    robot.turnRobot(90);
                }
            }
        }
    }
}
