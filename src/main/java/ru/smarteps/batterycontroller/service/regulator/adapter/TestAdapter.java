package ru.smarteps.batterycontroller.service.regulator.adapter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestAdapter implements PidAdapter{
    private List<Sample> samples;

    public TestAdapter() {
        this.samples = new ArrayList<>();
    }

    public Sample getSample(int index){
        if(this.samples.isEmpty()){
            log.error("Samples is empty, need to check in debug why!");
        }
        return this.samples.get(index);
    }

    @Override
    public void resetCollectedData() {
        this.samples = new ArrayList<>();
    }

    @Override
    public void addSample(double Umeas, double sOut, double angleDegree) {
        log.info("Sample was added. Udif = {}  Power = {}",Umeas,sOut);
        samples.add(new Sample(Umeas,sOut,angleDegree));
    }

    @Override
    public double generateAngle() {
        double sAngle = 0;
        if(samples.size()<2){
            log.error("Samples size less then 2, need to check in debug. Angle returned as 0");
            return sAngle;
        }
        else{
            Sample pSample = samples.get(0);
            Sample qSample = samples.get(1);
            double activePowerEfficiency = pSample.Umeas/ pSample.sOut;
            double reactivePowerEfficiency = qSample.Umeas/ qSample.sOut;
            sAngle = Math.toDegrees(Math.atan(reactivePowerEfficiency/activePowerEfficiency));
        }
        return sAngle;
    }

    @Getter
    public class Sample {
        private double Umeas;
        private double sOut;
        private double angleDegree;

        public Sample(double umeas, double sOut, double angleDegree) {
            Umeas = umeas;
            this.sOut = sOut;
            this.angleDegree = angleDegree;
        }
    }
}
