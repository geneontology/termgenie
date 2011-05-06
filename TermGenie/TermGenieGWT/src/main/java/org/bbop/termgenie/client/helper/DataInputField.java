package org.bbop.termgenie.client.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bbop.termgenie.shared.GWTTermTemplate.GWTCardinality;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public interface DataInputField
{
	public enum Kind {
		Simple,
		List,
		Prefix
	}
	
	public Collection<Kind> getKind();
	
	public String getSimpleValue();
	
	public List<String> getListSimpleValue();
	
	public List<String> getPrefixValues();
	
	public Widget getWidget();
	
	public static class TextFieldInput extends TextBox implements DataInputField {

		@Override
		public Collection<Kind> getKind() {
			return Collections.singleton(Kind.Simple);
		}

		@Override
		public String getSimpleValue() {
			return getText();
		}

		@Override
		public List<String> getListSimpleValue() {
			return null;
		}

		@Override
		public List<String> getPrefixValues() {
			return null;
		}

		@Override
		public Widget getWidget() {
			return this;
		}
	}
	
	public static class AutoCompleteInputField extends SuggestBox implements DataInputField {
		
		public AutoCompleteInputField(SuggestOracle oracle) {
			super(oracle);
		}

		@Override
		public Collection<Kind> getKind() {
			return Collections.singleton(Kind.Simple);
		}

		@Override
		public String getSimpleValue() {
			return getText();
		}

		@Override
		public List<String> getListSimpleValue() {
			return null;
		}

		@Override
		public List<String> getPrefixValues() {
			return null;
		}
		
		@Override
		public Widget getWidget() {
			return this;
		}
	}
	
	public static class PrefixAutoCompleteInputField extends VerticalPanel implements DataInputField {

		private final AutoCompleteInputField field;
		private final List<CheckBox> prefixes;
		
		public PrefixAutoCompleteInputField(SuggestOracle oracle, String[] functionalPrefixes) {
			super();
			setSize("100%", "100%");
			
			field = new AutoCompleteInputField(oracle);
			add(field);
			this.prefixes = new ArrayList<CheckBox>(functionalPrefixes.length);
			for (String prefix : functionalPrefixes) {
				CheckBox checkBox = new CheckBox(prefix);
				this.prefixes.add(checkBox);
				add(checkBox);
			}
		}
		
		@Override
		public Collection<Kind> getKind() {
			return Arrays.asList(Kind.Simple, Kind.Prefix);
		}

		@Override
		public String getSimpleValue() {
			return field.getSimpleValue();
		}

		@Override
		public List<String> getListSimpleValue() {
			return null;
		}

		@Override
		public List<String> getPrefixValues() {
			List<String> selectedPrefixes = new ArrayList<String>(prefixes.size());
			for (CheckBox box : prefixes) {
				if (box.getValue()) {
					selectedPrefixes.add(box.getText());
				}
			}
			return selectedPrefixes;
		}
		
		@Override
		public Widget getWidget() {
			return this;
		}
	}
	
	public static class ListAutoCompleteInputField extends VerticalPanel implements DataInputField {

		private final List<AutoCompleteInputField> fields;
		private final ModifyButtonsWidget buttonsWidget;
		
		public ListAutoCompleteInputField(final SuggestOracle oracle, final GWTCardinality cardinality) {
			super();
			fields = new ArrayList<AutoCompleteInputField>();
			buttonsWidget = new ModifyButtonsWidget();
			buttonsWidget.addAddHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					remove(buttonsWidget);
					if (fields.size() < cardinality.getMax()) {
						AutoCompleteInputField widget = new AutoCompleteInputField(oracle);
						fields.add(widget);
						add(widget);
					}
					add(buttonsWidget);
				}
			});
			buttonsWidget.addRemoveHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					int size = fields.size();
					if (size > 0 && size > cardinality.getMin()) {
						AutoCompleteInputField field = fields.remove(size - 1);
						remove(field);
					}
				}
			});
			
			int startCount = Math.min(cardinality.getMax(), Math.max(1, cardinality.getMin()));
			for (int i = 0; i < startCount; i++) {
				AutoCompleteInputField widget = new AutoCompleteInputField(oracle);
				fields.add(widget);
				add(widget);
			}
			add(buttonsWidget);
		}

		@Override
		public Collection<Kind> getKind() {
			return Collections.singleton(Kind.List);
		}

		@Override
		public String getSimpleValue() {
			return null;
		}

		@Override
		public List<String> getListSimpleValue() {
			List<String> values = new ArrayList<String>(fields.size());
			for (AutoCompleteInputField field : fields) {
				values.add(field.getSimpleValue());
			}
			return values;
		}

		@Override
		public List<String> getPrefixValues() {
			return null;
		}
		
		@Override
		public Widget getWidget() {
			return this;
		}
		
	}
}