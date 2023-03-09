package ru.smarteps.batterycontroller.service.dataExchange.iec104;

import lombok.extern.slf4j.Slf4j;
import org.openscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.openscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.openscada.protocol.iec60870.client.data.DataListener;
import ru.smarteps.batterycontroller.model.dataExchange.Measurement;
import ru.smarteps.batterycontroller.model.dataExchange.MeasurementTO;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class Iec104DataListener implements DataListener {
    private final MeasurementParser parser = new MeasurementParser();
    private final String serverInfo;
    private final Map<String, MeasurementTO> mtos;

    public Iec104DataListener(String serverInfo, Map<String, MeasurementTO> mtos) {
        this.serverInfo = serverInfo;
        this.mtos = mtos;
    }


    @Override
    public void started() {
        log.info("Connected to server {}", serverInfo);
    }

    @Override
    public void update(ASDUAddress commonAddress, InformationObjectAddress objectAddress, org.openscada.protocol.iec60870.asdu.types.Value<?> value) {
        String sigCodeName = Measurement.sigName(commonAddress.getAddress(), objectAddress.getAddress());
        if (mtos.containsKey(sigCodeName)) {
            Optional<Double> res = parser.chooseValue(value.getValue());
            if (res.isEmpty()) {
                log.error("Can not parse value of {} from ca={} and ioa={}", value, commonAddress.getAddress(), objectAddress.getAddress());
                return;
            }
            MeasurementTO mto = mtos.get(sigCodeName);
            log.debug("measurement value {} was updated to {}", mto.getName(), res.get());
            mto.setValue(res.get());
            mto.setTime(LocalDateTime.now().toString());
        }
    }

    @Override
    public void disconnected() {
        log.warn("Disconnected from server {}", serverInfo);
    }
}
