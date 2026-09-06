package fr.ensibs.robots.logic;

public class TeamMessage
{
    public enum MessageType
    {
        MOVE,
        TURN_BODY,
        TURN_GUN,
        FIRE,
        MOVE_TO,
        AIM_AT,
        BROADCAST
    }
    
    private final MessageType type;
    private final Object data;
    private final TeamLeader sender;
    private final long timestamp;
    
    public TeamMessage(MessageType type, Object data, TeamLeader sender)
    {
        this.type = type;
        this.data = data;
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
    }
    
    public MessageType getType()
    {
        return type;
    }
    
    public Object getData()
    {
        return data;
    }
    
    public TeamLeader getSender()
    {
        return sender;
    }
    
    public long getTimestamp()
    {
        return timestamp;
    }
    
    public Double getDataAsDouble()
    {
        if (data instanceof Number) {
            return ((Number) data).doubleValue();
        }
        return null;
    }
    
    public Integer getDataAsInteger()
    {
        if (data instanceof Number) {
            return ((Number) data).intValue();
        }
        return null;
    }
    
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

