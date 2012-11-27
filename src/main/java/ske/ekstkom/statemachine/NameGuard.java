package ske.ekstkom.statemachine;

/**
 * Guard validating the name of the name of the signal.
 */
public class NameGuard implements Guard {

	private final String name;

	public NameGuard(String name) {
		this.name = name;
	}

	public boolean check(Signal signal) {

		return name.equals(signal.getName());
	}

	public static NameGuard by(String name) {

		return new NameGuard(name);
	}

}
