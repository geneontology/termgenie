package org.bbop.termgenie.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class SubmitFeedbackPanel extends PopupPanel {

	private static volatile SubmitFeedbackPanel instance;
	
	private DockLayoutPanel globalPanel;
	private Label headerLabel;
	private ScrollPanel scrollPanel;
	
	public synchronized static void popup() {
		if (instance == null) {
			instance = new SubmitFeedbackPanel();
		}
		if (!instance.isShowing()) {
			instance.center();
		}
	}
	
	public void setLabelText(String label) {
		headerLabel.setText(label);
	}
	
	public void setMainWidget(Widget widget) {
		scrollPanel.setWidget(widget);
	}
	
	private SubmitFeedbackPanel() {
		super(false);
		setGlassEnabled(true);
		
		// create internal widgets
		globalPanel = new DockLayoutPanel(Unit.PX);
		headerLabel = new Label("Status");
		scrollPanel = new ScrollPanel();
		
		// configure internal widgets
		add(globalPanel);
		globalPanel.setSize("450px", "350px");
		
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
