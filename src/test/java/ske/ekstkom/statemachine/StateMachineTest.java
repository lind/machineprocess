package ske.ekstkom.statemachine;

import org.junit.Assert;
import org.junit.Test;

public class StateMachineTest {

	@Test
	public void test() {
		LogAction logAction = new LogAction("Logging Action");

		State finalState = State.named("FinalState").build();

		// State with transition with guard and action.
		State stateTwo = State.named("StateTwo") //
				.transition("toFinal").guardedBy(NameGuard.by("Sig4")).withAction(logAction).to(finalState).build();

		// State with transition without guard and no action.
		State stateThree = State.named("StateThree") //
				.transition("toFinal").to(finalState).build();

		// State with two transitions
		State stateOne = State.named("StateOne")
				//
				.transition("toTwo").guardedBy(NameGuard.by("Sig2")).withAction(logAction).to(stateTwo)
				.transition("toThree").guardedBy(NameGuard.by("Sig3")).withAction(logAction).to(stateThree).build();

		StateMachine machine = StateMachine.named("TestMachine").initState(stateOne).build();

		machine.execute(Signal.create("Sig1"));
		machine.execute(Signal.create("Sig2"));
		machine.execute(Signal.create("Sig3"));
		machine.execute(Signal.create("Sig4"));

		// validate active state
		Assert.assertEquals("FinalState", machine.getActiveState().getName());

		// validate actions occurred
		logAction.assertNumberOfExecute(2);

		// validate signals with no matching transitions
		Assert.assertEquals(2, machine.numberOfSignalsNotMatchedTransitions());

		// (if there are signals not matching any guard/transition log warn)
	}
}
