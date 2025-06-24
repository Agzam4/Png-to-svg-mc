package main;

import java.util.HashMap;
import java.util.Stack;

public class Log {

	private static HashMap<String, Colors> colors = new HashMap<>();
	
	static {
		for (var c : Colors.values()) {
			colors.put(c.name, c);
//			System.out.println("added: " + c.toString()  + c.name + "\033[0m");
		}
	}
	
	public static enum Colors {

		red(31),
		green(32),
		royal(34),
		yellow(33),
		magenta(35),
		gray(90),
		lightGreen(92),
		lightYellow(93),
		blue(94),
		cyan(96),
		reset(0);

		String color, name = name();
		
		private Colors(int code) {
			color = code == 0 ? "\033[0m" : "\033[1;" + code + "m";
			StringBuilder name = new StringBuilder();
			for (int i = 0; i < this.name.length(); i++) {
				char c = this.name.charAt(i);
				if(Character.isUpperCase(c)) {
					name.append('-');
					c = Character.toLowerCase(c);
				}
				name.append(c);
			}
			this.name = name.toString();
		}
		
		@Override
		public String toString() {
			return color;
		}
	}

	public static void info(Object... args) {
		System.out.println(getCaller() + paint(format("", args)));
	}

	public static void info(String text, Object... args) {
		System.out.println(getCaller() + paint(format(text, args)));
	}

	public static void warn(Object... args) {
		System.out.println(getCaller() + paint(format("[yellow]", args) + "[]"));
	}
	
	public static void warn(String text, Object... args) {
		System.out.println(getCaller() + paint("[yellow]" + format(text, args) + "[]"));
	}
	
	private static String getCaller() {
		try {
			throw new Exception("Meow");
		} catch (Exception e) {
			StackTraceElement method = e.getStackTrace()[2];
	        StringBuilder sb = new StringBuilder();
	        sb.append('(');
	        if (method.isNativeMethod()) {
	            sb.append("Native Method");
	        } else if (method.getFileName() == null) {
	            sb.append("Unknown Source");
	        } else {
	            sb.append(method.getFileName());
	            if (method.getLineNumber() >= 0) {
	                sb.append(':').append(method.getLineNumber());
	            }
	        }
	        sb.append(')');
	        sb.append(' ');
	        return sb.toString();
		}
	}

	public static void err(String text, Object... args) {
		System.err.println(getCaller() + format(text, args));
	}
	
	private static String format(String str, Object... args) {
		StringBuilder formated = new StringBuilder(str.length());
		int arg = 0;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(c == '@' && arg < args.length) {
				formated.append(toString(args[arg++]));
				continue;
			}
			formated.append(c);
		}
		for (int i = arg; i < args.length; i++) {
			if(formated.length() != 0) formated.append(' ');
			formated.append(toString(args[i]));
		}
		return formated.toString();
	}
	
	private static String paint(String str) {
		Stack<Colors> stack = new Stack<Log.Colors>();
		stack.push(Colors.reset);
		StringBuilder colored = new StringBuilder();
		
		int begin = -1;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			if(c == '[') {
				begin = i+1;
				continue;
			}
			if(c == ']' && begin != -1) {
				String tag = str.substring(begin, i);
				begin = -1;
				if(tag.length() == 0) {
					if(stack.size() > 0) stack.pop();
					if(stack.size() > 0) {
						colored.append(stack.peek());
					} else {
						colored.append("[]");
					}
					continue;
				}
				Colors color =colors.get(tag);
				if(color == null) {
					colored.append('[');
					colored.append(tag);
					colored.append(']');
				} else {
					colored.append(color);
					stack.push(color);
				}
				continue;
			}
			if(begin != -1) continue;
			colored.append(c);
		}
		if(begin != -1) {
			colored.append(str.substring(begin-1, str.length()));
		}
		return colored.toString();
	}
	
	private static String toString(Object object) {
		if(object == null) return "[magenta]null[]";
		if(object instanceof Boolean) {
			Boolean b = (Boolean) object;
			return b ? "[green]true[]" : "[red]false[]";
		}
		
		return object.toString();
	}

}
