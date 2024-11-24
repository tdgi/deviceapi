package com.example.deviceapi.json;

import com.example.deviceapi.enums.DevTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceJson {
    //private Integer deviceType;
    private DevTypeEnum deviceType;
    private String mac;
    private String uplinkMac;
    private String niceName;
}
