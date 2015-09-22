package com.github.kolandroid.kol.android.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.TabHost.TabSpec;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.screen.FragmentScreen;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.util.CustomFragmentTabHost;
import com.github.kolandroid.kol.model.GroupModel;
import com.github.kolandroid.kol.model.GroupModel.ChildModel;

public abstract class GroupController<C extends ChildModel, M extends GroupModel<C>>
        extends ModelController<M> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 5797782671758587158L;

    public GroupController(M model) {
        super(model);
    }

    @Override
    public int getView() {
        return R.layout.tabs_view;
    }

    protected abstract Controller getController(C child);

    @Override
    public void connect(View view, M model, Screen host) {
        final CustomFragmentTabHost tabHost = (CustomFragmentTabHost) view
                .findViewById(R.id.tabs_tab_host);
        tabHost.setup(host.getActivity(), host.getChildFragmentManager());

        for (C child : model.getChildren()) {
            Controller c = getController(child);
            Bundle bundle = FragmentScreen.prepare(c);
            TabSpec tab = tabHost.newTabSpec(child.getTitle()).setIndicator(child.getTitle());
            tabHost.addTab(tab, FragmentScreen.class, bundle);
        }

        tabHost.setCurrentTab(model.getActiveChild());
    }
}
