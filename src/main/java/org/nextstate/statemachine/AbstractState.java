package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractState implements State {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Optional<Action> entry = Optional.empty();
    protected Optional<Action> exit = Optional.empty();

    protected final String name;
    protected final List<Transition> transitions = new ArrayList<>();

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

    @Override public void onEntry() {
        entry.ifPresent(Action::perform);
    }

    @Override public void onExit() {
        exit.ifPresent(Action::perform);
    }

    public Optional<State> stateTransition(Event event) {

        Optional<Transition> matchedTransition = transitions.stream().filter(t -> t.guard.test(event))
                .findFirst();

        log.debug("execute - {} event: {} ", name, event.getName(), (matchedTransition.isPresent() ?
                " transition to state: " + matchedTransition.get().getTargetState().getName() :
                " no transition match."));

        if (!matchedTransition.isPresent()) {
            log.debug("execute - No transition match event: {} in state: {}", event.getName(), name);
            return Optional.empty();
        }

        Transition transition = matchedTransition.get();
        transition.onTransition.ifPresent(Action::perform);
        return Optional.ofNullable(transition.getTargetState());
    }

    public boolean transitionToFinalState() {
        Optional<Transition> transition = transitions.stream().filter(
                t -> t.guard.test(new Event(FinalState.FINAL_EVENT))).findFirst();

        if (transition.isPresent()) {
            return (transition.get().getTargetState() instanceof FinalState);
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
}
