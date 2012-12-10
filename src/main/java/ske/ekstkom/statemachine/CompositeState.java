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

	// private State initialState; // needed? In UML 2.0 the initial state should execute at once...
	private State activeState;

	public CompositeState() {
	}

	public CompositeState(String name) {
		super(); // name);
	}

	public State execute(Signal signal, boolean testScope) {
		System.out.println("CompositeState.execute - start");

		State nextInternalState = activeState.execute(signal, testScope);

		if (null != nextInternalState) {
			activeState = nextInternalState;
			System.out.println("CompositeState: nextInternalState " + nextInternalState);

			// is final state needed? The transition from the composite state can be modeled with signals not matching
			// any transition in the composite states.
			// if (activeState instanceof FinalState) {
			//
			// }
		} else { // no state transition. Check this state (the composite state) for transitions.
			System.out.println("CompositeState: no state transitions. Check this (the composite state).");

			for (Transition transition : transitions) {
				if (transition.checkGuard(signal)) {

					// TODO: any post processing? Check that nextstate is not null? Needed?
					return transition.execute(signal, testScope);
				}
			}
		}

		// No state transition in the composite state. Just internal state transition.
		return null;
	}

	public String getName() {
		return name;
	}

	// -- Builder
	public static CompositeStateBuilder cnamed(String name) {
		return new CompositeStateBuilder(name);
	}

	public static class CompositeStateBuilder {
		CompositeState compositeState;
		TransitionBuilder<CompositeStateBuilder> transitionBuilder;

		public CompositeStateBuilder(String name) {
			// super(CompositeState.class, name);
			this.compositeState = new CompositeState(name);
		}

		public CompositeState build() {
			if (null != transitionBuilder) {
				compositeState.transitions.add(transitionBuilder.build());
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
	}
}
