package ske.ekstkom.statemachine;

public interface State {

	public abstract State execute(Signal signal, boolean testScope);

	public abstract String getName();

}