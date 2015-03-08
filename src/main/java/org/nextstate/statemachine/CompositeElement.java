package org.nextstate.statemachine;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * Interface for composite elements as Composite State and State Machine.
 */
public interface CompositeElement {

    State getActiveState();

    default void activeStateConfiguration(ListIterator<String> configurationIterator) {}

    default Optional<State> configure(ListIterator<String> configurationIterator, List<State> states) {
        String stateName = configurationIterator.next();

        Optional<State> state = states.stream().filter(s -> stateName.equals(s.getName())).findFirst();
        if (!state.isPresent()) {
            throw new IllegalStateException(
                    "No state named " + stateName
                            + " exists. Add all states to the StateMachine before setting active state configuration.");
        }
        State activeState = state.get();

        if (configurationIterator.hasNext()) {
            if (activeState instanceof CompositeElement) {

                ((CompositeElement) activeState).activeStateConfiguration(configurationIterator);
            } else {
                String nextStateName = configurationIterator.next();
                throw new IllegalStateException(
                        "Current state is not Composite State but configuration element remains: " + nextStateName);
            }
        } else if (activeState instanceof CompositeElement) {
            // no more elements in configuration and current state is composite. Initial transition.
            activeState.entry();
        }
        return Optional.ofNullable(activeState);
    }
}
