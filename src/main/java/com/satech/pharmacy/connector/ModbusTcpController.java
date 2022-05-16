package com.satech.pharmacy.connector;

import de.re.easymodbus.modbusclient.ModbusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ModbusTcpController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${satech.plc.ip}")
    private String plcIp;

    @Value("${satech.plc.port}")
    private Integer plcPort;

    @Value("${satech.plc.connTimeout}")
    private Integer connTimeout;

    public boolean sendData(int returnCode, int plcRegisterNo) throws Exception {
        ModbusClient modbusClient = new ModbusClient(plcIp, plcPort);
        int tryCount = 0;
        if (!modbusClient.isConnected()) {

            while (true) {
                try {
                    modbusClient.setConnectionTimeout(connTimeout);
                    modbusClient.Connect();
                    logger.debug("[ModbusTcpController][SendData][Modbus Connection : OK ]");
                    if(modbusClient.isConnected()) {
                        try {
                        logger.info("[ModbusTcpController][SendData][ReturnCode: {}][PlcRegisterNo: {}]", returnCode, plcRegisterNo);
                        modbusClient.WriteSingleRegister(plcRegisterNo, returnCode);

                        break;
                        }
                        catch (Exception e) {
                        logger.error("[ModbusTcpController][SendData][Modbus Client Exception!]", e);
                            tryCount++;
                            Thread.sleep(500);
                        }
                    }

                } catch (Exception e) {
                    tryCount++;
                    logger.error("[ModbusTcpController][SendData][Modbus Connection Exception! Deneme başarısız. TryCount: {}]", tryCount);
                    e.printStackTrace();
                    Thread.sleep(500);
                }

                if (tryCount > 3) {
                    logger.debug("[ModbusTcpController][SendData][Modbus Connection Exception! 10 Deneme tamamlandı.]");
                    break;
                }
            }
        } else {
            logger.debug("[ModbusTcpController][SendData][Already Connected!]");
        }

        //try {
            //logger.info("[ModbusTcpController][SendData][ReturnCode: {}][PlcRegisterNo: {}]", returnCode, plcRegisterNo);
            //modbusClient.WriteSingleRegister(plcRegisterNo, returnCode);
        //} catch (Exception e) {
            //logger.error("[ModbusTcpController][SendData][Modbus Client Exception!]", e);
        //}

        if (modbusClient.isConnected()) {
            modbusClient.Disconnect();
            Thread.sleep(500);
        }

        return tryCount <= 3;
    }

}
 



