package org.nextstate.statemachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Transition {
    private final String name;
    private State targetState;
    protected Predicate<Event> guard;
    protected Optional<Action> onTransition;

    public Transition(Predicate<Event> guard, State state, String name, Action onTransition) {
        if (state == null) {
            throw new IllegalStateException("Missing target State!");
        }
        if (guard == null) {
            throw new IllegalStateException("Transitions must have guards!");
        }
        this.guard = guard;
        this.targetState = state;
        this.name = name;
        this.onTransition = Optional.ofNullable(onTransition);
    }

    public State getTargetState() {
        return targetState;
    }

    public String getName() {
        return name;
    }

    // =================
    //      Builder
    // =================
    public static <T> TransitionBuilder<T> transition(T parentBuilder, String name) {
        return new TransitionBuilder<>(parentBuilder, name);
    }

    /**
     * Build transition returning the parent builder.
     *
     * @param <T> The type of the parent builder
     */
    public static class TransitionBuilder<T> {
        private final String name;
        final T parentBuilder;
        Predicate<Event> guard;
        private State state;
        private Action onTransition;

        public TransitionBuilder(T parentBuilder, String name) {
            this.parentBuilder = parentBuilder;
            this.name = name;
        }

        public TransitionBuilder<T> guardedBy(Predicate<Event> guard) {
            this.guard = guard;
            return this;
        }

        public TransitionBuilder<T> onTransition(Action action) {
            this.onTransition = action;
            return this;
        }

        public T to(State state) {
            this.state = state;
            return parentBuilder;
        }

        public Transition build() {
            return new Transition(guard, state, name, onTransition);
        }
    }

    public static TransitionsBuilder transitions() {
        return new TransitionsBuilder();
    }

    /**
     * Build and return a list of transitions.
     */
    public static class TransitionsBuilder {

        final List<Transition> transitions = new ArrayList<>();
        Transition.TransitionBuilder<TransitionsBuilder> transitionBuilder;

        public Transition.TransitionBuilder<TransitionsBuilder> transition(String name) {
            // Add previous transition
            if (transitionBuilder != null) {
                transitions.add(transitionBuilder.build());
            }
            transitionBuilder = Transition.transition(this, name);
            return transitionBuilder;
        }

        public List<Transition> build() {
            // Add current transition on build
            if (transitionBuilder != null) {
                transitions.add(transitionBuilder.build());
            }
            return transitions;
        }

    }

    public static TransBuilder singleTransition(String name) {
        return new TransBuilder(name);
    }

    public static class TransBuilder {
        final String name;
        Transition.TransitionBuilder<TransBuilder> transitionBuilder;

        public TransBuilder(String name) {
            this.name = name;
        }

        public Transition to(State state) {
            //            return
            transitionBuilder = Transition.transition(this, name);
            transitionBuilder.to(state);
            transitionBuilder.guardedBy(e -> true);
            return transitionBuilder.build();
        }
    }
}
