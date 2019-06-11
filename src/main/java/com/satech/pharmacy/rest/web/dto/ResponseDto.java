package com.satech.pharmacy.rest.web.dto;

import com.satech.pharmacy.rest.web.enums.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ResponseDto implements Serializable {
    private ResponseStatus status;
    private String description;
}
