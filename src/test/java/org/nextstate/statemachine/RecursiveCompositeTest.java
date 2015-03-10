package org.nextstate.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nextstate.statemachine.CompositeState.compositeState;
import static org.nextstate.statemachine.FinalState.FINAL_EVENT;
import static org.nextstate.statemachine.SimpleState.state;
import static org.nextstate.statemachine.Transition.singleTransition;

import java.util.Arrays;

import org.junit.Test;

public class RecursiveCompositeTest {

    private static final String A_SIMPLE_STATE = "ASimpleState";
    private static final String A_SIMPLE_STATE_2 = "ASimpleState2";
    private static final String FINAL_ACTION = "FinalAction";
    private static final String FINAL_STATE = "FinalState";
    private static final String INNER_FINAL_STATE = "InnerFinalState";
    private static final String INNER_STATE_1 = "InnerState1";
    private static final String INNER_STATE_2_2 = "InnerState2_2";
    private static final String INNER_STATE_2_1 = "InnerState2_1";
    private static final String COMPOSITE_STATE_1 = "CompositeState1";
    private static final String COMPOSITE_STATE_2 = "CompositeState2";
    private static final String TO_INNER_STATE_2_2_ACTION = "ToInnerState2_2Action";
    private static final String TO_INNER_STATE_2_2_EVENT = "ToInnerState2_2Event";
    private static final String A_SIMPLE_EVENT = "ASimpleEvent";
    private static final String A_SIMPLE_ACTION = "ASimpleAction";
    private static final String INITIALIZE = "Initialize";

    @Test
    public void load_and_verify_active_state() {
        StateMachine stateMachine = new CompositeTimes2StateMachine();

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(A_SIMPLE_STATE);
    }

    @Test
    public void to_composite_state() {
        StateMachine stateMachine = new CompositeTimes2StateMachine();

        stateMachine.execute(new ASimpleEvent());

        assertThat(stateMachine.getActiveStateConfiguration())
                .containsSequence(COMPOSITE_STATE_1, COMPOSITE_STATE_2, INNER_STATE_2_1);
    }

    @Test
    public void inner_inner_transition() {
        StateMachine stateMachine = new CompositeTimes2StateMachine();

        stateMachine.execute(new ASimpleEvent());
        stateMachine.execute(new ToInnerState22());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(A_SIMPLE_STATE_2);
    }

    // ---- Helper classes for testing ----
    private class ToInnerState22 extends Event {
        public ToInnerState22() {
            super(TO_INNER_STATE_2_2_EVENT);
        }
    }

    private class ASimpleEvent extends Event {
        public ASimpleEvent() {
            super(A_SIMPLE_EVENT);
        }
    }

    //  ASimpleState -> CompositeState1 { CompositeState2 { InnerState2_1 -> InnerState2_2 -> InnerFinalState } -> InnerState1 -> FinalState } -> ASimpleState2
    private class CompositeTimes2StateMachine extends StateMachine {
        {
            State aSimpleState2 = state(A_SIMPLE_STATE_2)
                    .build();

            State finalState = new FinalState(FINAL_STATE);
            State innerState1 = state(INNER_STATE_1)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(finalState)
                    .build();

            State innerFinalState = new FinalState(INNER_FINAL_STATE);
            State innerState2_2 = state(INNER_STATE_2_2)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(innerFinalState)
                    .build();
            State innerState2_1 = state(INNER_STATE_2_1)
                    .transition(TO_INNER_STATE_2_2_ACTION).guardedBy(e -> e.getName().equals(TO_INNER_STATE_2_2_EVENT))
                    .to(innerState2_2)
                    .build();

            State compositeState2 = compositeState(COMPOSITE_STATE_2)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(innerState1)
                    .initialTransition(singleTransition(INITIALIZE).to(innerState2_1))
                    .internalStates(Arrays.asList(innerState2_1, innerState2_2, innerFinalState))
                    .build();

            State compositeState1 = compositeState(COMPOSITE_STATE_1)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(aSimpleState2)
                    .initialTransition(singleTransition(INITIALIZE).to(compositeState2))
                    .internalStates(Arrays.asList(innerState1, innerState1, compositeState2))
                    .build();
            State aSimpleState = state(A_SIMPLE_STATE)
                    .transition(A_SIMPLE_ACTION).guardedBy(e -> e.getName().equals(A_SIMPLE_EVENT))
                    .to(compositeState1)
                    .build();

            addStates(Arrays.asList(aSimpleState, compositeState1, aSimpleState2));
            activeState(aSimpleState);
        }
    }

}
