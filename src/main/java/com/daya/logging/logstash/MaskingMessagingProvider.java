package com.daya.logging.logstash;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.Data;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Data
public class MaskingMessagingProvider extends MessageJsonProvider {

    public static final String DEFAULT_RULES_DELIMITER = ",";

    private LogMasker logMasker;
    private String rules;

    public MaskingMessagingProvider() {
        super();
    }

    @Override
    public void start() {

        this.logMasker = LogMasker.create(StringUtils.tokenizeToStringArray(rules, DEFAULT_RULES_DELIMITER));
        super.start();
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {

        if (isStarted()) {
            JsonWritingUtils.writeStringField(generator, getFieldName(), logMasker.mask(event.getFormattedMessage()));
        }
    }

}
