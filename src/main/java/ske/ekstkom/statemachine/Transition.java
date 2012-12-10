package ske.ekstkom.statemachine;

public class Transition {

	protected final String name;
	protected Action action; // Action not required
	protected State targetState;
	protected Guard guard; // Guard not required (Automatic transition on any signal)

	public Transition(String name, Action action, Guard guard, SimpleState targetState) {
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

	public State execute(Signal signal, boolean testScope) {
		if (null != action) {
			action.execute(signal, testScope);
		}
		return targetState;
	}

	// -- Builder
	public static <T> TransitionBuilder<T> named(String name, T parentBuilder) {
		return new TransitionBuilder<T>(name, parentBuilder);
	}

	public static class TransitionBuilder<T> {
		Transition transition;
		T parentBuilder;

		public TransitionBuilder(String name, T parentBuilder) {
			this.transition = new Transition(name);
			this.parentBuilder = parentBuilder;
		}

		public Transition build() {
			if (null == transition.targetState) {
				throw new IllegalStateException("Missing target State!");
			}
			return transition;
		}

		public TransitionBuilder<T> guardedBy(Guard guard) {
			transition.guard = guard;
			return this;
		}

		public TransitionBuilder<T> withAction(Action action) {
			transition.action = action;
			return this;
		}

		public T to(State state) {
			transition.targetState = state;
			return parentBuilder;
		}
	}
}
