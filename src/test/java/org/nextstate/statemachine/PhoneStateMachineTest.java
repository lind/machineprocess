package org.nextstate.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nextstate.statemachine.PhoneStateMachine.*;

import org.junit.Test;
import org.nextstate.statemachine.PhoneStateMachine.*;

/**
 * Tests using the state machine defined in {@link org.nextstate.statemachine.PhoneStateMachine}
 */
public class PhoneStateMachineTest {

    @Test
    public void one_transition() {
        // Given
        StateMachine phone = new PhoneStateMachine();

        // When
        phone.execute(new CallDialed());

        // Then
        assertThat(phone.getSimpleActiveStateConfiguration()).isEqualTo(RINGING);
    }

    @Test
    public void no_transition() {
        StateMachine phone = new PhoneStateMachine();
        phone.execute(new HungUp());
        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);
    }

    @Test
    public void to_state_phone_destroyed() {
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
    public void start_with_ringing_active_state() {
        StateMachine phone = new PhoneStateMachine();
        phone.activeStateConfiguration(RINGING);

        phone.execute(new CallConnected());

        assertThat(phone.getActiveStateName()).isEqualTo(CONNECTED);
    }

    @Test
    public void through_all_states_using_all_transitions() {
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
    public void transitions_from_on__hold_state() {
        StateMachine phone = new PhoneStateMachine();
        phone.activeStateConfiguration(ON_HOLD);

        phone.execute(new TookOffHold());
        assertThat(phone.getActiveStateName()).isEqualTo(CONNECTED);

        phone.activeStateConfiguration(ON_HOLD);
        phone.execute(new HungUp());

        assertThat(phone.getActiveStateName()).isEqualTo(OFF_HOOK);
    }

    @Test
    public void to_dot() {
        StateMachine phone = new PhoneStateMachine();

        System.out.println(phone.toDot(false));
    }

}
