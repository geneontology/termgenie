package org.bbop.termgenie.client.helper;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Simple Helper to provide a remove button for a widget.
 *
 * @param <T> widget type to be wrapped.
 */
public class RemovableWidget<T extends Widget> extends HorizontalPanel {

	private final T childWidget;
	private final Button removeButton;
	
	public RemovableWidget(T childWidget) {
		super();
		this.removeButton = new Button("remove");
		this.childWidget = childWidget;
		
		add(removeButton);
		add(childWidget);
	}
	
	public T getChildWidget() {
		return childWidget;
	}
	
	public void addRemoveHandler(ClickHandler handler) {
		removeButton.addClickHandler(handler);
	}
}
