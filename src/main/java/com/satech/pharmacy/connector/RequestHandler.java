package com.satech.pharmacy.connector;


import com.satech.pharmacy.model.BoxStation;
import com.satech.pharmacy.model.ScannerMapping;
import com.satech.pharmacy.model.Station;
import com.satech.pharmacy.model.enums.StationStatus;
import com.satech.pharmacy.model.enums.StationType;
import com.satech.pharmacy.service.BoxStationService;
import com.satech.pharmacy.service.PharmacyCache;
import com.satech.pharmacy.util.PharmacyUtil;
import com.satech.pharmacy.util.SelectedStation;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.List;

@Component
public class RequestHandler extends IoHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BoxStationService boxStationService;

    private final ModbusTcpController modbusTcpController;

    @Autowired
    public RequestHandler(BoxStationService boxStationService, ModbusTcpController modbusTcpController) {
        this.boxStationService = boxStationService;
        this.modbusTcpController = modbusTcpController;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
        session.closeNow();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        IoBuffer input = (IoBuffer) message;
        String messageStr = input.getString(Charset.forName("UTF-8").newDecoder());
        messageStr = messageStr.replaceAll("\n", "").replaceAll("\r", "");
        logger.info("[RequestHandler][MessageReceived: {}]", messageStr);
        processInput(messageStr);
    }

    private void processInput(String input) throws Exception {
        long startProcess = System.currentTimeMillis();
        // Gelen Veri "ScannerId-BoxNumber" şeklinde olmalı.
        String[] data = input.split("-");

        String scannerId = data[0];
        String boxNumber = data[1];

        if (StringUtils.isEmpty(data[0]) || StringUtils.isEmpty(data[1]) || data[1].contains("NoRead")) {
            logger.warn("[ProcessInput][Wrong Data][Keep Going]");
            keepGoing(scannerId);
        } else {
            long subStart = System.currentTimeMillis();
            List<BoxStation> boxStations = boxStationService.getOpenBoxStations(boxNumber);
            logger.debug("[ProcessInput][Get BoxStation with Number in {}ms]", System.currentTimeMillis() - subStart);

            if (CollectionUtils.isEmpty(boxStations)) {
                // Bu kutuya ait hiç istasyon yok veya Kutu yok.
                // KeepGoing
                logger.debug("[ProcessInput][Box or BoxStation is not found][Keep Going]");
                keepGoing(scannerId);
            } else {
                subStart = System.currentTimeMillis();

                // TODO Burada kontol yapılırken istasyonlar sırayla dikkate alınıyor
                // TODO Eğer bir istasyonda önce bir rota varsa o zaman o değerlendiriliyor.
                // TODO İstasyonların bitmesi gibi bir olay yok.
                SelectedStation selectedStation = PharmacyUtil.findStationReturnCode(scannerId, boxStations);
                logger.debug("[ProcessInput][Find Station in {}ms]", System.currentTimeMillis() - subStart);

                // Eğer bir istasyon bulunabilmişse ve Scanner tanımlanmış bir Scanner ise
                if (selectedStation != null && PharmacyUtil.getScanner(scannerId) != null) {
                    // Bulunan Return Code PLC ye gönderliyor.
                    // Eger istasyonbulunmus ise ona ait ReturnCode
                    // Eger bulunamamıs ise 999 hata kodu veya KeepGoing kodu gonderilecek.
                    subStart = System.currentTimeMillis();
                    ScannerMapping scanner = PharmacyUtil.getScanner(scannerId);
                    modbusTcpController.sendData(selectedStation.getReturnCode(), scanner.getPlcRegisterNo());
                    logger.info("[ProcessInput][Scanner:{}][Box:{}][ReturnCode:{}][Send Data To PLC in {}ms]", scannerId, boxNumber,
                            selectedStation.getReturnCode(), (System.currentTimeMillis() - subStart));

                    // Verilen numara için bir istasyon bulunmuş.
                    if (selectedStation.getBoxStation() != null) {
                        Station station = PharmacyCache.allStations.get(selectedStation.getBoxStation().getStationId());

                        // Eğer otomatik Tamamlama aktif ise veya İstasyonTipi ERROR veya ROUTE ise istasyonun durumu
                        // TAMAMLANDI olarak değiştirilecek
                        if (PharmacyCache.isStationAutoCompleteActive || (station != null && StationType.ERROR.equals(station.getType()))
                                || (station != null && StationType.ROUTE.equals(station.getType()))) {

                            subStart = System.currentTimeMillis();

                            selectedStation.getBoxStation().setStatus(StationStatus.DONE);
                            boxStationService.save(selectedStation.getBoxStation());

                            logger.debug("[ProcessInput][Update Record in {}ms]", System.currentTimeMillis() - subStart);
                        }
                    }
                }
            }
        }
        logger.info("[ProcessInput][Process Ended In {}]", System.currentTimeMillis() - startProcess);
        logger.info("---------------------------------------------------------------------------------");
        System.out.println();
        System.out.println();
    }

    private void keepGoing(String scannerId) throws Exception {
        if (!StringUtils.isEmpty(scannerId) && PharmacyUtil.getScanner(scannerId) != null) {
            ScannerMapping scanner = PharmacyUtil.getScanner(scannerId);
            // Keep Going Station = 0
            modbusTcpController.sendData(scanner.getKeepGoingCode(), scanner.getPlcRegisterNo());
        }
    }

}

