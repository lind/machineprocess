package ske.ekstkom.statemachine;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple StateMachine without sub-state machine, composite state, deferred signals, queues and no runtime engine
 * environment.
 */
public class StateMachine {

	// Record signals received for validation purpose.
	private List<Signal> signalsReceived;
	private List<Signal> signalsNotMatchedTransitions;

	private final List<State> states = new ArrayList<State>();

	private State activeState;
	protected String name;

	private StateMachine(String name) {
		this.name = name;
	}

	public void execute(Signal signal) {
		System.out.println("StateMachine.execute-activeState: " + activeState.getName() + " signal: "
				+ signal.getName());
		if (null == signalsReceived) {
			signalsReceived = new ArrayList<Signal>();
		}
		signalsReceived.add(signal);

		State nextState = activeState.execute(signal);

		if (null != nextState) {
			System.out.println("StateMachine.execute - new active state: " + nextState.getName());
			activeState = nextState;
		} else { // no state transition - there might have been a state transition in a composite state...
			System.out.println("StateMachine.execute - no transitions for signal: " + signal.getName());
			if (null == signalsNotMatchedTransitions) {
				signalsNotMatchedTransitions = new ArrayList<Signal>();
			}
			signalsNotMatchedTransitions.add(signal);
		}
	}

	public State getActiveState() {
		return activeState;
	}

	public void addState(State state) {
		states.add(state);
	}

	public void setActiveState(String stateName) {
		for (State state : states) {
			if (state.getName().equals(stateName)) {
				activeState = state;
				break;
			}
		}
	}

	public List<Signal> getSignalsNotMatchedTransitions() {
		return signalsNotMatchedTransitions;
	}

	public int numberOfSignalsNotMatchedTransitions() {
		return null == signalsNotMatchedTransitions ? 0 : signalsNotMatchedTransitions.size();
	}

	public void clear() {
		if (null != signalsReceived) {
			signalsReceived.clear();
		}
		if (null != signalsNotMatchedTransitions) {
			signalsNotMatchedTransitions.clear();
		}
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

		public StateMachineBuilder initState(SimpleState state) {
			stateMachine.activeState = state;
			return this;
		}
	}

}
