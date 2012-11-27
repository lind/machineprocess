package ske.ekstkom.kanalvalg;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ske.ekstkom.statemachine.LogAction;
import ske.ekstkom.statemachine.Signal;
import ske.ekstkom.statemachine.State;
import ske.ekstkom.statemachine.StateMachine;

public class UtsendelseTest {

	private LogAction logAction;
	private StateMachine printStateMachine;

	// Eventer/hendelser
	public static final String RETUR_MOTTATT = "retur_mottatt"; // returpost
	public static final String UTSENDING_UTFOERT = "utsending_utfoert";
	public static final String FEIL_I_UTSKRIFT = "feil_i_utskrift";
	public static final String SEND_TIL_PRINT = "send_til_print";
	public static final String FEIL_I_RENDERING = "feil_i_rendering";
	public static final String SEND_TIL_RENDERING = "send_til_rendering";
	public static final String FEIL_I_DAL = "feil_i_dal";

	// Given
	@Before
	public void initPrintStateMachine() {
		logAction = new LogAction("Logging Action");

		State feilet = State.named("Feilet").build();

		State sendtUt = State.named("SendtUt")
				//
				.transition("til feilet").guardedBy(UtsendelseGuard.by(RETUR_MOTTATT)).withAction(logAction).to(feilet)
				.build();

		State sendtTilPrint = State
				.named("SendtTilPrint")
				//
				.transition("til_sendt_ut").guardedBy(UtsendelseGuard.by(UTSENDING_UTFOERT)).withAction(logAction)
				.to(sendtUt)
				//
				.transition("til_feil").guardedBy(UtsendelseGuard.by(FEIL_I_UTSKRIFT)).withAction(logAction).to(feilet)
				.build();

		State tilRendering = State
				.named("TilRendering")
				//
				.transition("Til_sendt_til_print").guardedBy(UtsendelseGuard.by(SEND_TIL_PRINT)).withAction(logAction)
				.to(sendtTilPrint)
				//
				.transition("til_feil").guardedBy(UtsendelseGuard.by(FEIL_I_RENDERING)).withAction(logAction)
				.to(feilet).build();

		// Starttilstand for printutsending
		State utsendelseBestilt = State
				.named("UtsendelseBestilt")
				//
				.transition("til_rendering").guardedBy(UtsendelseGuard.by(SEND_TIL_RENDERING)).withAction(logAction)
				.to(tilRendering)
				//
				.transition("til_feil").guardedBy(UtsendelseGuard.by(FEIL_I_DAL)).withAction(logAction).to(feilet)
				.build();

		printStateMachine = StateMachine.named("PrintStateMachine").initState(utsendelseBestilt).build();
	}

	@Test
	public void skal_gjennom_alle_tilstand() {

		// When
		printStateMachine.execute(Signal.create(SEND_TIL_RENDERING));
		printStateMachine.execute(Signal.create(SEND_TIL_PRINT));
		printStateMachine.execute(Signal.create(UTSENDING_UTFOERT));
		printStateMachine.execute(Signal.create(RETUR_MOTTATT));

		// Then
		Assert.assertEquals("Feilet", printStateMachine.getActiveState().getName());

		logAction.assertNumberOfExecute(4);
		Assert.assertEquals(0, printStateMachine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_feile_i_rendering() {

		// When
		printStateMachine.execute(Signal.create(SEND_TIL_RENDERING));
		printStateMachine.execute(Signal.create(FEIL_I_RENDERING));

		// Then
		Assert.assertEquals("Feilet", printStateMachine.getActiveState().getName());

		logAction.assertNumberOfExecute(2);

		Assert.assertEquals(0, printStateMachine.numberOfSignalsNotMatchedTransitions());
	}

	@Test
	public void skal_feile_i_rendering_og_ignorere_irrelevanet_signaler() {

		// When
		printStateMachine.execute(Signal.create(SEND_TIL_RENDERING));
		printStateMachine.execute(Signal.create(FEIL_I_RENDERING));
		printStateMachine.execute(Signal.create(FEIL_I_RENDERING));
		printStateMachine.execute(Signal.create(FEIL_I_RENDERING));
		printStateMachine.execute(Signal.create(FEIL_I_RENDERING));

		// Then
		Assert.assertEquals("Feilet", printStateMachine.getActiveState().getName());

		// Ikke så relevante assert...
		logAction.assertNumberOfExecute(2);
		Assert.assertEquals(3, printStateMachine.numberOfSignalsNotMatchedTransitions());
	}
}
