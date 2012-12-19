package ske.ekstkom.statemachine;

import junit.framework.AssertionFailedError;

/**
 * Action for testing purpose.
 */
public class LogAction extends Action {

	private int numberOfExecute = 0;

	public LogAction(String name) {
		super(name);
	}

	@Override
	protected void doAction(Signal signal) {
		numberOfExecute++;
		System.out.println("LogAction: " + getNname() + " executed. numberOfExecute: " + numberOfExecute);
	}

	/**
	 * Validate the number of usage of the action.
	 * 
	 * @param executes expected number of execute
	 */
	public void assertNumberOfExecute(int executes) {
		if (numberOfExecute != executes) {
			throw new AssertionFailedError("Number of executions was: " + numberOfExecute + ". Expected: " + executes
					+ ".");
		}
	}

	// -- Builder
	public static LogAction named(String name) {
		return new LogAction(name);
	}
}
