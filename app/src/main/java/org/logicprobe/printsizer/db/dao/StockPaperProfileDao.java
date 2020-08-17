package org.logicprobe.printsizer.db.dao;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import org.logicprobe.printsizer.db.entity.PaperGradeEntity;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StockPaperProfileDao {
    private static final String TAG = StockPaperProfileDao.class.getSimpleName();
    private static final String PAPER_PROFILES_FILENAME = "paper_profiles.json";
    private Context context;

    public StockPaperProfileDao(final Context context) {
        this.context = context;
    }

    public List<PaperProfileEntity> loadAllPaperProfiles() {
        List<PaperProfileEntity> list = new ArrayList<>();

        try (
                InputStream in = context.getAssets().open(PAPER_PROFILES_FILENAME);
                JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8))
        ) {
            readAllPaperProfiles(reader, list);
        } catch (IOException e) {
            Log.e(TAG, "Unable to read stock data file: " + e.getMessage());
            e.printStackTrace();
        }

        Collections.sort(list, new Comparator<PaperProfileEntity>() {
            @Override
            public int compare(PaperProfileEntity e1, PaperProfileEntity e2) {
                int cmp = e1.getName().compareTo(e2.getName());
                if (cmp == 0) {
                    cmp = e1.getId() - e2.getId();
                }
                return cmp;
            }
        });

        return list;
    }

    private void readAllPaperProfiles(JsonReader reader, List<PaperProfileEntity> list) throws IOException {
        int profileId = 0;
        reader.beginArray();
        while (reader.hasNext()) {
            PaperProfileEntity entity = readPaperProfile(reader);
            entity.setId(++profileId);
            list.add(entity);
        }
        reader.endArray();
    }

    private PaperProfileEntity readPaperProfile(JsonReader reader) throws IOException {
        PaperProfileEntity entity = new PaperProfileEntity();
        
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                entity.setName(reader.nextString());
            } else if (name.equals("description")) {
                entity.setDescription(reader.nextString());
            } else if (name.equals("grades")) {
                readPaperGrades(reader, entity);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        fillEmptyPaperFields(entity);
        return entity;
    }

    private void readPaperGrades(JsonReader reader, PaperProfileEntity entity) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String gradeName = reader.nextName();
            PaperGradeEntity gradeEntity = new PaperGradeEntity();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("isoP")) {
                    gradeEntity.setIsoP(reader.nextInt());
                } else if (name.equals("isoR")) {
                    gradeEntity.setIsoR(reader.nextInt());
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
            if (gradeEntity.getIsoP() == 0) { continue; }
            if (gradeName.equals("00")) {
                entity.setGrade00(gradeEntity);
            } else if (gradeName.equals("0")) {
                entity.setGrade0(gradeEntity);
            } else if (gradeName.equals("1")) {
                entity.setGrade1(gradeEntity);
            } else if (gradeName.equals("2")) {
                entity.setGrade2(gradeEntity);
            } else if (gradeName.equals("3")) {
                entity.setGrade3(gradeEntity);
            } else if (gradeName.equals("4")) {
                entity.setGrade4(gradeEntity);
            } else if (gradeName.equals("5")) {
                entity.setGrade5(gradeEntity);
            } else if (gradeName.equalsIgnoreCase("none")) {
                entity.setGradeNone(gradeEntity);
            }
        }
        reader.endObject();
    }

    private void fillEmptyPaperFields(PaperProfileEntity entity) {
        if (entity.getName() == null) {
            entity.setName("");
        }
        if (entity.getDescription() == null) {
            entity.setDescription("");
        }
        if (entity.getGrade00() == null) {
            entity.setGrade00(new PaperGradeEntity());
        }
        if (entity.getGrade0() == null) {
            entity.setGrade0(new PaperGradeEntity());
        }
        if (entity.getGrade1() == null) {
            entity.setGrade1(new PaperGradeEntity());
        }
        if (entity.getGrade2() == null) {
            entity.setGrade2(new PaperGradeEntity());
        }
        if (entity.getGrade3() == null) {
            entity.setGrade3(new PaperGradeEntity());
        }
        if (entity.getGrade4() == null) {
            entity.setGrade4(new PaperGradeEntity());
        }
        if (entity.getGrade5() == null) {
            entity.setGrade5(new PaperGradeEntity());
        }
        if (entity.getGradeNone() == null) {
            entity.setGradeNone(new PaperGradeEntity());
        }
    }
}
