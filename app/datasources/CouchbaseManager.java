package datasources;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.lang.Exception;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Base64;

import play.*;
import com.couchbase.client.CouchbaseClient;

/**
 * Creates and manages connections to the Couchbase cluster based on the given
 * configuration settings.
 */
public final class CouchbaseManager {

	/**
	 * The instance of the client to connect to.
	 */
	private static Map<String,CouchbaseInfos> clients = new HashMap<>();

	/**
	 * Make the constructor private so it will never be called directly.
	 */
	private CouchbaseManager() {
	}

	/**
	 * Connects to a Couchbase cluster.
	 *
	 * If connection is successfull, returns a token that will identify
	 * the connection.
	 */
	public synchronized static String connect(String hostname, Integer port, String bucket, String password) {
		String uri = "http://" + hostname + ":" + port + "/pools";
		return connect(uri,bucket,password);
	}

	public synchronized static String connect(String uri, String bucket, String password) {
		List<URI> hosts = new ArrayList<URI>();
		hosts.add(URI.create(uri));

		CouchbaseClient client = null;
		try {
			client = new CouchbaseClient(hosts, bucket, password);
		} catch (Exception e) {
			Logger.error("Error creating Couchbase client: " + e.getMessage());
		}

		if(client == null)
			return null;

		String token = java.util.UUID.randomUUID().toString();
		CouchbaseInfos infos = new CouchbaseInfos(uri,bucket,password,token,client);
		clients.put(token,infos);

		return token;
	}

	/**
	 * Disconnects from a Couchbase cluster.
	 */
	public synchronized static boolean disconnect(String token) {
		CouchbaseInfos infos = clients.get(token);
		if (infos == null) {
			return true;
		}

		if(infos.client.shutdown(20, TimeUnit.SECONDS)) {
			clients.remove(token);
			return true;
		} else return false;
	}

	/**
	 * Returns the client object in a safe way.
	 **/
	public synchronized static CouchbaseInfos getConnection(String token) {
		return clients.get(token);
	}

	public static class CouchbaseInfos {
		public String uri;
		public String bucket;
		public String password;
		public String token;
		public CouchbaseClient client;

		public CouchbaseInfos(String uri, String bucket, String password, String token, CouchbaseClient client) {
			this.uri = uri;
			this.bucket = bucket;
			this.password = password;
			this.token = token;
			this.client = client;
		}
	}
}
