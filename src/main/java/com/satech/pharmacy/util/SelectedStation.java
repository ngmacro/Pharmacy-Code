package com.satech.pharmacy.util;

import com.satech.pharmacy.model.BoxStation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SelectedStation {
    private BoxStation boxStation;

    private Integer returnCode;
}
