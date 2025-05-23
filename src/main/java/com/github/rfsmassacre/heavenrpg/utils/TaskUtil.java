package com.github.rfsmassacre.heavenrpg.utils;

import com.github.rfsmassacre.heavenlibrary.paper.utils.PaperTaskUtil;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;

public class TaskUtil extends PaperTaskUtil
{
    private static TaskUtil instance;

    public static void initialize()
    {
        instance = new TaskUtil();
    }

    public static void runTask(Runnable runnable)
    {
        instance.run(runnable);
    }

    public static void runTask(Runnable runnable, int delay)
    {
        instance.run(runnable, delay);
    }

    public static void runTaskAsync(Runnable runnable)
    {
        instance.runAsync(runnable);
    }

    public static void runTaskAsync(Runnable runnable, long delay)
    {
        instance.runAsync(runnable, delay);
    }

    public static void reload()
    {
        instance.stopTimers();
        instance.startTimers();
    }

    public TaskUtil()
    {
        super(HeavenRPG.getInstance());
    }

    @Override
    public void startTimers()
    {
        //Do nothing.
    }
}
