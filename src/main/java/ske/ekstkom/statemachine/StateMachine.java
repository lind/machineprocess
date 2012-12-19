package ske.ekstkom.statemachine;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple StateMachine without sub-state machine, deferred signals, queues and no runtime engine environment.
 */
public class StateMachine {

	// Record signals received for validation purpose.
	private List<Signal> signalsReceived;
	private List<Signal> signalsNotMatchedTransitions;
	private List<Action> actionsExecuted;

	private final List<State> states = new ArrayList<State>();

	private State activeState;
	protected String name;

	private StateMachine(String name) {
		this.name = name;
	}

	public void execute(Signal signal) {
		System.out.println();
		System.out.println("StateMachine.execute() - activeState: " + activeState.getName() + " - signal: "
				+ signal.getName());
		if (null == signalsReceived) {
			signalsReceived = new ArrayList<Signal>();
		}
		signalsReceived.add(signal);

		State nextState = activeState.execute(signal, this);

		if (null != nextState) {
			System.out.println("StateMachine.execute - new active state: " + nextState.getName());
			// exit behavior in the old active state and entry behavior in the new state
			activeState.exit(signal, this);
			nextState.entry(signal, this);
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

	/**
	 * Set the active state configuration for the state machine.
	 */
	public void setActiveState(String stateName, String internalStateName) {
		State newActiveState = null;
		for (State state : states) {
			if (state.getName().equals(stateName)) {
				newActiveState = state;
				if (state instanceof CompositeState) {
					CompositeState compositeState = (CompositeState) state;
					for (State internalState : states) {
						if (internalState.getName().equals(internalStateName)) {
							compositeState.setActiveState(internalState);
							newActiveState = compositeState;
							break;
						}
					}
				}
				break;
			}
		}
		this.activeState = newActiveState;
	}

	public void setActiveState(String stateName) {
		for (State state : states) {
			if (state.getName().equals(stateName)) {
				activeState = state;
				break;
			}
		}
	}

	public void addActionExecuted(Action action) {
		if (null == actionsExecuted) {
			actionsExecuted = new ArrayList<Action>();
		}
		actionsExecuted.add(action);
	}

	public List<Action> getActionsExecuted() {
		return actionsExecuted;
	}

	public int numberOfActionsExecuted() {
		return null == actionsExecuted ? 0 : actionsExecuted.size();
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
		if (null != actionsExecuted) {
			actionsExecuted.clear();
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

		public StateMachineBuilder initState(State state) {
			stateMachine.activeState = state;
			return this;
		}
	}

}
