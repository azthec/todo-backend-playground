package com.github.azthec;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/todo")
public class TodoResource {

    @Inject
    EntityManager em;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String todo() {
        return "TODO";
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<Todo> getAll() {
        return null; // TODO
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/add")
    public String doSomeStuff() {
        return "Hello from RESTEasy Reactive";
    }
}
