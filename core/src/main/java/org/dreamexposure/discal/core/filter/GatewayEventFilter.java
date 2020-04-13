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

@SuppressWarnings("unused")
public class GatewayEventFilter extends TurboFilter {

	private String include;
	private String exclude;
	private List<String> includedEvents;
	private List<String> excludedEvents;

	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
		if (params != null && logger.getName().startsWith("discord4j.gateway.inbound")) {
			for (Object param : params) {
				if (param instanceof GatewayPayload) {
					GatewayPayload<?> payload = (GatewayPayload) param;
					if (Opcode.DISPATCH.equals(payload.getOp())) {
						if (excludedEvents != null) {
							if (excludedEvents.contains(payload.getType())) {
								return FilterReply.DENY;
							}
						} else if (includedEvents != null) {
							if (!includedEvents.contains(payload.getType())) {
								return FilterReply.DENY;
							}
						}
					}
				}
			}
		}
		return FilterReply.NEUTRAL;
	}

	public void setInclude(String include) {
		this.include = include;
	}

	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	@Override
	public void start() {
		if (exclude != null && exclude.trim().length() > 0) {
			excludedEvents = Arrays.asList(exclude.split("[;,]"));
			super.start();
		} else if (include != null && include.trim().length() > 0) {
			includedEvents = Arrays.asList(include.split("[;,]"));
			super.start();
		}
	}
}