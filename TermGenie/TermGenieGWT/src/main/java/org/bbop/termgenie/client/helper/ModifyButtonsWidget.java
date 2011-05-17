package org.bbop.termgenie.client.helper;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;

public class ModifyButtonsWidget extends FlowPanel {

	private final Button addButton;
	private final Button removeButton;

	public ModifyButtonsWidget() {
		super();
		addStyleName("modify-buttons-widget");
		addButton = new Button("add");
		removeButton = new Button("remove");
		add(addButton);
		add(removeButton);
	}
	
	public void addAddHandler(ClickHandler handler) {
		addButton.addClickHandler(handler);
	}
	
	public void addRemoveHandler(ClickHandler handler) {
		removeButton.addClickHandler(handler);
	}
	
	public void allowAdd(boolean allow) {
		addButton.setEnabled(allow);
	}
	
	public void allowRemove(boolean allow) {
		removeButton.setEnabled(allow);
	}
}
