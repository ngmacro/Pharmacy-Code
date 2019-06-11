package com.satech.pharmacy.service;

import com.satech.pharmacy.model.Station;
import com.satech.pharmacy.model.enums.StationType;
import com.satech.pharmacy.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
public class StationService {
    private final StationRepository repository;

    @Autowired
    public StationService(StationRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    private void init() {
        // Bütün istasyonlar yüklenip hafizada tutulacak
        Iterable<Station> stations = repository.findAll();
        stations.forEach(station -> PharmacyCache.allStations.put(station.getId(), station));
    }

    /**
     * Verilen istasyon listesinin veritabanında tanımlı olup olmadığını kontrol eder.
     * @param stations
     * @return Veritabanında olmayan istasyonların listesi
     */
    public List<Integer> checkStations(Integer[] stations) {
        Set<Integer> stationIDs = PharmacyCache.allStations.keySet();
        List<Integer> checkedStations = new ArrayList<Integer>(Arrays.asList(stations));
        stationIDs.forEach(id -> checkedStations.remove(id));
        return checkedStations;
    }

    /**
     * Tipi ERROR olan ilk istasyonu geri verir
     * @return Error Station
     */
    public Station getFirstErrorStation() {
        return repository.getFirstByTypeOrderById(StationType.ERROR);
    }
}
