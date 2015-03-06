package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeState extends AbstractState {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private State activeState;
    private List<State> states = new ArrayList<>();
    private Transition initialTransition;

    public CompositeState(String name) {
        super(name);
    }

    @Override public State getActiveState() {
        return activeState;
    }

    @Override public void activeStateConfiguration(ListIterator<String> configurationIterator) {

        Optional<State> state = CompositeElement.configure(configurationIterator, states);
        if (state.isPresent()) {
            activeState = state.get();
        }
    }

    @Override public void entry() {
        log.debug("{} - entry. Setting active state: {}", name, initialTransition.getTargetState().getName());
        activeState = initialTransition.getTargetState();
    }

    @Override public boolean isCompositeState() {
        return true;
    }

    @Override public Optional<State> execute(Event event) {
        if (activeState == null) {
            throw new IllegalStateException("No internal active state in Composite State: " + name);
        }
        log.debug("execute - {} - activeState: {} event: {}", name, activeState.getName(), event.getName());

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

                Optional<Transition> tran = transitions.stream().filter(
                        transition -> transition.guard.test(new Event(FinalState.FINAL_EVENT))).findFirst();

                if (!tran.isPresent()) {
                    log.warn("execute - No Transition without guards exists in {} state!", name);
                    // Final state and no transitions in Composite state without guards... Exception?
                    return Optional.empty();
                }
                State targetState = tran.get().getTargetState();
                log.debug("execute - Target state: {}", targetState.getName());
                return Optional.ofNullable(targetState);
            }
        }

        // Transitions from Composite State
        Optional<Transition> matchedTransition = transitions.stream().filter(transition -> transition.guard.test(event)).findFirst();

        log.debug("execute - {} event: {} ", name, event.getName(), (matchedTransition.isPresent() ?
                " transition to state: " + matchedTransition.get().getTargetState().getName() :
                " no transition match."));

        if (!matchedTransition.isPresent()) {
            log.debug("execute - No transition match event: {} in state: {}", event.getName(), name);
            return Optional.empty();
        }
        return Optional.ofNullable(matchedTransition.get().getTargetState());
    }

    // --------------------- Builder ---------------------
    public static CompositeStateBuilder compositeState(String name) {
        return new CompositeStateBuilder(name);
    }

    public static class CompositeStateBuilder {
        CompositeState compositeState;

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

        public CompositeStateBuilder initialTrasnition(Transition transition) {
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
