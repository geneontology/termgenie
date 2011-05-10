package org.bbop.termgenie.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.logging.client.HasWidgetsLogHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class LoggingCallback<T> implements AsyncCallback<T> {
	
	static {
		Logger.getLogger("TermGenieLogger").addHandler(new HasWidgetsLogHandler(ErrorPanel.getInstance()));
	}

	@Override
	public void onFailure(Throwable caught) {
		Logger logger = Logger.getLogger("TermGenieLogger");
		logger.log(Level.SEVERE, "An Internal error occured.", caught);
		LoggingErrorPanel.popup();
	}
	 
	public static class ErrorPanel extends VerticalPanel {
		
		private static volatile ErrorPanel instance = null;
 
		private ErrorPanel() {
			super();
		}
		
		public synchronized static ErrorPanel getInstance() {
			if (instance == null) {
				instance = new ErrorPanel();
			}
			return instance;
		}
	}
	
	public static class LoggingErrorPanel extends PopupPanel {
		
		private static volatile LoggingErrorPanel instance;
		
		public synchronized static void popup() {
			if (instance == null) {
				instance = new LoggingErrorPanel();
			}
			if (!instance.isShowing()) {
				instance.center();
			}
		}
		
		private LoggingErrorPanel() {
			super(false);
			setGlassEnabled(true);
			DockLayoutPanel globalPanel = new DockLayoutPanel(Unit.PX);
			globalPanel.setSize("450px", "350px");
			
			final ErrorPanel errorPanel = ErrorPanel.getInstance();
			errorPanel.setSize("100%", "100%");
			ScrollPanel scrollPanel = new ScrollPanel(errorPanel);
			scrollPanel.setAlwaysShowScrollBars(true);
			
			
			HorizontalPanel buttonPanel = new HorizontalPanel();
			Button clearButton = new Button("Clear");
			buttonPanel.add(clearButton);
			clearButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					errorPanel.clear();
				}
			});
			
			Button button = new Button("Close");
			buttonPanel.add(button);
			button.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					LoggingErrorPanel.this.hide();
				}
			});
			globalPanel.addNorth(new Label("An internal error occured."), 25);
			globalPanel.addSouth(buttonPanel, 25);
			globalPanel.add(scrollPanel);
			
			add(globalPanel);
		}
	}
}
