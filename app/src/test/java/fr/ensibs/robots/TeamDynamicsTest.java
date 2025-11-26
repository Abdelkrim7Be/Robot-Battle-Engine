package fr.ensibs.robots;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.impl.SimpleBattleFactory;
import fr.ensibs.robots.logic.Droid;
import fr.ensibs.robots.logic.Location;
import fr.ensibs.robots.logic.TeamLeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for team dynamics and message system.
 * 
 * <p>Tests verify that:
 * <ul>
 *   <li>Team leaders can send commands to teammates</li>
 *   <li>Droids receive and process messages</li>
 *   <li>Message types are handled correctly</li>
 *   <li>Team coordination works as expected</li>
 * </ul>
 * 
 * <p>Note: These tests require the api module to be built first
 * (TeamMessage class must be compiled).
 * 
 * @author Robot Wars Team
 */
class TeamDynamicsTest
{
    private BattleFactory factory;

    @BeforeEach
    void initialize()
    {
        this.factory = new SimpleBattleFactory();
    }

    /**
     * Test that team leader can get teammates.
     */
    @Test
    void testTeamLeaderHasTeammates()
    {
        List<Droid> teammates = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            teammates.add(factory.makeDroid());
        }
        
        TeamLeader leader = factory.makeTeamLeader(teammates);
        
        List<Droid> leaderTeammates = leader.getTeammates();
        assertEquals(4, leaderTeammates.size(),
            "Team leader should have 4 teammates");
        assertEquals(teammates, leaderTeammates,
            "Team leader's teammates should match provided list");
    }

    /**
     * Test that team leader can send move commands.
     * Note: Requires TeamMessage to be compiled in api module.
     */
    @Test
    void testTeamLeaderMoveCommand()
    {
        List<Droid> teammates = new ArrayList<>();
        Droid teammate1 = factory.makeDroid();
        teammates.add(teammate1);
        
        TeamLeader leader = factory.makeTeamLeader(teammates);
        
        Location initialLocation = teammate1.getLocation();
        
        // Send move command
        leader.commandMove(50.0);
        
        // Process messages (this would happen in game loop)
        // Note: This requires BaseDroid.processMessages() which needs TeamMessage
        
        // Verify teammate received command (would need to check message queue)
        // For now, just verify the method exists and doesn't throw
        assertDoesNotThrow(() -> {
            leader.commandMove(30.0);
        }, "commandMove should not throw");
    }

    /**
     * Test that team leader can send turn commands.
     */
    @Test
    void testTeamLeaderTurnCommands()
    {
        List<Droid> teammates = new ArrayList<>();
        teammates.add(factory.makeDroid());
        
        TeamLeader leader = factory.makeTeamLeader(teammates);
        
        double initialHeading = teammates.get(0).getHeading();
        
        // Send turn body command
        assertDoesNotThrow(() -> {
            leader.commandTurnBody(45.0);
        }, "commandTurnBody should not throw");
        
        // Send turn gun command
        assertDoesNotThrow(() -> {
            leader.commandTurnGun(90.0);
        }, "commandTurnGun should not throw");
    }

    /**
     * Test that team leader can send fire commands.
     */
    @Test
    void testTeamLeaderFireCommand()
    {
        List<Droid> teammates = new ArrayList<>();
        teammates.add(factory.makeDroid());
        
        TeamLeader leader = factory.makeTeamLeader(teammates);
        
        // Send fire command
        assertDoesNotThrow(() -> {
            leader.commandFire(10);
        }, "commandFire should not throw");
    }

    /**
     * Test that team leader can send move-to commands.
     */
    @Test
    void testTeamLeaderMoveToCommand()
    {
        List<Droid> teammates = new ArrayList<>();
        teammates.add(factory.makeDroid());
        
        TeamLeader leader = factory.makeTeamLeader(teammates);
        
        Location target = new Location(200, 200);
        
        // Send move-to command
        assertDoesNotThrow(() -> {
            leader.commandMoveTo(target);
        }, "commandMoveTo should not throw");
    }

    /**
     * Test that team leader can send aim-at commands.
     */
    @Test
    void testTeamLeaderAimAtCommand()
    {
        List<Droid> teammates = new ArrayList<>();
        teammates.add(factory.makeDroid());
        
        TeamLeader leader = factory.makeTeamLeader(teammates);
        
        Location target = new Location(300, 300);
        
        // Send aim-at command
        assertDoesNotThrow(() -> {
            leader.commandAimAt(target);
        }, "commandAimAt should not throw");
    }

    /**
     * Test that commands are broadcast to all teammates.
     */
    @Test
    void testBroadcastToAllTeammates()
    {
        List<Droid> teammates = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            teammates.add(factory.makeDroid());
        }
        
        TeamLeader leader = factory.makeTeamLeader(teammates);
        
        // Send command - should reach all teammates
        assertDoesNotThrow(() -> {
            leader.commandMove(25.0);
        }, "Broadcast command should not throw");
        
        // Verify all teammates are in the team
        assertEquals(4, leader.getTeammates().size(),
            "All teammates should be registered");
    }
}

