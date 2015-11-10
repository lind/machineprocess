package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleState implements State {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Action entry = null;
    protected Action exit = null;

    protected final String name;
    protected final List<Transition> transitions = new ArrayList<>();

    public SimpleState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addTransition(Transition transition) {
        this.transitions.add(transition);
    }

    public void addTransitions(List<Transition> transitionList) {
        this.transitions.addAll(transitionList);
    }

    @Override public void onEntry() {
        if (entry != null) {
            entry.perform();
        }
    }

    @Override public void onExit() {
        if (exit != null) {
            exit.perform();
        }
    }

    @Override public State execute(String event) {
        return stateTransition(event);
    }

    public State stateTransition(String event) {

        Transition matchedTransition = null;
        for (Transition t : transitions) {
            if (t.guardEvent == null || t.guardEvent.equals(event)) {
                matchedTransition = t;
            }
        }

        log.debug("stateTransition state:{} - event:{} {}", name, event, (matchedTransition != null ?
                " transition to state: " + matchedTransition.getTargetState().getName() :
                " no transition match."));

        if (matchedTransition != null) {
            if (matchedTransition.onTransition != null) {
                log.debug("stateTransition - Transition match event: {} in state: {}", event, name);
                matchedTransition.onTransition.perform();
            }
            return matchedTransition.getTargetState();
        }
        return null;
    }

    public boolean transitionToFinalState() {
        for (Transition t : transitions) {
            if (t.guardEvent.equals(FinalState.FINAL_EVENT)) {
                return t.getTargetState() instanceof FinalState;
            }
        }
        return false;
    }

    @Override public void toDot(StringBuilder sb) {

        for (Transition t : transitions) {
            sb.append(name.replaceAll("\\s+", "_"));
            sb.append(" -> ");
            sb.append(t.getTargetState().getName().replaceAll("\\s+", "_"));
            if (t.getName() != null) {
                sb.append(" [label=\"");
                sb.append(t.getName());
                sb.append("\"];");
            }
            sb.append(System.lineSeparator());
        }
    }

    // =================
    //      Builder
    // =================
    public static StateBuilder state(String name) {
        return new StateBuilder(name);
    }

    public static class StateBuilder {
        final SimpleState state;

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

        public StateBuilder onEntry(Action action) {
            state.entry = action;
            return this;
        }

        public StateBuilder onExit(Action action) {
            state.exit = action;
            return this;
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
