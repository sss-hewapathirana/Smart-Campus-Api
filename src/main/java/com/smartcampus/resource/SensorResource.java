package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * REST resource for managing sensors.
 */
@Path("/sensors")
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> sensors = store.getAllSensors();

        if (type != null && !type.isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (!store.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Room with ID '" + sensor.getRoomId() + "' does not exist."
            );
        }

        store.addSensor(sensor);

        URI location = UriBuilder.fromResource(SensorResource.class)
                .path(sensor.getId())
                .build();

        return Response.created(location).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * Sub-resource locator for sensor readings.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        if (!store.sensorExists(sensorId)) {
            throw new NotFoundException("Sensor " + sensorId + " not found");
        }
        return new SensorReadingResource(sensorId);
    }
}
