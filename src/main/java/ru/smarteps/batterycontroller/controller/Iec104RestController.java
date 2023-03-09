package ru.smarteps.batterycontroller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.smarteps.batterycontroller.model.dataExchange.CommandTO;
import ru.smarteps.batterycontroller.model.dataExchange.MeasurementTO;
import ru.smarteps.batterycontroller.service.dataExchange.DataContainer;

import java.util.Optional;

@RestController
public class Iec104RestController {

    @Autowired
    private DataContainer iec104;

    @PostMapping("iec104/send/command")
    public ResponseEntity<Void> sendCommand(@RequestBody CommandTO command){
        boolean result = iec104.setCommand(command.getName(), command.getValue());
        return new ResponseEntity<>(result ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("iec104/receive/measurement")
    public ResponseEntity<MeasurementTO> getMeasurement(@RequestBody MeasurementTO meas){
        Optional<MeasurementTO> result = iec104.findLastByTag(meas.getName());
        if (result.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(result.get(), HttpStatus.OK );
        }
    }

}
