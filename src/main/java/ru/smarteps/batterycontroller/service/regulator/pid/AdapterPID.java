package ru.smarteps.batterycontroller.service.regulator.pid;

import lombok.extern.slf4j.Slf4j;
import ru.smarteps.batterycontroller.model.dataExchange.MeasurementTO;
import ru.smarteps.batterycontroller.model.regulator.OperationMode;
import ru.smarteps.batterycontroller.model.regulator.RegulatorSettings;
import ru.smarteps.batterycontroller.model.regulator.Tag;
import ru.smarteps.batterycontroller.service.dataExchange.DataContainer;
import ru.smarteps.batterycontroller.service.regulator.adapter.TestAdapter;

import java.util.Optional;

@Slf4j
public class AdapterPID extends GenericPid{
    private int testCycle = 0;
    TestAdapter adapter;

    private final String measTag;
    private double fAngle = 0;
    double outValue = 0;

    double initialU = 0;
    private boolean pTest = true;
    private boolean qTest = true;

    private double initialP;
    private double initialQ;

    public AdapterPID(DataContainer dataContainer, RegulatorSettings rs) {
        super(dataContainer, rs);
        this.measTag = rs.getTags().get(Tag.MeasPOutput);
        this.adapter = new TestAdapter();
    }

    @Override
    public double calculateOutputValue(double Umeas) {
        if (workWasPaused) {
            workWasPaused = false;
            Integ = getMeasuredOutput(measTag);
            log.debug("Work was paused. Integral part was updated to {}", Integ);
        }
        if(!pTest&!qTest){
            return outValue;
        }

        if(pTest&testCycle==0){
            Optional<MeasurementTO> P = dataContainer.findLastByTag(rs.getTags().get(Tag.MeasPOutput));
            Optional<MeasurementTO> Q = dataContainer.findLastByTag(rs.getTags().get(Tag.MeasQOutput));
            if(P.isPresent()& Q.isPresent()){
                initialP = P.get().getValue();
                initialQ = Q.get().getValue();
            }
            else{
                log.error("No initial values for P and Q meas, need to check debug!");
            }
            log.info("Adapter mode. P test");
            prevOutput = initialP;
        }
        if(!pTest&qTest&testCycle==0){
            log.info("Adapter mode. Q test");
            prevOutput = initialQ;
        }

        if(testCycle==0){
            initialU = Umeas;
            log.info("Initial U for test is {}",initialU);
        }


        if(testCycle>=6&testCycle<20){
            ++testCycle;
            log.info("Waiting for the end of transients. Return value is {}",outValue);
            return outValue;
        }


        outValue = prevOutput+1;
        log.info("Regulation process: U setpoint = {}, U measured = {},  output value = {}", rs.getUSetPoint(), Umeas, outValue);
        prevOutput = outValue;
        ++testCycle;

        if(testCycle>=20){
            if(initialU==0){
                log.info("Initial U doesnt exist! Check in debug!");
            }
            double Udif = Umeas - initialU;
            if(pTest){
                this.pTest = false;
            }
            else{
                this.qTest = false;
            }
            adapter.addSample(Udif,outValue-1,fAngle);
            testCycle=0;
            if(!pTest&!qTest){
                fAngle = adapter.generateAngle();
                log.info("S angle was calculated as: {}",fAngle);
                rs.setSAngle(fAngle);
            }
        }
        return outValue;
    }



    @Override
    public OperationMode getModeType() {
        return OperationMode.Adapter;
    }
    @Override
    public void resetAdapter(){
        this.pTest = true;
        this.qTest = true;
        adapter.resetCollectedData();
        testCycle = 0;
        rs.setSAngle(-1);

    }

    @Override
    public void sendOutput() {
        double P = 0;
        double Q = 0;

        if(pTest){
            P = outValue;
            Q = initialQ;
        }
        if(!pTest&qTest){
            P = adapter.getSample(0).getSOut();
            Q = outValue;
        }
        if(!pTest&qTest&testCycle==0){
            P = adapter.getSample(0).getSOut();
            Q = initialQ;
        }
        if(!pTest&!qTest){
            P = initialP;
            Q = initialQ;
            log.info("Adapter mode is completed, return back to initial output values.P = {} and Q = {}",P,Q);
        }

        dataContainer.setCommand(rs.getTags().get(Tag.CtrlPOutput), P);
        dataContainer.setCommand(rs.getTags().get(Tag.CtrlQOutput), Q);
    }
}
