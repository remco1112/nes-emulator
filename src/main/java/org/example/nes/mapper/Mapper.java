package org.example.nes.mapper;

import org.example.nes.bus.BusConfiguration;

public interface Mapper {

    BusConfiguration getCpuBusConfiguration();

    BusConfiguration getPpuBusConfiguration();
}
