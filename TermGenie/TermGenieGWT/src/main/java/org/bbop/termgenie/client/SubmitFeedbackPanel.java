package org.bbop.termgenie.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

public class SubmitFeedbackPanel extends PopupPanel {

	private static volatile SubmitFeedbackPanel instance;
	
	public synchronized static void popup() {
		if (instance == null) {
			instance = new SubmitFeedbackPanel();
		}
		if (!instance.isShowing()) {
			instance.center();
		}
	}
	
	private SubmitFeedbackPanel() {
		super(false);
		setGlassEnabled(true);
		DockLayoutPanel globalPanel = new DockLayoutPanel(Unit.PX);
		globalPanel.setSize("450px", "350px");
		
		HorizontalPanel buttonPanel = new HorizontalPanel();
		Button button = new Button("Close");
		buttonPanel.add(button);
		button.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				SubmitFeedbackPanel.this.hide();
			}
		});
		globalPanel.addNorth(new Label("Status"), 25);
		globalPanel.addSouth(buttonPanel, 25);
		
//		globalPanel.add(scrollPanel);
		
		add(globalPanel);
	}
}
