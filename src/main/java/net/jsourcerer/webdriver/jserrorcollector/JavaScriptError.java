package net.jsourcerer.webdriver.jserrorcollector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * Holds information about a JavaScript error that has occurred in the browser.
 * This can be currently only used with the {@link FirefoxDriver} (see {@link #addExtension(FirefoxProfile)}.
 * @author Marc Guillemot
 * @version $Revision:  $
 */
public class JavaScriptError {
	private final String errorCategory;
	private final String errorMessage;
	private final String url;
	private final String sourceName;
	private final Integer lineNumber;
	private final Integer columnNumber;
	private final String console;
	private final String stack;

	JavaScriptError(final Map<String, ? extends Object> map) {
		errorCategory = fromFlag(map.get("errorCategory"));
		errorMessage = (String) map.get("errorMessage");
		url = (String) map.get("url");
		sourceName = (String) map.get("sourceName");
		lineNumber = map.containsKey("lineNumber") ? ((Number) map.get("lineNumber")).intValue() : null;
		columnNumber = map.containsKey("columnNumber") ? ((Number) map.get("columnNumber")).intValue() : null;
		console = (String) map.get("console");
		stack = (String) map.get("stack");
	}

	private String fromFlag(final Object errorCategory) {
		try {
			int flag = ((Number)errorCategory).byteValue();
			switch (flag) {
				case 0: return "Error";
				case 1: return "Warning";
				case 2: return "Exception";
				case 4: return "Strict";
				case 8: return "Info";
				default:
					return "Error";
			}
		} catch (Exception e) {
			if (errorCategory != null) {
				return String.valueOf(errorCategory);
			}
		}
		return null;
	}

	public String getErrorCategory() {
		return errorCategory;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getUrl() {
		return url;
	}

	public String getStack() {
		return stack;
	}

	/**
	 * If Firebug is installed and active, this will contain the content of the Firebug Console since
	 * the previous JavaScript error.
	 * @return
	 */
	public String getConsole() {
		return console;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errorCategory == null) ? 0 :errorCategory.hashCode());
		result = prime * result + ((console == null) ? 0 : console.hashCode());
		result = prime * result
				+ ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((lineNumber == null) ? 0 : lineNumber.hashCode());
		result = prime * result + ((columnNumber == null) ? 0 : columnNumber.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((stack == null) ? 0 : stack.hashCode());
		result = prime * result
				+ ((sourceName == null) ? 0 : sourceName.hashCode());

		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JavaScriptError other = (JavaScriptError) obj;
		if (console == null) {
			if (other.console != null) {
				return false;
			}
		} else if (!console.equals(other.console)) {
			return false;
		}
		if (errorMessage == null) {
			if (other.errorMessage != null) {
				return false;
			}
		} else if (!errorMessage.equals(other.errorMessage)) {
			return false;
		}
		if (lineNumber == null) {
			if (other.lineNumber != null) {
				return false;
			}
		} else if (!lineNumber.equals(other.lineNumber)) {
			return false;
		}
		if (columnNumber == null) {
			if (other.columnNumber != null) {
				return false;
			}
		} else if (!columnNumber.equals(other.columnNumber)) {
			return false;
		}
		if (sourceName == null) {
			if (other.sourceName != null) {
				return false;
			}
		} else if (!sourceName.equals(other.sourceName)) {
			return false;
		}
		if (errorCategory == null) {
			if (other.errorCategory != null) {
				return false;
			}
		} else if (!errorCategory.equals(other.errorCategory)) {
			return false;
		}
		if (url == null) {
			if (other.url != null) {
				return false;
			}
		} else if (!url.equals(other.url)) {
			return false;
		}
		if (stack == null) {
			if (other.stack != null) {
				return false;
			}
		} else if (!stack.equals(other.stack)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(String.format("[%s]: \"%s\"", errorCategory, errorMessage));
		if (sourceName != null && sourceName.length() > 0) {
			s.append(" @").append(sourceName);
			if (lineNumber != null) {
				s.append(":").append(lineNumber);
			}
			if (columnNumber != null) {
				s.append(":").append(columnNumber);
			}
		}
		if (url != null) {
			s.append(String.format(" (URL: %s)", url));
		}
		if (stack != null) {
			s.append("\n").append("Stack: ").append(stack.trim());
		}
		if (console != null) {
			s.append("\n").append("Console: ").append(console);
		}
		return s.toString();
	}

	/**
	 * Gets the collected JavaScript errors that have occurred since last call to this method.
	 * @param driver the driver providing the possibility to retrieved JavaScript errors (see {@link #addExtension(FirefoxProfile)}.
	 * @return the errors or an empty list if the driver doesn't provide access to the JavaScript errors
	 */
	@SuppressWarnings("unchecked")
	public static List<JavaScriptError> readErrors(final WebDriver driver) {
		final String script = "return window.JSErrorCollector_errors ? window.JSErrorCollector_errors.pump() : []";
		final List<Object> errors = (List<Object>) ((JavascriptExecutor) driver).executeScript(script);
		final List<JavaScriptError> response = new ArrayList<JavaScriptError>();
		for (final Object rawError : errors) {
			response.add(new JavaScriptError((Map<String, ? extends Object>) rawError));
		}

		return response;
	}

	/**
	 * Adds the Firefox extension collecting JS errors to the profile what allows later use of {@link #readErrors(WebDriver)}.
	 * <p>
	 * Example:<br>
	 * <code><pre>
	 * final FirefoxProfile profile = new FirefoxProfile();
	 * JavaScriptError.addExtension(profile);
	 * final WebDriver driver = new FirefoxDriver(profile);
	 * </pre></code>
	 * @param ffProfile the Firefox profile to which the extension should be added.
	 * @throws IOException in case of problem
	 */
	public static void addExtension(final FirefoxProfile ffProfile) throws IOException {
		ffProfile.addExtension(JavaScriptError.class, "JSErrorCollector.xpi");
	}
}
