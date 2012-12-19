package ske.ekstkom.statemachine;

import java.util.ArrayList;
import java.util.List;

import ske.ekstkom.statemachine.Transition.TransitionBuilder;

/**
 * Composite state in UML 2.0 <br>
 * Not implemented: completion transition
 */
public class CompositeState implements State {

	protected String name;
	// protected Action action; // TODO: Action just on transitions? Not needed in state.
	protected List<Transition> transitions = new ArrayList<Transition>();

	// Initial transition should execute at once the composite state becomes active.
	private Transition initialTransition;

	// Final transition should execute when the active state in the composite state reaches a final state.
	public Transition finalTransition;

	private State activeState;

	public CompositeState() {
	}

	public CompositeState(String name) {
		this.name = name;
	}

	public State execute(Signal signal, StateMachine stateMachine) {
		System.out.println("CompositeState.execute - active state: " + activeState.getName() + " signal: "
				+ signal.getName());

		State nextInternalState = activeState.execute(signal, stateMachine);

		if (null != nextInternalState) {
			activeState = nextInternalState;
			System.out.println("CompositeState.execute - nextInternalState " + nextInternalState.getName());

			if (nextInternalState instanceof FinalState) {
				System.out
						.println("CompositeState.execute - nextInternalState is a final state. Execute final transition.");
				return finalTransition.execute(signal, stateMachine);
			}

		} else { // no state transition. Check this state (the composite state) for transitions.
			System.out.println("CompositeState: no state transitions. Check this (the composite state).");

			for (Transition transition : transitions) {
				if (transition.checkGuard(signal)) {
					return transition.execute(signal, stateMachine);
				}
			}
		}

		// No state transition in the composite state. Just internal state transition.
		return null;
	}

	/**
	 * hook for entry behavior
	 */
	public void entry(Signal signal, StateMachine stateMachine) {
		activeState = initialTransition.execute(signal, stateMachine);

		System.out.println("CompositeState.entry - set active state: " + activeState.getName());
	}

	/**
	 * hook for exit behavior
	 */
	public void exit(Signal signal, StateMachine stateMachine) {
	}

	public State getActiveState() {
		return activeState;
	}

	public void setActiveState(State internalState) {
		this.activeState = internalState;
	}

	public String getName() {
		return name;
	}

	// -- Builder
	public static CompositeStateBuilder named(String name) {
		return new CompositeStateBuilder(name);
	}

	public static class CompositeStateBuilder {
		CompositeState compositeState;
		TransitionBuilder<CompositeStateBuilder> transitionBuilder;
		TransitionBuilder<CompositeStateBuilder> initialTransitionBuilder;
		TransitionBuilder<CompositeStateBuilder> finalTransitionBuilder;

		public CompositeStateBuilder(String name) {
			this.compositeState = new CompositeState(name);
		}

		public CompositeState build() {
			if (null != transitionBuilder) {
				compositeState.transitions.add(transitionBuilder.build());
			}
			if (null != initialTransitionBuilder) {
				compositeState.initialTransition = initialTransitionBuilder.build();
			} else {
				throw new IllegalStateException("Initial transition mandatory in CompositeState!");
			}
			if (null != finalTransitionBuilder) {
				compositeState.finalTransition = finalTransitionBuilder.build();
			}
			return compositeState;
		}

		public CompositeStateBuilder internalInitState(State state) {
			this.compositeState.activeState = state;
			return this;
		}

		public TransitionBuilder<CompositeStateBuilder> transition(String name) {
			if (null != transitionBuilder) {
				compositeState.transitions.add(transitionBuilder.build());
			}
			transitionBuilder = Transition.named(name, this);
			return transitionBuilder;
		}

		public TransitionBuilder<CompositeStateBuilder> initTransition(String name) {
			initialTransitionBuilder = Transition.named(name, this);

			return initialTransitionBuilder;
		}

		public TransitionBuilder<CompositeStateBuilder> finalTransition(String name) {
			finalTransitionBuilder = Transition.named(name, this);

			return finalTransitionBuilder;
		}
	}
}
