package org.nextstate.statemachine;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.nextstate.statemachine.PhoneStateMachine.*;

import org.junit.Test;
import org.nextstate.statemachine.PhoneStateMachine.*;

/**
 * Tests using the state machine defined in {@link org.nextstate.statemachine.PhoneStateMachine}
 */
public class PhoneStateMachineTest {

    @Test
    public void oneTransition() {
        // Given
        StateMachine phone = new PhoneStateMachine();

        // When
        phone.execute(new CallDialed());

        // Then
        assertThat(phone.getActiveStateName()).isEqualTo(RINGING);
    }

    @Test
    public void noTransition() {
        StateMachine phone = new PhoneStateMachine();
        phone.execute(new HungUp());
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);
    }

    @Test
    public void toStatePhoneDestroyed() {
        StateMachine phone = new PhoneStateMachine();

        phone.execute(new CallDialed());
        phone.execute(new CallConnected());
        phone.execute(new PlacedOnHold());
        phone.execute(new PhoneHurledAgainstWall());

        assertThat(phone.getActiveStateName()).isEqualTo(PHONE_DESTROYED);
    }

    @Test
    public void transition_to_same_state() {
        StateMachine phone = new PhoneStateMachine();

        phone.execute(new CallDialed());
        phone.execute(new HungUp());

        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);
    }

    @Test
    public void startWithRingingActiveState() {
        StateMachine phone = new PhoneStateMachine();
        phone.activeStateConfiguration(asList(RINGING));

        phone.execute(new CallConnected());

        assertThat(phone.getActiveStateName()).isEqualTo(CONNECTED);
    }

    @Test
    public void throughAllStatesUsingAllTransitions() {
        StateMachine phone = new PhoneStateMachine();

        phone.execute(new CallDialed());
        phone.execute(new HungUp());
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);

        phone.execute(new CallDialed());
        phone.execute(new CallConnected());
        phone.execute(new HungUp());
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);

        phone.execute(new CallDialed());
        phone.execute(new CallConnected());
        phone.execute(new MessageLeft());
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);

        phone.execute(new CallDialed());
        phone.execute(new CallConnected());
        phone.execute(new HungUp());
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);

        phone.execute(new CallDialed());
        phone.execute(new CallConnected());
        phone.execute(new PlacedOnHold());
        phone.execute(new PhoneHurledAgainstWall());

        assertThat(phone.getActiveStateName()).isEqualTo(PHONE_DESTROYED);
    }

    @Test
    public void transitionsFromOnHoldState() {
        StateMachine phone = new PhoneStateMachine();
        phone.activeStateConfiguration(asList(ON_HOLD));

        phone.execute(new TookOffHold());
        assertThat(phone.getActiveStateName()).isEqualTo(CONNECTED);

        phone.activeStateConfiguration(asList(ON_HOLD));
        phone.execute(new HungUp());

        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);
    }

}
