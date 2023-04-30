package me.illusion.cosmos.utilities.sql;

import lombok.Data;

/**
 * Represents a column in a SQL table.
 */
@Data
public class SQLColumn {

    private final ColumnData data;
    private final SQLTable table;

    public SQLColumn(SQLTable table, ColumnData data) {
        this.table = table;
        this.data = data;
    }


}
