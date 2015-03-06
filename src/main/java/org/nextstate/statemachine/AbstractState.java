package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public abstract class AbstractState implements State {
    protected String name;
    protected List<Transition> transitions = new ArrayList<>();

    public AbstractState(String name) {
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

    @Override public void activeStateConfiguration(ListIterator<String> configurationIterator) {}

    public boolean transitionToFinalState() {
        Optional<Transition> tran = transitions.stream().filter(
                transition -> transition.guard.test(new Event(FinalState.FINAL_EVENT))).findFirst();

        if (tran.isPresent()) {
            if (tran.get().getTargetState() instanceof FinalState) {
                return true;
            }
        }
        return false;
    }

    @Override public void entry() {}

    @Override public void exit() {}

}
