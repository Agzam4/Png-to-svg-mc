package svg;

import java.util.ArrayList;
import java.util.HashMap;

import logic.Strings;
import main.Main;

public class SvgElement {

	public final String tag;
	
	public SvgElement(String tag) {
		this.tag = tag;
	}
	
	private HashMap<String, String> attributes = new HashMap<>();
	private ArrayList<SvgElement> elements = new ArrayList<>();
	
	public String attribute(String attribute) {
		return attributes.get(attribute);
	}

	public SvgElement attribute(String attribute, String value) {
		if(!attributes.containsKey(attribute)) {
			attributes.put(attribute, value);
			return this;
		}
		attributes.replace(attribute, value);
		return this;
	}
	
	public SvgElement attribute(String attribute, Object value) {
		return attribute(attribute, Strings.toString(value));
	}

	public SvgElement attribute(String attribute, String value, Object... args) {
		for (int i = 0; i < args.length; i++) {
			value = value.replaceFirst("@", Strings.toString(args[i]));
		}
		return attribute(attribute, value);
	}

	public SvgElement add(SvgElement element) {
		elements.add(element);
		return this;
	}
	
	@Override
	public String toString() {
		return toString("");
	}
	
	public String toString(String before) {
		boolean newLines = Main.svgTab != null;
		
		StringBuilder sb = new StringBuilder(before);
		sb.append('<');
		sb.append(tag);
		
		for (var attrib : attributes.entrySet()) {
			sb.append(' ');
			sb.append(attrib.getKey());
			sb.append("=\"");
			sb.append(attrib.getValue());
			sb.append('\"');
		}
		
		if(elements.size() == 0) {
			sb.append("/>");
			return sb.toString();
		}

		sb.append('>');
		for (SvgElement element : elements) {
			if(newLines) sb.append('\n');
			sb.append(element.toString(Main.svgTab == null ? "" : (before + Main.svgTab)));
		}
		if(newLines) sb.append('\n');
		sb.append(before);
		sb.append("</");
		sb.append(tag);
		sb.append('>');
		
		return sb.toString();
	}
}
