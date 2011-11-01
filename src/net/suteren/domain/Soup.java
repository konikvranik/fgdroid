package net.suteren.domain;

public class Soup extends Food {

	public Soup(String name) {
		super(name);
	}

	@Override
	public String getType() {
		return "soup";
	}
}
