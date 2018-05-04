package db2lci;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	private final String[] args;
	private final Logger log = LoggerFactory.getLogger(getClass());

	App(String[] args) {
		this.args = args;
	}

	public static void main(String[] args) {
		new App(args).run();
	}

	public void run() {
		try {
			IDatabase db = getDb();
			if (db == null)
				return;
			db.close();
			log.info("All done");
		} catch (Exception e) {
			log.error("Unexpected exception", e);
		}
	}

	private IDatabase getDb() {
		if (args == null || args.length == 0) {
			log.error("No database given");
			return null;
		}
		File dir = getDbFolder(args[0]);
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			log.error("The folder {} is not a database", args[0]);
			return null;
		}
		File lockFile = new File(dir, "db.lck");
		if (lockFile.exists() && !lockFile.delete()) {
			log.error("Cannot open database {}; is it already opened?", args[0]);
			return null;
		}
		try {
			return new DerbyDatabase(dir);
		} catch (Exception e) {
			log.error("Failed to open database", e);
			return null;
		}
	}

	private File getDbFolder(String name) {
		if (name == null)
			return null;
		File dir = new File(name);
		if (dir.exists() && dir.isDirectory()) {
			return dir;
		}
		File homeDir = new File(System.getProperty("user.home"));
		File olcaDir = new File(homeDir, "openLCA-data-1.4");
		File dbDir = new File(olcaDir, "databases");
		return new File(dbDir, name);
	}
}
