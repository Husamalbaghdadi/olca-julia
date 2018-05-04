package db2lci;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactMatrix;
import org.openlca.core.matrix.ImpactTable;
import org.openlca.core.matrix.Inventory;
import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.AllocationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DbMatrix {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase db;
	private final MatrixCache matrixCache;
	private final IMatrixSolver solver;


	DbMatrix(IDatabase db, IMatrixSolver solver) {
		this.db = db;
		matrixCache = MatrixCache.createEager(db);
		this.solver = solver;
	}

	ImpactMatrix getImpacts(FlowIndex flowIndex, String method) {
		ImpactMethodDao dao = new ImpactMethodDao(db);
		long methodID = dao.getForName(method).get(0).getId();
		ImpactMatrix m = ImpactTable.build(
			matrixCache, methodID, flowIndex)
				.createMatrix(solver);
		return m;
	}

	InventoryMatrix getInventory() {
		log.info("Build TechIndex");
		TechIndex idx = DbTechIndex.build(db);
		log.info("TechIndex has {} providers", idx.size());
		log.info("Build Inventory");
		Inventory inventory = Inventory.build(matrixCache, idx,
				AllocationMethod.NONE);
		log.info("Inventory created");
		log.info("Build matrices");
		// TODO: pass a formula interpreter if the database has parameters
		InventoryMatrix m = inventory.createMatrix(solver);
		log.info("Matrices created");
		return m;
	}

}
