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

    private static final String TURNED_ON = "TurnedOn";
    private static final String A_SIMPLE_STATE = "ASimpleState";
    private static final String A_SIMPLE_STATE_2 = "ASimpleState2";
    private static final String FINAL_ACTION = "FinalAction";
    private static final String FINAL_STATE = "FinalState";
    private static final String INNER_STATE_2 = "InnerState2";
    private static final String INNER_STATE_1 = "InnerState1";
    private static final String INNER_STATE_2_2 = "InnerState2_2";
    private static final String INNER_STATE_2_1 = "InnerState2_1";
    private static final String COMPOSITE_STATE_1 = "CompositeState1";
    private static final String COMPOSITE_STATE_2 = "CompositeState2";
    private static final String TO_INNER_STATE_2_ACTION = "ToInnerState2Action";
    private static final String TO_INNER_STATE_2_EVENT = "ToInnerState2Event";
    private static final String TO_INNER_STATE_2_2_ACTION = "ToInnerState2_2Action";
    private static final String TO_INNER_STATE_2_2_EVENT = "ToInnerState2_2Event";
    private static final String A_SIMPLE_EVENT = "ASimpleEvent";
    private static final String A_SIMPLE_ACTION = "ASimpleAction";
    private static final String INITIALIZE = "Initialize";

    @Test
    public void no_active_state() {

        // Given
        StateMachine stateMachine = new NoActiveStateMachine();

        // When
        try {
            stateMachine.execute(new TurnedOn());

            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            // Then
            assertThat(e).hasMessage("No active state");
        }
    }

    @Test
    public void load_simple_state() {
        StateMachine stateMachine = new ASimpleStateMachine();

        stateMachine.execute(new ASimpleEvent());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(COMPOSITE_STATE_1, INNER_STATE_1);
    }

    @Test
    public void load_composite_state_and_inner_state() {
        StateMachine stateMachine = new ASimpleStateMachine();
        stateMachine.activeStateConfiguration(Arrays.asList(COMPOSITE_STATE_1, INNER_STATE_1));

        stateMachine.execute(new ToInnerState2());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(COMPOSITE_STATE_1, INNER_STATE_2);
    }

    @Test
    public void composite_first_to_inner_2() {
        StateMachine stateMachine = new CompositeFirstStateMachine();

        stateMachine.execute(new ToInnerState2());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(COMPOSITE_STATE_1, INNER_STATE_2);
    }

    @Test
    public void composite_to_simple_state() {
        StateMachine stateMachine = new CompositeFirstStateMachine();

        stateMachine.execute(new ASimpleEvent());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(A_SIMPLE_STATE);
    }

    @Test
    public void composite_to_fina() {
        StateMachine stateMachine = new ASimpleStateMachine();

        stateMachine.execute(new ASimpleEvent());
        stateMachine.execute(new ToInnerState2());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(A_SIMPLE_STATE_2);
    }

    // TODO
    @Test
    public void two_composite_states_to_inner_inner_2() {
        StateMachine stateMachine = new CompositeFirstStateMachine();

        stateMachine.execute(new ASimpleEvent());

        assertThat(stateMachine.getActiveStateConfiguration()).containsSequence(A_SIMPLE_STATE);
    }

    // ---- Helper classes for testing ----
    private class TurnedOn extends Event {
        public TurnedOn() {
            super(TURNED_ON);
        }
    }

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

            State compositeState1 = compositeState(COMPOSITE_STATE_1)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(aSimpleState2)
                    .initialTrasnition(singleTransition(INITIALIZE).to(innerState1))
                    .internalStates(Arrays.asList(innerState1, innerState2))
                    .build();
            State aSimpleState = state(A_SIMPLE_STATE)
                    .transition(A_SIMPLE_ACTION).guardedBy(e -> e.getName().equals(A_SIMPLE_EVENT))
                    .to(compositeState1)
                    .build();

            addStates(Arrays.asList(aSimpleState, compositeState1, finalState));
            activeState(aSimpleState);
        }
    }

    private class CompositeFirstStateMachine extends StateMachine {
        {
            State finalState = new FinalState(FINAL_STATE);
            State innerState2 = state(INNER_STATE_2).build();
            State innerState1 = state(INNER_STATE_1)
                    .transition(TO_INNER_STATE_2_ACTION).guardedBy(e -> e.getName().equals(TO_INNER_STATE_2_EVENT))
                    .to(innerState2)
                    .build();

            State aSimpleState = state(A_SIMPLE_STATE)
                    .build();
            State compositeState1 = compositeState(COMPOSITE_STATE_1)
                    .transition(A_SIMPLE_ACTION).guardedBy(e -> e.getName().equals(A_SIMPLE_EVENT))
                    .to(aSimpleState)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(finalState)
                    .initialTrasnition(singleTransition(INITIALIZE).to(innerState1))
                    .internalStates(Arrays.asList(innerState1, innerState2))
                    .build();

            addStates(Arrays.asList(aSimpleState, compositeState1, finalState));
            activeState(compositeState1);
        }
    }

    // 2 Composite states
    private class CompositeTimes2StateMachine extends StateMachine {
        {
            State inner_finalState = new FinalState(FINAL_STATE);
            State innerState2_2 = state(INNER_STATE_2_2).build();
            State innerState2_1 = state(INNER_STATE_2_1)
                    .transition(TO_INNER_STATE_2_2_ACTION).guardedBy(e -> e.getName().equals(TO_INNER_STATE_2_2_EVENT))
                    .to(innerState2_2)
                    .build();

            State compositeState2 = compositeState(COMPOSITE_STATE_2)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(inner_finalState)
                    .initialTrasnition(singleTransition(INITIALIZE).to(innerState2_1))
                    .internalStates(Arrays.asList(innerState2_1, innerState2_2))
                    .build();

            State finalState = new FinalState(FINAL_STATE);
            State innerState2 = state(INNER_STATE_2).build();
            State innerState1 = state(INNER_STATE_1)
                    .transition(TO_INNER_STATE_2_ACTION).guardedBy(e -> e.getName().equals(TO_INNER_STATE_2_EVENT))
                    .to(innerState2)
                    .build();
            State compositeState1 = compositeState(COMPOSITE_STATE_1)
                    .transition(FINAL_ACTION).guardedBy(e -> e.getName().equals(FINAL_EVENT))
                    .to(finalState)
                    .initialTrasnition(singleTransition(INITIALIZE).to(compositeState2))
                    .internalStates(Arrays.asList(innerState1, innerState2, compositeState2))
                    .build();
            State aSimpleState = state(A_SIMPLE_STATE)
                    .transition(A_SIMPLE_ACTION).guardedBy(e -> e.getName().equals(A_SIMPLE_EVENT))
                    .to(compositeState1)
                    .build();

            addStates(Arrays.asList(aSimpleState, compositeState1, finalState));
            activeState(aSimpleState);
        }
    }

}
