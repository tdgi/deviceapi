package com.example.deviceapi.service;

import com.example.deviceapi.enums.DevTypeEnum;
import com.example.deviceapi.json.DeviceJson;
import com.example.deviceapi.json.DeviceNodeJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<DeviceJson> getDevices() {
        String query = "select * from device order by devicetype";
        List<DeviceJson> allDevices = jdbcTemplate.query(query, DeviceService::deviceMapper);
        return allDevices;
    }

    public DeviceJson getDevice(String mac) {
        String query = "select * from device where mac = :mac";
        DeviceJson device = jdbcTemplate.query(query, Map.of("mac", mac), DeviceService::deviceMapper).stream().findFirst().orElse(null);
        return device;
    }

    public void createDevice(DeviceJson deviceJson) {
        String insertQuery = "insert into device (devicetype, mac) values (:deviceType, :mac)";
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("deviceType", deviceJson.getDeviceType().getCode());
        paramMap.put("mac", deviceJson.getMac());
        if (deviceJson.getUplinkMac() != null) {
            // check if parent exists
            if (getDevice(deviceJson.getUplinkMac()) == null) {
                throw new RuntimeException("mac address not found");
            }
            insertQuery = "insert into device (devicetype, mac, uplinkmac) values (:deviceType, :mac, :uplinkmac)";
            paramMap.put("uplinkmac", deviceJson.getUplinkMac());
        }
        jdbcTemplate.update(insertQuery, paramMap);
        log.debug("record created");
    }

    public List<DeviceNodeJson> getDeviceTree() {
        String recursiveQuery = """
                with recursive cte_device as (
                	select mac, uplinkmac
                	from device
                	where uplinkmac is null
                  union all
                  	select device.mac, device.uplinkmac
                  	from device
                  	join cte_device on device.uplinkmac = cte_device.mac
                )
                select * from cte_device;
                """;
        List<DeviceJson> data = jdbcTemplate.query(recursiveQuery, DeviceService::deviceMapper2);

        HashMap<String, DeviceNodeJson> map = new HashMap<>();
        data.forEach(device -> map.put(device.getMac(), new DeviceNodeJson(device.getMac(), device.getUplinkMac(), null)));

        List<DeviceNodeJson> tree = buildTree(data, map, null);
        return tree;
    }

    public List<DeviceNodeJson> getDeviceTreeFrom(String mac) {
        String recursiveQuery = """
                with recursive cte_device as (
                	select mac, uplinkmac
                	from device
                	where mac = :mac
                  union all
                  	select device.mac, device.uplinkmac
                  	from device
                  	join cte_device on device.uplinkmac = cte_device.mac
                )
                select * from cte_device;
                """;
        List<DeviceJson> data = jdbcTemplate.query(recursiveQuery, Map.of("mac", mac), DeviceService::deviceMapper2);
        if (data.size() == 0) {
            throw new RuntimeException("mac address not found");
        }

        HashMap<String, DeviceNodeJson> map = new HashMap<>();
        data.forEach(device -> map.put(device.getMac(), new DeviceNodeJson(device.getMac(), device.getUplinkMac(), null)));

        List<DeviceNodeJson> tree = buildTree(data, map, map.get(mac).getParent());
        return tree;
    }

    /* build device tree recursively */
    private List<DeviceNodeJson> buildTree(List<DeviceJson> data, HashMap<String, DeviceNodeJson> map, String parent) {
        List<DeviceNodeJson> tree = new ArrayList<>();
        map.values().forEach(node -> {
            if (Objects.equals(node.getParent(), parent)) {
                node.setChilds(buildTree(data, map, node.getMac()));
                tree.add(node);
            }
        });
        return tree;
    }

    private static DeviceJson deviceMapper(ResultSet rs, int rowNum) throws SQLException {
        DeviceJson deviceJson = new DeviceJson();
        //deviceJson.setDeviceType(DevTypeEnum. rs.getInt("devicetype"));
        DevTypeEnum devTypeEnum = switch (rs.getInt("devicetype")) {
            case 0 -> DevTypeEnum.GATEWAY;
            case 1 -> DevTypeEnum.SWITCH;
            case 2 -> DevTypeEnum.ACCESS_POINT;
            default -> throw new IllegalStateException("Unexpected value: " + rs.getInt("devicetype"));
        };
        deviceJson.setDeviceType(devTypeEnum);
        deviceJson.setMac(rs.getString("mac"));
        deviceJson.setUplinkMac(rs.getString("uplinkmac"));
        deviceJson.setNiceName(rs.getString("nicename"));
        return deviceJson;
    }

    private static DeviceJson deviceMapper2(ResultSet rs, int rowNum) throws SQLException {
        DeviceJson deviceJson = new DeviceJson();
        deviceJson.setMac(rs.getString("mac"));
        deviceJson.setUplinkMac(rs.getString("uplinkmac"));
        return deviceJson;
    }
}
