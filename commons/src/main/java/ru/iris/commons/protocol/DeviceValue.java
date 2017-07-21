package ru.iris.commons.protocol;

import ru.iris.commons.LIFO;
import ru.iris.commons.protocol.enums.ValueType;

import java.util.Date;

public interface DeviceValue {

    long getId();

    Date getDate();

    String getName();

    void setName(String name);

    <T> T getCurrentValue(Class<T> type);

    Object getCurrentValue();

    void setCurrentValue(Object value);

    Date getLastUpdated();

    String getUnits();

    void setUnits(String units);

    boolean isReadOnly();

    void setReadOnly(boolean readOnly);

    ValueType getType();

    void setType(ValueType type);

    String getAdditionalData();

    void setAdditionalData(String json);

    LIFO<? extends DeviceValueChange> getChanges();

    void setChanges(LIFO<? extends DeviceValueChange> changes);
}
