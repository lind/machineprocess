package ske.ekstkom.kanalvalg;

import ske.ekstkom.statemachine.Guard;
import ske.ekstkom.statemachine.Signal;

public class UtsendelseGuard implements Guard {

	private String channel;
	private final String event;

	public UtsendelseGuard(String event) {
		this.event = event;
	}

	public boolean check(Signal signal) {
		if (signal instanceof UtsendelseSignal) {
			UtsendelseSignal utsendelseSignal = (UtsendelseSignal) signal;
			if (null == channel) {
				return event.equals(utsendelseSignal.getName());
			} else {
				return event.equals(utsendelseSignal.getName()) && channel.equals(utsendelseSignal.getChannel());
			}
			// return event.equals(utsendelseSignal.getName()) && null == channel ? true :
			// channel.equals(utsendelseSignal
			// .getChannel());
		}
		return false;
	}

	// -- Builder
	public static UtsendelseGuardBuilder guardedByEvent(String event) {
		return new UtsendelseGuardBuilder(event);
	}

	public static class UtsendelseGuardBuilder {
		UtsendelseGuard utsendelseGuard;

		public UtsendelseGuardBuilder(String event) {
			this.utsendelseGuard = new UtsendelseGuard(event);
		}

		public UtsendelseGuard build() {
			if (null == utsendelseGuard.event) {
				throw new IllegalStateException("Missing event!");
			}
			return utsendelseGuard;
		}

		public UtsendelseGuardBuilder withChannel(String channel) {
			utsendelseGuard.channel = channel;
			return this;
		}
	}

}
