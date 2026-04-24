package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Root discovery endpoint providing HATEOAS links and API metadata.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        Map<String, Object> apiInfo = new HashMap<>();

        apiInfo.put("name", "Smart Campus API");
        apiInfo.put("version", "1.0.0");
        apiInfo.put("status", "operational");
        apiInfo.put("contact", "backend-team@university.edu");

        // HATEOAS links
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        apiInfo.put("_links", links);

        // Resource collections
        Map<String, String> collections = new HashMap<>();
        collections.put("rooms", "/api/v1/rooms");
        collections.put("sensors", "/api/v1/sensors");

        apiInfo.put("resources", collections);

        return Response.ok(apiInfo).build();
    }
}
