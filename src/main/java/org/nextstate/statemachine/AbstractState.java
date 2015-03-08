package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractState implements State {
    private Logger log = LoggerFactory.getLogger(this.getClass());

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

    public Optional<State> stateTransition(Event event) {

        Optional<Transition> transition = transitions.stream().filter(t -> t.guard.test(event))
                .findFirst();

        log.debug("execute - {} event: {} ", name, event.getName(), (transition.isPresent() ?
                " transition to state: " + transition.get().getTargetState().getName() :
                " no transition match."));

        if (!transition.isPresent()) {
            log.debug("execute - No transition match event: {} in state: {}", event.getName(), name);
            return Optional.empty();
        }
        return Optional.ofNullable(transition.get().getTargetState());
    }

    public boolean transitionToFinalState() {
        Optional<Transition> transition = transitions.stream().filter(
                t -> t.guard.test(new Event(FinalState.FINAL_EVENT))).findFirst();

        if (transition.isPresent()) {
            if (transition.get().getTargetState() instanceof FinalState) {
                return true;
            }
        }
        return false;
    }

    @Override public void entry() {
    }

    @Override public void exit() {
    }

}
