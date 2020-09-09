package com.satech.pharmacy.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ScannerMapping {

    private String scannerCode;

    private Integer keepGoingCode;

    private Integer noReadCode;

    private Integer plcRegisterNo;

    private List<StationMapping> stationMappingList;

    private Map<Integer, StationMapping> stationMap;

}
