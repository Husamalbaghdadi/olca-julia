package db2lci;

import java.util.function.BiConsumer;

import org.openlca.core.matrix.InventoryMatrix;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.solvers.DenseSolver;
import org.openlca.core.results.SimpleResult;
import org.openlca.umfpack.UmfFactorizedMatrix;
import org.openlca.umfpack.UmfMatrix;
import org.openlca.umfpack.Umfpack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Lci {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final InventoryMatrix inventory;

	private Lci(InventoryMatrix inventory) {
		this.inventory = inventory;
	}

	static void each(InventoryMatrix inventory, BiConsumer<LongPair, SimpleResult> fn) {
		new Lci(inventory).each(fn);
	}

	private void each(BiConsumer<LongPair, SimpleResult> fn) {
		UmfMatrix m = UmfMatrix.from(inventory.technologyMatrix);
		UmfFactorizedMatrix fm = Umfpack.factorize(m);
		TechIndex techIndex = inventory.productIndex;
		DenseSolver solver = new DenseSolver();
		for (int i = 0; i < techIndex.size(); i++) {
			LongPair provider = techIndex.getProviderAt(i);
			SimpleResult r = new SimpleResult();
			r.flowIndex = inventory.flowIndex;
			r.productIndex = techIndex;
			log.info("Solve matrix for i={}", i);
			double[] demand = new double[techIndex.size()];
			demand[i] = 1.0;
			double[] s = Umfpack.solve(fm, demand);
			log.info("Calculate LCI and LCIA results for i={}", i);
			r.totalFlowResults = solver.multiply(inventory.interventionMatrix, s);
			fn.accept(provider, r);
		}
	}
}
