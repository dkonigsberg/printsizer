package org.logicprobe.printsizer.db;

import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;
import org.logicprobe.printsizer.db.entity.PaperGradeEntity;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

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

    static final PaperProfileEntity PAPER_ENTITY1 = new PaperProfileEntity(0,
            "Test 1", "Description 1",
            new PaperGradeEntity(200, 120), new PaperGradeEntity(210, 110),
            new PaperGradeEntity(220, 100), new PaperGradeEntity(230, 90),
            new PaperGradeEntity(240, 80), new PaperGradeEntity(250, 70),
            new PaperGradeEntity(260, 60), new PaperGradeEntity(500, 105));
    static final PaperProfileEntity PAPER_ENTITY2 = new PaperProfileEntity(0,
            "Test 2", "Description 2",
            new PaperGradeEntity(240, 160), new PaperGradeEntity(240, 130),
            new PaperGradeEntity(240, 110), new PaperGradeEntity(240, 90),
            new PaperGradeEntity(240, 70), new PaperGradeEntity(220, 60),
            new PaperGradeEntity(220, 50), new PaperGradeEntity(500, 90));

    static final List<PaperProfileEntity> PAPERS = Arrays.asList(
            PAPER_ENTITY1, PAPER_ENTITY2);
}
