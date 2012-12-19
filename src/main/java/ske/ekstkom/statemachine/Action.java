package ske.ekstkom.statemachine;

public abstract class Action {

	private String name;

	// The next element in the chain of responsibility
	private Action nextAction;

	public Action() {
	}

	public Action(String name) {
		this.name = name;
	}

	public void execute(final Signal signal, StateMachine stateMachine) {

		stateMachine.addActionExecuted(this);

		// Todo: Any preaction needed?
		// Signal preActionSignal = doPreAction(stateMashine, signal);

		doAction(signal);

		if (null != nextAction) {
			nextAction.execute(signal, stateMachine);
		}
		// doPostAction(stateMashine, signal);
	}

	protected abstract void doAction(final Signal signal);

	// hook...
	public String getDestination() {
		return null;
	}

	public void setNext(Action action) {
		this.nextAction = action;
	}

	public String getNname() {
		return name;
	}

}
