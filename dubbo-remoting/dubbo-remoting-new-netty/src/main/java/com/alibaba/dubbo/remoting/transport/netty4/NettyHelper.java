package com.alibaba.dubbo.remoting.transport.netty4;/*
 * Copyright 1999-2011 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import io.netty.util.internal.logging.AbstractInternalLogger;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * @author <a href="mailto:gang.lvg@taobao.com">kimi</a>
 */
final class NettyHelper {

    public static void setNettyLoggerFactory() {
        InternalLoggerFactory factory = InternalLoggerFactory.getDefaultFactory();
        if (factory == null || !(factory instanceof DubboLoggerFactory)) {
            InternalLoggerFactory.setDefaultFactory(new DubboLoggerFactory());
        }
    }

    static class DubboLoggerFactory extends InternalLoggerFactory {

        @Override
        public InternalLogger newInstance(String name) {
            return new DubboLogger(LoggerFactory.getLogger(name), name);
        }
    }

    static class DubboLogger extends AbstractInternalLogger {

        private Logger logger;
        
        DubboLogger(Logger logger, String name) {
        	super(name);
            this.logger = logger;
        }

        /**
         * Delegates to the {@link Log#isTraceEnabled} method of the underlying
         * {@link Log} instance.
         */
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled();
        }

        /**
         * Delegates to the {@link Log#trace(Object)} method of the underlying
         * {@link Log} instance.
         *
         * @param msg - the message object to be logged
         */
        public void trace(String msg) {
            logger.trace(msg);
        }

        /**
         * Delegates to the {@link Log#trace(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level TRACE.
         * </p>
         *
         * @param format
         *          the format string
         * @param arg
         *          the argument
         */
        public void trace(String format, Object arg) {
            if (logger.isTraceEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.trace(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#trace(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level TRACE.
         * </p>
         *
         * @param format
         *          the format string
         * @param argA
         *          the first argument
         * @param argB
         *          the second argument
         */
        public void trace(String format, Object argA, Object argB) {
            if (logger.isTraceEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.trace(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#trace(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level TRACE.
         * </p>
         *
         * @param format the format string
         * @param arguments a list of 3 or more arguments
         */
        public void trace(String format, Object... arguments) {
            if (logger.isTraceEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.trace(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#trace(Object, Throwable)} method of
         * the underlying {@link Log} instance.
         *
         * @param msg
         *          the message accompanying the exception
         * @param t
         *          the exception (throwable) to log
         */
        public void trace(String msg, Throwable t) {
            logger.trace(msg, t);
        }

        /**
         * Delegates to the {@link Log#isDebugEnabled} method of the underlying
         * {@link Log} instance.
         */
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        //

        /**
         * Delegates to the {@link Log#debug(Object)} method of the underlying
         * {@link Log} instance.
         *
         * @param msg - the message object to be logged
         */
        public void debug(String msg) {
            logger.debug(msg);
        }

        /**
         * Delegates to the {@link Log#debug(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level DEBUG.
         * </p>
         *
         * @param format
         *          the format string
         * @param arg
         *          the argument
         */
        public void debug(String format, Object arg) {
            if (logger.isDebugEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.debug(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#debug(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level DEBUG.
         * </p>
         *
         * @param format
         *          the format string
         * @param argA
         *          the first argument
         * @param argB
         *          the second argument
         */
        public void debug(String format, Object argA, Object argB) {
            if (logger.isDebugEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.debug(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#debug(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level DEBUG.
         * </p>
         *
         * @param format the format string
         * @param arguments a list of 3 or more arguments
         */
        public void debug(String format, Object... arguments) {
            if (logger.isDebugEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.debug(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#debug(Object, Throwable)} method of
         * the underlying {@link Log} instance.
         *
         * @param msg
         *          the message accompanying the exception
         * @param t
         *          the exception (throwable) to log
         */
        public void debug(String msg, Throwable t) {
            logger.debug(msg, t);
        }

        /**
         * Delegates to the {@link Log#isInfoEnabled} method of the underlying
         * {@link Log} instance.
         */
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        /**
         * Delegates to the {@link Log#debug(Object)} method of the underlying
         * {@link Log} instance.
         *
         * @param msg - the message object to be logged
         */
        public void info(String msg) {
            logger.info(msg);
        }

        /**
         * Delegates to the {@link Log#info(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level INFO.
         * </p>
         *
         * @param format
         *          the format string
         * @param arg
         *          the argument
         */

        public void info(String format, Object arg) {
            if (logger.isInfoEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.info(ft.getMessage(), ft.getThrowable());
            }
        }
        /**
         * Delegates to the {@link Log#info(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level INFO.
         * </p>
         *
         * @param format
         *          the format string
         * @param argA
         *          the first argument
         * @param argB
         *          the second argument
         */
        public void info(String format, Object argA, Object argB) {
            if (logger.isInfoEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.info(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#info(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level INFO.
         * </p>
         *
         * @param format the format string
         * @param arguments a list of 3 or more arguments
         */
        public void info(String format, Object... arguments) {
            if (logger.isInfoEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.info(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#info(Object, Throwable)} method of
         * the underlying {@link Log} instance.
         *
         * @param msg
         *          the message accompanying the exception
         * @param t
         *          the exception (throwable) to log
         */
        public void info(String msg, Throwable t) {
            logger.info(msg, t);
        }

        /**
         * Delegates to the {@link Log#isWarnEnabled} method of the underlying
         * {@link Log} instance.
         */
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        /**
         * Delegates to the {@link Log#warn(Object)} method of the underlying
         * {@link Log} instance.
         *
         * @param msg - the message object to be logged
         */
        public void warn(String msg) {
            logger.warn(msg);
        }

        /**
         * Delegates to the {@link Log#warn(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level WARN.
         * </p>
         *
         * @param format
         *          the format string
         * @param arg
         *          the argument
         */
        public void warn(String format, Object arg) {
            if (logger.isWarnEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.warn(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#warn(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level WARN.
         * </p>
         *
         * @param format
         *          the format string
         * @param argA
         *          the first argument
         * @param argB
         *          the second argument
         */
        public void warn(String format, Object argA, Object argB) {
            if (logger.isWarnEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.warn(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#warn(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level WARN.
         * </p>
         *
         * @param format the format string
         * @param arguments a list of 3 or more arguments
         */
        public void warn(String format, Object... arguments) {
            if (logger.isWarnEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.warn(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#warn(Object, Throwable)} method of
         * the underlying {@link Log} instance.
         *
         * @param msg
         *          the message accompanying the exception
         * @param t
         *          the exception (throwable) to log
         */

        public void warn(String msg, Throwable t) {
            logger.warn(msg, t);
        }

        /**
         * Delegates to the {@link Log#isErrorEnabled} method of the underlying
         * {@link Log} instance.
         */
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled();
        }

        /**
         * Delegates to the {@link Log#error(Object)} method of the underlying
         * {@link Log} instance.
         *
         * @param msg - the message object to be logged
         */
        public void error(String msg) {
            logger.error(msg);
        }

        /**
         * Delegates to the {@link Log#error(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level ERROR.
         * </p>
         *
         * @param format
         *          the format string
         * @param arg
         *          the argument
         */
        public void error(String format, Object arg) {
            if (logger.isErrorEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, arg);
                logger.error(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#error(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level ERROR.
         * </p>
         *
         * @param format
         *          the format string
         * @param argA
         *          the first argument
         * @param argB
         *          the second argument
         */
        public void error(String format, Object argA, Object argB) {
            if (logger.isErrorEnabled()) {
                FormattingTuple ft = MessageFormatter.format(format, argA, argB);
                logger.error(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#error(Object)} method of the underlying
         * {@link Log} instance.
         *
         * <p>
         * However, this form avoids superfluous object creation when the logger is disabled
         * for level ERROR.
         * </p>
         *
         * @param format the format string
         * @param arguments a list of 3 or more arguments
         */
        public void error(String format, Object... arguments) {
            if (logger.isErrorEnabled()) {
                FormattingTuple ft = MessageFormatter.arrayFormat(format, arguments);
                logger.error(ft.getMessage(), ft.getThrowable());
            }
        }

        /**
         * Delegates to the {@link Log#error(Object, Throwable)} method of
         * the underlying {@link Log} instance.
         *
         * @param msg
         *          the message accompanying the exception
         * @param t
         *          the exception (throwable) to log
         */
        public void error(String msg, Throwable t) {
            logger.error(msg, t);
        }
	 
    }

}
