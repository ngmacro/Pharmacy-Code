/*
package com.satech.pharmacy.service;

import com.satech.pharmacy.connector.LogToExcel;
import com.satech.pharmacy.model.Box;
import com.satech.pharmacy.repository.BoxRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@Transactional
public class BoxService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BoxRepository boxRepository;

    private static final DateTimeFormatter CURRDAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter CURRTIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private LocalDateTime time;

    @Autowired
    public BoxService(BoxRepository boxRepository) {
        this.boxRepository = boxRepository;
    }

    public Box getBox(String boxNumber) {
        return boxRepository.getByBoxNumber(boxNumber);
    }

    public void updateBox(String boxNumber, String stations, boolean urgent) throws Exception {
        long startTime = System.currentTimeMillis();
        logger.info("[BoxService][UpdateBox][BoxNumber: {}][Stations: {}][Urgent: {}]", boxNumber, stations, urgent);

        Box box = boxRepository.getByBoxNumber(boxNumber);

        boolean station1 = stations.charAt(0) == '1';
        boolean station2 = stations.charAt(1) == '1';
        boolean station3 = stations.charAt(2) == '1';
        boolean station4 = stations.charAt(3) == '1';
        
        time = LocalDateTime.now();

        if (box == null) {
            // Bu numara ile hiç kutu yok. Yeni ekle.
            logger.info("[BoxService][NewEntry][BoxNumber: {}][Stations: {}][Urgent: {}]", boxNumber, stations, urgent);
            box = new Box(boxNumber, station1, station2, station3, station4, urgent, CURRTIME.format(time), CURRDAY.format(time));
        } else {
            // Kutu var. Update et.
            logger.info("[BoxService][UpdateOldOne][BoxNumber: {}][Stations: {}][Urgent: {}]", boxNumber, stations, urgent);
            box.setStation1(station1);
            box.setStation2(station2);
            box.setStation3(station3);
            box.setStation4(station4);
            box.setUrgent(urgent);
            box.setRecordTime(CURRTIME.format(time));
            box.setRecordDay(CURRDAY.format(time));
        }
        boxRepository.save(box);

        LogToExcel.appendRecords(boxNumber, stations + urgent, CURRDAY.format(time), CURRTIME.format(time));

        logger.info("[ProcessingTime: {}]", System.currentTimeMillis() - startTime);
        logger.info("[--------------------------------------------------------------]");
    }

    public int stationCheck1(Box box, String scannerId) throws Exception {
        try {
            if (scannerId.equals("1")) {
                if (box.getStation1() == null || box.getStation2() == null) {
                    return 9;
                } else {
                    if (box.getStation1()) {
                        return 1;
                    } else if (!box.getStation1() && box.getStation2()) {
                        return 2;
                    } else {
                        return 9;
                    }
                }
            } else if (scannerId.equals("2")) {
                if (box.getStation3() == null || box.getStation4() == null) {
                    return 10;
                } else {
                    if (box.getStation3()) {
                        return 3;
                    } else if (!box.getStation3() && box.getStation4()) {
                        return 4;
                    } else {
                        return 10;
                    }
                }
            } else if (scannerId.equals("3")) {
                if (box.getUrgent() == null) {
                    return 11;
                } else {
                    if (box.getUrgent()) {
                        return 5;
                    } else {
                        return 11;
                    }
                }
            } else {
                logger.info("[StationCheck][Invalid Scanner : {}]", scannerId);
                return 7;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 14;
    }

    public int stationCheck2(Box box, String scannerId) throws Exception {
        try {
            if (scannerId.equals("1")) {
                if (box.getStation1() == null || box.getStation2() == null) {
                    return 9;
                } else {
                    if (box.getStation1()) {
                        return 1;
                    } else if (!box.getStation1() && box.getStation2()) {
                        return 2;
                    } else {
                        return 9;
                    }
                }
            } else if (scannerId.equals("2")) {
                if (box.getStation3() == null || box.getStation4() == null) {
                    return 10;
                } else {
                    if (box.getStation3()) {
                        return 3;
                    } else {
                        return 10;
                    }
                }
            } else if (scannerId.equals("3")) {
                if (box.getUrgent() == null) {
                    return 11;
                } else {
                    if (!box.getStation3() && box.getStation4()) {
                        return 4;
                    } else {
                        return 11;
                    }
                }
            } else {
                logger.info("[StationCheck][Invalid Scanner : {}]", scannerId);
                return 7;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 14;
    }

    public void updateRecords(Box box, int station) throws Exception {
        if (station == 1) {
            box.setStation1(false);
        } else if (station == 2) {
            box.setStation2(false);
        } else if (station == 3) {
            box.setStation3(false);
        } else if (station == 4) {
            box.setStation4(false);
        } else if (station == 5) {
            box.setUrgent(false);
        }

        boxRepository.save(box);

        boxRepository.deleteEmptyBox();

    }

}

*/

package com.satech.pharmacy.service;

import com.satech.pharmacy.exception.BoxExistsException;
import com.satech.pharmacy.exception.BoxNotFoundException;
import com.satech.pharmacy.model.Box;
import com.satech.pharmacy.model.BoxStation;
import com.satech.pharmacy.model.Station;
import com.satech.pharmacy.model.enums.StationStatus;
import com.satech.pharmacy.repository.BoxRepository;
import com.satech.pharmacy.rest.web.dto.BoxDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Transactional
public class ScheduleService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BoxService boxService;

    private final BoxStationService boxStationService;

    private final StationService stationService;

    @Autowired
    public ScheduleService(BoxService boxService, BoxStationService boxStationService, StationService stationService) {
        this.boxService = boxService;
        this.boxStationService = boxStationService;
        this.stationService = stationService;
    }

    @Scheduled(cron = "${satech.scheduler.cron.archiveCompletedBox}")
    public void archiveCompletedBox() {
        boxService.archiveCompletedBox();
    }

    @Scheduled(cron = "${satech.scheduler.cron.archiveAllBox}")
    public void archiveAllBox() {
        boxService.archiveAllBox();
    }

}

