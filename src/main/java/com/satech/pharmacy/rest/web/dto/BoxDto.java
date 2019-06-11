package com.satech.pharmacy.rest.web.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BoxDto implements Serializable {

    private String boxNumber;

    private String orderNumber;

    // Çoklu istasyon bilgisi
    private Integer[] stations;

    // Tek istasyon
    private Integer station;

    // ERR veya OK
    private String status;
}
