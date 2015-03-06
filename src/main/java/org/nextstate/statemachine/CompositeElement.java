package org.nextstate.statemachine;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils for composite elements as Composite State and State Machine.
 */
public class CompositeElement {
    private static Logger LOG = LoggerFactory.getLogger(CompositeElement.class);

    private CompositeElement() {
    }

    public static Optional<State> configure(ListIterator<String> configurationIterator, List<State> states) {
        String stateName = configurationIterator.next();
        LOG.debug("configure - {} ", stateName);

        Optional<State> state = states.stream().filter(s -> stateName.equals(s.getName())).findFirst();
        if (!state.isPresent()) {
            throw new IllegalStateException(
                    "No state named " + stateName + " exists. Add all states to the StateMachine before setting active state.");
        }
        State activeState = state.get();
        LOG.debug("configure - Active state: {}", activeState.getName());

        if (configurationIterator.hasNext()) {
            if (activeState.isCompositeState()) {

                activeState.activeStateConfiguration(configurationIterator);
            } else {
                String nextStateName = configurationIterator.next();
                LOG.warn("Current state is not Composite State but configuration element remains: {}",
                        nextStateName);
            }
        } else if (activeState.isCompositeState()) {
            // no more elements in configuration and current state is composite. Initial transition.
            activeState.entry();
        } else {
            // configuration ok
        }
        return Optional.ofNullable(activeState);
    }
}
