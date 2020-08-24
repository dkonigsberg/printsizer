package org.logicprobe.printsizer.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import org.logicprobe.printsizer.model.PaperGrade;

import java.util.Objects;

public class PaperGradeEntity implements PaperGrade, Parcelable {
    @ColumnInfo(name = "iso_p")
    private int isoP;

    @ColumnInfo(name = "iso_r")
    private int isoR;

    @Override
    public int getIsoP() {
        return isoP;
    }

    public void setIsoP(int isoP) {
        this.isoP = isoP;
    }

    @Override
    public int getIsoR() {
        return isoR;
    }

    public void setIsoR(int isoR) {
        this.isoR = isoR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaperGradeEntity that = (PaperGradeEntity) o;
        return isoP == that.isoP &&
                isoR == that.isoR;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isoP, isoR);
    }

    public PaperGradeEntity() {
    }

    @Ignore
    public static final Parcelable.Creator<PaperGradeEntity> CREATOR = new Parcelable.Creator<PaperGradeEntity>() {
        @Override
        public PaperGradeEntity createFromParcel(Parcel in) {
            PaperGradeEntity gradeEntity = new PaperGradeEntity();
            gradeEntity.isoP = in.readInt();
            gradeEntity.isoR = in.readInt();
            return gradeEntity;
        }

        @Override
        public PaperGradeEntity[] newArray(int size) {
            return new PaperGradeEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(isoP);
        out.writeInt(isoR);
    }

    @Ignore
    public PaperGradeEntity(int isoP,
                            int isoR) {
        this.isoP = isoP;
        this.isoR = isoR;
    }

    public PaperGradeEntity(PaperGrade paperGrade) {
        this.isoP = paperGrade.getIsoP();
        this.isoR = paperGrade.getIsoR();
    }
}
