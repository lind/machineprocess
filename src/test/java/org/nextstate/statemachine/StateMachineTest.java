package org.nextstate.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.nextstate.statemachine.CompositeState.compositeState;
import static org.nextstate.statemachine.FinalState.FINAL_EVENT;
import static org.nextstate.statemachine.SimpleState.state;
import static org.nextstate.statemachine.Transition.singleTransition;

import java.util.Arrays;

import org.junit.Test;

public class StateMachineTest {

    private static final String A_SIMPLE_STATE = "ASimpleState";
    private static final String A_SIMPLE_STATE_2 = "ASimpleState2";
    private static final String INNER_STATE_1 = "InnerState1";
    private static final String INNER_STATE_2 = "InnerState2";
    private static final String COMPOSITE_STATE = "CompositeState";
    private static final String FINAL_ACTION = "FinalAction";
    private static final String FINAL_STATE = "FinalState";
    private static final String TO_INNER_STATE_2_ACTION = "ToInnerState2Action";
    private static final String TO_INNER_STATE_2_EVENT = "ToInnerState2Event";
    private static final String A_SIMPLE_EVENT = "ASimpleEvent";
    private static final String A_SIMPLE_ACTION = "ASimpleAction";
    private static final String INITIALIZE = "Initialize";

    @Test
    public void no_active_state() {
        StateMachine stateMachine = new NoActiveStateMachine();

        try {
            stateMachine.execute(new ASimpleEvent());

            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("No active state");
        }
    }

    @Test
    public void load_simple_state() {
        StateMachine stateMachine = new ASimpleStateMachine();

        stateMachine.execute(new ASimpleEvent());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(COMPOSITE_STATE, INNER_STATE_1);
    }

    @Test
    public void load_composite_state_and_inner_state() {
        StateMachine stateMachine = new ASimpleStateMachine();
        stateMachine.activeStateConfiguration(Arrays.asList(COMPOSITE_STATE, INNER_STATE_1));

        stateMachine.execute(new ToInnerState2());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(A_SIMPLE_STATE_2);
    }

    @Test
    public void composite_to_fina() {
        StateMachine stateMachine = new ASimpleStateMachine();

        stateMachine.execute(new ASimpleEvent());
        stateMachine.execute(new ToInnerState2());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(A_SIMPLE_STATE_2);
    }

    // ---- Helper classes for testing ----
    private class NoActiveStateMachine extends StateMachine {
    }

    private class ToInnerState2 extends Event {
        public ToInnerState2() {
            super(TO_INNER_STATE_2_EVENT);
        }
    }

    private class ASimpleEvent extends Event {
        public ASimpleEvent() {
            super(A_SIMPLE_EVENT);
        }
    }

    // ASimpleState -> CompositeState {InnerState1 -> InnerState2 -> FinalState} -> ASimpleState2
    private class ASimpleStateMachine extends StateMachine {
        {
            State aSimpleState2 = state(A_SIMPLE_STATE_2).build();

            State finalState = new FinalState(FINAL_STATE);
            State innerState2 = state(INNER_STATE_2)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(finalState)
                    .build();
            State innerState1 = state(INNER_STATE_1)
                    .transition(TO_INNER_STATE_2_ACTION).guardedBy(e -> e.getName().equals(TO_INNER_STATE_2_EVENT))
                    .to(innerState2)
                    .build();

            State compositeState1 = compositeState(COMPOSITE_STATE)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(aSimpleState2)
                    .initialTransition(singleTransition(INITIALIZE).to(innerState1))
                    .internalStates(Arrays.asList(innerState1, innerState2))
                    .build();
            State aSimpleState = state(A_SIMPLE_STATE)
                    .transition(A_SIMPLE_ACTION).guardedBy(e -> e.getName().equals(A_SIMPLE_EVENT))
                    .to(compositeState1)
                    .build();

            addStates(Arrays.asList(aSimpleState, compositeState1, finalState));
            activeState(aSimpleState);
            validate();
        }
    }
}
