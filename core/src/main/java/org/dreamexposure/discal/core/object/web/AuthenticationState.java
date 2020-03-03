package org.dreamexposure.discal.core.object.web;

public class AuthenticationState {
	private final boolean success;

	private int status;

	private String reason;

	private boolean fromDiscalNetwork;

	private boolean readOnly;

	private String keyUsed;

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

	public String getKeyUsed() {
		return keyUsed;
	}

	public boolean isFromDiscalNetwork() {
		return fromDiscalNetwork;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public AuthenticationState setStatus(int _status) {
		status = _status;
		return this;
	}

	public AuthenticationState setReason(String _reason) {
		reason = _reason;
		return this;
	}

	public AuthenticationState setKeyUsed(String _key) {
		keyUsed = _key;
		return this;
	}

	public AuthenticationState setFromDisCalNetwork(boolean _fromDisCal) {
		fromDiscalNetwork = _fromDisCal;
		return this;
	}

	public AuthenticationState setIsReadOnly(boolean _readOnly) {
		readOnly = _readOnly;
		return this;
	}

	public String toJson() {
		return "{\"Message\": \"" + reason + "\"}";
	}
}
