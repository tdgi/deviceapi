package com.example.deviceapi.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceNodeJson {
    private String mac;

    @JsonIgnore
    private String parent;

    private List<DeviceNodeJson> childs;
}
