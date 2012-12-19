package ske.ekstkom.kanalvalg;

import ske.ekstkom.statemachine.Signal;

public class UtsendelseSignal extends Signal {

	private final String channel;

	public UtsendelseSignal(String event, String channel) {
		super(event);
		this.channel = channel;
	}

	public String getChannel() {
		return channel;
	}

	public static UtsendelseSignal create(String event, String channel) {
		return new UtsendelseSignal(event, channel);
	}
}
