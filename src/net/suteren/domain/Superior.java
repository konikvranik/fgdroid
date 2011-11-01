package net.suteren.domain;

public class Superior extends Food {
	public Superior(String name) {
		super(name);
	}
	
	@Override
	public String getType() {
		return "superior";
	}
}
