package fr.ensibs.robots;

import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;
import fr.ensibs.robots.logic.TeamLeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for a {@link RobotTaskFactory} implementation
 *
 * @author Pascale Launay
 */
public class RobotTaskFactoryTest
{
    private RobotTaskFactory factory;   // the tasks factory instance initialized before each test
    private File jarFile;               // the jar file to be loaded

    /**
     * Initialize a new factory and a jar file before each test
     */
    @BeforeEach
    void initialize()
    {
        this.factory = null; // TODO create your own implementation
        // create a temporary jar file
        try (InputStream in = RobotTaskFactoryTest.class.getResourceAsStream("/examples.jar")) {
            Path path = Files.createTempFile("test", ".jar");
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            jarFile = path.toFile();
        } catch (IOException e) {
            fail("Failed to load examples.jar. This exception should never happen.");
        }
    }

    /**
     * Delete the jar file after each test
     */
    @AfterEach
    void clear()
    {
        if (jarFile != null) {
            jarFile.delete();
        }
    }

    /**
     * Tests for the {@link RobotTaskFactory#listRobotClasses()} method
     */
    @Test
    void testListRobotClasses()
    {
        // initialize the expected result
        String[] expected = {"fr.ensibs.tasks.examples.ducks.DuckRobot", "fr.ensibs.tasks.examples.snails.SnailRobot"};

        // load the jar file and invoke the tested method
        factory.loadJar(jarFile);
        List<Class<? extends RobotTask<Robot>>> classes = factory.listRobotClasses();

        // check the result
        assertNotNull(classes);
        assertEquals(expected.length, classes.size());
        List<String> actual = classes.stream().map(Class::getName).toList();
        for (String name : expected) {
            assertTrue(actual.contains(name), "Class " + name + " not found");
        }
    }

    /**
     * Tests for the {@link RobotTaskFactory#listLeaderClasses()} method
     */
    @Test
    void testListLeaderClasses()
    {
        // initialize the expected result
        String expected = "fr.ensibs.tasks.examples.ducks.DuckLeader";

        // load the jar file and invoke the tested method
        factory.loadJar(jarFile);
        List<Class<? extends RobotTask<TeamLeader>>> classes = factory.listLeaderClasses();

        // check the result
        assertNotNull(classes);
        assertEquals(1, classes.size());
        String actual = classes.getFirst().getName();
        assertEquals(expected, actual, "Class " + expected + " not found");
    }

    /**
     * Tests for the {@link RobotTaskFactory#makeRobotTask(Class)} method
     */
    @Test
    void testMakeRobotTask()
    {
        // load the jar file and invoke the tested method
        factory.loadJar(jarFile);
        Class<? extends RobotTask<Robot>> clazz = factory.listRobotClasses().getFirst();
        RobotTask<Robot> result = factory.makeRobotTask(clazz);

        // check the result
        assertNotNull(result);
    }

    /**
     * Tests for the {@link RobotTaskFactory#makeLeaderTask(Class)} method
     */
    @Test
    void testMakeLeaderTask()
    {
        // load the jar file and invoke the tested method
        factory.loadJar(jarFile);
        Class<? extends RobotTask<TeamLeader>> clazz = factory.listLeaderClasses().getFirst();
        RobotTask<TeamLeader> result = factory.makeLeaderTask(clazz);

        // check the result
        assertNotNull(result);
    }
}
