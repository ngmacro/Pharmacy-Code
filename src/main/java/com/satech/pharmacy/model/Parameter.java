package com.satech.pharmacy.model;

import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.Data;
import org.json.JSONObject;

import javax.persistence.*;

@Entity
@Table(name = "parameter")
@Data
public class Parameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Double numberValue;

    private String stringValue;

}

