package org.nextstate.statemachine;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * Interface for composite elements as Composite State and State Machine.
 */
public interface CompositeElement {

    List<State> getStates();

    State getActiveState();

    String getName();

    default void activeStateConfiguration(ListIterator<String> configurationIterator) {
    }

    default Optional<State> configureActiveState(ListIterator<String> configurationIterator, List<State> states) {
        String stateName = configurationIterator.next();

        Optional<State> state = states.stream().filter(s -> stateName.equals(s.getName())).findFirst();
        state.orElseThrow(() -> new IllegalStateException("No state named " + stateName
                + " exists. Add all states to the StateMachine before setting active state configuration."));

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
            activeState.onEntry();
        }
        return Optional.ofNullable(activeState);
    }

    /**
     * DOT graph description language for the State Machine.
     * Composite states is filled with grey but internal states is not included. Should be possible at least for one
     * level with DOT attrs compound:
     * See: http://www.graphviz.org/content/attrs#dcompound and
     * http://stackoverflow.com/questions/2012036/graphviz-how-to-connect-subgraphs
     *
     * @return sting describing the state machine graph
     */
    default String toDot(boolean showActiveState) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph ");
        sb.append(getName());
        sb.append(" { ");
        sb.append(System.lineSeparator());
        sb.append(getActiveState().getName());
        sb.append("[label=\"");
        sb.append(getActiveState().getName());
        sb.append("\"");
        if (showActiveState) {
            sb.append(", style=filled, fillcolor=lightblue");
        }
        sb.append("];");
        sb.append(System.lineSeparator());

        getActiveState().toDot(sb);

        getStates().stream().filter(state -> state != getActiveState()).forEach(state -> {
            sb.append(state.getName());
            sb.append("[label=\"");
            sb.append(state.getName());
            sb.append("\"");
            if (state instanceof CompositeElement) {
                sb.append(", style=filled, fillcolor=lightgrey");
            }
            sb.append("];");
            sb.append(System.lineSeparator());
            state.toDot(sb);
        });
        sb.append("} ");
        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
