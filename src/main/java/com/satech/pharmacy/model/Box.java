package com.satech.pharmacy.model;

import lombok.Data;
import org.springframework.util.ClassUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "box")
@Data
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String boxNumber;

    private String orderNumber;

    @OneToMany(mappedBy = "box",cascade = CascadeType.ALL)
    private List<BoxStation> boxStations;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

}

