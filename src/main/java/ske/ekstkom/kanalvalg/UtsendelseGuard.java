package ske.ekstkom.kanalvalg;

import ske.ekstkom.statemachine.Guard;
import ske.ekstkom.statemachine.NameGuard;
import ske.ekstkom.statemachine.Signal;

public class UtsendelseGuard implements Guard {

	private final String event;

	public UtsendelseGuard(String event) {
		this.event = event;
	}

	public boolean check(Signal signal) {
		return event.equals(signal.getName());
	}

	public static NameGuard by(String name) {
		return new NameGuard(name);
	}

}
