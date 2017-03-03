package net.jsourcerer.webdriver.jserrorcollector;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * @author Marc Guillemot
 */
public class SimpleTest {
	private static final String EX = "Exception";
	private static final String ERR = "Error";

	private final String urlSimpleHtml = getResource("simple.html");
	private final JavaScriptError errorSimpleHtml = new ErrorBuilder()
			.errorMessage("TypeError: null has no properties")
			.sourceAndUrl(urlSimpleHtml)
			.lineNumber(9)
			.columnNumber(1)
			.errorCategory(EX)
			.build();

	private final String urlWithNestedFrameHtml = getResource("withNestedFrame.html");
	private final JavaScriptError errorWithNestedFrameHtml = new ErrorBuilder().errorMessage("TypeError: \"foo\".notHere is not a function").sourceAndUrl(urlWithNestedFrameHtml).lineNumber(7).columnNumber(1).errorCategory(EX).build();
	private final JavaScriptError errorInNestedFrame = new ErrorBuilder().errorMessage("TypeError: null has no properties").source(urlSimpleHtml).url(urlWithNestedFrameHtml).lineNumber(9).columnNumber(1).errorCategory(EX).build();

	private final String urlWithPopupHtml = getResource("withPopup.html");
	private final String urlPopupHtml = getResource("popup.html");
	private final JavaScriptError errorPopupHtml = new ErrorBuilder().errorMessage("ReferenceError: error is not defined").source(urlPopupHtml).url(urlWithPopupHtml).lineNumber(5).columnNumber(3).errorCategory(EX).build();

	private final String urlWithExternalJs = getResource("withExternalJs.html");
	private final String urlExternalJs = getResource("external.js");
	private final JavaScriptError errorExternalJs = new ErrorBuilder().errorMessage("TypeError: document.notExisting is undefined").source(urlExternalJs).url(urlWithExternalJs).lineNumber(1).columnNumber(1).errorCategory(EX).build();

	private final String urlThrowing = getResource("throwing.html");
	private final JavaScriptError errorThrowingErrorObject = new ErrorBuilder().errorMessage("Error: an explicit error object!").sourceAndUrl(urlThrowing).lineNumber(9).columnNumber(11).errorCategory(EX).build();
	private final JavaScriptError errorThrowingPlainObject = new ErrorBuilder().errorMessage("uncaught exception: a plain JS object!").url(urlThrowing).lineNumber(0).columnNumber(0).errorCategory(ERR).buildWithStack("undefined");
	private final JavaScriptError errorThrowingString = new ErrorBuilder().errorMessage("uncaught exception: a string error!").url(urlThrowing).lineNumber(0).columnNumber(0).errorCategory(ERR).buildWithStack("undefined");

	private static WebDriver driver;

	@BeforeClass
	public static void setup() throws IOException {
		driver = buildFFDriver();
	}

	@AfterClass
	public static void teardown() {
		driver.quit();
	}

	@Test
	public void simple() throws Exception {
		driver.get(urlSimpleHtml);

		final List<JavaScriptError> expectedErrors = Arrays.asList(errorSimpleHtml);
		final List<JavaScriptError> jsErrors = JavaScriptError.readErrors(driver);
		assertEquals(expectedErrors.toString(), jsErrors.toString());
	}

	@Test
	public void errorInNestedFrame() throws Exception {
		final List<JavaScriptError> expectedErrors = Arrays.asList(errorWithNestedFrameHtml, errorInNestedFrame);

		driver.get(urlWithNestedFrameHtml);

		final List<JavaScriptError> jsErrors = JavaScriptError.readErrors(driver);
		assertEquals(expectedErrors.toString(), jsErrors.toString());
	}

	@Test
	public void errorInPopup() throws Exception {
		final List<JavaScriptError> expectedErrors = Arrays.asList(errorPopupHtml);

		driver.get(urlWithPopupHtml);
		driver.findElement(By.tagName("button")).click();

		final List<JavaScriptError> jsErrors = JavaScriptError.readErrors(driver);
		assertEquals(expectedErrors.toString(), jsErrors.toString());
	}

	@Test
	public void errorInExternalJS() throws Exception {
		final List<JavaScriptError> expectedErrors = Arrays.asList(errorExternalJs);

		driver.get(urlWithExternalJs);

		final List<JavaScriptError> jsErrors = JavaScriptError.readErrors(driver);
		assertEquals(expectedErrors.toString(), jsErrors.toString());
	}

	@Test
	public void errorTypes() throws Exception {
		final List<JavaScriptError> expectedErrors = Arrays.asList(errorThrowingErrorObject, errorThrowingPlainObject, errorThrowingString);

		driver.get(urlThrowing);

		final List<JavaScriptError> jsErrors = JavaScriptError.readErrors(driver);
		assertEquals(expectedErrors.toString(), jsErrors.toString());
	}


	private static WebDriver buildFFDriver() throws IOException {
		FirefoxProfile ffProfile = new FirefoxProfile();
		ffProfile.setPreference("extensions.JSErrorCollector.console.logLevel", "all");
		ffProfile.addExtension(new File("firefox")); // assuming that the test is started in project's root

		return new FirefoxDriver(ffProfile);
	}

	private String getResource(final String string) {
		String resource = getClass().getClassLoader().getResource(string).toExternalForm();
		if (resource.startsWith("file:/") && !resource.startsWith("file:///")) {
			resource = "file://" + resource.substring(5);
		}
		return resource;
	}
}
