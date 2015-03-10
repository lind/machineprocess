package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composite State containing inner states.
 * <br>
 * The default transition from the Composite state after the {@link org.nextstate.statemachine.FinalState} is reached
 * is checked with a {@link org.nextstate.statemachine.FinalState#FINAL_EVENT} as a guard.
 */
public class CompositeState extends AbstractState implements CompositeElement {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private State activeState;
    private final List<State> states = new ArrayList<>();
    private Transition initialTransition;

    private CompositeState(String name) {
        super(name);
    }

    @Override public State getActiveState() {
        return activeState;
    }

    @Override public void activeStateConfiguration(ListIterator<String> configurationIterator) {

        Optional<State> state = configure(configurationIterator, states);
        if (state.isPresent()) {
            log.debug("activeStateConfiguration - active state: {}", state.get().getName());
            activeState = state.get();
        }
    }

    @Override public void entry() {
        log.debug("{} - entry. Setting active state: {}", name, initialTransition.getTargetState().getName());
        activeState = initialTransition.getTargetState();
        if (activeState instanceof CompositeElement) {
            activeState.entry();
        }
    }

    @Override public Optional<State> execute(Event event) {
        if (activeState == null) {
            throw new IllegalStateException("No internal active state in Composite State: " + name);
        }
        log.debug("execute - {} - activeState: {} - event: {}", name, activeState.getName(), event.getName());

        Optional<State> state = activeState.execute(event);

        // Check if new active state and execute exit on the old and entry on the new ...
        if (state.isPresent()) {
            activeState.exit();
            activeState = state.get();
            activeState.entry();
            log.debug("execute - new active state: {}", activeState.getName());

            // If next state is of type Final State then execute the transition on the Composite State with no guard.
            if (activeState.transitionToFinalState()) {
                log.debug("execute - Transition from {} to final state", activeState.getName());

                // Check final transition from the composite state...
                return stateTransition(new Event(FinalState.FINAL_EVENT));
            }
        }
        // Or check transition from composite state with the event.
        return stateTransition(event);
    }

    // --------------------- Builder ---------------------
    public static CompositeStateBuilder compositeState(String name) {
        return new CompositeStateBuilder(name);
    }

    public static class CompositeStateBuilder {
        final CompositeState compositeState;

        Transition.TransitionBuilder<CompositeStateBuilder> transitionBuilder;

        public CompositeStateBuilder(String name) {
            this.compositeState = new CompositeState(name);
        }

        public Transition.TransitionBuilder<CompositeStateBuilder> transition(String name) {
            // Add previous transition
            if (transitionBuilder != null) {
                compositeState.transitions.add(transitionBuilder.build());
            }
            transitionBuilder = Transition.transition(this, name);
            return transitionBuilder;
        }

        public CompositeStateBuilder internalStates(List<State> states) {
            compositeState.states.addAll(states);
            return this;
        }

        public CompositeStateBuilder initialTransition(Transition transition) {
            compositeState.initialTransition = transition;
            return this;
        }

        public State build() {
            // Add current transition on build
            if (transitionBuilder != null) {
                compositeState.transitions.add(transitionBuilder.build());
            }
            return compositeState;
        }
    }
}
