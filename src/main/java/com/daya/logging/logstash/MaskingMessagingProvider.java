package com.daya.logging.logstash;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.Data;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        super.start();
        this.logMasker = LogMasker.create(StringUtils.tokenizeToStringArray(rules, DEFAULT_RULES_DELIMITER));
    }

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {

        if (isStarted()) {
            JsonWritingUtils.writeStringField(generator, getFieldName(), logMasker.mask(event.getFormattedMessage()));
        }
    }
}

class LogMasker {

    private MaskingRule[] masks;

    public LogMasker(MaskingRule[] masks) {
        super();
        this.masks = masks.clone();
    }

    public static LogMasker create(String[] rules) {

        return new LogMasker(Arrays.stream(rules).map(rule -> MaskingRule.create(rule)).toArray(MaskingRule[]::new));
    }

    public String mask(String input) {
        String transformed = input;
        for (MaskingRule m : masks) {
            transformed = m.mask(transformed);
        }
        return transformed;
    }
}

class MaskingRule {
    public static final int REG_EX_DEFAULT_GROUP_SELECTOR = 2;
    public static final String DEFAULT_REPLACEMENT = "*";

    private Pattern pattern;
    private UnaryOperator<String> replacement;

    public MaskingRule(Pattern maskPattern, UnaryOperator<String> replacement) {
        super();
        this.pattern = maskPattern;
        this.replacement = replacement;
    }

    public static MaskingRule create(String rule) {
        return new MaskingRule(Pattern.compile(rule), (in) -> MaskingRule.maskDataWithReplacement(in, DEFAULT_REPLACEMENT));
    }

    public String mask(String transformed) {
        Matcher matcher = pattern.matcher(transformed);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, replacement.apply(getDataToBeMasked(matcher)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String maskDataWithReplacement(String input, String replacement) {
        int repetition = !StringUtils.hasLength(input) ? 0 : input.length();
        return String.join("", Collections.nCopies(repetition, replacement));
    }

    private static String getDataToBeMasked(Matcher matcher) {
        if (matcher.groupCount() > 1) {
            return matcher.group(REG_EX_DEFAULT_GROUP_SELECTOR);
        }
        return matcher.groupCount() > 0 ? matcher.group(1) : "";
    }
}
