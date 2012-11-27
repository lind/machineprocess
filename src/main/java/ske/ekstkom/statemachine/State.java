package ske.ekstkom.statemachine;

import java.util.ArrayList;
import java.util.List;

import ske.ekstkom.statemachine.Transition.TransitionBuilder;

public class State {

	protected String name;
	// protected Action action; // TODO: Action just on transitions? Not needed in state.
	protected List<Transition> transitions = new ArrayList<Transition>();

	public State(String name) {
		this.name = name;
	}

	public State execute(Signal signal) {

		// Validate transitions not empty.
		// TODO illegal state of State. Throw ex or log warn.
		if (null == transitions || transitions.isEmpty()) {
			return null;
		}

		for (Transition transition : transitions) {
			if (transition.checkGuard(signal)) {

				// TODO: any post prosessing? Check that nextstate is not null? Needed?
				return transition.execute(signal);
			}
		}
		return null; // No matching transitions for this signal. TODO: Log info
	}

	public String getName() {
		return name;
	}

	// -- Builder
	public static StateBuilder named(String name) {
		return new StateBuilder(name);
	}

	public static class StateBuilder {
		State state;
		TransitionBuilder transitionBuilder;

		public StateBuilder(String name) {
			this.state = new State(name);
		}

		public State build() {
			if (null != transitionBuilder) {
				state.transitions.add(transitionBuilder.build());
			}
			return state;
		}

		public TransitionBuilder transition(String name) {
			if (null != transitionBuilder) {
				state.transitions.add(transitionBuilder.build());
			}
			transitionBuilder = Transition.named(name, this);
			return transitionBuilder;
		}
	}

}
