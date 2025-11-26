package fr.ensibs.robots.logic;

/**
 * Represents a message/command sent from a team leader to teammates.
 * 
 * <p>Messages are used for team coordination, allowing the leader to
 * give orders to droids without directly controlling them.
 * 
 * @author Robot Wars Team
 */
public class TeamMessage
{
    /**
     * Message types for different commands.
     */
    public enum MessageType
    {
        /** Command to move in a specific direction */
        MOVE,
        /** Command to turn the body */
        TURN_BODY,
        /** Command to turn the gun */
        TURN_GUN,
        /** Command to fire */
        FIRE,
        /** Command to move to a specific location */
        MOVE_TO,
        /** Command to aim at a specific location */
        AIM_AT,
        /** General broadcast message */
        BROADCAST
    }
    
    private final MessageType type;
    private final Object data; // Message payload (can be Double, Integer, Location, etc.)
    private final TeamLeader sender;
    private final long timestamp;
    
    /**
     * Constructor
     * 
     * @param type the message type
     * @param data the message data (can be null)
     * @param sender the team leader sending the message
     */
    public TeamMessage(MessageType type, Object data, TeamLeader sender)
    {
        this.type = type;
        this.data = data;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the message type
     * 
     * @return the type
     */
    public MessageType getType()
    {
        return type;
    }
    
    /**
     * Get the message data
     * 
     * @return the data (can be null)
     */
    public Object getData()
    {
        return data;
    }
    
    /**
     * Get the sender of this message
     * 
     * @return the team leader
     */
    public TeamLeader getSender()
    {
        return sender;
    }
    
    /**
     * Get the timestamp when this message was created
     * 
     * @return the timestamp in milliseconds
     */
    public long getTimestamp()
    {
        return timestamp;
    }
    
    /**
     * Get data as a Double (for numeric values like distance, angle).
     * 
     * @return the data as Double, or null if not a number
     */
    public Double getDataAsDouble()
    {
        if (data instanceof Number) {
            return ((Number) data).doubleValue();
        }
        return null;
    }
    
    /**
     * Get data as an Integer (for values like power).
     * 
     * @return the data as Integer, or null if not an integer
     */
    public Integer getDataAsInteger()
    {
        if (data instanceof Number) {
            return ((Number) data).intValue();
        }
        return null;
    }
    
    /**
     * Get data as a Location (for MOVE_TO, AIM_AT commands).
     * 
     * @return the data as Location, or null if not a Location
     */
    public Location getDataAsLocation()
    {
        if (data instanceof Location) {
            return (Location) data;
        }
        return null;
    }
    
    @Override
    public String toString()
    {
        return String.format("TeamMessage{type=%s, data=%s, sender=%s, timestamp=%d}",
            type, data, sender, timestamp);
    }
}

