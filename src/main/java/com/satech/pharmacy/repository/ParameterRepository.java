package com.satech.pharmacy.repository;

import com.satech.pharmacy.model.Parameter;
import org.springframework.data.repository.CrudRepository;

public interface ParameterRepository extends CrudRepository<Parameter, Long> {

    Parameter getByName(String parameterName);

}
