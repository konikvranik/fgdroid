package net.suteren.fg;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class FileLocker implements Locker {

	public static final String LOCK_FILE_NAME = ".lock";

	private File lockDir;

	public FileLocker(File lockDir) {
		this.lockDir = lockDir;
	}

	/* (non-Javadoc)
	 * @see net.suteren.fg.Locker#lock()
	 */
	public void lock() throws IOException {
		File f = new File(lockDir, LOCK_FILE_NAME);
		f.createNewFile();
	}

	/* (non-Javadoc)
	 * @see net.suteren.fg.Locker#unlock()
	 */
	public void unlock() {
		File[] f = findLockFiles();

		for (int i = 0; i < f.length; i++) {
			f[i].delete();
		}
	}

	private File[] findLockFiles() {
		return lockDir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (LOCK_FILE_NAME.equals(name))
					return true;
				return false;
			}
		});
	}

	/* (non-Javadoc)
	 * @see net.suteren.fg.Locker#isLocked()
	 */
	public boolean isLocked() {
		File[] f = findLockFiles();
		if (f.length > 0)
			return true;
		return false;
	}

}
