package org.nextstate.statemachine;

import static org.nextstate.statemachine.SimpleState.state;
import static org.nextstate.statemachine.Transition.transitions;

import java.util.Arrays;

/**
 * StateMachine for a phone inspired by: http://simplestatemachine.codeplex.com/
 * <p>
 * States and transitions:
 * <p>
 * state @OffHook:
 * when @CallDial  >> @Ringing
 * <p>
 * state @Ringing:
 * when @HangUp  >> @OffHook
 * when @ConnectCall  >> @Connected
 * <p>
 * state @Connected:
 * when @LeaveMessage  >> @OffHook
 * when @HangUp   >> @OffHook
 * when @PlaceOnHold  >> @OnHold
 * <p>
 * state @OnHold:
 * when @TakeOffHold >> @Connected
 * when @HangUp  >> @OffHook
 * when @HurlPhone >> @PhoneDestroyed
 * state @PhoneDestroyed
 * <p>
 * Events:
 * <p>
 * CallDial trigger @CallDialed
 * HangUp trigger @HungUp
 * ConnectCall trigger @CallConnected
 * LeaveMessage trigger @MessageLeft
 * PlaceOnHold trigger @PlacedOnHold: on_event @PlayMuzak
 * TakeOffHold trigger @TakenOffHold: on_event @StopMuzak
 * HurlPhone trigger @PhoneHurledAgainstWall
 */
class PhoneStateMachine extends StateMachine {
    // State names
    public static final String OFF_HOOK = "OffHook";
    public static final String RINGING = "Ringing";
    public static final String CONNECTED = "Connected";
    public static final String ON_HOLD = "OnHold";
    public static final String PHONE_DESTROYED = "PhoneDestroyed";

    // Event names
    private static final String CALL_DIALED = "CallDialed";
    private static final String HUNG_UP = "HungUp";
    private static final String CALL_CONNECTED = "CallConnected";
    private static final String MESSAGE_LEFT = "MessageLeft";
    private static final String PLACED_ON_HOLD = "PlacedOnHold";
    private static final String TOOK_OFF_HOLD = "TookOffHold";
    private static final String PHONE_HURLED_AGAINST_WALL = "PhoneHurledAgainstWall";

    // Actions (Command) - name of transitions
    private static final String CALL_DIAL = "CallDial";
    private static final String HANG_UP = "HangUp";
    private static final String CONNECT_CALL = "ConnectCall";
    private static final String LEAVE_MESSAGE = "LeaveMessage";
    private static final String PLACE_ON_HOLD = "PlaceOnHold";
    private static final String TAKE_OFF_HOLD = "TakeOffHold";
    private static final String HURL_PHONE = "HurlPhone";

    {
        SimpleState offHook = state(OFF_HOOK).build();
        State phoneDestroyed = state(PHONE_DESTROYED).build();
        SimpleState connected = state(CONNECTED).build();
        State onHold = state(ON_HOLD)
                .transition(HURL_PHONE).guardedBy(event -> PHONE_HURLED_AGAINST_WALL.equals(event.getName()))
                .to(phoneDestroyed)
                .transition(HANG_UP).guardedBy(event -> HUNG_UP.equals(event.getName()))
                .to(offHook)
                .transition(TAKE_OFF_HOLD).guardedBy(event -> TOOK_OFF_HOLD.equals(event.getName()))
                .to(connected)
                .build();
        connected.addTransitions(transitions()
                .transition(PLACE_ON_HOLD).guardedBy(event -> event.getName().equals(PLACED_ON_HOLD))
                .to(onHold)
                .transition(HANG_UP).guardedBy(event -> event.getName().equals(HUNG_UP))
                .to(offHook)
                .transition(LEAVE_MESSAGE).guardedBy(event -> event.getName().equals(MESSAGE_LEFT))
                .to(offHook)
                .build());
        State ringing = state(RINGING)
                .transition(CONNECT_CALL).guardedBy(event -> event.getName().equals(CALL_CONNECTED))
                .to(connected)
                .transition(HANG_UP).guardedBy(event -> event.getName().equals(HUNG_UP))
                .to(offHook)
                .build();
        offHook.addTransitions(transitions()
                .transition(CALL_DIAL).guardedBy(event -> event.getName().equals(CALL_DIALED))
                .to(ringing).build());
        addStates(Arrays.asList(offHook, phoneDestroyed, onHold, connected, ringing));
        activeState(offHook);
        validate();
    }

    public static class CallDialed extends Event {
        public CallDialed() {
            super(CALL_DIALED);
        }
    }

    public static class HungUp extends Event {
        public HungUp() {
            super(HUNG_UP);
        }
    }

    public static class CallConnected extends Event {
        public CallConnected() {
            super(CALL_CONNECTED);
        }
    }

    public static class MessageLeft extends Event {
        public MessageLeft() {
            super(MESSAGE_LEFT);
        }
    }

    public static class PlacedOnHold extends Event {
        public PlacedOnHold() {
            super(PLACED_ON_HOLD);
        }
    }

    public static class TookOffHold extends Event {
        public TookOffHold() {
            super(TOOK_OFF_HOLD);
        }
    }

    public static class PhoneHurledAgainstWall extends Event {
        public PhoneHurledAgainstWall() {
            super(PHONE_HURLED_AGAINST_WALL);
        }
    }
}
