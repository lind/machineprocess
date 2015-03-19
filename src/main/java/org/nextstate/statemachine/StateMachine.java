package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StateMachine - Subclass and use the builder in the constructor. See the unit tests for examples.
 * <br>
 * To state should not have the same name. If to states have the same name it is not deterministic which one is chosen
 * when the active stave is loaded.
 */
public class StateMachine implements CompositeElement {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private State activeState;
    private final List<State> states = new ArrayList<>();

    protected void addStates(List<State> states) {
        this.states.addAll(states);
    }

    protected void activeState(State state) {
        this.activeState = state;
        this.activeState.onEntry();
    }

    @Override public String getName() {
        return this.getClass().getSimpleName();
    }

    public String getActiveStateName() {
        return activeState.getName();
    }

    @Override public List<State> getStates() {
        return states;
    }

    @Override public State getActiveState() {
        return activeState;
    }

    public void validate() {
        if (activeState == null) {
            throw new IllegalStateException("No active state");
        }
    }

    public List<String> getActiveStateConfiguration() {
        List<String> activeStateConfiguration = new ArrayList<>();

        State state = activeState;

        while (state instanceof CompositeElement) {
            activeStateConfiguration.add(state.getName());
            state = ((CompositeElement) state).getActiveState();
        }
        activeStateConfiguration.add(state.getName());

        log.debug("getActiveStateConfiguration - {}", activeStateConfiguration);
        return activeStateConfiguration;
    }

    public void activeStateConfiguration(List<String> activeStateConfiguration) {
        log.debug("activeStateConfiguration - active states: {} ", activeStateConfiguration);

        ListIterator<String> configurationIterator = activeStateConfiguration.listIterator();

        Optional<State> state = configureActiveState(configurationIterator, states);
        state.ifPresent(s -> {
            log.debug("activeStateConfiguration - active state: {}", s.getName());
            activeState = s;
        });
    }

    public void execute(Event event) {
        if (activeState == null) {
            throw new IllegalStateException("No active state");
        }
        log.debug("execute - activeState: " + activeState.getName() + " event: " + event.getClass().getSimpleName());

        Optional<State> state = activeState.execute(event);

        // Check if new active state and execute onExit on the old and onEntry on the new ...
        if (state.isPresent()) {
            activeState.onExit();
            activeState = state.get();
            activeState.onEntry();
            log.debug("execute - new active state: {}", activeState.getName());
        }
    }
}
