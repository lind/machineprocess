package ske.ekstkom.statemachine;

public interface Guard {

	boolean check(Signal signal);

}