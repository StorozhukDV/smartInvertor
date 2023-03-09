package ru.smarteps.batterycontroller.service.dataExchange.iec104;

import lombok.extern.slf4j.Slf4j;
import org.openscada.protocol.iec60870.ProtocolOptions;
import org.openscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.openscada.protocol.iec60870.asdu.types.DoublePoint;
import org.openscada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.openscada.protocol.iec60870.client.AutoConnectClient;
import org.openscada.protocol.iec60870.client.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.smarteps.batterycontroller.model.dataExchange.*;
import ru.smarteps.batterycontroller.service.dataExchange.DataContainer;
import ru.smarteps.batterycontroller.service.dataExchange.iec104.asducommands.ICommandCreator;
import ru.smarteps.batterycontroller.utils.WorkWithCfg;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class OpenScadaClient implements DataContainer {
    @Value("${iec104.client-cfg-path}")
    private String pathToCfg;
    @Autowired
    private final Map<String, ICommandCreator> commandCreators = new HashMap<>();
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, MeasurementTO> measurementsTO = new HashMap<>();

    private AutoConnectClient clientInstance;
    private AutoConnectClient.ModulesFactory modulesFactory;

    private ConnectedServerInfo clientCfg;
    private AutoConnectClient.State connectionState = AutoConnectClient.State.DISCONNECTED;


    public boolean openConnection(AutoConnectClient.StateListener serviceListener) {

        AutoConnectClient.StateListener stateListener = (state, e) -> {
            connectionState = state;
            if (e != null)
                log.error("Connection throwed exeption : {}, cause {} ", e.getMessage(), e.getCause());

            if (serviceListener != null)
                serviceListener.stateChanged(state, e);
        };

        try {
            clientInstance = new AutoConnectClient(clientCfg.getAddress(), clientCfg.getPort(), new ProtocolOptions.Builder().build(), modulesFactory, stateListener);
        } catch (Exception ex) {
            log.error("Connection opening failed . reason : {}", ex.getMessage());
            return false;
        }

        return true;
    }

    public boolean sendCommand(String cmdName, Double cmdVal) {
        if (clientInstance == null || connectionState != AutoConnectClient.State.CONNECTED) {
            log.warn("Command {} can not be sen as the connection is not opened", cmdName);
            return false;
        }

        if (cmdVal == null) {
            log.warn("value of command can not be null or empty {}. It will not be sent", cmdName);
            return false;
        }

        if (!commands.containsKey(cmdName)) {
            log.warn("This command  is not declared as command to send to server {} (config file does not contain cmd info for given node {}). It will not be sent", clientCfg.getServerInfo(), cmdName);
            return false;
        }

        Command cmdInfo = commands.get(cmdName);
        ICommandCreator iCommandCreator = commandCreators.get(cmdInfo.getCommandType().toString());
        if (iCommandCreator == null) {
            log.error("Command {} type is not supported", cmdInfo.getCommandType().toString());
            return false;
        }


        boolean res = iCommandCreator.createAndSend(clientInstance, cmdVal, cmdInfo.getAsduAddress(), cmdInfo.getIoAddress(), cmdInfo.isSetTimeTag(), false);
        if (res){
            log.info("Command {} = {} successfully sent", cmdName, cmdVal);
        } else {
            log.warn("Failed to send command {} = {}", cmdName, cmdVal);
        }
        return res;
    }

    @PostConstruct
    public void init() {
        clientCfg = WorkWithCfg.unMarshalAny(ConnectedServerInfo.class, pathToCfg);
        //TODO: check cfg
        if (clientCfg == null)
            throw new RuntimeException("Config is incorrect");

        for (Command command : clientCfg.getCommands()) {
            commands.put(command.getName(), command);
        }

        for (Measurement measurement : clientCfg.getMeasurements()) {
            measurementsTO.put(measurement.sigName(), new MeasurementTO(measurement.getName()));
        }

        DataHandler dataHandler = new DataProcessor(Executors.newSingleThreadExecutor(), new Iec104DataListener(clientCfg.getServerInfo(), measurementsTO));
        modulesFactory = () -> Collections.singletonList(new DataModule(dataHandler, new DataModuleOptions.Builder().build()));

        openConnection((state, throwable) -> log.info("status changed to {}",state));
    }

    @Override
    public Optional<MeasurementTO> findLastByTag(String tag) {
        return measurementsTO.values().stream().filter(el -> el.getName().equals(tag)).findAny();
    }

    @Override
    public boolean setCommand(String tag, Double value) {
        return sendCommand(tag, value);
    }
}
