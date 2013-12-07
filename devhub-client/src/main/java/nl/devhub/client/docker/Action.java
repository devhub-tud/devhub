package nl.devhub.client.docker;

import javax.ws.rs.client.Client;

abstract class Action {
	public abstract void perform(Client client);
}