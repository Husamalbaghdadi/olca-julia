package db2lci;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.FlowTypeTable;
import org.openlca.core.model.FlowType;

class DbTechIndex {

	private final IDatabase db;

	private final Map<Long, List<LongPair>> links = new HashMap<>();
	private final List<LongPair> providers = new ArrayList<>();

	private DbTechIndex(IDatabase db) {
		this.db = db;
	}

	static TechIndex build(IDatabase db) {
		return new DbTechIndex(db).doIt();
	}

	private TechIndex doIt() {
		try {
			scanExchanges();
			if (providers.isEmpty())
				return null;
			TechIndex idx = new TechIndex(providers.get(0));
			for (int i = 0; i < providers.size(); i++) {
				LongPair provider = providers.get(i);
				idx.put(provider);
				List<LongPair> links = this.links.get(provider.getFirst());
				if (links == null)
					continue;
				for (LongPair link : links) {
					idx.putLink(link, provider);
				}
			}
			return idx;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void scanExchanges() throws Exception {
		FlowTypeTable types = FlowTypeTable.create(db);
		String q = "select id, f_owner, f_flow, is_input, f_default_provider"
				+ " from tbl_exchanges";
		NativeSql.on(db).query(q, r -> {
			long flowID = r.getLong(3);
			FlowType type = types.get(flowID);
			if (type == null || type == FlowType.ELEMENTARY_FLOW)
				return true;
			long providerID = r.getLong(5);
			if (providerID > 0) {
				addLink(providerID, LongPair.of(r.getLong(2), r.getLong(1)));
				return true;
			}
			if (isProvider(r.getBoolean(4), type)) {
				providers.add(
					LongPair.of(r.getLong(2), r.getLong(3)));
			}
			return true;
		});
	}

	private boolean isProvider(boolean isInput, FlowType type) {
		return (isInput && type == FlowType.WASTE_FLOW)
				|| (!isInput && type == FlowType.PRODUCT_FLOW);
	}

	private void addLink(long provider, LongPair link) {
		List<LongPair> list = links.get(provider);
		if (list == null) {
			list = new ArrayList<>();
			links.put(provider, list);
		}
		list.add(link);
	}

}
