package com.mohistmc.banner.stackdeobf.util;

import com.mohistmc.banner.stackdeobf.mappings.RemappingUtil;
import java.util.Set;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.message.SimpleMessage;

public final class RemappingRewritePolicy implements RewritePolicy {

    private final boolean rewriteMessages;

    public RemappingRewritePolicy() {
        this.rewriteMessages = true;
    }

    public void inject(Logger logger) {
        // code mostly based on ChatGPT

        Set<Appender> appenders = Set.copyOf(logger.getAppenders().values());
        for (Appender appender : appenders) {
            logger.removeAppender(appender);
        }

        Configuration config = logger.getContext().getConfiguration();
        LoggerConfig logCfg = config.getLoggerConfig(logger.getName());
        AppenderRef[] refs = logCfg.getAppenderRefs().toArray(AppenderRef[]::new);

        RewriteAppender appender = RewriteAppender.createAppender("StackDeobfAppender",
                null, refs, config, this, null);
        appender.start();
        logger.addAppender(appender);
    }

    @Override
    public LogEvent rewrite(LogEvent source) {
        if (!this.rewriteMessages && source.getThrown() == null) {
            return source;
        }

        Log4jLogEvent.Builder builder = new Log4jLogEvent.Builder(source);
        if (this.rewriteMessages) {
            String message = source.getMessage().getFormattedMessage();
            message = RemappingUtil.remapString(message);
            builder.setMessage(new SimpleMessage(message));
        }
        if (source.getThrown() != null) {
            // the remapping part of the logger needs to be done in the ThrowableProxy,
            // otherwise the ExtendedClassInfo would be lost

            try {
                ThrowableProxy proxy = new ThrowableProxy(source.getThrown());
                Log4jRemapUtil.remapThrowableProxy(proxy);
                builder.setThrownProxy(proxy);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }
        return builder.build();
    }
}
