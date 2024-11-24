package com.example.deviceapi.enums;

public enum DevTypeEnum {

    GATEWAY(0),
    SWITCH(1),
    ACCESS_POINT(2)
    ;


    private final int code;

    //private final String encode;

    DevTypeEnum(int code) {
        this.code = code;
        //this.encode = encode;
    }

    public int getCode() {
        return code;
    }
}
