package com.satech.pharmacy.repository;

import com.satech.pharmacy.model.BoxStation;
import com.satech.pharmacy.model.enums.StationStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.validation.constraints.Null;
import java.util.List;

public interface BoxStationRepository extends CrudRepository<BoxStation, Long> {

    List<BoxStation> getByBoxBoxNumberAndStatus(String boxNumber, StationStatus status);

    @Modifying
    @Query(value = "insert into box_station_archive (id,box_id,station_id, status, create_date, update_date, archive_date) " +
            "select id,box_id,station_id, status, create_date, update_date, current_timestamp  from box_station " +
            "where box_id in :boxIdList", nativeQuery = true)
    void archiveBoxStation(List<Long> boxIdList);

    @Modifying
    @Query(value = "delete from box_station where box_id in :boxIdList", nativeQuery = true)
    void deleteBoxStation(List<Long> boxIdList);

}
