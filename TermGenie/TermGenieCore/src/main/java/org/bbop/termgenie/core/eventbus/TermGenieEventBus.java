package org.bbop.termgenie.core.eventbus;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceExistsException;
import org.bushe.swing.event.EventServiceLocator;
import org.bushe.swing.event.ThreadSafeEventService;

public class TermGenieEventBus {

	private static volatile boolean doneSetup = false;

	static synchronized void setup() {
		if (!doneSetup) {
			// register the implementation for the EventBus
			try {
				EventService eventService = EventServiceLocator.getEventService(EventServiceLocator.SERVICE_NAME_SWING_EVENT_SERVICE);
				if (eventService == null) {
					eventService = new ThreadSafeEventService();
					EventServiceLocator.setEventService(EventServiceLocator.SERVICE_NAME_SWING_EVENT_SERVICE,
							eventService);
				}
				doneSetup = true;
			} catch (EventServiceExistsException exception) {
				throw new RuntimeException(exception);
			}
		}
	}
}
