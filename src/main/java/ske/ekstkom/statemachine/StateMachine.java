package ske.ekstkom.statemachine;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple StateMachine without sub-state machine, composite state, deferred signals, queues and no runtime engine
 * environment.
 */
public class StateMachine {

	// Record signals received for validation purpose.
	private final List<Signal> signalsReceived = new ArrayList<Signal>();
	private final List<Signal> signalsNotMatchedTransitions = new ArrayList<Signal>();

	private State activeState;
	protected String name;

	private StateMachine(String name) {
		this.name = name;
	}

	public void execute(Signal signal) {
		signalsReceived.add(signal);

		State nextState = activeState.execute(signal);

		if (null != nextState) {
			activeState = nextState;
		} else { // no state transition
			signalsNotMatchedTransitions.add(signal);
		}
	}

	public State getActiveState() {
		return activeState;
	}

	public List<Signal> getSignalsNotMatchedTransitions() {
		return signalsNotMatchedTransitions;
	}

	public int numberOfSignalsNotMatchedTransitions() {
		return signalsNotMatchedTransitions.size();
	}

	// -- Builder
	public static StateMachineBuilder named(String name) {
		return new StateMachineBuilder(name);
	}

	public static class StateMachineBuilder {
		StateMachine stateMachine;

		public StateMachineBuilder(String name) {
			this.stateMachine = new StateMachine(name);
		}

		public StateMachine build() {
			if (null == stateMachine.activeState) {
				throw new IllegalStateException("No init state!");
			}
			return stateMachine;
		}

		public StateMachineBuilder initState(State state) {
			stateMachine.activeState = state;
			return this;
		}
	}

}
