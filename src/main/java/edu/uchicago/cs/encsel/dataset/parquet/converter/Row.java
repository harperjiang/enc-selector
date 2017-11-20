package edu.uchicago.cs.encsel.dataset.parquet.converter;

import org.apache.parquet.io.api.Binary;

public class Row {

    private Object[] data;

    public Object[] getData() { return data; }

    public Row(int colCount) {
        this.data = new Object[colCount];
    }

    public void add(int index, Binary value) {
        data[index] = value;
    }

    public void add(int index, boolean value) {
        data[index] = value;
    }

    public void add(int index, double value) {
        data[index] = value;
    }

    public void add(int index, float value) {
        data[index] = value;
    }

    public void add(int index, int value) {
        data[index] = value;
    }

    public void add(int index, long value) {
        data[index] = value;
    }
}
