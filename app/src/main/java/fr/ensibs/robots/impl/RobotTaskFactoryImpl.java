package fr.ensibs.robots.impl;

import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;
import fr.ensibs.robots.logic.TeamLeader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Default {@link RobotTaskFactory} that loads tasks through reflection.
 */
public class RobotTaskFactoryImpl implements RobotTaskFactory, AutoCloseable
{
    private final List<Class<? extends RobotTask<Robot>>> robotClasses = new ArrayList<>();
    private final List<Class<? extends RobotTask<TeamLeader>>> leaderClasses = new ArrayList<>();
    private ClassLoader classLoader;

    @Override
    @SuppressWarnings("unchecked")
    public void loadJar(File jarFile)
    {
        Objects.requireNonNull(jarFile, "jarFile");
        if (!jarFile.exists()) {
            throw new IllegalArgumentException("Jar file " + jarFile + " does not exist");
        }
        robotClasses.clear();
        leaderClasses.clear();
        closeQuietly();
        Map<String, byte[]> classData = new HashMap<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class") || entry.getName().endsWith("module-info.class")) {
                    continue;
                }
                String className = entry.getName().replace('/', '.').replace(".class", "");
                try (InputStream in = jar.getInputStream(entry)) {
                    classData.put(className, in.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load jar file " + jarFile, e);
        }

        try {
            classLoader = new BytecodeClassLoader(classData, RobotTaskFactoryImpl.class.getClassLoader());
            for (String className : classData.keySet()) {
                Class<?> clazz = Class.forName(className, false, classLoader);
                if (!RobotTask.class.isAssignableFrom(clazz) || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                Class<?> robotType = resolveRobotType(clazz);
                if (robotType == null) {
                    continue;
                }
                if (TeamLeader.class.isAssignableFrom(robotType)) {
                    leaderClasses.add((Class<? extends RobotTask<TeamLeader>>) clazz.asSubclass(RobotTask.class));
                } else if (Robot.class.isAssignableFrom(robotType)) {
                    robotClasses.add((Class<? extends RobotTask<Robot>>) clazz.asSubclass(RobotTask.class));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to define classes from " + jarFile, e);
        }
    }

    @Override
    public List<Class<? extends RobotTask<Robot>>> listRobotClasses()
    {
        return Collections.unmodifiableList(new ArrayList<>(robotClasses));
    }

    @Override
    public List<Class<? extends RobotTask<TeamLeader>>> listLeaderClasses()
    {
        return Collections.unmodifiableList(new ArrayList<>(leaderClasses));
    }

    @Override
    public RobotTask<Robot> makeRobotTask(Class<? extends RobotTask<Robot>> clazz)
    {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    @Override
    public RobotTask<TeamLeader> makeLeaderTask(Class<? extends RobotTask<TeamLeader>> clazz)
    {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    private Class<?> resolveRobotType(Class<?> clazz)
    {
        for (Type type : clazz.getGenericInterfaces()) {
            Class<?> robotType = resolveRobotType(type);
            if (robotType != null) {
                return robotType;
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            return resolveRobotType(superclass);
        }
        return null;
    }

    private Class<?> resolveRobotType(Type type)
    {
        if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() instanceof Class<?> raw && RobotTask.class.equals(raw)) {
                Type argument = parameterizedType.getActualTypeArguments()[0];
                if (argument instanceof Class<?>) {
                    return (Class<?>) argument;
                }
            }
        } else if (type instanceof Class<?>) {
            return resolveRobotType((Class<?>) type);
        }
        return null;
    }

    @Override
    public void close() throws Exception
    {
        closeQuietly();
    }

    private void closeQuietly()
    {
        classLoader = null;
    }

    private static final class BytecodeClassLoader extends ClassLoader
    {
        private final Map<String, byte[]> classes;

        BytecodeClassLoader(Map<String, byte[]> classes, ClassLoader parent)
        {
            super(parent);
            this.classes = classes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException
        {
            byte[] bytes = classes.get(name);
            if (bytes == null) {
                throw new ClassNotFoundException(name);
            }
            byte[] patched = patchVersion(bytes);
            return defineClass(name, patched, 0, patched.length);
        }

        private static byte[] patchVersion(byte[] original)
        {
            byte[] copy = original.clone();
            int major = ((copy[6] & 0xFF) << 8) | (copy[7] & 0xFF);
            if (major > 65) {
                copy[6] = 0;
                copy[7] = 65;
            }
            return copy;
        }
    }
}

