package me.illusion.cosmos.utilities.sql;

import lombok.Data;

@Data
public class ColumnData {

    private final String name;
    private final ColumnType type;

    private boolean primary;
    private Object data;


    public ColumnData(String name, ColumnType type) {
        this.name = name;
        this.type = type;
    }

    public ColumnData(String name, ColumnType type, Object data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public ColumnData(String name, ColumnType type, Object data, boolean primary) {
        this.name = name;
        this.type = type;
        this.data = data;
        this.primary = primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
