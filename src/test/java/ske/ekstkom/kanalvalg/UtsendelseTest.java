package ske.ekstkom.kanalvalg;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ske.ekstkom.statemachine.Action;
import ske.ekstkom.statemachine.ActionAdapter;
import ske.ekstkom.statemachine.Guard;
import ske.ekstkom.statemachine.GuardAdapter;
import ske.ekstkom.statemachine.LogAction;
import ske.ekstkom.statemachine.Signal;
import ske.ekstkom.statemachine.State;
import ske.ekstkom.statemachine.StateMachine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UtsendelseTest {

	// Eventer/hendelser
	public static final String RETUR_MOTTATT = "retur_mottatt"; // returpost
	public static final String UTSENDING_UTFOERT = "utsending_utfoert";
	public static final String FEIL_I_UTSKRIFT = "feil_i_utskrift";
	public static final String SEND_TIL_PRINT = "send_til_print";
	public static final String START_UTSENDING = "start_utsending";
	public static final String FEIL_I_RENDERING = "feil_i_rendering";
	public static final String SEND_TIL_RENDERING = "send_til_rendering";
	public static final String FEIL_I_DAL = "feil_i_dal";
	public static final String FEIL_I_ALTINN = "feil_i_altinn";
	public static final String MOTTATT_I_ALTINN = "mottatt_i_altinn";

	// State navn
	private static final String SENDT_TIL_PRINT_STATE = "SendtTilPrint";
	private static final String TIL_RENDERING_STATE = "TilRendering";
	private static final String TIL_ALTINN_STATE = "TilAltInn";
	private static final String UTSENDELSE_BESTILT = "UtsendelseBestilt";

	private static StateMachine utsendingStateMachine;
	private static String machineAsJSON;

	private final Gson gson = new GsonBuilder().setPrettyPrinting()
			.registerTypeAdapter(Action.class, new ActionAdapter())
			.registerTypeAdapter(Guard.class, new GuardAdapter()).create();

	// Given
	@BeforeClass
	public static void initPrintStateMachine() {
		LogAction logAction = new LogAction("Logging Action");
		AddToQueueAction addToPrintQueue = AddToQueueAction.named("PrintKø").queueNameJNDI("JNDI/print..")
				.queueFactoryJNDI("JNDI/factory").build();
		AddToQueueAction addToAltInnQueue = AddToQueueAction.named("AltInnKø").queueNameJNDI("JNDI/AltInn..")
				.queueFactoryJNDI("JNDI/factory").build();

		State feilet = State.named("Feilet").build();

		// Sluttilstand hvis ikke brev mottas i retur.
		State sendtUt = State.named("SendtUt")
				//
				.transition("til feilet").guardedBy(UtsendelseGuard.by(RETUR_MOTTATT)).withAction(logAction).to(feilet)
				.build();

		State sendtTilPrint = State
				.named(SENDT_TIL_PRINT_STATE)
				//
				.transition("til_sendt_ut").guardedBy(UtsendelseGuard.by(UTSENDING_UTFOERT)).withAction(logAction)
				.to(sendtUt)
				//
				.transition("til_feil").guardedBy(UtsendelseGuard.by(FEIL_I_UTSKRIFT)).withAction(logAction).to(feilet)
				.build();

		State tilRendering = State
				.named(TIL_RENDERING_STATE)
				//
				.transition("Til_sendt_til_print").guardedBy(UtsendelseGuard.by(SEND_TIL_PRINT)).withAction(logAction)
				.to(sendtTilPrint)
				//
				.transition("til_feil").guardedBy(UtsendelseGuard.by(FEIL_I_RENDERING)).withAction(logAction)
				.to(feilet).build();

		State tilAltInn = State
				.named(TIL_ALTINN_STATE)
				//
				.transition("til_sendt_ut").guardedBy(UtsendelseGuard.by(MOTTATT_I_ALTINN)).withAction(logAction)
				.to(sendtUt)
				//
				.transition("til_sendt_til_print").guardedBy(UtsendelseGuard.by(FEIL_I_ALTINN))
				.withAction(addToPrintQueue).to(tilRendering).build();

		// Starttilstand for utsending
		State utsendelseBestilt = State.named(UTSENDELSE_BESTILT)
				//
				.transition("til_altinn").guardedBy(UtsendelseGuard.by(START_UTSENDING)).withAction(addToAltInnQueue)
				.to(tilAltInn).build();

		utsendingStateMachine = StateMachine.named("UtsendingStateMachine").initState(utsendelseBestilt).build();
		// Legg til state slik at aktiv state kan settes etter deserialize
		utsendingStateMachine.addState(utsendelseBestilt);
		utsendingStateMachine.addState(tilAltInn);
		utsendingStateMachine.addState(tilRendering);
		utsendingStateMachine.addState(sendtTilPrint);
		utsendingStateMachine.addState(sendtUt);
		utsendingStateMachine.addState(feilet);

		Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Action.class, new ActionAdapter())
				.registerTypeAdapter(Guard.class, new GuardAdapter()).create();

		machineAsJSON = gson.toJson(utsendingStateMachine);
		System.out.println(machineAsJSON);
	}

	@Test
	public void skal_gjennom_alle_tilstand() {

		// Given
		utsendingStateMachine.clear();

		// When
		utsendingStateMachine.execute(Signal.create(START_UTSENDING));
		utsendingStateMachine.execute(Signal.create(FEIL_I_ALTINN));
		utsendingStateMachine.execute(Signal.create(SEND_TIL_PRINT));
		utsendingStateMachine.execute(Signal.create(UTSENDING_UTFOERT));
		utsendingStateMachine.execute(Signal.create(RETUR_MOTTATT));

		// Then
		Assert.assertEquals("Feilet", utsendingStateMachine.getActiveState().getName());
		Assert.assertEquals(0, utsendingStateMachine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_gjennom_alle_tilstand_med_dezerialisert_sm() {

		// Given
		StateMachine machine = gson.fromJson(machineAsJSON, StateMachine.class);

		// When
		machine.execute(Signal.create(START_UTSENDING));
		machine.execute(Signal.create(FEIL_I_ALTINN));
		machine.execute(Signal.create(SEND_TIL_PRINT));
		machine.execute(Signal.create(UTSENDING_UTFOERT));
		machine.execute(Signal.create(RETUR_MOTTATT));

		// Then
		Assert.assertEquals("Feilet", machine.getActiveState().getName());
		Assert.assertEquals(0, machine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_fra_rendering_til_sent_til_print_med_dezerialisert_sm() {

		// Given
		StateMachine machine = gson.fromJson(machineAsJSON, StateMachine.class);
		machine.setActiveState(TIL_RENDERING_STATE);

		// When
		machine.execute(Signal.create(SEND_TIL_PRINT));
		machine.execute(Signal.create(SEND_TIL_PRINT));
		machine.execute(Signal.create(SEND_TIL_PRINT));

		// Then
		Assert.assertEquals(SENDT_TIL_PRINT_STATE, machine.getActiveState().getName());
		Assert.assertEquals(2, machine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_feile_i_rendering() {

		// Given
		utsendingStateMachine.setActiveState(UTSENDELSE_BESTILT);
		utsendingStateMachine.clear();

		// When
		utsendingStateMachine.execute(Signal.create(START_UTSENDING));
		utsendingStateMachine.execute(Signal.create(FEIL_I_ALTINN));
		utsendingStateMachine.execute(Signal.create(FEIL_I_RENDERING));

		// Then
		Assert.assertEquals("Feilet", utsendingStateMachine.getActiveState().getName());
		Assert.assertEquals(0, utsendingStateMachine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_feile_i_rendering_og_ignorere_irrelevanet_signaler() {

		// Given
		utsendingStateMachine.setActiveState(UTSENDELSE_BESTILT);
		utsendingStateMachine.clear();

		// When
		utsendingStateMachine.execute(Signal.create(START_UTSENDING));
		utsendingStateMachine.execute(Signal.create(FEIL_I_ALTINN));
		utsendingStateMachine.execute(Signal.create(FEIL_I_RENDERING));
		utsendingStateMachine.execute(Signal.create(FEIL_I_RENDERING));
		utsendingStateMachine.execute(Signal.create(FEIL_I_RENDERING));
		utsendingStateMachine.execute(Signal.create(FEIL_I_RENDERING));

		// Then
		Assert.assertEquals("Feilet", utsendingStateMachine.getActiveState().getName());
		Assert.assertEquals(3, utsendingStateMachine.numberOfSignalsNotMatchedTransitions());
	}
}
