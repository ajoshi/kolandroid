package com.starfish.kol.android.controller;

import android.view.View;

import com.starfish.kol.android.screen.Screen;
import com.starfish.kol.android.screen.ScreenSelection;
import com.starfish.kol.android.util.adapters.ListElementBuilder;

public class ListElementController<E> implements Controller {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = -8821670180432097129L;
	
	private final ListElementBuilder<E> base;
	private E displayed;
	
	private transient View view;
	
	public ListElementController(ListElementBuilder<E> base, E defaultElem) {
		this.base = base;
		this.displayed = defaultElem;
	}
	
	@Override
	public int getView() {
		return base.getChildLayout();
	}

	public E getValue() {
		return displayed;
	}
	
	public void setValue(E toDisplay) {
		this.displayed = toDisplay;
		if(view != null)
			base.fillChild(view, toDisplay);
	}
	@Override
	public void connect(View view, Screen host) {
		this.view = view;
		base.fillChild(view, displayed);
	}

	@Override
	public void disconnect() {
		view = null;
	}

	@Override
	public void chooseScreen(ScreenSelection choice) {
		choice.displayPrimary(this);
	}

}
