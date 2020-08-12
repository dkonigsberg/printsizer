package org.logicprobe.printsizer.model;

public interface PaperProfile {
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
}
