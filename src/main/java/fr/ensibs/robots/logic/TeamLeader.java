package fr.ensibs.robots.logic;

import java.util.List;

/**
 * An advanced robot that is able to give orders to other robots in its team.
 *
 * @author Pascale Launay
 */
public interface TeamLeader extends Robot
{
    /**
     * Gets the team leader's teammates
     *
     * @return the teammates
     */
    List<Droid> getTeammates();
    
    /**
     * Broadcast a message to all teammates.
     * 
     * @param message the message to broadcast
     */
    default void broadcastMessage(TeamMessage message)
    {
        for (Droid teammate : getTeammates()) {
            if (teammate instanceof BaseDroid) {
                ((BaseDroid) teammate).receiveMessage(message);
            }
        }
    }
    
    /**
     * Send a move command to all teammates.
     * 
     * @param distance the distance to move (positive = forward, negative = backward)
     */
    default void commandMove(double distance)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.MOVE, distance, this);
        broadcastMessage(message);
    }
    
    /**
     * Send a turn body command to all teammates.
     * 
     * @param degrees the angle to turn in degrees
     */
    default void commandTurnBody(double degrees)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.TURN_BODY, degrees, this);
        broadcastMessage(message);
    }
    
    /**
     * Send a turn gun command to all teammates.
     * 
     * @param degrees the angle to turn in degrees
     */
    default void commandTurnGun(double degrees)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.TURN_GUN, degrees, this);
        broadcastMessage(message);
    }
    
    /**
     * Send a fire command to all teammates.
     * 
     * @param power the bullet power
     */
    default void commandFire(int power)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.FIRE, power, this);
        broadcastMessage(message);
    }
    
    /**
     * Send a move-to command to all teammates.
     * 
     * @param target the target location
     */
    default void commandMoveTo(Location target)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.MOVE_TO, target, this);
        broadcastMessage(message);
    }
    
    /**
     * Send an aim-at command to all teammates.
     * 
     * @param target the target location to aim at
     */
    default void commandAimAt(Location target)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.AIM_AT, target, this);
        broadcastMessage(message);
    }
}
