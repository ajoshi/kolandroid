package com.github.kolandroid.kol.android.controllers.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TabHost.OnTabChangeListener;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.controller.Controller;
import com.github.kolandroid.kol.android.screen.FragmentScreen;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.screen.ViewScreen;
import com.github.kolandroid.kol.android.util.CustomFragmentTabHost;
import com.github.kolandroid.kol.model.models.chat.ChannelModel;
import com.github.kolandroid.kol.model.models.chat.ChatModel;
import com.github.kolandroid.kol.model.models.chat.ChatModelSegment;
import com.github.kolandroid.kol.model.models.chat.stubs.ChatStubModel;
import com.github.kolandroid.kol.util.Logger;

import java.util.ArrayList;
import java.util.HashSet;

public class ChatController extends ChatStubController<ChatStubModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -2290839327914902450L;
    private final ChatSubmissionController submission;
    private HashSet<String> currentTabs;
    private transient CustomFragmentTabHost tabs;
    private transient ViewScreen submissionScreen;


    public ChatController(ChatModel model) {
        super(new ChatStubModel(model));

        submission = new ChatSubmissionController(model);
        this.currentTabs = new HashSet<>();
    }

    @Override
    public void receiveProgress(View view, ChatStubModel model, Iterable<ChatModelSegment> message, Screen host) {
        updateTabs(model, host);
    }

    @Override
    public int getView() {
        return R.layout.chat_view;
    }

    @Override
    public void doConnect(View view, ChatStubModel model, Screen host) {
        tabs = (CustomFragmentTabHost) view.findViewById(R.id.tabs_tabhost);
        tabs.setup(host.getActivity(), host.getChildFragmentManager());
        tabs.clearAllTabs();
        tabs.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                ChannelModel channel = getModel().getChannel(tabId);
                if (channel != null) {
                    channel.makePrimaryChannel();
                }
            }
        });

        this.currentTabs = new HashSet<>();

        ViewScreen submissionScreen = (ViewScreen) view.findViewById(R.id.chat_submissionscreen);
        submissionScreen.display(submission, host);
    }

    private void updateTabs(ChatModel model, Screen host) {
        ArrayList<String> currentChannels = new ArrayList<>();
        for (ChannelModel child : model.getChannels()) {
            if (child.isActive())
                currentChannels.add(child.getName());
        }

        for (String channel : currentChannels) {
            if (!currentTabs.contains(channel)) {
                addTab(channel, model, host);
            }
        }

        ArrayList<String> toRemove = new ArrayList<>();
        for (String channel : currentTabs) {
            if (!currentChannels.contains(channel)) {
                toRemove.add(channel);
            }
        }

        for (String channel : toRemove) {
            removeTab(channel);
        }

        String changeTabTo = model.getCurrentChannel();
        if (currentTabs.contains(changeTabTo)) {
            tabs.setCurrentTabByTag(changeTabTo);
        }

        View current = tabs.getCurrentTabView();
        if (current != null && current.getVisibility() == View.GONE && currentChannels.size() != 0) {
            tabs.setCurrentTabByTag(currentChannels.get(0));
        }
    }

    private void addTab(final String tag, final ChatModel chat, final Screen host) {
        if (tabs == null)
            return;

        View preexisting = tabs.getTabByTag(tag);
        if (preexisting != null) {
            preexisting.setVisibility(View.VISIBLE);
        } else {
            Controller channel = new ChannelController(getModel(), tag);

            //Inflate a new view for the tag
            LayoutInflater inflater = (LayoutInflater) tabs.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater == null) {
                Logger.log("ChatController", "Unable to find LayoutInflater for [" + tag + "]");
                tabs.addTab(tabs.newTabSpec(tag).setIndicator(tag),
                        FragmentScreen.class, FragmentScreen.prepare(channel));
            } else {
                ChannelModel channelModel = getModel().getChannel(tag);
                if (channelModel == null) {
                    return; // unable to link to channel which does not exist
                }

                Controller channelName = new ChannelCounterController(getModel().getChannel(tag));
                View tabView = inflater.inflate(channelName.getView(), null);
                channelName.connect(tabView, host);

                tabs.addTab(tabs.newTabSpec(tag).setIndicator(tabView),
                        FragmentScreen.class, FragmentScreen.prepare(channel));
            }
            currentTabs.add(tag);
            View tabTitle = tabs.getTabByTag(tag);

            tabTitle.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    new AlertDialog.Builder(host.getActivity())
                            .setTitle("Close " + tag + "?")
                            .setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            if (chat != null) {
                                                ChannelModel channel = chat
                                                        .getChannel(tag);
                                                channel.leave();
                                                dialog.dismiss();
                                            }
                                        }

                                    }).setNegativeButton("Cancel", null).show();
                    return true;
                }
            });
        }

        currentTabs.add(tag);
    }

    private void removeTab(String tag) {
        if (tabs == null)
            return;

        View tabTitle = tabs.getTabByTag(tag);
        tabTitle.setVisibility(View.GONE);
        currentTabs.remove(tag);
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayChat(this);
    }
}
