package com.satech.pharmacy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.satech.pharmacy.model.Parameter;
import com.satech.pharmacy.model.ScannerMapping;
import com.satech.pharmacy.model.StationMapping;
import com.satech.pharmacy.repository.ParameterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class ParameterService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ParameterRepository repository;

    @Autowired
    public ParameterService(ParameterRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    private void init() {
        try {
            initScannerMappings();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Parameter prm = repository.getByName("BOX_NUMBER_LENGTH");
        if (prm != null && prm.getNumberValue() != null) {
            PharmacyCache.boxNumberLength = prm.getNumberValue().intValue();
        } else {
            PharmacyCache.boxNumberLength = 6;
        }

        Parameter prm1 = repository.getByName("STATION_AUTO_COMPLETE");
        PharmacyCache.isStationAutoCompleteActive = Double.valueOf(1).equals(prm1.getNumberValue());

        Parameter prm4 = repository.getByName("ERROR_STATION_AUTO_COMPLETE");
        PharmacyCache.isErrorStationAutoCompleteActive = Double.valueOf(1).equals(prm4.getNumberValue());

        Parameter prm2 = repository.getByName("THROW_EXCEPTION_IF_BOX_EXISTS");
        PharmacyCache.throwExceptionIfBoxExists = Double.valueOf(1).equals(prm2.getNumberValue());

        Parameter prm3 = repository.getByName("ADD_ERROR_STATION_BY_DEFAULT");
        PharmacyCache.addErrorStationByDefault = Double.valueOf(1).equals(prm3.getNumberValue());

    }

    private void initScannerMappings() throws JsonProcessingException {
        Parameter prm = repository.getByName("SCANNER_MAPPING");
        System.out.println(prm.getStringValue());
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<ScannerMapping> scanners =  mapper.readValue(prm.getStringValue(), new TypeReference<ArrayList<ScannerMapping>>(){});
            if (!CollectionUtils.isEmpty(scanners)) {
                for (ScannerMapping scanner : scanners){
                    scanner.setStationMap(new HashMap<>());
                    for (StationMapping station : scanner.getStationMappingList()) {
                        scanner.getStationMap().put(station.getStationNumber(), station);
                    }
                    PharmacyCache.scannerMapping.put(scanner.getScannerCode(), scanner);
                }
                logger.info("----------------------------------------------------------------");
                logger.info("Scanner Mapping Loaded Seccesfuly");
                logger.info("----------------------------------------------------------------");
            } else {
                logger.error("----------------------------------------------------------------");
                logger.error("UNABLE TO FIND SCANNER MAPPIN WITH PARAMETER \"SCANNER_MAPPING\"");
                logger.error("----------------------------------------------------------------");
                System.exit(1);
            }
        } catch (IOException e) {
            logger.error("----------------------------------------------------------------");
            logger.error("UNABLE TO FIND SCANNER MAPPIN WITH PARAMETER \"SCANNER_MAPPING\"", e);
            logger.error("----------------------------------------------------------------");
            System.exit(1);
        }
    }

}
