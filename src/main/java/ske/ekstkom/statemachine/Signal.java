package ske.ekstkom.statemachine;

public class Signal {

	private final String event;

	public Signal(String event) {
		this.event = event;
	}

	public String getName() {
		return event;
	}

	public static Signal create(String event) {
		return new Signal(event);
	}

}
