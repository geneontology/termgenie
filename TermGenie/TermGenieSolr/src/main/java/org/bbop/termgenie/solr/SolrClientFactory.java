package org.bbop.termgenie.solr;

import java.net.MalformedURLException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;

/**
 * Use static httpClient to minimize the number of connection managers and
 * hanging threads. See https://issues.apache.org/jira/browse/SOLR-861 for more
 * details.
 */
public class SolrClientFactory {

	private static HttpClient httpClient = new HttpClient();

	public static synchronized CommonsHttpSolrServer getServer(String url) {
		try {
			CommonsHttpSolrServer server = new CommonsHttpSolrServer(url, httpClient);
			server.setSoTimeout(1000); // socket read timeout
			server.setConnectionTimeout(100);
			server.setDefaultMaxConnectionsPerHost(100);
			server.setMaxTotalConnections(100);
			server.setFollowRedirects(false); // defaults to false
			// allowCompression defaults to false.
			// Server side must support gzip or deflate for this to have any
			// effect.
			server.setAllowCompression(true);
			server.setMaxRetries(1); // defaults to 0. > 1 not recommended.
			server.setParser(new XMLResponseParser()); // binary parser is used
														// by default
			return server;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
