package org.bbop.termgenie.client.helper;

import java.util.ArrayList;
import java.util.List;

import org.bbop.termgenie.shared.GWTTermGenerationParameter;
import org.bbop.termgenie.shared.GWTTermGenerationParameter.OntologyTerm;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTCardinality;
import org.bbop.termgenie.shared.GWTTermTemplate.GWTTemplateField;

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
	public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field);
	
	public Widget getWidget();
	
	public static class TextFieldInput extends TextBox implements DataInputField {

		@Override
		public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field) {
			String text = getText().trim();
			if (text != null && !text.isEmpty()) {
				parameter.getStrings().addValue(text, field, 0);
				return true;
			}
			return false;
		}

		@Override
		public Widget getWidget() {
			return this;
		}
	}
	
	public static class AutoCompleteInputField extends SuggestBox implements DataInputField {
		
		private volatile OntologyTerm ontologyTerm = null;
		
		public AutoCompleteInputField(SuggestOracle oracle) {
			super(oracle);
		}

		@Override
		public Widget getWidget() {
			return this;
		}

		@Override
		public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field) {
			OntologyTerm term = ontologyTerm;
			if (term != null) {
				parameter.getTerms().addValue(term, field, 0);
				return true;
			}
			return false;
		}

		/**
		 * @param ontologyTerm the ontologyTerm to set
		 */
		void setOntologyTerm(OntologyTerm ontologyTerm) {
			this.ontologyTerm = ontologyTerm;
		}

		/**
		 * @return the ontologyTerm
		 */
		OntologyTerm getOntologyTerm() {
			return ontologyTerm;
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
		public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field) {
			OntologyTerm term = this.field.getOntologyTerm();
			if (term != null) {
				parameter.getTerms().addValue(term, field, 0);
				List<String> selectedPrefixes = new ArrayList<String>(prefixes.size());
				for (CheckBox box : prefixes) {
					if (box.getValue()) {
						selectedPrefixes.add(box.getText());
					}
				}
				parameter.getPrefixes().addValue(selectedPrefixes, field, 0);
				return true;
			}
			return false;
		}

		@Override
		public Widget getWidget() {
			return this;
		}
		
		/**
		 * @param ontologyTerm the ontologyTerm to set
		 */
		void setOntologyTerm(OntologyTerm ontologyTerm) {
			field.setOntologyTerm(ontologyTerm);
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
		public boolean extractParameter(GWTTermGenerationParameter parameter, GWTTemplateField field) {
			int pos = 0;
			for (AutoCompleteInputField inputField : fields) {
				OntologyTerm term = inputField.getOntologyTerm();
				if (term != null) {
					parameter.getTerms().addValue(term, field, pos++);
				}
			}
			return pos > 0;
		}

		@Override
		public Widget getWidget() {
			return this;
		}
	}
}