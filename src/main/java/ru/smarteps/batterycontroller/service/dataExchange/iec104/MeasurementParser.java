package ru.smarteps.batterycontroller.service.dataExchange.iec104;

import org.openscada.protocol.iec60870.asdu.types.DoublePoint;

import java.util.Optional;

public class MeasurementParser {

    public Optional<Double> chooseValue(Object val) {
        if (val.getClass().equals(Double.class) || val.getClass().equals(Short.class)) {
            return Optional.of((double) val);
        }

        if (val.getClass().equals(Float.class)) {
            float fval = (Float) val;
            if (Math.abs(fval) < 0.001 || Math.abs(fval) > 999_000_000_000L)
                return Optional.of(0.0);
            return Optional.of(((Float) val).doubleValue());
        }


        if (val.getClass().equals(Boolean.class)) {
            return Optional.of(((boolean) val) ? 1.0 : 0.0);
        }

        if (val.getClass().equals(DoublePoint.class))
            switch ((DoublePoint) val) {
                case ON:
                    return Optional.of(1.0);
                case OFF:
                    return Optional.of(0.0);
                default:
                    return Optional.empty();
            }

        return Optional.empty();
    }

}
