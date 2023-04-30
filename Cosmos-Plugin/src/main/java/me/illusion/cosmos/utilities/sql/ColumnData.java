package me.illusion.cosmos.utilities.sql;

import lombok.Data;

/**
 * Represents a column in a SQL table.
 */
@Data
public class ColumnData {

    private final String name; // the name of the column
    private final ColumnType type; // the type of the column

    private boolean primary; // if the column is a primary key
    private Object data; // the length of a varchar, or the precision of a decimal, or just null

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

    /**
     * Sets the data of the column.
     *
     * @param primary The data of the column
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
