package com.satech.pharmacy.service;

import com.satech.pharmacy.model.ScannerMapping;
import com.satech.pharmacy.model.Station;

import java.util.HashMap;
import java.util.Map;

public class PharmacyCache {

    // Barkod okuyucuları ve buna göre hangi istasyona gidecek ise onun kodunu tutar.
    // Parametre tablosundaki JSON alandan yüklenerek oluşturulur.
    public static Map<String, ScannerMapping> scannerMapping = new HashMap<>();

    // Kutuların üzerinde bulunan kutu numarsının uzunluğu.
    public static int boxNumberLength;

    // Tanımlı bütün istasyonları ID leri ile saklar.
    public static Map<Integer, Station> allStations = new HashMap<>();

    public static boolean isStationAutoCompleteActive = true;

    public static boolean throwExceptionIfBoxExists = true;

    public static boolean addErrorStationByDefault = true;

    public static boolean isErrorStationAutoCompleteActive = false;
}
