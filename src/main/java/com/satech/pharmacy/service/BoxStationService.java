package com.satech.pharmacy.service;


import com.satech.pharmacy.model.BoxStation;
import com.satech.pharmacy.model.enums.StationStatus;
import com.satech.pharmacy.repository.BoxStationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


@Component
@Transactional(readOnly = true)
public class BoxStationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BoxStationRepository repository;

    @Autowired
    public BoxStationService(BoxStationRepository repository) {
        this.repository = repository;
    }

    public List<BoxStation> getOpenBoxStations(String boxNumber) {
        return repository.getByBoxBoxNumberAndStatus(boxNumber, StationStatus.OPEN);
    }

    @Transactional
    public BoxStation save(BoxStation boxStation) {
        return repository.save(boxStation);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAll(Long boxId) {
        repository.deleteBoxStation(Arrays.asList(boxId));
    }

    public void archiveBoxStation(List<Long> boxIdList) {
        repository.archiveBoxStation(boxIdList);
    }

    public void deleteBoxStation(List<Long> boxIdList) {
        repository.deleteBoxStation(boxIdList);
    }
}

