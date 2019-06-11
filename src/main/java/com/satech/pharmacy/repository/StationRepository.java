package com.satech.pharmacy.repository;

import com.satech.pharmacy.model.Station;
import com.satech.pharmacy.model.enums.StationType;
import org.springframework.data.repository.CrudRepository;

public interface StationRepository extends CrudRepository<Station, Long> {

    Station getFirstByTypeOrderById(StationType type);

}
