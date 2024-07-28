package main;

import svg.SvgElement;

public class TestDrive {

	public static void main(String[] args) {
		
		SvgElement element = new SvgElement("svg");
		element.attribute("version", "2.0");
		element.attribute("xmlns", "http://www.w3.org/2000/svg");
		
		SvgElement layer = new SvgElement("g");
		layer.add(new SvgElement("hm"));
		element.add(layer);
		
		System.out.println(element.toString(""));
	}
}