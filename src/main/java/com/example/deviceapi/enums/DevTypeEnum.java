package com.example.deviceapi.enums;

public enum DevTypeEnum {

    GATEWAY(0),
    SWITCH(1),
    ACCESS_POINT(2)
    ;


    private final int code;

    DevTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
