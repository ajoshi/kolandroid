package com.starfish.kol.android.controllers.inventory;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.starfish.kol.android.R;
import com.starfish.kol.android.binders.ElementBinder;
import com.starfish.kol.android.controller.Controller;
import com.starfish.kol.android.controllers.MultiusableController;
import com.starfish.kol.android.screen.DialogScreen;
import com.starfish.kol.android.screen.Screen;
import com.starfish.kol.android.screen.ScreenSelection;
import com.starfish.kol.android.util.ImageDownloader;
import com.starfish.kol.android.util.adapters.ListAdapter;
import com.starfish.kol.model.elements.interfaces.DeferredGameAction;
import com.starfish.kol.model.elements.interfaces.Multiuseable;
import com.starfish.kol.model.models.inventory.InventoryAction;
import com.starfish.kol.model.models.inventory.InventoryActionVisitor;
import com.starfish.kol.model.models.inventory.InventoryItem;

public class ItemController implements Controller {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = 2863460893944088836L;

	private InventoryItem base;
	
	public ItemController(InventoryItem base) {
		this.base = base;
	}
	
	@Override
	public int getView() {
		return R.layout.dialog_item_screen;
	}

	@Override
	public void connect(View view, final Screen host) {
	    ListAdapter<InventoryAction> adapter = new ListAdapter<InventoryAction>(host.getActivity(), base.getActions(), ElementBinder.ONLY);
	    
	    final InventoryActionVisitor visitor = new InventoryActionVisitor() {
			@Override
			public void executeRequest(DeferredGameAction action) {
				action.submit(host.getViewContext());
			}

			@Override
			public void displayAutosell(Multiuseable item) {
				Controller controller = new MultiusableController(item, "Sell");
				DialogScreen.display(controller, host);
			}

			@Override
			public void displayMultiuse(Multiuseable item) {
				Controller controller = new MultiusableController(item, "Use");
				DialogScreen.display(controller, host);
			}	    	
	    };
	    
	    ListView list = (ListView)view.findViewById(R.id.dialog_item_list);
	    list.setAdapter(adapter);
	    list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> ad, View list, int pos,
					long arg3) {
				InventoryAction select = (InventoryAction)ad.getItemAtPosition(pos);
				
				if(select != null) {
					select.select(visitor);
					host.close();
				}
			}
	    });
		
	    TextView text = (TextView)view.findViewById(R.id.dialog_item_text);
	    text.setText(base.getText());
	    
	    ImageView img = (ImageView)view.findViewById(R.id.dialog_item_image);
	    ImageDownloader.loadFromUrl(img, base.getImage());
	}

	@Override
	public void disconnect() {
		// do nothing
	}

	@Override
	public void chooseScreen(ScreenSelection choice) {
		choice.displayDialog(this);
	}

}
