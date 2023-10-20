package svg;

import java.util.ArrayList;
import java.util.HashMap;

import logic.Strings;
import main.Main;

/**
 * Simple SVG elements builder that creates elements like this:<br>
 * <code>&lt;tag attribute="value">...&lt;/tag></code>
 * @author Agzam4
 */
public class SvgElement {

	public final String tag;
	
	/**
	 * Create new SVG element like <code>&lt;tag>&lt;/tag></code>
	 * @param tag - tag of SVG element
	 */
	public SvgElement(String tag) {
		this.tag = tag;
	}
	
	private HashMap<String, String> attributes = new HashMap<>(); // attributes of element
	private ArrayList<SvgElement> elements = new ArrayList<>(); // children of element
	
	/**
	 * @param attribute - attribute name
	 * @return attribute value by name
	 */
	public String attribute(String attribute) {
		return attributes.get(attribute);
	}

	/**
	 * Return attribute value by name
	 * @param attribute - attribute name
	 * @param value - 
	 * @return self
	 */
	public SvgElement attribute(String attribute, String value) {
		if(!attributes.containsKey(attribute)) {
			attributes.put(attribute, value);
			return this;
		}
		attributes.replace(attribute, value);
		return this;
	}
	
	/**
	 * 
	 * @param attribute - attribute name
	 * @param value - attribute value
	 * @return self
	 */
	public SvgElement attribute(String attribute, Object value) {
		return attribute(attribute, Strings.toString(value));
	}

	/**
	 * Set attribute to element
	 * @param attribute - attribute name
	 * @param value - attribute value, where all <code>@</code> will be replaced on <code>args<code>
	 * @param args
	 * @return self
	 */
	public SvgElement attribute(String attribute, String value, Object... args) {
		for (int i = 0; i < args.length; i++) {
			value = value.replaceFirst("@", Strings.toString(args[i]));
		}
		return attribute(attribute, value);
	}

	/**
	 * Add new child to element
	 * @param child - child to add
	 * @return self
	 */
	public SvgElement add(SvgElement child) {
		elements.add(child);
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
