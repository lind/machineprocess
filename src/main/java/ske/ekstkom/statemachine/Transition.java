package ske.ekstkom.statemachine;

import ske.ekstkom.statemachine.State.StateBuilder;

public class Transition {

	protected final String name;
	protected Action action; // Action not required
	protected State targetState;
	protected Guard guard; // Guard not required (Automatic transition on any signal)

	public Transition(String name, Action action, Guard guard, State targetState) {
		this.name = name;
		this.action = action;
		this.targetState = targetState;
		this.guard = guard;
	}

	public Transition(String name) {
		this.name = name;
	}

	public boolean checkGuard(Signal signal) {
		return guard.check(signal);
	}

	public State execute(Signal signal) {
		if (null != action) {
			action.execute(signal);
		}
		return targetState;
	}

	// -- Builder
	public static TransitionBuilder named(String name, StateBuilder stateBuilder) {
		return new TransitionBuilder(name, stateBuilder);
	}

	public static class TransitionBuilder {
		Transition transition;
		StateBuilder parentBuilder;

		public TransitionBuilder(String name, StateBuilder stateBuilder) {
			this.transition = new Transition(name);
			this.parentBuilder = stateBuilder;
		}

		public Transition build() {
			if (null == transition.targetState) {
				throw new IllegalStateException("Missing target State!");
			}
			return transition;
		}

		public TransitionBuilder guardedBy(Guard by) {
			transition.guard = by;
			return this;
		}

		public TransitionBuilder withAction(Action action) {
			transition.action = action;
			return this;
		}

		public StateBuilder to(State state) {
			transition.targetState = state;
			return parentBuilder;
		}
	}
}
