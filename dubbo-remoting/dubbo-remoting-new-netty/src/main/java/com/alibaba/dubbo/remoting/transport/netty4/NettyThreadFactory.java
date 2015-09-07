package com.alibaba.dubbo.remoting.transport.netty4;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NettyThreadFactory implements ThreadFactory {

    private final String pattern;
    private final String name;
    private final boolean daemon;

    public NettyThreadFactory(String pattern, String name, boolean daemon) {
        this.pattern = pattern;
        this.name = name;
        this.daemon = daemon;
    }

    
    public static final String DEFAULT_PATTERN = "Dubbo Thread ##counter# - #name#";
    private static final Pattern INVALID_PATTERN = Pattern.compile(".*#\\w+#.*");

    private static AtomicLong threadCounter = new AtomicLong();

    public Thread newThread(Runnable runnable) {
        String threadName = resolveThreadName(pattern, name);
        Thread answer = new Thread(runnable, threadName);
        answer.setDaemon(daemon);

        return answer;
    }

    public String getName() {
        return name;
    }
    
    public static String before(String text, String before) {
        if (!text.contains(before)) {
            return null;
        }
        return text.substring(0, text.indexOf(before));
    }
    
    private static long nextThreadCounter() {
        return threadCounter.getAndIncrement();
    }
    
    public static String resolveThreadName(String pattern, String name) {
        if (pattern == null) {
            pattern = DEFAULT_PATTERN;
        }

        // we support #longName# and #name# as name placeholders
        String longName = name;
        String shortName = name.contains("?") ? before(name, "?") : name;
        // must quote the names to have it work as literal replacement
        shortName = Matcher.quoteReplacement(shortName);
        longName = Matcher.quoteReplacement(longName);

        // replace tokens
        String answer = pattern.replaceFirst("#counter#", "" + nextThreadCounter());
        answer = answer.replaceFirst("#longName#", longName);
        answer = answer.replaceFirst("#name#", shortName);

        // are there any #word# combos left, if so they should be considered invalid tokens
        if (INVALID_PATTERN.matcher(answer).matches()) {
            throw new IllegalArgumentException("Pattern is invalid: " + pattern);
        }

        return answer;
    }

    public String toString() {
        return "com.alibaba.dubbo.remoting.transport.netty4.NettyThreadFactory[" + name + "]";
    }
}