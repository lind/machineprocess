package org.nextstate.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nextstate.statemachine.Transition.transitions;

import java.util.Optional;

import org.junit.Test;

public class StateTest {
    public static final String CALL_DIAL = "CallDial";
    public static final String CALL_DIALED = "CallDialed";
    private static final String FINAL = "Final";

    @Test
    public void transitionMatch() {
        SimpleState from = new SimpleState("From");
        State to = new SimpleState("To");
        from.addTransitions(transitions()
                .transition(CALL_DIAL).guardedBy(event -> CALL_DIALED.equals(event.getName()))
                .to(to).build());

        Optional<State> target = from.execute(new CallDialed());

        assertThat(target.isPresent());
        assertThat(target.get().getName()).isEqualTo("To");
    }

    @Test
    public void transitionToFinalState() {
        SimpleState from = new SimpleState("From");
        State to = new FinalState("To");

        from.addTransitions(transitions()
                .transition(FINAL).guardedBy(e -> e.getName().equals(FinalState.FINAL_EVENT))
                .to(to)
                .build());

        assertThat(from.transitionToFinalState()).isTrue();
    }

    public static class CallDialed extends Event {
        public CallDialed() {
            super(CALL_DIALED);
        }
    }

}
