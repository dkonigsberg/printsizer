package org.logicprobe.printsizer.model;

public interface EnlargerProfile {
    int getId();
    String getName();
    String getDescription();

    double getHeightMeasurementOffset();

    double getLensFocalLength();

    double getSmallerTestDistance();
    double getSmallerTestTime();
    double getLargerTestDistance();
    double getLargerTestTime();
}
