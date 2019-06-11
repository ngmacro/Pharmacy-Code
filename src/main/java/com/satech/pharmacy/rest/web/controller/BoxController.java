package com.satech.pharmacy.rest.web.controller;

import com.satech.pharmacy.exception.BoxExistsException;
import com.satech.pharmacy.exception.BoxNotFoundException;
import com.satech.pharmacy.rest.web.dto.BoxDto;
import com.satech.pharmacy.rest.web.dto.ResponseDto;
import com.satech.pharmacy.rest.web.enums.ResponseStatus;
import com.satech.pharmacy.service.BoxService;
import com.satech.pharmacy.service.PharmacyCache;
import com.satech.pharmacy.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(path = "/box")
public class BoxController {

    private final BoxService boxService;
    private final StationService stationService;

    @Autowired
    public BoxController(BoxService boxService, StationService stationService) {
        this.boxService = boxService;
        this.stationService = stationService;
    }

    @RequestMapping(method = POST, path = "/add")
    public ResponseEntity<ResponseDto> add(@RequestBody BoxDto box) {
        if (StringUtils.isEmpty(box.getBoxNumber()) || box.getBoxNumber().length() != PharmacyCache.boxNumberLength) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "INVALID BOX NUMBER"));
        }
        if (box.getStations() == null || box.getStations().length == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "STATIONS IS EMPTY"));
        }
        List<Integer> invalidStations = stationService.checkStations(box.getStations());
        if(!CollectionUtils.isEmpty(invalidStations)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "INVALID STATIONS : " + invalidStations.toString()));
        }

        try {
            boxService.addNewBox(box);
            return ResponseEntity.ok().body(new ResponseDto(ResponseStatus.OK, "SUCCESSFUL"));
        } catch (BoxExistsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "BOX ALLREADY EXISTS : " + box.getBoxNumber()));
        }
    }

    @RequestMapping(method = POST, path = "/stationComplete")
    public ResponseEntity<ResponseDto> stationComplete(@RequestBody BoxDto box) {
        if (StringUtils.isEmpty(box.getBoxNumber()) || box.getBoxNumber().length() != PharmacyCache.boxNumberLength) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "INVALID BOX NUMBER"));
        }
        if (box.getStation() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "STATION IS NULL"));
        }
        List<Integer> invalidStations = stationService.checkStations(new Integer[]{box.getStation()});
        if(!CollectionUtils.isEmpty(invalidStations)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "INVALID STATION : " + invalidStations.toString()));
        }

        try {
            boxService.stationComplete(box);
        } catch (BoxNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "BOX NOT FOUND : " + box.getBoxNumber()));
        }
        return ResponseEntity.ok().body(new ResponseDto(ResponseStatus.OK, "SUCCESSFUL"));
    }

    @RequestMapping(method = POST, path = "/full")
    public ResponseEntity<ResponseDto> boxFull(@RequestBody BoxDto box) {
        if (StringUtils.isEmpty(box.getBoxNumber()) || box.getBoxNumber().length() != PharmacyCache.boxNumberLength) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "INVALID BOX NUMBER"));
        }

        try {
            boxService.boxFull(box);
        } catch (BoxNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "BOX NOT FOUND : " + box.getBoxNumber()));
        }
        return ResponseEntity.ok().body(new ResponseDto(ResponseStatus.OK, "SUCCESSFUL"));
    }

    @RequestMapping(method = POST, path = "/setOrderStatus")
    public ResponseEntity<ResponseDto> setOrderStatus(@RequestBody BoxDto box) {
        if (StringUtils.isEmpty(box.getBoxNumber()) || box.getBoxNumber().length() != PharmacyCache.boxNumberLength) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "INVALID BOX NUMBER"));
        }

        try {
            boxService.setOrderToCompleted(box);
        } catch (BoxNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto(ResponseStatus.ERR, "BOX NOT FOUND : " + box.getBoxNumber()));
        }
        return ResponseEntity.ok().body(new ResponseDto(ResponseStatus.OK, "SUCCESSFUL"));
    }

}
