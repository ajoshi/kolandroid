package com.starfish.kol.android.util.listbuilders;

import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.starfish.kol.android.R;
import com.starfish.kol.android.util.adapters.ListElementBuilder;
import com.starfish.kol.model.ProgressHandler;
import com.starfish.kol.model.elements.interfaces.DeferredAction;
import com.starfish.kol.model.models.chat.ChatChannel;
import com.starfish.kol.model.models.chat.ChatModel;

public class ChannelBuilder implements ListElementBuilder<ChatChannel>{	
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = -3074449411826485757L;

	private ProgressHandler<ChatChannel> channelHandler;
	private ProgressHandler<DeferredAction<ChatModel>> actionHandler;
	
	public ChannelBuilder(ProgressHandler<ChatChannel> channelHandler, ProgressHandler<DeferredAction<ChatModel>> actionHandler) {
		this.channelHandler = channelHandler;
		this.actionHandler = actionHandler;
	}
	
	@Override
	public int getChildLayout() {
		return R.layout.list_chat_channel_item;
	}

	@Override
	public void fillChild(View view, final ChatChannel child) {
		TextView text = (TextView)view.findViewById(R.id.list_item_text);
		text.setText(Html.fromHtml(child.getName()));
		text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(channelHandler != null)
					channelHandler.reportProgress(child);
			}			
		});
		
		Button enter = (Button)view.findViewById(R.id.chat_channel_enter);
		Button leave = (Button)view.findViewById(R.id.chat_channel_leave);
		
		if(child.isActive()) {
			enter.setEnabled(false);
			leave.setEnabled(true);
			leave.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(actionHandler != null)
						actionHandler.reportProgress(child.leave());
				}
			});
		} else {
			enter.setEnabled(true);
			leave.setEnabled(false);
			enter.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(actionHandler != null)
						actionHandler.reportProgress(child.enter());
				}
			});
		}
	}
}
