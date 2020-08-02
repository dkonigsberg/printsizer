package org.logicprobe.printsizer.db;

import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;

import java.util.Arrays;
import java.util.List;

public class TestData {
    static final EnlargerProfileEntity ENLARGER_ENTITY1 = new EnlargerProfileEntity(0,
            "Test 1", "Description 1", 10.0d,
            50.0d);
    static final EnlargerProfileEntity ENLARGER_ENTITY2 = new EnlargerProfileEntity(0,
            "Test 2", "Description 2", 10.0d,
            50.0d);
    static final EnlargerProfileEntity ENLARGER_ENTITY3 = new EnlargerProfileEntity(0,
            "Test 3", "Description 3",10.0d,
            50.0d,
            300.0d, 15.0d,
            600.0d, 30.0d);
    static final EnlargerProfileEntity ENLARGER_ENTITY4 = new EnlargerProfileEntity(0,
            "Test 4", "Description 4",12.0d,
            80.0d,
            320.0d, 16.0d,
            620.0d, 34.0d);

    static final List<EnlargerProfileEntity> ENLARGERS = Arrays.asList(
            ENLARGER_ENTITY1, ENLARGER_ENTITY2, ENLARGER_ENTITY3, ENLARGER_ENTITY4);
}
