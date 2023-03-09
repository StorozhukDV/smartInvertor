package ru.smarteps.batterycontroller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.smarteps.batterycontroller.model.regulator.SetpointsControl;
import ru.smarteps.batterycontroller.service.regulator.PID;

@RestController
public class RegulatorController {

    @Autowired
    private PID pid;

    @GetMapping("/pregulator/start")
    public boolean start(){
        return pid.start();
    }

    @GetMapping("/pregulator/stop")
    public boolean stop(){
        return pid.stop();
    }

    @GetMapping("/pregulator/uset/")
    public void newVoltageSetpoint(@RequestParam(name = "uset") double setpoint){
        pid.changeSetpoint(setpoint, SetpointsControl.Automatic);
    }

    @GetMapping("/pregulator/adapterMode")
    public boolean adapterMode(){
       return pid.resetAdapter();
    }

//    @PostMapping("/pregulator/rs_set")
//    public void setNewSettings(@RequestBody RegulatorSettings rs){
//        pid.set
//    }

}
