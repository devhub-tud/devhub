package nl.devhub.client.docker;

import javax.ws.rs.client.Client;

abstract class Request<T> {
	public abstract T request(Client client);
}