package ske.ekstkom.kanalvalg;

import ske.ekstkom.statemachine.Signal;

public class UtsendelseSignal extends Signal {

	private final String event;

	public UtsendelseSignal(String name, String event) {
		super(name);
		this.event = event;
	}

	public String getEvent() {
		return event;
	}
}
