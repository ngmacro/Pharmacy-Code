package com.satech.pharmacy.repository;

import com.satech.pharmacy.model.Box;
import com.satech.pharmacy.model.enums.StationStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BoxRepository extends CrudRepository<Box, Long> {

    Box getByBoxNumber(String boxNumber);

    @Modifying
    @Query("update BoxStation set status=:newStatus, updateDate=current_timestamp where box= :box and stationId=:stationId")
    void setStationStatus(Box box, Integer stationId, StationStatus newStatus);

    @Modifying
    @Query("update BoxStation set status=:newStatus, updateDate=current_timestamp where box= :box and status='OPEN' " +
            "and stationId in (select id from Station where type ='STATION')")
    void cancelAllStation(Box box, StationStatus newStatus);

    @Modifying
    @Query("update BoxStation set status=:newStatus, updateDate=current_timestamp where box= :box and status='OPEN' " +
            "and stationId in (select id from Station where type ='ERROR')")
    void setOrderToCompleted(Box box, StationStatus newStatus);

    @Query("select b.id from Box b where not exists (select 1 from BoxStation bs where bs.box = b and bs.status='OPEN')")
    List<Long> getAllCompletedBoxId();


    @Modifying
    @Query(value = "insert into box_archive (id,box_number,order_number, create_date, archive_date) " +
            "select id,box_number,order_number, create_date, current_timestamp  from box " +
            "where id in :boxIdList", nativeQuery = true)
    void archiveBox(List<Long> boxIdList);

    @Modifying
    @Query(value = "delete from box where id in :boxIdList", nativeQuery = true)
    void deleteBox(List<Long> boxIdList);


}
