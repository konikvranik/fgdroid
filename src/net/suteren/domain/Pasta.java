package net.suteren.domain;

public class Pasta extends Food {

	public Pasta(String name) {
		super(name);
	}

	@Override
	public String getType() {
		return "pasta";
	}
}
