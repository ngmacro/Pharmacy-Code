package com.satech.pharmacy.model;

import com.satech.pharmacy.model.enums.StationStatus;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "box_station")
@Data
public class BoxStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name="box_id", referencedColumnName="id")
    private Box box;

    private Integer stationId;

    @Enumerated(EnumType.STRING)
    private StationStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

}

