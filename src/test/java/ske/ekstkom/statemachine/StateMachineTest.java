package ske.ekstkom.statemachine;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
		machine.addState(stateOne);
		machine.addState(stateTwo);
		machine.addState(stateThree);
		machine.addState(finalState);

		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Action.class, new ActionAdapter())
				.registerTypeAdapter(Guard.class, new GuardAdapter()).create();

		String machineAsJSON = gson.toJson(machine);
		System.out.println(machineAsJSON);

		StateMachine machine2 = gson.fromJson(machineAsJSON, StateMachine.class);

		machine2.execute(Signal.create("Sig1"));
		machine2.execute(Signal.create("Sig2"));
		machine2.execute(Signal.create("Sig3"));
		machine2.execute(Signal.create("Sig4"));

		// validate active state
		Assert.assertEquals("FinalState", machine2.getActiveState().getName());

		// validate actions occurred
		// TODO: not same log action after GSON ser/dez...
		// logAction.assertNumberOfExecute(2);

		// validate signals with no matching transitions
		Assert.assertEquals(2, machine2.numberOfSignalsNotMatchedTransitions());

		// (if there are signals not matching any guard/transition log warn)
	}
}
