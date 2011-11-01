package net.suteren.domain;

public class Live extends Food {
	public Live(String name) {
		super(name);
	}

	@Override
	public String getType() {
		return "live";
	}
}
