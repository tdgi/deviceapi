package com.example.deviceapi.api.v1.controller;

import com.example.deviceapi.json.DeviceJson;
import com.example.deviceapi.json.DeviceNodeJson;
import com.example.deviceapi.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<DeviceJson> getDevices() {
        return deviceService.getDevices();
    }

    @GetMapping(value = "{mac}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public DeviceJson getDevice(@PathVariable String mac) {
        return deviceService.getDevice(mac);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void registerDevice(@RequestBody DeviceJson deviceJson) {
        deviceService.createDevice(deviceJson);
    }

    @GetMapping(value = "topology", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<DeviceNodeJson> getDevicesTopology() {
        return deviceService.getDeviceTree();
    }

    @GetMapping(value = "topology/{mac}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<DeviceNodeJson> getDevicesTopologyFrom(@PathVariable String mac) {
        return deviceService.getDeviceTreeFrom(mac);
    }
}
