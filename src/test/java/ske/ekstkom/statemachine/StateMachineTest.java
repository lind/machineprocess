package ske.ekstkom.statemachine;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StateMachineTest {

	private static final String SIG_INTERNAL = "SigInternal";

	@Test
	public void test() {
		LogAction logAction = new LogAction("Logging Action");

		SimpleState finalState = SimpleState.named("FinalState").build();

		SimpleState internal2 = SimpleState.named("Internal2").build();
		SimpleState internal1 = SimpleState.named("Internal1").transition("toInternal2")
				.guardedBy(NameGuard.by(SIG_INTERNAL)).to(internal2).build();

		// State with transition with guard and action.
		CompositeState stateTwo = CompositeState.cnamed("StateTwo").internalInitState(internal1) //
				.transition("toFinal").guardedBy(NameGuard.by("Sig4")).withAction(logAction).to(finalState).build();

		// State stateTwo = State.named("Internal2").build();

		// State with transition without guard and no action.
		SimpleState stateThree = SimpleState.named("StateThree") //
				.transition("toFinal").to(finalState).build();

		// State with two transitions
		SimpleState stateOne = SimpleState.named("StateOne")

		.transition("toTwo").guardedBy(NameGuard.by("Sig2")).withAction(logAction).to(stateTwo).transition("toThree")
				.guardedBy(NameGuard.by("Sig3")).withAction(logAction).to(stateThree).build();

		StateMachine machine = StateMachine.named("TestMachine").initState(stateOne).build();
		machine.addState(stateOne);
		machine.addState(stateTwo);
		machine.addState(stateThree);
		machine.addState(finalState);

		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Action.class, new ActionAdapter())
				.registerTypeAdapter(Guard.class, new GuardAdapter()) //
				.registerTypeAdapter(State.class, new StateAdapter()) //
				.create();

		String machineAsJSON = gson.toJson(machine);
		System.out.println(machineAsJSON);

		StateMachine machine2 = gson.fromJson(machineAsJSON, StateMachine.class);

		machine2.execute(Signal.create("Sig1"));
		machine2.execute(Signal.create("Sig2")); // toTwo
		machine2.execute(Signal.create("Sig3"));
		machine2.execute(Signal.create(SIG_INTERNAL)); // toInternal2 - just internal state transition in the composite
														// state stateTwo - not registered
		machine2.execute(Signal.create("Sig4")); // toFinal

		// validate active state
		Assert.assertEquals("FinalState", machine2.getActiveState().getName());

		// validate actions occurred
		// TODO: not same log action after GSON ser/dez...
		// logAction.assertNumberOfExecute(2);

		// validate signals with no matching transitions (does not count composite state internal transitions)
		Assert.assertEquals(3, machine2.numberOfSignalsNotMatchedTransitions());

		// (if there are signals not matching any guard/transition log warn)
	}
}
