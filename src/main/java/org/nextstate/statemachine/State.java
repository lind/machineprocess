package org.nextstate.statemachine;

import java.util.ListIterator;
import java.util.Optional;

public interface State {

    String getName();

    boolean transitionToFinalState();

    Optional<State> execute(Event event);

    /**
     * hook for entry behavior
     */
    void entry();

    /**
     * hook for exit behavior
     */
    void exit();

    boolean isCompositeState();

    State getActiveState();

    void activeStateConfiguration(ListIterator<String> configurationIterator);
}