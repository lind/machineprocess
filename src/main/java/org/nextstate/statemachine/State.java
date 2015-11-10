package org.nextstate.statemachine;

public interface State {

    String getName();

    boolean transitionToFinalState();

    State execute(String event);

    void onEntry();

    void onExit();

    void toDot(StringBuilder sb);
}
