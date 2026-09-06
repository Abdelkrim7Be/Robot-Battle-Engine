package fr.ensibs.robots.impl;

public final class AppLog
{
    private static final boolean DEBUG = Boolean.getBoolean("robots.debug");

    private AppLog()
    {
    }

    public static void debug(String message)
    {
        if (DEBUG) {
            System.out.println(message);
        }
    }
}
