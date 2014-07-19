package com.starfish.kol.android.game.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;

import com.starfish.kol.android.R;
import com.starfish.kol.android.dialogs.ItemDialog;
import com.starfish.kol.android.dialogs.WebDialog;
import com.starfish.kol.android.game.BaseGameFragment;
import com.starfish.kol.android.game.GameFragment;
import com.starfish.kol.android.util.AndroidProgressHandler;
import com.starfish.kol.android.util.CustomFragmentTabHost;
import com.starfish.kol.android.util.CustomFragmentTabHost.OnCreateFragmentListener;
import com.starfish.kol.android.util.listbuilders.SubtextBuilder;
import com.starfish.kol.android.util.searchlist.GroupSearchListFragment;
import com.starfish.kol.android.util.searchlist.OnListSelection;
import com.starfish.kol.android.view.AndroidViewContext;
import com.starfish.kol.model.models.InventoryModel;
import com.starfish.kol.model.models.InventoryModel.InvItem;
import com.starfish.kol.model.models.InventoryModel.InvPocketModel;
import com.starfish.kol.model.models.WebModel;
import com.starfish.kol.model.util.LiveModel.LiveMessage;

public class InventoryFragment extends BaseGameFragment<Void, InventoryModel> implements OnCreateFragmentListener {
	public InventoryFragment() {
		super(R.layout.fragment_tabs_screen);
	}

	@Override
	public void onCreateSetup(View view, InventoryModel base,
			Bundle savedInstanceState) {

		final CustomFragmentTabHost host = (CustomFragmentTabHost) view
				.findViewById(R.id.tabs_tabhost);
		host.setup(getActivity(), getChildFragmentManager(),
				R.id.tabs_tabcontent);

		addTab(host, "recent", "Recent", 3);
		addTab(host, "consum", "Consume", 0);
		addTab(host, "equip", "Equip", 1);
		addTab(host, "misc", "Misc", 2);
		
		int current = base.getInitialChosen();
		if(current == 3)
			host.setCurrentTab(0);
		else
			host.setCurrentTab(current + 1);
		
		host.setOnCreateFragmentListener(this);
		
		WebModel results = base.getResultsPane();
		if(results != null) {
			DialogFragment newFragment = new WebDialog();
			newFragment.setArguments(GameFragment.getModelBundle(results));
		    newFragment.show(getFragmentManager(), "dialog");
		}
	}
	private void addTab(CustomFragmentTabHost host, String tab, String name, int slot) {
		Bundle bundle = new Bundle();
		bundle.putInt("slot", slot);
		bundle.putSerializable("builder", new SubtextBuilder<InvItem>());
		host.addTab(host.newTabSpec(tab).setIndicator(name),
				GroupSearchListFragment.class, bundle);
	}

	@Override
	public void setup(Fragment f, String tag) {
		@SuppressWarnings("unchecked")
		final GroupSearchListFragment<InvItem> fragment = (GroupSearchListFragment<InvItem>)f;
		
		int slot = fragment.getArguments().getInt("slot");
		final InvPocketModel model = getModel().getPocket(slot);
		
		fragment.setItems(model.getItems());
		
		// Regester for future inventory updates.
		model.connectView(new AndroidProgressHandler<LiveMessage>() {
			@Override
			public void recieveProgress(LiveMessage message) {
				switch (message) {
				case REFRESH:
					fragment.setItems(model.getItems());
					break;
				}
			}
		}, new AndroidViewContext(getActivity()));
		
		//Display an item selection dialog when an item is selected
		fragment.setOnSelectionX(new OnListSelection<InvItem>() {
			@Override
			public boolean selectItem(DialogFragment list, InvItem item) {
				ItemDialog.create(item).show(getFragmentManager(), "itemoptions");
				return true;
			}
		});
	}

	@Override
	protected void recieveProgress(Void message) {
		//do nothing
	}
}
