package ru.smarteps.batterycontroller.service.dataExchange.iec104.asducommands;


import org.openscada.protocol.iec60870.client.AutoConnectClient;

public interface ICommandCreator {
    boolean createAndSend(AutoConnectClient client, Object cmdValue, int asduAddr, int ioAddr, boolean useTimeTag, boolean invertSignal);
    String getMyName();
}
