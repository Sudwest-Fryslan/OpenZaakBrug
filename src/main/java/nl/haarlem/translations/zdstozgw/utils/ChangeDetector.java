/*
 * Copyright 2020-2021 The Open Zaakbrug Contributors
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package nl.haarlem.translations.zdstozgw.utils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import nl.haarlem.translations.zdstozgw.converter.ConverterException;
import nl.haarlem.translations.zdstozgw.translation.zds.model.ZdsRol;

@Data
public class ChangeDetector {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Data
	public static class Change {
		private Field field;
		private ChangeType changeType;
		private Object currentValue;
		private Object newValue;

		public Change(Field field, ChangeType changeType, Object currentValue, Object newValue) {
			this.field = field;
			this.changeType = changeType;
			this.currentValue = currentValue;
			this.newValue = newValue;
		}
	}

	public enum ChangeType {
		DELETED, CHANGED, NEW
	}

	public class Changes extends HashMap<Change, ChangeType>  {
		public Changes() {
			super();
		}

		public Changes(Map<Change, ChangeType> list) {
			super(list);
		}

		public ChangeDetector.Changes getAllChangesByFieldType(Class classType) {

			Map<Change, ChangeType> result =  this.entrySet().stream()
					.filter(changeTypeChangeEntry -> changeTypeChangeEntry.getKey().getField().getType().equals(classType))
					.collect(Collectors.toMap(changeTypeChangeEntry -> changeTypeChangeEntry.getKey(),
							changeTypeChangeEntry -> changeTypeChangeEntry.getValue()));
			return new Changes(result);
		}

		public ChangeDetector.Changes getAllChangesByDeclaringClassAndFilter(Class classType, Class filterFieldType) {
			Map<Change, ChangeType> result =  this.entrySet().stream()
					.filter(changeTypeChangeEntry -> changeTypeChangeEntry.getKey().getField().getDeclaringClass()
							.equals(classType))
					.filter(changeChangeTypeEntry -> !changeChangeTypeEntry.getKey().getField().getType()
							.equals(filterFieldType))
					.collect(Collectors.toMap(changeTypeChangeEntry -> changeTypeChangeEntry.getKey(),
							changeTypeChangeEntry -> changeTypeChangeEntry.getValue()));
			return new Changes(result);
		}
	}

	public ChangeDetector() {
	}

	public Changes detect(Object currentState, Object newState) throws ConverterException {
		try {
			var changes = new Changes();
			for (Field field : List.of(newState.getClass().getDeclaredFields())) {
				Object currentValue = field.get(currentState);
				Object newValue = field.get(newState);
				ChangeType changeType = null;

				log.debug("looking for changes in current: '" + currentValue + "' into: '" + field + "'");

				if(newValue instanceof ZdsRol) {
					String verwerkingsoort = ((ZdsRol) newValue).getVerwerkingssoort();
					switch(verwerkingsoort) {
						case "W":
							changes.put(new Change(field, changeType, field.get(currentState), field.get(newState)), ChangeType.CHANGED);
							break;
						case "T":
							if(!newValue.equals(currentValue)) {
								changes.put(new Change(field, changeType, field.get(currentState), field.get(newState)), ChangeType.NEW);
							}
							break;
						case "V":
							changes.put(new Change(field, changeType, field.get(currentState), field.get(newState)), ChangeType.DELETED);
							break;
					}
				} else {
					if (currentValue == null && newValue != null) {
						changeType = ChangeType.NEW;
					}
					else if (currentValue != null && newValue == null) {
						changeType = ChangeType.DELETED;
					}
					else if (currentValue != null && !currentValue.equals(newValue)) {
						changeType = ChangeType.CHANGED;
					}

					if (changeType != null) {
						changes.put(new Change(field, changeType, field.get(currentState), field.get(newState)), changeType);
					}
				}
			}
			return changes;
		} catch (IllegalAccessException iae) {
			throw new ConverterException("fout bij het detecteren van de verschillende tussen de objecten", iae);
		}
	}

	public Map<Change, ChangeType> filterChangesByType(Map<Change, ChangeType> changes, ChangeType changeType) {

		return changes.entrySet().stream()
				.filter(changeTypeChangeEntry -> changeTypeChangeEntry.getValue().equals(changeType))
				.collect(Collectors.toMap(changeTypeChangeEntry -> changeTypeChangeEntry.getKey(),
						changeTypeChangeEntry -> changeTypeChangeEntry.getValue()));

	}
}
