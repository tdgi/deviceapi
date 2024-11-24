package com.example.deviceapi;

import com.example.deviceapi.enums.DevTypeEnum;
import com.example.deviceapi.json.DeviceJson;
import com.example.deviceapi.json.DeviceNodeJson;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ApplicationMain.class)
@AutoConfigureMockMvc
class ApplicationMainTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void prepareDB() {
        try (Connection connection = dataSource.getConnection()) {
            try {
                Statement statement = connection.createStatement();
                statement.execute("TRUNCATE TABLE public.device CONTINUE IDENTITY RESTRICT");
            } catch (SQLException sqlException) {
                createSchema(connection);
            }
        } catch (Exception exception) {
        }
    }

    private void createSchema(Connection connection) {
        try {
            PreparedStatement prepareStatement = connection.prepareStatement("CREATE schema public");
            prepareStatement.execute();
            prepareStatement = connection.prepareStatement("CREATE TABLE public.device (devicetype int4 NOT NULL, mac varchar(64) NOT NULL, uplinkmac varchar(64) NULL, nicename varchar(255) NULL, CONSTRAINT device_mac_key UNIQUE (mac))");
            prepareStatement.execute();
        } catch (SQLException exception) {
        }
    }


    @Test
    void testDevices() throws Exception {
        prepareSampleData();
        var allDevices = getAll();
        assertEquals(8, allDevices.size());
        allDevices.forEach(device -> assertNotNull(device.getMac()));

        assertEquals(DevTypeEnum.GATEWAY, getOne("aa0000000000").getDeviceType());
        assertEquals(DevTypeEnum.SWITCH, getOne("bb0000000000").getDeviceType());
        assertEquals(DevTypeEnum.ACCESS_POINT, getOne("cc0000000000").getDeviceType());

        var allNodes = getAllTopology();
        assertEquals(2, allNodes.size());
        assertEquals(3, allNodes.get(0).getChilds().size());
        assertEquals(0, allNodes.get(1).getChilds().size());
        var nodesFromSpecific = getTopologyFromDevice("bb0000000000");
        assertEquals(1, nodesFromSpecific.size());
        assertEquals("cc0000000000", nodesFromSpecific.get(0).getChilds().get(0).getMac());
        assertEquals("cc0000000001", nodesFromSpecific.get(0).getChilds().get(0).getChilds().get(0).getMac());
    }

    private List<DeviceJson> getAll() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/devices"))
                .andReturn().getResponse().getContentAsString();
        List<DeviceJson> deviceJsons = objectMapper.readValue(result, new TypeReference<>() {
        });
        return deviceJsons;
    }

    private DeviceJson getOne(String mac) throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/devices/" + mac))
                .andReturn().getResponse().getContentAsString();
        DeviceJson deviceJson = objectMapper.readValue(result, new TypeReference<>() {
        });
        return deviceJson;
    }

    private List<DeviceNodeJson> getAllTopology() throws Exception {
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/devices/topology"))
                .andReturn().getResponse().getContentAsString();
        List<DeviceNodeJson> allNodes = objectMapper.readValue(result, new TypeReference<>() {
        });
        return allNodes;
    }

    private List<DeviceNodeJson> getTopologyFromDevice(String mac) throws Exception {
        String result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/devices/topology/" + mac))
                .andReturn().getResponse().getContentAsString();
        List<DeviceNodeJson> nodesSpecific = objectMapper.readValue(result, new TypeReference<>() {
        });
        return nodesSpecific;
    }

    private void create(DeviceJson deviceJson) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/devices")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(deviceJson)))
            .andExpect(status().is(201));
    }

    private void prepareSampleData() throws Exception {
        List<DeviceJson> deviceList = new ArrayList<>();
        deviceList.add(setDeviceData(DevTypeEnum.GATEWAY, "aa0000000000", null));
        deviceList.add(setDeviceData(DevTypeEnum.SWITCH, "bb0000000000", "aa0000000000"));
        deviceList.add(setDeviceData(DevTypeEnum.SWITCH, "bb0000000001", "aa0000000000"));
        deviceList.add(setDeviceData(DevTypeEnum.SWITCH, "bb0000000002", null));
        deviceList.add(setDeviceData(DevTypeEnum.ACCESS_POINT, "cc0000000000", "bb0000000000"));
        deviceList.add(setDeviceData(DevTypeEnum.SWITCH, "bb0000000003", "bb0000000001"));
        deviceList.add(setDeviceData(DevTypeEnum.ACCESS_POINT, "cc0000000001", "cc0000000000"));
        deviceList.add(setDeviceData(DevTypeEnum.ACCESS_POINT, "bb0000000004", "aa0000000000"));
        for (DeviceJson dev : deviceList) {
            create(dev);
        }
    }

    private DeviceJson setDeviceData(DevTypeEnum devTypeEnum, String mac, String uplinkMac) {
        DeviceJson device = new DeviceJson();
        device.setDeviceType(devTypeEnum);
        device.setMac(mac);
        device.setUplinkMac(uplinkMac);
        return device;
    }
}
