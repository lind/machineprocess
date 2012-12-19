package ske.ekstkom.kanalvalg;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ske.ekstkom.statemachine.Action;
import ske.ekstkom.statemachine.CompositeState;
import ske.ekstkom.statemachine.FinalState;
import ske.ekstkom.statemachine.Guard;
import ske.ekstkom.statemachine.LogAction;
import ske.ekstkom.statemachine.Signal;
import ske.ekstkom.statemachine.SimpleState;
import ske.ekstkom.statemachine.State;
import ske.ekstkom.statemachine.StateMachine;
import ske.ekstkom.statemachine.gson.ActionAdapter;
import ske.ekstkom.statemachine.gson.GuardAdapter;
import ske.ekstkom.statemachine.gson.StateAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UtsendelseElektroniskPapirTest {

	// Events
	public static final String START_UTSENDING = "start_utsending";
	public static final String OK = "ok";
	public static final String FEIL = "feil";
	public static final String RETUR_MOTTATT = "retur_mottatt"; // returpost

	// Event channels
	public static final String ALTINN = "altinn";
	public static final String PRINT = "print";

	// State navn
	private static final String INIT_STATE = "init_state";
	private static final String TIL_UTSENDING_STATE = "til_utsending";
	private static final String SENDT_UT = "SendtUt";
	private static final String FEILET = "Feilet";

	private static final String SENDT_TIL_ALTINN_STATE = "sendt_til_altinn";
	private static final String SENDT_TIL_PRINT_STATE = "sendt_til_print";
	private static final String TIL_UTSENDING_FINAL_STATE = "tilUtseningfinalState";

	private static StateMachine utsendingStateMachine;
	private static String machineAsJSON;

	private static final Gson gson = new GsonBuilder().setPrettyPrinting()
			.registerTypeAdapter(Action.class, new ActionAdapter()) //
			.registerTypeAdapter(Guard.class, new GuardAdapter()) //
			.registerTypeAdapter(State.class, new StateAdapter()) //
			.create();

	// Given
	@BeforeClass
	public static void initPrintStateMachine() {
		LogAction logAction = new LogAction("Logging Action");
		AddToQueueAction addToPrintQueue = AddToQueueAction.named("PrintKø").queueNameJNDI("JNDI/print..")
				.queueFactoryJNDI("JNDI/factory").build();
		AddToQueueAction addToAltInnQueue = AddToQueueAction.named("AltInnKø").queueNameJNDI("JNDI/AltInn..")
				.queueFactoryJNDI("JNDI/factory").build();

		SimpleState feilet = SimpleState.named(FEILET).build();

		// Sluttilstand hvis ikke brev mottas i retur.
		SimpleState sendtUt = SimpleState.named(SENDT_UT).transition("til feilet")
				.guardedBy(UtsendelseGuard.guardedByEvent(RETUR_MOTTATT).withChannel(PRINT).build())
				.withAction(logAction).to(feilet).build();

		FinalState tilUtseningfinalState = new FinalState(TIL_UTSENDING_FINAL_STATE);

		SimpleState sendtTilPrint = SimpleState.named(SENDT_TIL_PRINT_STATE)
				//
				.transition("til_feil").guardedBy(UtsendelseGuard.guardedByEvent(FEIL).withChannel(PRINT).build())
				.withAction(logAction).to(tilUtseningfinalState).build();

		SimpleState tilAltInn = SimpleState
				.named(SENDT_TIL_ALTINN_STATE)
				//
				.transition("til_sendt_til_print")
				.guardedBy(UtsendelseGuard.guardedByEvent(FEIL).withChannel(ALTINN).build())
				.withAction(addToPrintQueue).to(sendtTilPrint).build();

		// CompositeState for utsending
		State tilUtsending = CompositeState.named(TIL_UTSENDING_STATE)
				// Initial transition for the composite state
				.initTransition("til_altinn").withAction(addToAltInnQueue).to(tilAltInn)
				// Final transition for the composite state
				.finalTransition("tilFeilet").to(feilet)
				// OK transition - could 'log' successful channel action
				.transition("til_sendt_ut").guardedBy(UtsendelseGuard.guardedByEvent(OK).build()).withAction(logAction)
				.to(sendtUt)
				//
				// TODO: done by the internal states in the composite state
				// .transition("til_feilet").guardedBy(UtsendelseGuard.guardedByEvent(FEIL).build()).withAction(logAction)
				// .to(feilet) //
				.build();

		// Starttilstand for utsending
		SimpleState initState = SimpleState.named(INIT_STATE).transition("til_utsending").withAction(logAction)
				.to(tilUtsending).build();

		utsendingStateMachine = StateMachine.named("Utsending_Elektronisk-Papir_StateMachine").initState(initState)
				.build();

		// Legg til state slik at aktiv state kan settes etter deserialize
		utsendingStateMachine.addState(initState);
		utsendingStateMachine.addState(tilUtsending);
		utsendingStateMachine.addState(tilAltInn);
		utsendingStateMachine.addState(sendtTilPrint);
		utsendingStateMachine.addState(sendtUt);
		utsendingStateMachine.addState(feilet);

		machineAsJSON = gson.toJson(utsendingStateMachine);
		System.out.println(machineAsJSON);
	}

	@Test
	public void skal_gjennom_alle_tilstand() {

		// Given
		utsendingStateMachine.clear();

		// When
		utsendingStateMachine.execute(Signal.create(START_UTSENDING));
		utsendingStateMachine.execute(UtsendelseSignal.create(FEIL, ALTINN));
		utsendingStateMachine.execute(UtsendelseSignal.create(OK, PRINT));
		utsendingStateMachine.execute(UtsendelseSignal.create(RETUR_MOTTATT, PRINT));

		// Then
		Assert.assertEquals(FEILET, utsendingStateMachine.getActiveState().getName());

		// TODO: internal transitions in composite state is recorded as "not match"
		Assert.assertEquals(1, utsendingStateMachine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_gjennom_alle_tilstand_med_dezerialisert_sm() {

		// Given
		StateMachine machine = gson.fromJson(machineAsJSON, StateMachine.class);

		// When
		machine.execute(Signal.create(START_UTSENDING));
		machine.execute(UtsendelseSignal.create(FEIL, ALTINN));
		machine.execute(UtsendelseSignal.create(OK, PRINT));
		machine.execute(UtsendelseSignal.create(RETUR_MOTTATT, PRINT));

		// Then
		Assert.assertEquals("Feilet", machine.getActiveState().getName());

		// TODO: internal transitions in composite state is recorded as "not match"
		// Assert.assertEquals(0, machine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_fra_sendt_til_altinn_til_sent_til_print_med_dezerialisert_sm() {

		// Given
		StateMachine machine = gson.fromJson(machineAsJSON, StateMachine.class);
		machine.setActiveState(TIL_UTSENDING_STATE, SENDT_TIL_ALTINN_STATE);

		// When
		machine.execute(UtsendelseSignal.create(FEIL, ALTINN));
		machine.execute(UtsendelseSignal.create(FEIL, ALTINN));
		machine.execute(UtsendelseSignal.create(FEIL, ALTINN));

		// Then
		Assert.assertEquals(TIL_UTSENDING_STATE, machine.getActiveState().getName());

		State state = machine.getActiveState();
		if (state instanceof CompositeState) {
			Assert.assertEquals(SENDT_TIL_PRINT_STATE, ((CompositeState) state).getActiveState().getName());
		} else {
			Assert.fail("Should have active state in the CompositeState!");
		}

		// TODO: internal transitions in composite state is recorded as "not match"
		// Assert.assertEquals(2, machine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_feile_i_print() {

		// Given
		utsendingStateMachine.clear();
		utsendingStateMachine.setActiveState(TIL_UTSENDING_STATE, SENDT_TIL_PRINT_STATE);

		// When
		utsendingStateMachine.execute(UtsendelseSignal.create(FEIL, PRINT));

		// Then
		Assert.assertEquals("Feilet", utsendingStateMachine.getActiveState().getName());
		Assert.assertEquals(0, utsendingStateMachine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_feile_i_print_og_ignorere_irrelevanet_signaler() {

		// Given
		utsendingStateMachine.clear();
		utsendingStateMachine.setActiveState(INIT_STATE);

		// When
		utsendingStateMachine.execute(Signal.create(START_UTSENDING));
		utsendingStateMachine.execute(UtsendelseSignal.create(FEIL, ALTINN));
		utsendingStateMachine.execute(UtsendelseSignal.create(FEIL, PRINT));
		utsendingStateMachine.execute(UtsendelseSignal.create(FEIL, PRINT));
		utsendingStateMachine.execute(UtsendelseSignal.create(FEIL, PRINT));
		utsendingStateMachine.execute(UtsendelseSignal.create(FEIL, PRINT));

		// Then
		Assert.assertEquals("Feilet", utsendingStateMachine.getActiveState().getName());
		// TODO: internal transitions in composite state is recorded as "not match"
		// Assert.assertEquals(3, utsendingStateMachine.numberOfSignalsNotMatchedTransitions());
	}

}
