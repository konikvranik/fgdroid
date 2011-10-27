package net.suteren;

import android.util.Log;

public class Food {

	protected float price;
	protected String text;

	public Food(String name) {
		Log.d("Food", "Creating instance " + getClass().getName() + ": " + name);
		text = name;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
