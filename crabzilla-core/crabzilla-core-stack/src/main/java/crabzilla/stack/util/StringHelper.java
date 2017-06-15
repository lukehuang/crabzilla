package crabzilla.stack.util;

import crabzilla.model.AggregateRoot;
import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class StringHelper {

	static final String COMMAND_HANDLER = "apply-cmd-";
	static final String EVENTS_HANDLER = "%s-apply-events";

	public String commandHandlerId(Class<? extends AggregateRoot> aggregateRootClass) {
		return COMMAND_HANDLER + camelCaseToSnakeCase(aggregateRootClass.getSimpleName());
	}

	public  String eventsHandlerId(String bcName) {
		return String.format(EVENTS_HANDLER, bcName);
	}

	public String aggregateRootId(Class<? extends AggregateRoot> aggregateRootClass) {
		return camelCaseToSnakeCase(aggregateRootClass.getSimpleName());
	}

	private String camelCaseToSnakeCase(String start) {
		Matcher m = Pattern.compile("(?<=[a-z])[A-Z]").matcher(start);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "_" + m.group());
		}
		m.appendTail(sb);
		return sb.toString().toLowerCase();
	}



  public String snakeCaseToCamelCase(String start) {
		StringBuffer sb = new StringBuffer();
		for (String s : start.split("_")) {
			sb.append(Character.toUpperCase(s.charAt(0)));
			if (s.length() > 1) {
				sb.append(s.substring(1, s.length()).toLowerCase());
			}
		}
		return sb.toString().toLowerCase();
	}

	public String camelizedCron(String cron) {
		return cron.replace(' ', '+');
	}

}
