package org.dreamexposure.discal.core.filter;

import org.slf4j.Marker;

import java.util.Arrays;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import discord4j.discordjson.json.gateway.Opcode;
import discord4j.gateway.json.GatewayPayload;

@SuppressWarnings({"unused", "MethodWithMoreThanThreeNegations", "OverlyNestedMethod"})
public class GatewayEventFilter extends TurboFilter {

    private String include;
    private String exclude;
    private List<String> includedEvents;
    private List<String> excludedEvents;

    @Override
    public FilterReply decide(final Marker marker, final Logger logger, final Level level, final String format,
                              final Object[] params, final Throwable t) {
        if (params != null && logger.getName().startsWith("discord4j.gateway.inbound")) {
            for (final Object param : params) {
                if (param instanceof GatewayPayload) {
                    final GatewayPayload<?> payload = (GatewayPayload<?>) param;
                    if (Opcode.DISPATCH.equals(payload.getOp())) {
                        if (this.excludedEvents != null) {
                            if (this.excludedEvents.contains(payload.getType())) {
                                return FilterReply.DENY;
                            }
                        } else if (this.includedEvents != null) {
                            if (!this.includedEvents.contains(payload.getType())) {
                                return FilterReply.DENY;
                            }
                        }
                    }
                }
            }
        }
        return FilterReply.NEUTRAL;
    }

    public void setInclude(final String include) {
        this.include = include;
    }

    public void setExclude(final String exclude) {
        this.exclude = exclude;
    }

    @Override
    public void start() {
        if (this.exclude != null && !this.exclude.trim().isEmpty()) {
            this.excludedEvents = Arrays.asList(this.exclude.split("[;,]"));
            super.start();
        } else if (this.include != null && !this.include.trim().isEmpty()) {
            this.includedEvents = Arrays.asList(this.include.split("[;,]"));
            super.start();
        }
    }
}