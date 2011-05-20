package org.bbop.termgenie.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MessagePanel extends PopupPanel {
	
	private final static MessagePanel instance = new MessagePanel();

	private boolean hasErrors = false;
	private final InternalMessagePanel successPanel;
	private final InternalMessagePanel errorPanel;
	private final InternalMessagePanel systemErrorPanel;
	private final TabLayoutPanel tabs;
	private final DockLayoutPanel internalPanel;
	
	private MessagePanel() {
		super(false);
		setGlassEnabled(true);
		
		successPanel = new InternalMessagePanel("Success");
		errorPanel = new InternalMessagePanel("Error");
		systemErrorPanel = new InternalMessagePanel("An internal error occured.");
		tabs = new TabLayoutPanel(1.5, Unit.EM);
		
		internalPanel = new DockLayoutPanel(Unit.PX);
		internalPanel.setSize("450px", "350px");
		add(internalPanel);
		
		tabs.add(createScrollbars(successPanel), "Success");
		tabs.add(createScrollbars(errorPanel), "Error");
		tabs.add(createScrollbars(systemErrorPanel), "SystemErrors");
		
		HorizontalPanel buttonPanel = new HorizontalPanel();
		Button clearButton = new Button("Clear");
		buttonPanel.add(clearButton);
		clearButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				int index = tabs.getSelectedIndex();
				switch (index) {
				case 0:
					successPanel.clear();
					break;
				case 1:
					errorPanel.clear();
					break;
				case 2:
					systemErrorPanel.clear();
					break;
				default:
					break;
				}
			}
		});
		
		Button button = new Button("Close");
		buttonPanel.add(button);
		button.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				instance.hide();
			}
		});
		
		internalPanel.addSouth(buttonPanel, 25);
		internalPanel.add(tabs);
	}
	
	private static Widget createScrollbars(Widget w) {
		ScrollPanel s = new ScrollPanel(w);
		w.setSize("430px", "350px");
		s.setAlwaysShowScrollBars(true);
		return s;
	}
	
	public enum FocusType {
		success,
		error,
		systemerror
	}
	
	public synchronized static void popupError() {
		popup(FocusType.error);
	}
	
	public synchronized static void popupSuccess() {
		popup(FocusType.success);
	}
	
	public synchronized static void popup(FocusType focus) {
		if (focus == FocusType.success && instance.hasErrors) {
			// if one error was reported switch to error tab no matter what
			instance.tabs.selectTab(FocusType.error.ordinal());
		}
		else {
			instance.tabs.selectTab(focus.ordinal());
		}
		
		if (!instance.isShowing()) {
			instance.center();
		}
	}
	
	public static synchronized void addSuccessMessage(Widget widget) {
		instance.successPanel.messages.add(widget);
	}
	
	public static synchronized void addErrorMessage(Widget widget) {
		instance.errorPanel.messages.add(widget);
		instance.hasErrors = true;
	}
	
	public static synchronized void clearMessages() {
		instance.successPanel.clear();
		instance.errorPanel.clear();
		instance.hasErrors = false;
	}
	
	public static VerticalPanel getSystemErrorPanel() {
		return instance.systemErrorPanel.messages;
	}
	
	private static class InternalMessagePanel extends VerticalPanel {
		
		private final VerticalPanel messages;
		
		InternalMessagePanel(String title) {
			super();
			messages = new VerticalPanel();
			Label titleLabel = new Label(title);
			titleLabel.addStyleName("message-popup-title-style");
			add(titleLabel);
			add(messages);
		}
		
		@Override
		public void clear() {
			messages.clear();
		}
	}
}
