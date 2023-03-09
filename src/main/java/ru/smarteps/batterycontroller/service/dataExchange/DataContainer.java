package ru.smarteps.batterycontroller.service.dataExchange;


import ru.smarteps.batterycontroller.model.dataExchange.MeasurementTO;

import java.util.Optional;

public interface DataContainer {

    /**
     * returns last measured value of given tag.
     * if values by this tag was not found, throws exception
     * @param tag - describes parameter to get value
     * @return double value of measured parameter
     */
    Optional<MeasurementTO> findLastByTag(String tag);

    /**
     * set calculate command value to apply it to energy object
     * @param tag - describes paramter to set as value
     * @param value - double representation of command
     */
    boolean setCommand(String tag, Double value);

}
