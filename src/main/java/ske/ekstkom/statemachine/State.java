package ske.ekstkom.statemachine;

public interface State {

	public abstract State execute(Signal signal);

	public abstract String getName();

}