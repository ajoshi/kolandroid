package com.github.kolandroid.kol.android.login;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.github.kolandroid.kol.android.BuildConfig;
import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.controller.ModelController;
import com.github.kolandroid.kol.android.controllers.web.WebController;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.screen.ViewScreen;
import com.github.kolandroid.kol.gamehandler.SettingsContext;
import com.github.kolandroid.kol.model.models.WebModel;
import com.github.kolandroid.kol.model.models.login.LoginModel;
import com.github.kolandroid.kol.model.models.login.PasswordHash;
import com.github.kolandroid.kol.util.Logger;

import java.util.ArrayList;

public class LoginController extends ModelController<LoginModel> implements ExpiringController {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -1263867061071715065L;
    private String savedUser = null;
    private boolean enterChatImmediately = true;

    public LoginController(LoginModel model) {
        super(model);
    }

    @Override
    public int getView() {
        return R.layout.login_view;
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayExternal(this);
    }

    @Override
    public void attach(View view, LoginModel model, final Screen host) {
        final EditText userField = (EditText) view
                .findViewById(R.id.login_username);
        final EditText passField = (EditText) view
                .findViewById(R.id.login_password);
        final Button login = (Button) view
                .findViewById(R.id.login_submit);
        final CheckBox configPass = (CheckBox) view.findViewById(R.id.login_config_save_password);
        final CheckBox configChat = (CheckBox) view.findViewById(R.id.login_config_enter_chat);

        final SettingsContext settings = host.getViewContext().getSettingsContext();

        if (settings.get("update_automatically", true)) {
            // Trigger an update check
            getModel().checkAppUpdate();
        }

        userField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String[] passwordHash = settings.get("login_defaultPassword", ":").split(":");
                if (passwordHash.length == 2 && userField.getText().toString().equalsIgnoreCase(passwordHash[0])) {
                    passField.setHint("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"); //unicode dot x10
                } else {
                    passField.setHint("Password");
                }
            }
        });

        userField.setText(settings.get("login_defaultUsername", ""));

        String[] passwordHash = settings.get("login_defaultPassword", ":").split(":");
        if(passwordHash.length == 2) {
            savedUser = passwordHash[0];
        } else {
            savedUser = null;
        }

        configPass.setChecked(settings.get("login_savePassword", true));
        configChat.setChecked(settings.get("login_enterChat", true));


        passField.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    login.performClick();
                    return true;
                }
                return false;
            }

        });

        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String username = userField.getText().toString();

                char[] password = new char[passField.getText().length()];
                passField.getText().getChars(0, password.length, password, 0);

                PasswordHash pass;

                if (username.equals(""))
                    return;

                if (savedUser != null && username.equalsIgnoreCase(savedUser) && (password.length == 0)) {
                    String[] passwordHash = settings.get("login_defaultPassword", ":").split(":");
                    if (passwordHash.length != 2)
                        return;

                    char[] hashChars = new char[passwordHash[1].length()];
                    passwordHash[1].getChars(0, hashChars.length, hashChars, 0);
                    pass = new PasswordHash(hashChars, true);
                } else {
                    if (password.length == 0)
                        return;
                    pass = new PasswordHash(password, false);
                }

                View focus = host.getActivity().getCurrentFocus();
                if (focus != null) {
                    InputMethodManager inputManager = (InputMethodManager) host.getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(focus.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                getModel().login(username, pass);

                settings.set("login_savePassword", configPass.isChecked());
                settings.set("login_enterChat", configChat.isChecked());
                enterChatImmediately = configChat.isChecked();

                if (configPass.isChecked()) {
                    settings.set("login_defaultUsername", username);
                    settings.set("login_defaultPassword", username + ":" + new String(pass.getBaseHash()));
                } else {
                    settings.remove("login_defaultUsername");
                    settings.remove("login_defaultPassword");
                }

                pass.clear();
            }
        });

        Button createAccount = (Button) view.findViewById(R.id.login_create_account);
        createAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getModel().createAccount();
            }
        });

        ArrayList<LoginModel.MagicLoginAction> magicActions = model.getMagicCharacters();
        if (magicActions.size() > 0) {
            View panel = view.findViewById(R.id.login_magic_panel);
            panel.setVisibility(View.VISIBLE);

            ViewGroup container = (ViewGroup) view.findViewById(R.id.login_magic_group);
            for (LoginModel.MagicLoginAction magicAction : magicActions) {
                final LoginModel.MagicLoginAction action = magicAction;
                Button magicButton = new Button(host.getActivity());
                magicButton.setText(action.getCharacter());
                magicButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        action.magicLogin();
                    }
                });
                container.addView(magicButton);
            }
        }

        WebModel announcements = model.getAnnouncementsModel();
        if (announcements == null) {
            View announcementsPanel = view.findViewById(R.id.login_announcements_panel);
            announcementsPanel.setVisibility(View.GONE);
        } else {
            Logger.log("LoginController", "Displaying Announcements!");
            ViewScreen announcementsScreen = (ViewScreen) view.findViewById(R.id.login_announcements_screen);
            announcementsScreen.display(new WebController(announcements), host);
        }

        TextView versionInfo = (TextView) view.findViewById(R.id.login_version);
        versionInfo.setText("Application Version " + BuildConfig.VERSION_NAME);
    }

    @Override
    public boolean hasExpired() {
        return getModel().isStale();
    }
}
