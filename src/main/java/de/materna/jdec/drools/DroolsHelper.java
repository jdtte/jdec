package de.materna.jdec.drools;

import org.apache.log4j.Logger;
import org.kie.dmn.api.core.DMNUnaryTest;

import java.util.LinkedList;
import java.util.List;

public class DroolsHelper {
	private static final Logger log = Logger.getLogger(DroolsHelper.class);

	/**
	 * Drools does not return a correctly typed list of allowed values
	 * This method converts the list of allowed values manually
	 *
	 * @param type    Correct type of the allowed values
	 * @param options List of all allowed values
	 */
	public static List<Object> convertOptions(String type, List<DMNUnaryTest> options) {
		List<Object> convertedOptions = new LinkedList<>();
		for (DMNUnaryTest option : options) {
			switch (type) {
				case "string":
				case "date":
				case "time":
				case "dateTime":
					// We need to remove the quotation marks from the allowed value
					convertedOptions.add(option.toString().substring(1, option.toString().length() - 1));
					continue;
				case "number":
					// According to the dmn specification, input ranges like [0..999] could be specified as an allowed value.
					// We'll catch the exception for now.
					try {
						convertedOptions.add(Double.valueOf(option.toString()));
					}
					catch (NumberFormatException ignored) {
					}
					continue;
			}
		}
		return convertedOptions;
	}
}
