package com.github.kolandroid.kol.android.controllers.web;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.controller.UpdatableModelController;
import com.github.kolandroid.kol.android.game.GameScreen;
import com.github.kolandroid.kol.android.screen.DialogScreen;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.model.models.WebModel;
import com.github.kolandroid.kol.util.Callback;
import com.github.kolandroid.kol.util.Logger;
import com.github.kolandroid.kol.util.Regex;

import java.io.InputStream;
import java.lang.ref.WeakReference;

public class WebController extends UpdatableModelController<WebModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -8051419766943400254L;

    private transient WebView web;

    private String inputChanges;

    public WebController(WebModel model) {
        super(model);

        this.inputChanges = "{}";
    }

    public static String difference(String str1, String str2) {
        if (str1 == null) {
            return str2;
        }
        if (str2 == null) {
            return str1;
        }
        int at = indexOfDifference(str1, str2);
        if (at == -1) {
            return "";
        }
        return str2.substring(at);
    }

    public static int indexOfDifference(String str1, String str2) {
        if (str1 == str2) {
            return -1;
        }
        if (str1 == null || str2 == null) {
            return 0;
        }
        int i;
        for (i = 0; i < str1.length() && i < str2.length(); ++i) {
            if (str1.charAt(i) != str2.charAt(i)) {
                break;
            }
        }
        if (i < str2.length() || i < str1.length()) {
            return i;
        }
        return -1;
    }

    public void updateModel(WebModel base) {
        super.updateModel(base);
        if (web != null) {
            //inputChanges = "{}";
            loadContent(base);
        }
    }

    @Override
    public int getView() {
        return getModel().visitType(new WebModel.WebModelTypeVisitor<Integer>() {
            public Integer forRegular() {
                return R.layout.fragment_web_screen;
            }

            public Integer forSmall() {
                return R.layout.dialog_web_screen;
            }

            public Integer forResults() {
                return R.layout.dialog_results_screen;
            }

            public Integer forExternal() {
                return R.layout.fragment_web_screen;
            }
        });
    }

    @Override
    public void chooseScreen(final ScreenSelection choice) {
        getModel().visitType(new WebModel.WebModelTypeVisitor<Void>() {
            public Void forRegular() {
                choice.displayPrimary(WebController.this, false);
                return null;
            }

            public Void forSmall() {
                choice.displayDialog(WebController.this);
                return null;
            }

            public Void forResults() {
                choice.displayDialog(WebController.this);
                return null;
            }

            @Override
            public Void forExternal() {
                choice.displayExternalDialog(WebController.this, true);
                return null;
            }
        });
    }

    @Override
    public void disconnect(Screen host) {
        super.disconnect(host);

        /*
        if(web != null) {
            //Check all input changes in the document, and pass them to reportInputChanges()
            web.loadUrl("javascript:checkInputChanges();");
        }
        */

    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public void connect(View view, WebModel model, final Screen host) {
        WebViewClient client = new WebViewClient() {
            private final Regex INTERNAL_FULL_URL = new Regex("^(https?://)?([^\\.]*\\.)?kingdomofloathing\\.com");

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("data:text/html"))
                    return true;

                url = url.replace("reallyquitefake/", "");
                Logger.log("WebModel", "Request made to " + url);
                if (!getModel().makeRequest(url)) {
                    Logger.log("WedModel", "External request: " + url);

                    // Otherwise, the link is not for a kol page; launch an
                    // external activity
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    host.getActivity().startActivity(intent);
                }

                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.startsWith("data:text/html"))
                    return null;

                if (url.contains(".php")) {
                    if ((url.startsWith("http://") || url.startsWith("https://") || url.startsWith("www"))
                            && !INTERNAL_FULL_URL.matches(url)) {
                        //do not mess with external requests
                        return null;
                    }

                    //All requests to .php must include the proper cookies
                    InputStream result = getModel().makeBlockingRequest(url);
                    return new WebResourceResponse("text/html; charset=UTF-8", null, result);
                } else {
                    return null;
                }
            }
        };

        web = (WebView) view.findViewById(R.id.webview);

        web.getSettings().setJavaScriptEnabled(true);

        Logger.log("WebController", "Adding html with input cache: " + inputChanges);
        web.addJavascriptInterface(new JavaScriptInterface(host), "ANDROIDAPP");
        web.setWebViewClient(client);

        loadContent(model);
    }

    private void loadContent(WebModel model) {
        boolean allowZoom = model.visitType(new WebModel.WebModelTypeVisitor<Boolean>() {
            @Override
            public Boolean forRegular() {
                return true;
            }

            @Override
            public Boolean forSmall() {
                return false;
            }

            @Override
            public Boolean forResults() {
                return false;
            }

            @Override
            public Boolean forExternal() {
                return true;
            }
        });

        web.getSettings().setBuiltInZoomControls(allowZoom);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);

        String html = model.getHTML();
        Logger.log("WebController", "Loading content of size " + html.length());
        web.loadDataWithBaseURL(model.getURL(), html, "text/html", null,
                null);

        web.invalidate();

    }

    class JavaScriptInterface {
        private final WeakReference<Screen> host;

        public JavaScriptInterface(Screen host) {
            this.host = new WeakReference<>(host);
        }

        @android.webkit.JavascriptInterface
        public void debug(String text) {
            Logger.log("WebController Javascript", text);
        }

        @android.webkit.JavascriptInterface
        public void processFormData(String formData) {
            Logger.log("WebController", "Form data: " + formData);
            getModel().makeRequest(formData);
        }

        @android.webkit.JavascriptInterface
        public void refreshStatsPane() {
            Screen host = this.host.get();
            if (host == null) return;

            if (host instanceof GameScreen) {
                ((GameScreen) host).refreshStatsPane();
            } else {
                Logger.log("WebController", "Pane refresh triggered by javascript, but host was " + host);
            }
        }

        @android.webkit.JavascriptInterface
        public void displayFormNumeric(String question, String button, final String onResult) {
            Logger.log("WebController", "Querying numeric value: " + question);

            TextInputController input = new TextInputController(button, new Callback<String>() {
                @Override
                public void execute(String result) {
                    result = result.replace("\"", ""); //attempt to stop OTHER javascript injection
                    result = onResult.replace("#VAL", "\"" + result + "\"");
                    Logger.log("WebController", "Result: [" + result + "]");
                    web.loadUrl(result);
                }
            });
            Screen host = this.host.get();
            if (host == null) return;
            DialogScreen.display(input, host, question);
        }


        @android.webkit.JavascriptInterface
        public void reportInputChanges(String changes) {
            Logger.log("WebController", "Cached input changes: " + changes);
            inputChanges = changes;
        }

        @android.webkit.JavascriptInterface
        public String getInputChanges() {
            return inputChanges;
        }

        @android.webkit.JavascriptInterface
        public void saveHtml(String html) {
            Logger.log("WebController", "Found html [size " + html.length() + "]");
            String oldHtml = getModel().getHTML();
            if (!oldHtml.equals(html)) {
                Logger.logBig("WebController", "New:" + html);
                Logger.logBig("WebController", "Old:" + oldHtml);
                Logger.log("WebController", "Saving changed html");
                getModel().setFixedHTML(html);
            }
        }
    }

}
