package tuttifrutti.utils;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;

/**
 * @author rfanego
 */
public class ElasticSearchEmbeddedServer {
	private final Node elasticSearchNode;

	public ElasticSearchEmbeddedServer() {
		super();
		this.elasticSearchNode = createESNode();
	}

	public Client getClient() {
		return elasticSearchNode.client();
	}

	public void close() {
		if (elasticSearchNode != null) {
			elasticSearchNode.close();
		}
	}

	private Node createESNode() {
		Settings settings = ImmutableSettings.settingsBuilder().put("http.enabled", false).put("path.logs", "target/elasticsearch/logs").put(
				"path.data", "target/elasticsearch/data").build();

		return nodeBuilder().local(true).settings(settings).node();
	}
}
