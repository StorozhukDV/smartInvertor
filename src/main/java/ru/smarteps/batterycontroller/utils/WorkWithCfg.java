package ru.smarteps.batterycontroller.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class WorkWithCfg {
    public static <T> T unMarshalAny(Class<T> clazz, String outPutFileName) {
        T object = null;
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Object obj = jaxbUnmarshaller.unmarshal(new File(outPutFileName));
            try {
                object = (T) obj;
            } catch (ClassCastException cce) {
                object = null;
                cce.printStackTrace();
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static <T> void marshalAny(Class<T> clazz, T information, String outPutFileName) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(clazz);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(information, new File(outPutFileName));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
