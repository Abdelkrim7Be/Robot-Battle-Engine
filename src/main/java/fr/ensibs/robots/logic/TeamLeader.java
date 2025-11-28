package fr.ensibs.robots.logic;

import java.util.List;

public interface TeamLeader extends Robot
{
    List<Droid> getTeammates();
    
    default void broadcastMessage(TeamMessage message)
    {
        for (Droid teammate : getTeammates()) {
            try {
                java.lang.reflect.Method method = teammate.getClass().getMethod("receiveMessage", TeamMessage.class);
                method.invoke(teammate, message);
            } catch (Exception e) {
            }
        }
    }
    
    default void commandMove(double distance)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.MOVE, distance, this);
        broadcastMessage(message);
    }
    
    default void commandTurnBody(double degrees)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.TURN_BODY, degrees, this);
        broadcastMessage(message);
    }
    
    default void commandTurnGun(double degrees)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.TURN_GUN, degrees, this);
        broadcastMessage(message);
    }
    
    default void commandFire(int power)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.FIRE, power, this);
        broadcastMessage(message);
    }
    
    default void commandMoveTo(Location target)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.MOVE_TO, target, this);
        broadcastMessage(message);
    }
    
    default void commandAimAt(Location target)
    {
        TeamMessage message = new TeamMessage(TeamMessage.MessageType.AIM_AT, target, this);
        broadcastMessage(message);
    }
}
