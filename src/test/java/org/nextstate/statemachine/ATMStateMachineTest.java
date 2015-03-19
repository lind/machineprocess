package org.nextstate.statemachine;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.nextstate.statemachine.CompositeState.compositeState;
import static org.nextstate.statemachine.SimpleState.state;
import static org.nextstate.statemachine.Transition.singleTransition;
import static org.nextstate.statemachine.Transition.transitions;

import java.util.Arrays;

import org.junit.Test;

/**
 * Testing composite state.
 * <p>
 * See example used: http://www.uml-diagrams.org/bank-atm-uml-state-machine-diagram-example.html
 * <p>
 */
public class ATMStateMachineTest {

    // State name
    private static final String SELF_TEST = "SelfTest";
    private static final String OFF = "Off";
    private static final String IDLE = "Idle";
    private static final String SERVING_CUSTOMER = "ServingCustomer";
    private static final String AUTHENTICATION = "Authentication";
    private static final String SELECTING_TRANSACTION = "SelectingTransaction";
    private static final String TRANSACTION = "Transaction";
    private static final String FINAL = "Final";

    // Event name
    private static final String TURNED_ON = "TurnedOn";
    private static final String TESTED_OK = "TestedOk";
    private static final String TURNED_OFF = "TurnedOff";
    private static final String CARD_INSERTED = "CardInserted";
    private static final String CANCELED = "Canceled";
    private static final String AUTHENTICATED = "Authenticated";
    private static final String TRANSACTION_SELECTED = "TransactionSelected";

    // Actions (Command) - name of transitions
    private static final String TURN_ON = "TurnOn";
    private static final String TEST_OK = "TestOk";
    private static final String TURN_OFF = "TurnOff";
    private static final String CARD_INSERT = "CardInsert";
    private static final String CANCEL = "Cancel";
    private static final String AUTHENTICATE = "Authenticate";
    private static final String TRANSACTION_SELECT = "TransactionSelect";
    private static final String INITIALIZE = "Initialize";

    @Test
    public void simple_state_transfers() {
        // Given
        StateMachine atm = new ATMStateMachine();

        // When
        atm.execute(new TurnedOn());

        // Then
        assertThat(atm.getActiveStateName()).isEqualTo(SELF_TEST);
    }

    @Test
    public void from_off_through_idle_back_to_off() {
        StateMachine atm = new ATMStateMachine();

        atm.execute(new TurnedOn());
        atm.execute(new TestedOk());
        atm.execute(new TurnedOff());

        assertThat(atm.getActiveStateName()).isEqualTo(OFF);
    }

    @Test
    public void from_idle_to_authentication() {
        StateMachine atm = new ATMStateMachine();
        atm.activeStateConfiguration(asList(IDLE));

        atm.execute(new CardInserted());

        assertThat(atm.getActiveStateConfiguration()).containsSequence(SERVING_CUSTOMER, AUTHENTICATION);
    }

    @Test
    public void from_idle_to_idle_with_transaction() {
        StateMachine atm = new ATMStateMachine();
        atm.activeStateConfiguration(asList(IDLE));

        atm.execute(new CardInserted());
        atm.execute(new Authenticated());
        atm.execute(new TransactionSelected());

        assertThat(atm.getActiveStateName()).isEqualTo(IDLE);
    }

    @Test
    public void load_and_set_active_state_as_a_inner_state() {
        StateMachine atm = new ATMStateMachine();
        atm.activeStateConfiguration(Arrays.asList(SERVING_CUSTOMER, AUTHENTICATION));

        atm.execute(new Authenticated());

        assertThat(atm.getActiveStateConfiguration()).containsSequence(SERVING_CUSTOMER, SELECTING_TRANSACTION);
    }

    @Test
    public void to_dot() {
        StateMachine atm = new ATMStateMachine();

        System.out.println("DOT notation for ATM:");
        System.out.println(atm.toDot(false));

        // Force the composite state to active state for producing DOT notation of the composite state
        atm.activeStateConfiguration(Arrays.asList(SERVING_CUSTOMER, AUTHENTICATION));
        System.out.println("DOT notation for Composite state ServingCustomer:");
        System.out.println(((CompositeState) atm.getActiveState()).toDot(false));
    }

    //--------------------------------------------------
    private class ATMStateMachine extends StateMachine {
        {
            SimpleState off = state(OFF).build();
            State finalState = new FinalState(FINAL);
            SimpleState idle = state(IDLE).build();
            State transaction = state(TRANSACTION)
                    .transition(FINAL).guardedBy(e -> true)
                    .to(finalState)
                    .build();
            State selectingTransaction = state(SELECTING_TRANSACTION)
                    .transition(TRANSACTION_SELECT).guardedBy(e -> e.getName().equals(TRANSACTION_SELECTED))
                    .to(transaction)
                    .build();
            State authentication = state(AUTHENTICATION)
                    .transition(AUTHENTICATE).guardedBy(e -> e.getName().equals(AUTHENTICATED))
                    .to(selectingTransaction)
                    .build();
            State servingCustomerCompositeState = compositeState(SERVING_CUSTOMER)
                    .transition(CANCEL).guardedBy(e -> e.getName().equals(CANCELED))
                    .to(idle)
                    .transition(FINAL).guardedBy(e -> e.getName().equals(FinalState.FINAL_EVENT))
                    .to(idle)
                    .initialTransition(singleTransition(INITIALIZE).to(authentication))
                    .internalStates(Arrays.asList(authentication, selectingTransaction, transaction,
                            finalState))
                    .build();
            idle.addTransitions(transitions()
                    .transition(TURN_OFF).guardedBy(e -> e.getName().equals(TURNED_OFF))
                    .to(off)
                    .transition(CARD_INSERT).guardedBy(e -> e.getName().equals(CARD_INSERTED))
                    .to(servingCustomerCompositeState)
                    .build());
            State selfTest = state(SELF_TEST)
                    .transition(TEST_OK).guardedBy(e -> e.getName().equals(TESTED_OK))
                    .to(idle)
                    .build();
            off.addTransitions(transitions()
                    .transition(TURN_ON).guardedBy(e -> e.getName().equals(TURNED_ON))
                    .to(selfTest)
                    .build());

            addStates(asList(off, selfTest, idle, servingCustomerCompositeState));
            activeState(off);
            validate();
        }
    }

    private class Authenticated extends Event {
        public Authenticated() {
            super(AUTHENTICATED);
        }
    }

    private class TurnedOn extends Event {
        public TurnedOn() {
            super(TURNED_ON);
        }
    }

    private class TestedOk extends Event {
        public TestedOk() {
            super(TESTED_OK);
        }
    }

    private class TurnedOff extends Event {
        public TurnedOff() {
            super(TURNED_OFF);
        }
    }

    private class CardInserted extends Event {
        public CardInserted() {
            super(CARD_INSERTED);
        }
    }

    private class TransactionSelected extends Event {
        public TransactionSelected() {
            super(TRANSACTION_SELECTED);
        }
    }
}
