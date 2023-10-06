package com.github.azthec;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.List;

@Path("/todo")
public class TodoResource {

    @Context
    UriInfo uriInfo;

    String path = "todo/";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Todo> list(@Context UriInfo uriInfo) {
        List<Todo> todos = Todo.listAll();
        for (Todo todo : todos) {
            todo.url = url(todo.id);
        }
        return todos;
    }

    @DELETE
    @Transactional
    @ResponseStatus(204)
    public void deleteAll() {
        Todo.deleteAll();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ResponseStatus(201)
    public Todo add(Todo todo) {
        todo.persist();
        todo.url = url(todo.id);
        return todo;
    }

    @PATCH
    @Path("{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(Todo todo, Long id) {
        Todo existing = Todo.findById(id);

        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        existing.title = todo.title;
        existing.completed = todo.completed;
        existing.order = todo.order;
        existing.url = url(id);
        existing.persist();

        return Response.ok(existing).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Todo get(String id) {
        Todo todo = Todo.findById(id);
        todo.url = url(todo.id);
        return todo;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @ResponseStatus(204)
    public boolean delete(String id) {
        return Todo.deleteById(id);
    }

    private String url(Long id) {
        return String.format("%s%s%s", uriInfo.getBaseUri().toString(), path, id);
    }
}