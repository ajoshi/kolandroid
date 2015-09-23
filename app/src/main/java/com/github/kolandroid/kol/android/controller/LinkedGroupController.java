package com.github.kolandroid.kol.android.controller;

import android.app.Fragment;
import android.support.annotation.CallSuper;
import android.view.View;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.screen.FragmentScreen;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.util.CustomFragmentTabHost;
import com.github.kolandroid.kol.android.util.CustomFragmentTabHost.OnCreateFragmentListener;
import com.github.kolandroid.kol.model.GroupModel;
import com.github.kolandroid.kol.model.GroupModel.ChildModel;

public abstract class LinkedGroupController<C extends ChildModel, M extends GroupModel<C>> extends GroupController<C, M> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 3395000588012861100L;

    public LinkedGroupController(M model) {
        super(model);
    }

    protected abstract UpdatableController<C> getController(C child);

    protected abstract void linkChild(UpdatableController<C> controller, String tag);

    @CallSuper
    @Override
    public void attach(View view, M model, Screen host) {
        super.attach(view, model, host);
        final CustomFragmentTabHost tabHost = (CustomFragmentTabHost) view
                .findViewById(R.id.tabs_tab_host);
        tabHost.setOnCreateFragmentListener(new OnCreateFragmentListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void setup(Fragment f, String tag) {
                FragmentScreen screen = (FragmentScreen) f;
                UpdatableController<C> controller = (UpdatableController<C>) screen.getController();
                linkChild(controller, tag);
            }
        });
    }
}
