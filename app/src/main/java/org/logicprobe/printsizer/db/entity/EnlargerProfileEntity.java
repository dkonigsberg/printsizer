package org.logicprobe.printsizer.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.logicprobe.printsizer.model.EnlargerProfile;

@Entity(tableName = EnlargerProfileEntity.TABLE_NAME)
public class EnlargerProfileEntity implements EnlargerProfile {
    public static final String TABLE_NAME = "enlarger_profiles";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "has_test_exposures")
    private boolean hasTestExposures;

    @ColumnInfo(name = "height_measurement_offset")
    private double heightMeasurementOffset;

    @ColumnInfo(name = "lens_focal_length")
    private double lensFocalLength;

    @ColumnInfo(name = "smaller_test_distance")
    private double smallerTestDistance;

    @ColumnInfo(name = "smaller_test_time")
    private double smallerTestTime;

    @ColumnInfo(name = "larger_test_distance")
    private double largerTestDistance;

    @ColumnInfo(name = "larger_test_time")
    private double largerTestTime;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public double getHeightMeasurementOffset() {
        return heightMeasurementOffset;
    }

    public void setHeightMeasurementOffset(double heightMeasurementOffset) {
        this.heightMeasurementOffset = heightMeasurementOffset;
    }

    @Override
    public double getLensFocalLength() {
        return lensFocalLength;
    }

    public void setLensFocalLength(double lensFocalLength) {
        this.lensFocalLength = lensFocalLength;
    }

    @Override
    public boolean hasTestExposures() {
        return hasTestExposures;
    }

    public void setHasTestExposures(boolean hasTestExposures) {
        this.hasTestExposures = hasTestExposures;
    }

    @Override
    public double getSmallerTestDistance() {
        return smallerTestDistance;
    }

    public void setSmallerTestDistance(double smallerTestDistance) {
        this.smallerTestDistance = smallerTestDistance;
    }

    @Override
    public double getSmallerTestTime() {
        return smallerTestTime;
    }

    public void setSmallerTestTime(double smallerTestTime) {
        this.smallerTestTime = smallerTestTime;
    }

    @Override
    public double getLargerTestDistance() {
        return largerTestDistance;
    }

    public void setLargerTestDistance(double largerTestDistance) {
        this.largerTestDistance = largerTestDistance;
    }

    @Override
    public double getLargerTestTime() {
        return largerTestTime;
    }

    public void setLargerTestTime(double largerTestTime) {
        this.largerTestTime = largerTestTime;
    }

    public EnlargerProfileEntity() {
    }

    @Ignore
    public EnlargerProfileEntity(int id,
                                 String name,
                                 String description,
                                 double heightMeasurementOffset,
                                 double lensFocalLength) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.heightMeasurementOffset = heightMeasurementOffset;
        this.lensFocalLength = lensFocalLength;
        this.hasTestExposures = false;
    }

    @Ignore
    public EnlargerProfileEntity(int id,
                                 String name,
                                 String description,
                                 double heightMeasurementOffset,
                                 double lensFocalLength,
                                 double smallerTestDistance,
                                 double smallerTestTime,
                                 double largerTestDistance,
                                 double largerTestTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.heightMeasurementOffset = heightMeasurementOffset;
        this.lensFocalLength = lensFocalLength;
        this.hasTestExposures = true;
        this.smallerTestDistance = smallerTestDistance;
        this.smallerTestTime = smallerTestTime;
        this.largerTestDistance = largerTestDistance;
        this.largerTestTime = largerTestTime;
    }

    public EnlargerProfileEntity(EnlargerProfile enlargerProfile) {
        this.id = enlargerProfile.getId();
        this.name = enlargerProfile.getName();
        this.description = enlargerProfile.getDescription();
        this.heightMeasurementOffset = enlargerProfile.getHeightMeasurementOffset();
        this.lensFocalLength = enlargerProfile.getLensFocalLength();
        this.hasTestExposures = enlargerProfile.hasTestExposures();
        if (enlargerProfile.hasTestExposures()) {
            this.smallerTestDistance = enlargerProfile.getSmallerTestDistance();
            this.smallerTestTime = enlargerProfile.getSmallerTestTime();
            this.largerTestDistance = enlargerProfile.getLargerTestDistance();
            this.largerTestTime = enlargerProfile.getLargerTestTime();
        }
    }
}
