package com.satech.pharmacy.model;

import com.satech.pharmacy.model.enums.StationType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "station")
@Data
public class Station {

    @Id
    private Integer id;

    private String description;

    @Enumerated(EnumType.STRING)
    private StationType type;

}

