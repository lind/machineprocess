package org.nextstate.statemachine;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleState extends AbstractState implements State {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public SimpleState(String name) {
        super(name);
    }

    @Override public Optional<State> execute(Event event) {
        return stateTransition(event);
    }

    // --------------------- Builder ---------------------
    public static StateBuilder state(String name) {
        return new StateBuilder(name);
    }

    public static class StateBuilder {
        SimpleState state;

        Transition.TransitionBuilder<StateBuilder> transitionBuilder;

        public StateBuilder(String name) {
            this.state = new SimpleState(name);
        }

        public Transition.TransitionBuilder<StateBuilder> transition(String name) {
            // Add previous transition
            if (transitionBuilder != null) {
                state.addTransition(transitionBuilder.build());
            }
            transitionBuilder = Transition.transition(this, name);
            return transitionBuilder;
        }

        public SimpleState build() {
            // Add current transition on build
            if (transitionBuilder != null) {
                state.addTransition(transitionBuilder.build());
            }
            return state;
        }
    }

}
