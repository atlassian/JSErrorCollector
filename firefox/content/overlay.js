var JSErrorCollector = new function() {
    var list = [];
    this.collectedErrors = {
        push: function (jsError) {
            list.push(jsError);
        },
        pump: function() {
            var resp = [];
            for (var i=0; i<list.length; ++i) {
                var scriptError = list[i];
                resp[i] = {
                        errorCategory: scriptError.errorCategory,
                        errorMessage: scriptError.errorMessage,
                        sourceName: scriptError.sourceName,
                        lineNumber: scriptError.lineNumber,
                        url: scriptError.sourceUrl,
                        console: scriptError.console
                        };
            }
            list = [];
            return resp;
        },
        toString: function() {
            var s = "";
            for (var i=0; i<list.length; ++i) {
                s += i + ": " + list[i] + "\n";
            }
            return s;
        }
    };

    this.onLoad = function(event) {
        // initialization code
        this.initialize(event);
        this.initialized = true;
    };

    this.initialize = function(event) {
        var windowContent = window.getBrowser();

        var consoleService = Components.classes["@mozilla.org/consoleservice;1"].getService().QueryInterface(Components.interfaces.nsIConsoleService);
        if (consoleService) {
            consoleService.registerListener(JSErrorCollector_ErrorConsoleListener);
        }

        var onPageLoad = function(aEvent) {
            var doc = aEvent.originalTarget;
            var win = doc.defaultView;
            if (win) {
                win.wrappedJSObject.JSErrorCollector_errors = Components.utils.cloneInto(JSErrorCollector.collectedErrors, win.wrappedJSObject, {cloneFunctions: true});
            }
        };

        windowContent.addEventListener("load", onPageLoad, true);
    };

    this.addError = function(error) {
        this.collectedErrors.push(error);

        var labelField = document.getElementById("JSErrorCollector-nb");
        labelField.nb = labelField.nb || 0;
        labelField.nb++;
        labelField.value = labelField.nb;
    }
};

//Error console listener
var JSErrorCollector_ErrorConsoleListener = {
    observe: function(consoleMessage) {
        // attempts to get the URL where the error occurred
        function getUrl() {
            var url = "<<unknown>>";
            try {
                url = window.top.getBrowser().selectedBrowser.contentWindow.location.href;
            } catch (e) {}
            return url;
        }

        // tries to get content from Firebug's console if it exists
        function getFirebugContent() {
            var consoleContent = null;
            var fb = window.Firebug;
            try {
                if (fb && fb.currentContext) {
                    var doc = fb.getPanel("console").document;
                    var logNodes = doc.querySelectorAll(".logRow .logContent span");
                    var consoleLines = [];
                    for (var i=0; i<logNodes.length; ++i) {
                        var logNode = logNodes[i];
                        if (!logNode.JSErrorCollector_extracted) {
                            consoleLines.push(logNodes[i].textContent);
                            logNode.JSErrorCollector_extracted = true;
                        }
                    }

                    consoleContent = consoleLines.join("\n");
                }
            } catch (e) {
                consoleContent = "Error extracting content of Firebug console: " + e.message;
            }
            return consoleContent;
        }

        if (document && consoleMessage) {
            // Try to convert the error to a script error
            try {
                var scriptError = consoleMessage.QueryInterface(Components.interfaces.nsIScriptError);

                if (scriptError.sourceName) {
                    if (scriptError.sourceName.indexOf("about:") == 0 || scriptError.sourceName.indexOf("chrome:") == 0) {
                        return; // not interested in internal errors
                    }
                }

                // We're just looking for content JS errors (see https://developer.mozilla.org/en/XPCOM_Interface_Reference/nsIScriptError#Categories)
                if (scriptError.category == "content javascript") {
                    var err = {
                        errorMessage: scriptError.errorMessage,
                        errorCategory: scriptError.category || scriptError.errorCategory,
                        sourceName: scriptError.sourceName,
                        lineNumber: scriptError.lineNumber,
                        sourceUrl: getUrl(),
                        console: getFirebugContent()
                    };
                    console.log("collecting JS error", err);
                    JSErrorCollector.addError(err);
                }
            }
            catch (exception) {
                // ignore
            }
        }

        return false;
    }
};

window.addEventListener("load", function(e) { JSErrorCollector.onLoad(e); }, false);
