package ske.ekstkom.statemachine;

public class Signal {

	private final String name;

	public Signal(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static Signal create(String name) {
		return new Signal(name);
	}

}
