package ru.iris.commons.protocol;

import java.util.Date;

public interface DeviceValueChange {

    long getId();

    Date getDate();

    Object getValue();

    void setValue(Object value);

    String getAdditionalData();

    void setAdditionalData(String json);
}
