package com.satech.pharmacy.util;

import com.satech.pharmacy.model.BoxStation;
import com.satech.pharmacy.model.ScannerMapping;
import com.satech.pharmacy.model.StationMapping;
import com.satech.pharmacy.service.PharmacyCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class PharmacyUtil {

    private static final Logger logger = LoggerFactory.getLogger(PharmacyUtil.class);

    public static ScannerMapping getScanner(String scannerCode) {
        return PharmacyCache.scannerMapping.get(scannerCode);
    }

    public static Integer getStationKeepGoingCode(String scannerCode) {
        ScannerMapping scanner = PharmacyCache.scannerMapping.get(scannerCode);
        if (scanner == null) {
            logger.error("Scanner Mapping Is Null. ScannerCode:{}", scannerCode);
            logger.error("Return Error Code = 999");
            return 999;
        }

        if (scanner.getKeepGoingCode() == null) {
            logger.warn("Keep Going Return Code Is Null. ScannerCode:{}", scannerCode);
            logger.warn("Return Error Code = 999");
            return 999;
        } else {
            return scanner.getKeepGoingCode();
        }
    }

    public static Integer getStationReturnCode(String scannerCode, Integer stationNumber) {
        ScannerMapping scanner = PharmacyCache.scannerMapping.get(scannerCode);
        if (scanner == null) {
            logger.error("Scanner Mapping Is Null. ScannerCode:{}", scannerCode);
            logger.error("Return Error Code = 999");
            return 999;
        }

        StationMapping station = scanner.getStationMap().get(stationNumber);
        if (station == null || station.getReturnCode() == null) {
            logger.warn("Return Code Is Null. ScannerCode:{} | StationNumber:{}", scannerCode, stationNumber);
            logger.warn("Return Keep Going Code");

            if (scanner.getKeepGoingCode() == null) {
                logger.warn("Keep Going Return Code Is Null. ScannerCode:{} | StationNumber:{}", scannerCode, stationNumber);
                logger.warn("Return Error Code = 999");
                return 999;
            } else {
                // Keep Going kodu dön
                return scanner.getKeepGoingCode();
            }
        } else {
            // İstasyon bulunduysa
            logger.debug("Station Found! Return Code:{}", station.getReturnCode());
            return station.getReturnCode();
        }

    }

    public static SelectedStation findStationReturnCode(String scannerCode, List<BoxStation> boxStations) {
        ScannerMapping scanner = PharmacyCache.scannerMapping.get(scannerCode);
        if (scanner == null) {
            logger.error("[Unknown Scanner][Scanner Mapping Is Null][ScannerCode:{}]", scannerCode);
            return null;
        }

        // Scanner için tanımlanmış istasyonları tarayıp tanımlanmış
        // Return Code bulunur
        // Tanımlanmış ilk istasyon kllanılır
        for (StationMapping station : scanner.getStationMappingList()) {
            for (BoxStation boxStation : boxStations) {
                if (station.getStationNumber().equals(boxStation.getStationId())) {
                    return new SelectedStation(boxStation, station.getReturnCode());
                }
            }
        }

        // Eğer tanımlanmış bir istasyon yok ise ozaman NULL olarak dönülür.
        // Bu durumda kutunun devam etmesi için devam kodu gönderilir.
        return new SelectedStation(null, scanner.getKeepGoingCode());
    }

}
