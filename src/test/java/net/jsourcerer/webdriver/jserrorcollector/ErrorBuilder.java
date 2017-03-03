package net.jsourcerer.webdriver.jserrorcollector;

import com.google.common.collect.Maps;

import java.util.Map;

public class ErrorBuilder {
    private final Map<String,Object> data;

    public ErrorBuilder() {
        this.data = Maps.newHashMap();
    }

    public ErrorBuilder errorCategory(String errorCategory) {
        data.put("errorCategory", errorCategory);
        return this;
    }

    public ErrorBuilder errorMessage(String errorMessage) {
        data.put("errorMessage", errorMessage);
        return this;
    }

    public ErrorBuilder source(String url) {
        data.put("sourceName", url);
        return this;
    }

    public ErrorBuilder url(String url) {
        data.put("url", url);
        return this;
    }

    public ErrorBuilder sourceAndUrl(String sourceName) {
        return source(sourceName).url(sourceName);
    }

    public ErrorBuilder lineNumber(int lineNumber) {
        data.put("lineNumber", lineNumber);
        return this;
    }

    public ErrorBuilder console(String console) {
        data.put("console", console);
        return this;
    }

    public JavaScriptError build() {
        return new JavaScriptError(data);
    }
}
