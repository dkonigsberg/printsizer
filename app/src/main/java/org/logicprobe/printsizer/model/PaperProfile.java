package org.logicprobe.printsizer.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface PaperProfile {
    int GRADE_00 = -1;
    int GRADE_0 = 0;
    int GRADE_1 = 1;
    int GRADE_2 = 2;
    int GRADE_3 = 3;
    int GRADE_4 = 4;
    int GRADE_5 = 5;
    int GRADE_NONE = -2;

    @IntDef({GRADE_00, GRADE_0, GRADE_1, GRADE_2, GRADE_3, GRADE_4, GRADE_5, GRADE_NONE})
    @Retention(RetentionPolicy.SOURCE)
    @interface GradeId {}

    int getId();
    String getName();
    String getDescription();
    PaperGrade getGrade00();
    PaperGrade getGrade0();
    PaperGrade getGrade1();
    PaperGrade getGrade2();
    PaperGrade getGrade3();
    PaperGrade getGrade4();
    PaperGrade getGrade5();
    PaperGrade getGradeNone();

    PaperGrade getGrade(@GradeId int gradeId);
}
