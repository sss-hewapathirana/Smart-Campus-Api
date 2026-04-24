package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe singleton data store using ConcurrentHashMap.
 * Shared across all requests since JAX-RS resources are request-scoped by default.
 */
public class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public void removeRoom(String id) {
        rooms.remove(id);
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    public Collection<Sensor> getAllSensors() {
        return sensors.values();
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        Room room = rooms.get(sensor.getRoomId());
        if (room != null) {
            room.addSensorId(sensor.getId());
        }
    }

    public void removeSensor(String id) {
        Sensor sensor = sensors.remove(id);
        if (sensor != null) {
            Room room = rooms.get(sensor.getRoomId());
            if (room != null) {
                room.removeSensorId(id);
            }
        }
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }

    public boolean roomHasSensors(String roomId) {
        Room room = rooms.get(roomId);
        return room != null && !room.getSensorIds().isEmpty();
    }
}
