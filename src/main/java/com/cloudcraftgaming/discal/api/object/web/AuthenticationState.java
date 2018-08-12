package com.cloudcraftgaming.discal.api.object.web;

import com.cloudcraftgaming.discal.web.utils.ResponseUtils;

public class AuthenticationState {
	private final boolean success;

	private int status;

	private String reason;

	public AuthenticationState(boolean _success) {
		success = _success;
	}

	public boolean isSuccess() {
		return success;
	}

	public int getStatus() {
		return status;
	}

	public String getReason() {
		return reason;
	}

	public AuthenticationState setStatus(int _status) {
		status = _status;
		return this;
	}

	public AuthenticationState setReason(String _reason) {
		reason = _reason;
		return this;
	}

	public String toJson() {
		return ResponseUtils.getJsonResponseMessage(reason);
	}
}
