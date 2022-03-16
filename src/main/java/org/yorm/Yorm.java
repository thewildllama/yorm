package org.yorm;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import org.yorm.db.QueryBuilder;
import org.yorm.exception.YormException;

public class Yorm {

    private Map<String, YormTable> map = new HashMap<>();
    private MapBuilder mapBuilder;
    private QueryBuilder queryBuilder;
    private DataSource ds;

    public Yorm(DataSource ds) {
        this.ds = ds;
        this.mapBuilder = new MapBuilder(ds);
        this.queryBuilder = new QueryBuilder(ds);
    }

    public int save(Record recordObj) throws YormException {
        String objectName = getRecordName(recordObj);
        YormTable yormTable = map.computeIfAbsent(objectName, o -> mapBuilder.buildMap(recordObj.getClass()));
        int result = 0;
        try {
            result = queryBuilder.save(ds, recordObj, yormTable);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new YormException(e.getMessage());
        }
        return result;
    }

    public int insert(Record recordObj) throws YormException {
        String objectName = getRecordName(recordObj);
        YormTable yormTable = map.computeIfAbsent(objectName, o -> mapBuilder.buildMap(recordObj.getClass()));
        int result = 0;
        try {
            result = queryBuilder.insert(ds, recordObj, yormTable);
        } catch (YormException e) {
            throw new YormException(e.getMessage());
        }
        return result;
    }

    public <T extends Record> void insert(List<T> recordListObj) throws YormException {
        if (recordListObj.isEmpty()) {
            return;
        }
        Record recordObj = recordListObj.get(0);
        String objectName = getRecordName(recordObj);
        YormTable yormTable = map.computeIfAbsent(objectName, o -> mapBuilder.buildMap(recordObj.getClass()));
        try {
            queryBuilder.bulkInsert(ds, recordListObj, yormTable);
        } catch (YormException e) {
            throw new YormException(e.getMessage());
        }
    }

    public void update(Record recordObj) throws YormException {
        String objectName = getRecordName(recordObj);
        YormTable yormTable = map.computeIfAbsent(objectName, o -> mapBuilder.buildMap(recordObj.getClass()));
        try {
            queryBuilder.update(ds, recordObj, yormTable);
        } catch (YormException e) {
            throw new YormException(e.getMessage());
        }
    }

    public <T extends Record> T get(Class<T> recordObject, int id) throws YormException {
        String objectName = getClassName(recordObject);
        YormTable yormTable = map.computeIfAbsent(objectName, o -> mapBuilder.buildMap(recordObject));
        return queryBuilder.get(ds, yormTable, id);
    }

    public <T extends Record> List<T> get(Class<T> referenceObject, Record filterObject) throws YormException {
        String filterObjectName = getRecordName(filterObject);
        String referenceObjectName = getClassName(referenceObject);
        YormTable yormTableFilter = map.computeIfAbsent(filterObjectName, o -> mapBuilder.buildMap(filterObject.getClass()));
        YormTable yormTableObject = map.computeIfAbsent(referenceObjectName, o -> mapBuilder.buildMap(referenceObject));
        List<T> result = new ArrayList<>();
        try {
            result = queryBuilder.get(ds, filterObject, yormTableFilter, yormTableObject);
        } catch (InvocationTargetException | IllegalAccessException | YormException e) {
            throw new YormException(e.getMessage());
        }
        return result;
    }

    public <T extends Record> List<T> get(Class<T> referenceObject) throws YormException {
        String referenceObjectName = getClassName(referenceObject);
        YormTable yormTable = map.computeIfAbsent(referenceObjectName, o -> mapBuilder.buildMap(referenceObject));
        return queryBuilder.get(ds, yormTable);
    }

    public <T extends Record> List<T> get(List<T> list) throws YormException {
        List<T> result = new ArrayList<>();
        if (list.isEmpty()) {
            return result;
        }
        Record recordObj = list.get(0);
        String objectName = getRecordName(recordObj);
        YormTable yormTable = map.computeIfAbsent(objectName, o -> mapBuilder.buildMap(recordObj.getClass()));
        try {
            result = queryBuilder.get(ds, list, yormTable);
        } catch (InvocationTargetException | IllegalAccessException | YormException e) {
            throw new YormException(e.getMessage());
        }
        return result;
    }

    private <T extends Record> String getClassName(Class<T> clazz) {
        return clazz.getSimpleName().toLowerCase(Locale.ROOT);
    }

    private String getRecordName(Record recordObj) {
        return recordObj.getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }

    public <T extends Record> void delete(Class<T> recordObject, int id) throws YormException {
        String objectName = getClassName(recordObject);
        YormTable yormTable = map.computeIfAbsent(objectName, o -> mapBuilder.buildMap(recordObject));
        queryBuilder.delete(ds, yormTable, id);
    }

}