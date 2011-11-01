package net.suteren.fg;

import java.io.IOException;

public interface Locker {

	public abstract void lock() throws IOException;

	public abstract void unlock();

	public abstract boolean isLocked();

}