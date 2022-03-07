package com.satech.pharmacy.service;

import com.satech.pharmacy.connector.ModbusTcpController;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Transactional
public class BoxService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BoxRepository boxRepository;

    private final BoxStationService boxStationService;

    private final StationService stationService;

    private final ModbusTcpController modbusTcpController;

    @Autowired
    public BoxService(BoxRepository boxRepository, BoxStationService boxStationService, StationService stationService, ModbusTcpController modbusTcpController) {
        this.boxRepository = boxRepository;
        this.boxStationService = boxStationService;
        this.stationService = stationService;
        this.modbusTcpController= modbusTcpController;
    }

    public void addNewBox(BoxDto boxDto) {
        Box box = boxRepository.getByBoxNumber(boxDto.getBoxNumber());
        if(box == null) {
            // Kutu sistemde yok. Yeni Kutu ekle.
            final Box newBox = new Box();
            newBox.setBoxNumber(boxDto.getBoxNumber());
            newBox.setOrderNumber(boxDto.getOrderNumber());
            newBox.setCreateDate(new Date());
            addBoxStations(boxDto, newBox);

            boxRepository.save(newBox);
            try {
                modbusTcpController.sendData(1, 15);
                logger.info("Eşleme bilgisi yollandı");
            }

            catch(Exception e)
            {logger.info("Eşleme bilgisi yollanamadı");

            }

            logger.info("New Box added with ID:{} | BoxNumber:{} | OrderNumber:{}", newBox.getId(), newBox.getBoxNumber(),
                    newBox.getOrderNumber());
        } else {
            // Kutu sistemde var. Hata dön veya Update et.
            if(PharmacyCache.throwExceptionIfBoxExists) {
                throw new BoxExistsException();
            }

            box.setOrderNumber(boxDto.getOrderNumber());
            boxStationService.deleteAll(box.getId());
            box.setBoxStations(null);
            boxRepository.save(box);

            addBoxStations(boxDto, box);
            boxRepository.save(box);
            try {
                modbusTcpController.sendData(1, 15);
                logger.info("Eşleme bilgisi yollandı");
            }

            catch(Exception e)
            {logger.info("Eşleme bilgisi yollanamadı");

            }
            logger.info("Box updated with ID:{} | BoxNumber:{} | OrderNumber:{}", box.getId(), box.getBoxNumber(),
                    box.getOrderNumber());
        }

    }

    private void addBoxStations(BoxDto boxDto, Box newBox) {
        List<BoxStation> stations = new ArrayList<>();
        Arrays.stream(boxDto.getStations()).forEach(st -> {
            BoxStation boxStation = new BoxStation();
            boxStation.setBox(newBox);
            boxStation.setStationId(st);
            boxStation.setStatus(StationStatus.OPEN);
            boxStation.setCreateDate(new Date());
            stations.add(boxStation);
        });

        // Eğer varsayılan olarak Hata istasyonun eklenmesi isteniyorsa
        // bu istasyon listeye eklenecek.
        if(PharmacyCache.addErrorStationByDefault) {
            Station errorStation = stationService.getFirstErrorStation();
            if(errorStation != null) {
                BoxStation boxStation = new BoxStation();
                boxStation.setBox(newBox);
                boxStation.setStationId(errorStation.getId());
                boxStation.setStatus(StationStatus.OPEN);
                boxStation.setCreateDate(new Date());
                stations.add(boxStation);
            } else {
                logger.warn("[AddBoxStations][AddingErrorStationByDefault is active but Error Station is NULL]");
            }
        }

        newBox.setBoxStations(stations);
    }

    public void stationComplete(BoxDto boxDto) {
        Box box = boxRepository.getByBoxNumber(boxDto.getBoxNumber());
        if(box == null) {
            logger.warn("Box Not Found | BoxNumber:{}", boxDto.getBoxNumber());
            throw new BoxNotFoundException();
        }
        boxRepository.setStationStatus(box, boxDto.getStation(), StationStatus.DONE);
        logger.info("[Station Status Set To DONE][BoxNumber:{} | Station:{}]", boxDto.getBoxNumber(), boxDto.getStation());
    }

    public void boxFull(BoxDto boxDto) {
        Box box = boxRepository.getByBoxNumber(boxDto.getBoxNumber());
        if(box == null) {
            logger.warn("Box Not Found | BoxNumber:{}", boxDto.getBoxNumber());
            throw new BoxNotFoundException();
        }
        boxRepository.cancelAllStation(box, StationStatus.CANCELED);
        logger.info("[Box Full][Set Station Status To CANCELED][BoxNumber:{}]", boxDto.getBoxNumber());
    }

    public void setOrderToCompleted(BoxDto boxDto) {
        Box box = boxRepository.getByBoxNumber(boxDto.getBoxNumber());
        if(box == null) {
            logger.warn("Box Not Found | BoxNumber:{}", boxDto.getBoxNumber());
            throw new BoxNotFoundException();
        }

        // Hata istasyounu bul ve durumunu iptal olarak işaretle.
        boxRepository.setOrderToCompleted(box, StationStatus.CANCELED);
        logger.info("[Order StatusCompleted][Set ERROR Station Status To CANCELED][BoxNumber:{}]", boxDto.getBoxNumber());
    }

    public void archiveCompletedBox() {
        long startProcess = System.currentTimeMillis();
        List<Long> completedBoxList = boxRepository.getAllCompletedBoxId();
        if(!CollectionUtils.isEmpty(completedBoxList)) {
            logger.debug("[ArchiveCompletedBox][{} Box To Archive]", completedBoxList.size());
            // Tamamlanmış kutular var. Bunları arşive taşı.
            boxRepository.archiveBox(completedBoxList);
            boxStationService.archiveBoxStation(completedBoxList);
            boxStationService.deleteBoxStation(completedBoxList);
            boxRepository.deleteBox(completedBoxList);
        } else {
            logger.info("[ArchiveCompletedBox][No Completed Box To Archive]");
        }
        logger.info("[ArchiveCompletedBox][Process Ended In {}]", System.currentTimeMillis() - startProcess);
        logger.info("---------------------------------------------------------------------------------");
    }

    public void archiveAllBox() {
        long startProcess = System.currentTimeMillis();
        Iterable<Box> alldBoxList = boxRepository.findAll();
        List<Long> completedBoxList = new ArrayList<>();
        alldBoxList.forEach(box -> completedBoxList.add(box.getId()));
        if(!CollectionUtils.isEmpty(completedBoxList)) {
            logger.debug("[ArchiveAllBox][{} Box To Archive]", completedBoxList.size());
            // Tamamlanmış kutular var. Bunları arşive taşı.
            boxRepository.archiveBox(completedBoxList);
            boxStationService.archiveBoxStation(completedBoxList);
            boxStationService.deleteBoxStation(completedBoxList);
            boxRepository.deleteBox(completedBoxList);
        } else {
            logger.info("[ArchiveAllBox][No Completed Box To Archive]");
        }
        logger.info("[ArchiveAllBox][Process Ended In {}]", System.currentTimeMillis() - startProcess);
        logger.info("---------------------------------------------------------------------------------");
    }


//    public int stationCheck(Box box, String scannerId) throws Exception {
//        try {
//            if (scannerId.equals("1")) {
//                if (box.getStation1() == null || box.getStation2() == null) {
//                    return 9;
//                } else {
//                    if (box.getStation1()) {
//                        return 1;
//                    } else if (!box.getStation1() && box.getStation2()) {
//                        return 2;
//                    } else {
//                        return 9;
//                    }
//                }
//            } else if (scannerId.equals("2")) {
//                if (box.getStation3() == null || box.getStation4() == null) {
//                    return 10;
//                } else {
//                    if (box.getStation3()) {
//                        return 3;
//                    } else {
//                        return 10;
//                    }
//                }
//            } else if (scannerId.equals("3")) {
//                if (box.getUrgent() == null) {
//                    return 11;
//                } else {
//                    if (!box.getStation3() && box.getStation4()) {
//                        return 4;
//                    } else {
//                        return 11;
//                    }
//                }
//            } else {
//                logger.info("[StationCheck][Invalid Scanner : {}]", scannerId);
//                return 7;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return 14;
//    }

//     public int stationCheck2(Box box, String scannerId) throws Exception {
//        try {
//            if (scannerId.equals("1")) {
//                if (box.getStation1() == null || box.getStation2() == null) {
//                    return 9;
//                } else {
//                    if (box.getStation1()) {
//                        return 1;
//                    } else if (!box.getStation1() && box.getStation2()) {
//                        return 2;
//                    } else {
//                        return 9;
//                    }
//                }
//            } else if (scannerId.equals("2")) {
//                if (box.getStation3() == null ) {
//                    return 10;
//                } else {
//                    if (box.getStation3()) {
//                        return 3;
//                    } else {
//                        return 10;
//                    }
//                }
//            } else if (scannerId.equals("3")) {
//                if (box.getStation4() == null ) {
//                    return 11;
//                } else {
//                    if ( box.getStation4()) {
//                        return 4;
//                    } else {
//                        return 11;
//                    }
//                }
//            } else {
//                logger.info("[StationCheck][Invalid Scanner : {}]", scannerId);
//                return 7;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return 14;
//    }

//    public int stationCheck3(Box box, String scannerId) throws Exception {
//        try {
//            if (scannerId.equals("1")) {
//                if (box.getStation1() == null || box.getStation2() == null) {
//                    return 9;
//                } else {
//                    if (box.getStation1()) {
//                        return 1;
//                    } else if (!box.getStation1() && box.getStation2()) {
//                        return 2;
//                    } else {
//                        return 9;
//                    }
//                }
//            } else if (scannerId.equals("2")) {
//                if (box.getStation3() == null || box.getStation4() == null) {
//                    return 10;
//                } else {
//                    if (box.getStation3()) {
//                        return 3;
//                    } else if (!box.getStation3() && box.getStation4()) {
//                        return 4;
//                    } else {
//                        return 10;
//                    }
//                }
//            } else if (scannerId.equals("3")) {
//                if (box.getUrgent() == null) {
//                    return 11;
//                } else {
//                    if (box.getUrgent()) {
//                        return 5;
//                    } else {
//                        return 11;
//                    }
//                }
//            } else {
//                logger.info("[StationCheck][Invalid Scanner : {}]", scannerId);
//                return 7;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return 14;
//    }

//    public void updateRecords(Box box, int station) throws Exception {
//        if (station == 1) {
//            box.setStation1(false);
//        } else if (station == 2) {
//            box.setStation2(false);
//        } else if (station == 3) {
//            box.setStation3(false);
//        } else if (station == 4) {
//            box.setStation4(false);
//        } else if (station == 5) {
//            box.setUrgent(false);
//        }
//
//        boxRepository.save(box);
//
//        boxRepository.deleteEmptyBox();
//    }

}

