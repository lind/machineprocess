package ske.ekstkom.statemachine;

public interface State {

	public abstract State execute(Signal signal, StateMachine stateMachine);

	public abstract String getName();

	public abstract void exit(Signal signal, StateMachine stateMachine);

	public abstract void entry(Signal signal, StateMachine stateMachine);

}