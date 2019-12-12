package de.materna.jdec.drools;

import de.materna.jdec.DecisionSession;
import de.materna.jdec.model.ModelContext;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.event.*;
import org.kie.dmn.core.ast.DMNFunctionDefinitionEvaluator;

import java.util.*;

public class DroolsDebugger {
	private DecisionSession decisionSession;

	private Map<String, Map<String, Object>> decisions = new LinkedHashMap<>();
	private Stack<String> decisionStack = new Stack<>();
	private List<String> messages = new LinkedList<>();
	private Stack<ModelContext> contextStack;

	private DMNRuntimeEventListener listener;

	public DroolsDebugger(DecisionSession decisionSession) {
		this.decisionSession = decisionSession;
	}

	public void start() {
		listener = new DMNRuntimeEventListener() {
			@Override
			public void beforeEvaluateDecision(BeforeEvaluateDecisionEvent event) {
				decisionStack.push(event.getDecision().getName());
				decisions.put(decisionStack.peek(), new LinkedHashMap<>());
				contextStack = new Stack<>();
			}

			@Override
			public void beforeEvaluateContextEntry(BeforeEvaluateContextEntryEvent event) {
				// We create a context and put it on the stack.
				// The name allows us to set the value to a higher context level.
				ModelContext context = new ModelContext();
				context.setName(event.getVariableName());
				contextStack.push(context);
			}

			@Override
			public void afterEvaluateContextEntry(AfterEvaluateContextEntryEvent event) {
				// When we leave the context, we remove it from the stack.
				// If the value has not yet been set by a higher context level, we'll do it.
				// Otherwise, we could overwrite context that we cannot see from this level.
				ModelContext context = contextStack.pop();
				if (context.getValue() == null) {
					context.setValue(cleanResult(event.getExpressionResult()));
				}

				// When we have reached the bottom context, we attach it to the decision.
				if (contextStack.size() == 0) {
					decisions.get(decisionStack.peek()).put(context.getName(), context.getValue());
					return;
				}

				// If we haven't reached the bottom context, we attach the context to the parent context.
				ModelContext parentContext = contextStack.peek();
				// If this is the first value, we'll create a map.
				if (parentContext.getValue() == null) {
					Map<String, Object> value = new LinkedHashMap<>();
					value.put(context.getName(), context.getValue());
					parentContext.setValue(value);
					return;
				}
				Map<String, Object> parentContextValue = (Map<String, Object>) parentContext.getValue();
				parentContextValue.put(context.getName(), context.getValue());
			}

			@Override
			public void afterEvaluateDecision(AfterEvaluateDecisionEvent event) {
				for (DMNMessage message : event.getResult().getMessages()) {
					// noinspection deprecation
					messages.add(message.getMessage());
				}

				decisionStack.pop();
			}
		};
		decisionSession.getRuntime().addListener(listener);
	}

	public void stop() {
		decisionSession.getRuntime().removeListener(listener);
	}

	/**
	 * We need to remove all functions because serializing them is not possible.
	 */
	private Object cleanResult(Object result) {
		if (result instanceof Map) {
			Map<String, Object> results = (Map<String, Object>) result;

			Map<String, Object> cleanedResults = new LinkedHashMap<>();
			for (Map.Entry<String, Object> entry : results.entrySet()) {
				cleanedResults.put(entry.getKey(), cleanResult(entry.getValue()));
			}
			return cleanedResults;
		}

		if (result instanceof DMNFunctionDefinitionEvaluator.DMNFunction) {
			return "__FUNCTION_DEFINITION__";
		}

		return result;
	}

	public Map<String, Map<String, Object>> getDecisions() {
		return decisions;
	}

	public List<String> getMessages() {
		return messages;
	}
}
