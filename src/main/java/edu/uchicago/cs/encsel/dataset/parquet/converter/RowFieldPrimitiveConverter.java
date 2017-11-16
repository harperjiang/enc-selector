package edu.uchicago.cs.encsel.dataset.parquet.converter;

import org.apache.parquet.column.Dictionary;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.io.api.PrimitiveConverter;
import org.apache.parquet.schema.PrimitiveType;

public class RowFieldPrimitiveConverter extends PrimitiveConverter {

    private int index;

    private Dictionary dictionary;

    private RowConverter parent;

    private PrimitiveType type;

    public RowFieldPrimitiveConverter(RowConverter parent, int index, PrimitiveType type) {
        this.parent = parent;
        this.index = index;
        this.type = type;
    }

    @Override
    public boolean hasDictionarySupport() {
        return true;
    }

    @Override
    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public void addValueFromDictionary(int dictionaryId) {
        switch (this.type.getPrimitiveTypeName()) {
            case BINARY:
                addBinary(this.dictionary.decodeToBinary(dictionaryId));
                break;
            case FLOAT:
                addFloat(this.dictionary.decodeToFloat(dictionaryId));
                break;
            case DOUBLE:
                addDouble(this.dictionary.decodeToDouble(dictionaryId));
                break;
            case INT32:
                addInt(this.dictionary.decodeToInt(dictionaryId));
                break;
            case INT64:
                addLong(this.dictionary.decodeToLong(dictionaryId));
                break;
            case BOOLEAN:
                addBoolean(this.dictionary.decodeToBoolean(dictionaryId));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void addBinary(Binary value) {
        parent.getCurrentRecord().add(index, value);
    }

    @Override
    public void addBoolean(boolean value) {
        parent.getCurrentRecord().add(index, value);
    }

    @Override
    public void addDouble(double value) {
        parent.getCurrentRecord().add(index, value);
    }

    @Override
    public void addFloat(float value) {
        parent.getCurrentRecord().add(index, value);
    }

    @Override
    public void addInt(int value) {
        parent.getCurrentRecord().add(index, value);
    }

    @Override
    public void addLong(long value) {
        parent.getCurrentRecord().add(index, value);
    }

}
