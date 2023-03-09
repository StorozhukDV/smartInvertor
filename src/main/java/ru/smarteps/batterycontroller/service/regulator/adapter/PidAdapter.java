package ru.smarteps.batterycontroller.service.regulator.adapter;

public interface PidAdapter {

    /**
     * remove all collected samples to start generating new angle value
     */
    void resetCollectedData();

    /**
     * add new data sample to sample window
     * @param Umeas - measured voltage
     * @param sOut - power output (S)
     * @param angleDegree - angle of generated S in degree
     */
    void addSample(double Umeas, double sOut, double angleDegree);

    /**
     * calculate new value of S angle based on collected data
     * @return new value of S angle in degree
     */
    double generateAngle();
}
