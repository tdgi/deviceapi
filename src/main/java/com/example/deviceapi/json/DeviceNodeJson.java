package com.example.deviceapi.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeviceNodeJson {
    private String mac;
    @JsonIgnore
    private String parent;
    private List<DeviceNodeJson> childs;

    public DeviceNodeJson(String mac, String parent) {
        this.mac = mac;
        this.parent = parent;
    }
}
