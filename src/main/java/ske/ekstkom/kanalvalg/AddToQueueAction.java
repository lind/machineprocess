package ske.ekstkom.kanalvalg;

import ske.ekstkom.statemachine.Action;
import ske.ekstkom.statemachine.Signal;

public class AddToQueueAction extends Action {

	private String queueNameJNDI;
	private String queueFactoryJNDI;

	public AddToQueueAction(String name) {
		super(name);
	}

	@Override
	protected void doAction(Signal signal) {
		System.out.println("TODO: get queue from JNDI and add message");
	}

	// -- Builder
	public static AddToQueueActionBuilder named(String name) {
		return new AddToQueueActionBuilder(name);
	}

	public static class AddToQueueActionBuilder {
		AddToQueueAction action;

		public AddToQueueActionBuilder(String name) {
			this.action = new AddToQueueAction(name);
		}

		public AddToQueueActionBuilder queueNameJNDI(String queueNameJNDI) {
			this.action.queueNameJNDI = queueNameJNDI;
			return this;
		}

		public AddToQueueActionBuilder queueFactoryJNDI(String queueFactoryJNDI) {
			this.action.queueFactoryJNDI = queueFactoryJNDI;
			return this;
		}

		public AddToQueueAction build() {
			if (null == action.queueNameJNDI || null == action.queueFactoryJNDI) {
				throw new IllegalStateException("Queue name and factory missing!");
			}
			return action;
		}
	}

}
