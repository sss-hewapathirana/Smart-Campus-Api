package com.smartcampus;

import com.smartcampus.exception.mapper.GlobalExceptionMapper;
import com.smartcampus.exception.mapper.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.exception.mapper.RoomNotEmptyExceptionMapper;
import com.smartcampus.exception.mapper.SensorUnavailableExceptionMapper;
import com.smartcampus.filter.ApiLoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application configuration for Smart Campus API.
 * Explicitly registers all resources, exception mappers, and filters.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resource classes
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Exception mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        // Filters
        classes.add(ApiLoggingFilter.class);

        return classes;
    }
}
