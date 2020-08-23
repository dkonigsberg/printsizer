package org.logicprobe.printsizer.db.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.logicprobe.printsizer.model.PaperGrade;
import org.logicprobe.printsizer.model.PaperProfile;

import java.util.Objects;

@Entity(tableName = PaperProfileEntity.TABLE_NAME)
public class PaperProfileEntity implements PaperProfile, Parcelable {
    public static final String TABLE_NAME = "paper_profiles";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @Embedded(prefix = "00_")
    private PaperGradeEntity grade00;

    @Embedded(prefix = "0_")
    private PaperGradeEntity grade0;

    @Embedded(prefix = "1_")
    private PaperGradeEntity grade1;

    @Embedded(prefix = "2_")
    private PaperGradeEntity grade2;

    @Embedded(prefix = "3_")
    private PaperGradeEntity grade3;

    @Embedded(prefix = "4_")
    private PaperGradeEntity grade4;

    @Embedded(prefix = "5_")
    private PaperGradeEntity grade5;

    @Embedded(prefix = "none_")
    private PaperGradeEntity gradeNone;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public PaperGradeEntity getGrade00() {
        return grade00;
    }

    public void setGrade00(PaperGradeEntity grade00) {
        this.grade00 = grade00;
    }

    @Override
    public PaperGradeEntity getGrade0() {
        return grade0;
    }

    public void setGrade0(PaperGradeEntity grade0) {
        this.grade0 = grade0;
    }

    @Override
    public PaperGradeEntity getGrade1() {
        return grade1;
    }

    public void setGrade1(PaperGradeEntity grade1) {
        this.grade1 = grade1;
    }

    @Override
    public PaperGradeEntity getGrade2() {
        return grade2;
    }

    public void setGrade2(PaperGradeEntity grade2) {
        this.grade2 = grade2;
    }

    @Override
    public PaperGradeEntity getGrade3() {
        return grade3;
    }

    public void setGrade3(PaperGradeEntity grade3) {
        this.grade3 = grade3;
    }

    @Override
    public PaperGradeEntity getGrade4() {
        return grade4;
    }

    public void setGrade4(PaperGradeEntity grade4) {
        this.grade4 = grade4;
    }

    @Override
    public PaperGradeEntity getGrade5() {
        return grade5;
    }

    public void setGrade5(PaperGradeEntity grade5) {
        this.grade5 = grade5;
    }

    @Override
    public PaperGradeEntity getGradeNone() {
        return gradeNone;
    }

    public void setGradeNone(PaperGradeEntity gradeNone) {
        this.gradeNone = gradeNone;
    }

    @Ignore
    @Override
    public PaperGrade getGrade(int gradeId) {
        PaperGrade grade;
        switch (gradeId) {
            case GRADE_00:
                grade = this.grade00;
                break;
            case GRADE_0:
                grade = this.grade0;
                break;
            case GRADE_1:
                grade = this.grade1;
                break;
            case GRADE_2:
                grade = this.grade2;
                break;
            case GRADE_3:
                grade = this.grade3;
                break;
            case GRADE_4:
                grade = this.grade4;
                break;
            case GRADE_5:
                grade = this.grade5;
                break;
            case GRADE_NONE:
                grade = this.gradeNone;
                break;
            default:
                grade = null;
        }
        return grade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaperProfileEntity that = (PaperProfileEntity) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(grade00, that.grade00) &&
                Objects.equals(grade0, that.grade0) &&
                Objects.equals(grade1, that.grade1) &&
                Objects.equals(grade2, that.grade2) &&
                Objects.equals(grade3, that.grade3) &&
                Objects.equals(grade4, that.grade4) &&
                Objects.equals(grade5, that.grade5) &&
                Objects.equals(gradeNone, that.gradeNone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, grade00, grade0, grade1, grade2, grade3, grade4, grade5, gradeNone);
    }

    public PaperProfileEntity() {
    }

    @Ignore
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public PaperProfileEntity createFromParcel(Parcel in) {
            PaperProfileEntity profileEntity = new PaperProfileEntity();
            profileEntity.id = in.readInt();
            profileEntity.name = in.readString();
            profileEntity.description = in.readString();
            profileEntity.grade00 = in.readParcelable(PaperGradeEntity.class.getClassLoader());
            profileEntity.grade0 = in.readParcelable(PaperGradeEntity.class.getClassLoader());
            profileEntity.grade1 = in.readParcelable(PaperGradeEntity.class.getClassLoader());
            profileEntity.grade2 = in.readParcelable(PaperGradeEntity.class.getClassLoader());
            profileEntity.grade3 = in.readParcelable(PaperGradeEntity.class.getClassLoader());
            profileEntity.grade4 = in.readParcelable(PaperGradeEntity.class.getClassLoader());
            profileEntity.grade5 = in.readParcelable(PaperGradeEntity.class.getClassLoader());
            profileEntity.gradeNone = in.readParcelable(PaperGradeEntity.class.getClassLoader());
            return profileEntity;
        }

        @Override
        public PaperProfileEntity[] newArray(int size) {
            return new PaperProfileEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(name);
        out.writeString(description);
        out.writeParcelable(grade00, flags);
        out.writeParcelable(grade0, flags);
        out.writeParcelable(grade1, flags);
        out.writeParcelable(grade2, flags);
        out.writeParcelable(grade3, flags);
        out.writeParcelable(grade4, flags);
        out.writeParcelable(grade5, flags);
        out.writeParcelable(gradeNone, flags);
    }

    @Ignore
    public PaperProfileEntity(int id,
                              String name,
                              String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Ignore
    public PaperProfileEntity(int id,
                              String name,
                              String description,
                              PaperGradeEntity grade00,
                              PaperGradeEntity grade0,
                              PaperGradeEntity grade1,
                              PaperGradeEntity grade2,
                              PaperGradeEntity grade3,
                              PaperGradeEntity grade4,
                              PaperGradeEntity grade5,
                              PaperGradeEntity gradeNone) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.grade00 = grade00;
        this.grade0 = grade0;
        this.grade1 = grade1;
        this.grade2 = grade2;
        this.grade3 = grade3;
        this.grade4 = grade4;
        this.grade5 = grade5;
        this.gradeNone = gradeNone;
    }

    public PaperProfileEntity(PaperProfile paperProfile) {
        this.id = paperProfile.getId();
        this.name = paperProfile.getName();
        this.description = paperProfile.getDescription();
        this.grade00 = new PaperGradeEntity(paperProfile.getGrade00());
        this.grade0 = new PaperGradeEntity(paperProfile.getGrade0());
        this.grade1 = new PaperGradeEntity(paperProfile.getGrade1());
        this.grade2 = new PaperGradeEntity(paperProfile.getGrade2());
        this.grade3 = new PaperGradeEntity(paperProfile.getGrade3());
        this.grade4 = new PaperGradeEntity(paperProfile.getGrade4());
        this.grade5 = new PaperGradeEntity(paperProfile.getGrade5());
        this.gradeNone = new PaperGradeEntity(paperProfile.getGradeNone());
    }
}
