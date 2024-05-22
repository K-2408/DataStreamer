package com.testingone.example.testingone;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class ThreadMonitor {

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public static void printThreadInfo() {
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        Arrays.stream(threadInfos).forEach(threadInfo -> {
            System.out.println(threadInfo.toString());
        });
    }
}
