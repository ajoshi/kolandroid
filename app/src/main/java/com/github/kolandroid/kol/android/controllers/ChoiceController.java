package com.github.kolandroid.kol.android.controllers;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.controller.ModelController;
import com.github.kolandroid.kol.android.controllers.web.WebController;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.screen.ViewScreen;
import com.github.kolandroid.kol.model.elements.ActionElement;
import com.github.kolandroid.kol.model.models.ChoiceModel;

public class ChoiceController extends ModelController<ChoiceModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -1321276090635113471L;

    public ChoiceController(ChoiceModel model) {
        super(model);
    }

    @Override
    public int getView() {
        return R.layout.choice_view;
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayPrimary(this);
    }

    @Override
    public void attach(View view, ChoiceModel model, final Screen host) {
        LinearLayout options = (LinearLayout) view.findViewById(R.id.choice_button_group);
        for (ActionElement option : model.getOptions()) {
            Log.i("ChoiceFragment", "Making button for " + option.getText());
            Button optionBtn = new Button(options.getContext());
            optionBtn.setText(option.getText());
            optionBtn.setWidth(options.getWidth());

            final ActionElement thisOption = option;
            optionBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    thisOption.submit(host.getViewContext());
                }
            });
            options.addView(optionBtn);
        }

        ViewScreen webScreen = (ViewScreen) view.findViewById(R.id.choice_web_screen);
        WebController web = new WebController(model);
        webScreen.display(web, host);
    }

}
