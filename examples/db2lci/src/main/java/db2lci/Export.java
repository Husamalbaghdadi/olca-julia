package db2lci;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.results.SimpleResult;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export the results as system processes.
 */
class Export {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase db;
	private final ZipStore store;
	private final JsonExport export;

	private long counter = 42_000_000;

	Export(IDatabase db) {
		this.db = db;
		File out = new File(db.getName() + "_jsonld.zip");
		if (out.exists()) {
			log.info("Delete existing file {}", out);
			out.delete();
		}
		try {
			store = ZipStore.open(out);
			export = new JsonExport(db, store);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void next(LongPair provider, SimpleResult result) {
		log.info("Create LCI result process for result {}", provider);
		Process p = init(provider);
		addRefFlow(provider, p);
		addResults(result, p);
		log.info("Export LCI result process {}", p);
		export.write(p, (message, data) -> {
			if (message.error != null) {
				log.error("Export failed: " + message.text, message.error);
			}
		});
	}

	private Process init(LongPair provider) {
		ProcessDao dao = new ProcessDao(db);
		Process p = dao.getForId(provider.getFirst());
		Process lci = new Process();
		lci.setId(counter++); // otherwise the export does not work
		lci.setCategory(p.getCategory());
		lci.setDefaultAllocationMethod(AllocationMethod.NONE);
		lci.setDescription(p.getDescription());
		lci.setDocumentation(p.getDocumentation().clone());
		lci.setLastChange(new Date().getTime());
		lci.setLocation(p.getLocation());
		lci.setName(p.getName());
		lci.setProcessType(ProcessType.LCI_RESULT);
		lci.setRefId(UUID.randomUUID().toString());
		lci.setVersion(p.getVersion());
		lci.dqEntry = p.dqEntry;
		lci.dqSystem = p.dqSystem;
		lci.exchangeDqSystem = p.exchangeDqSystem;
		return lci;
	}

	private void addRefFlow(LongPair provider, Process p) {
		FlowDao dao = new FlowDao(db);
		Flow flow = dao.getForId(provider.getSecond());
		Exchange qRef = p.exchange(flow);
		qRef.amount = 1.0;
		qRef.isInput = false;
		p.setQuantitativeReference(qRef);
	}

	private void addResults(SimpleResult result, Process p) {
		FlowDao dao = new FlowDao(db);
		FlowIndex index = result.flowIndex;
		for (int i = 0; i < index.size(); i++) {
			long id = index.getFlowAt(i);
			double val = result.getTotalFlowResult(id);
			if (val == 0d)
				continue;
			Flow flow = dao.getForId(id);
			Exchange e = p.exchange(flow);
			boolean isInput = index.isInput(id);
			e.amount = isInput ? -val : val;
			e.isInput = isInput;
		}
	}

	void finish() {
		try {
			log.info("Close package");
			store.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
