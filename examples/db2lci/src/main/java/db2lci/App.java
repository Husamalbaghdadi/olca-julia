package db2lci;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.solvers.DenseSolver;
import org.openlca.eigen.NativeLibrary;
import org.openlca.umfpack.Umfpack;
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
			log.info("Connect to database");
			IDatabase db = getDb();
			if (db == null)
				return;
			log.info("Load native libraries");
			NativeLibrary.loadFromDir(new File("."));
			Umfpack.load("julia/olca-umfpack.dll");

			log.info("Build the inventory matrix");
			DenseSolver solver = new DenseSolver();
			DbMatrix dbMatrix = new DbMatrix(db, solver);
			InventoryMatrix inventory = dbMatrix.getInventory();

			Export export = new Export(db);
			Lci.each(inventory, export::next);
			export.finish();

			// db.close(); -> TODO throws an exception ?!?
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
