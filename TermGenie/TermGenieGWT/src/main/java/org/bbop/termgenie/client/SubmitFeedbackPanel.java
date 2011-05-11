package org.bbop.termgenie.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SubmitFeedbackPanel extends PopupPanel {

	private static volatile SubmitFeedbackPanel instance;
	
	private final DockLayoutPanel globalPanel;
	private final Label headerLabel;
	private final ScrollPanel scrollPanel;
	private final VerticalPanel messagesPanel;
	
	public synchronized static void popup(String header) {
		SubmitFeedbackPanel instance = getInstance();
		instance.setLabelText(header);
		if (!instance.isShowing()) {
			instance.center();
		}
	}
	
	private synchronized static SubmitFeedbackPanel getInstance() {
		if (instance == null) {
			instance = new SubmitFeedbackPanel();
		}
		return instance; 
	}
	
	private void setLabelText(String label) {
		headerLabel.setText(label);
	}
	
	public static void addMessage(Widget widget) {
		getInstance().messagesPanel.add(widget);
	}
	
	public static void clearMessages() {
		getInstance().messagesPanel.clear();
	}
	
	private SubmitFeedbackPanel() {
		super(false);
		setGlassEnabled(true);
		
		// create internal widgets
		globalPanel = new DockLayoutPanel(Unit.PX);
		headerLabel = new Label("Status");
		scrollPanel = new ScrollPanel();
		messagesPanel = new VerticalPanel();
		
		// configure internal widgets
		add(globalPanel);
		globalPanel.setSize("450px", "350px");
		scrollPanel.setWidget(messagesPanel);
		
		Button closeButton = new Button("Close");
		closeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				SubmitFeedbackPanel.this.hide();
			}
		});
		
		globalPanel.addNorth(headerLabel, 25);
		globalPanel.addSouth(closeButton, 25);
		globalPanel.add(scrollPanel);
	}
}
