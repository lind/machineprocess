package ske.ekstkom.statemachine;

import java.util.ArrayList;
import java.util.List;

import ske.ekstkom.statemachine.Transition.TransitionBuilder;

public class SimpleState implements State {

	protected String name;
	// protected Action action; // TODO: Action just on transitions? Not needed in state.
	protected List<Transition> transitions = new ArrayList<Transition>();

	public SimpleState() {
	}

	public SimpleState(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ske.ekstkom.statemachine.Execution#execute(ske.ekstkom.statemachine.Signal)
	 */
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

	public void setName(String name) {
		this.name = name;
	}

	// -- Builder
	public static StateBuilder named(String name) {
		return new StateBuilder(name);
	}

	public static class StateBuilder {
		SimpleState state;
		TransitionBuilder<StateBuilder> transitionBuilder;

		protected StateBuilder(SimpleState state) {
			this.state = state;
		}

		public StateBuilder(String name) {
			this.state = new SimpleState(name);
		}

		public SimpleState build() {
			if (null != transitionBuilder) {
				state.transitions.add(transitionBuilder.build());
			}
			return state;
		}

		public TransitionBuilder<StateBuilder> transition(String name) {
			if (null != transitionBuilder) {
				state.transitions.add(transitionBuilder.build());
			}
			transitionBuilder = Transition.named(name, this);
			return transitionBuilder;
		}
	}

}
