package main;

public class Log {
	
	public static enum Errors {
		
		
	}

	public static void info(Object... args) {
		System.out.println(getCaller() + format("", args));
	}

	public static void info(String text, Object... args) {
		System.out.println(getCaller() + format(text, args));
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
	
	public static void err(Errors error) {
		System.err.println(error);
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
		formated.append("\033[0m ");
		return formated.toString();
	}
	
	private static String toString(Object object) {
		if(object == null) return "\033[1;35mnull\033[0m";
		if(object instanceof Boolean) {
			Boolean b = (Boolean) object;
			return b ? "\033[1;32mtrue \033[0m" : "\033[1;31mfalse\033[0m";
		}
		
		return object.toString();
	}
}
