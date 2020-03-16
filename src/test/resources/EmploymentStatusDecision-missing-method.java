package de.materna.jdec.java.test;

import de.materna.jdec.java.DecisionModel;
import de.materna.jdec.model.ComplexInputStructure;
import de.materna.jdec.model.InputStructure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmploymentStatusDecision extends DecisionModel {
	@Override
	public Map<String, Object> executeDecision(Map<String, Object> inputs) {
		Map<String, Object> output = new HashMap<>();
		output.put("Employment Status Statement", "You are " + inputs.get("Employment Status"));
		return output;
	}
}