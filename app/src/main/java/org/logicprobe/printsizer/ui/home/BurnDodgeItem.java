package org.logicprobe.printsizer.ui.home;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.model.ExposureAdjustment;
import org.logicprobe.printsizer.ui.Converter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

public class BurnDodgeItem implements Parcelable {
    private long itemId;
    private int index;
    private String name;
    private ExposureAdjustment adjustment;

    public BurnDodgeItem() {
        itemId = -1;
        adjustment = new ExposureAdjustment();
    }

    public BurnDodgeItem(BurnDodgeItem item) {
        if (item != null) {
            this.itemId = item.itemId;
            this.index = item.index;
            this.name = item.name;
            this.adjustment = item.adjustment;
        } else {
            this.itemId = -1;
            this.adjustment = new ExposureAdjustment();
        }
    }

    public static final Parcelable.Creator<BurnDodgeItem> CREATOR = new Parcelable.Creator<BurnDodgeItem>() {
        @Override
        public BurnDodgeItem createFromParcel(Parcel in) {
            BurnDodgeItem item = new BurnDodgeItem();
            item.itemId = in.readLong();
            item.index = in.readInt();
            item.name = in.readString();
            item.adjustment = in.readParcelable(ExposureAdjustment.class.getClassLoader());
            return item;
        }

        @Override
        public BurnDodgeItem[] newArray(int size) {
            return new BurnDodgeItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(itemId);
        out.writeInt(index);
        out.writeString(name);
        out.writeParcelable(adjustment, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BurnDodgeItem that = (BurnDodgeItem) o;
        return itemId == that.itemId &&
                index == that.index &&
                Objects.equals(name, that.name) &&
                Objects.equals(adjustment, that.adjustment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, index, name, adjustment);
    }

    public String getDefaultName(Resources res) {
        return getDefaultName(res, index);
    }

    public static String getDefaultName(Resources res, int index) {
        String indexLabel = Integer.toString(index + 1);
        return res.getString(R.string.burndodge_area_default_name, indexLabel);
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getItemId() {
        return itemId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName(Resources res) {
        if (name != null && name.length() > 0) {
            return name;
        } else {
            return getDefaultName(res);
        }
    }

    public ExposureAdjustment getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(ExposureAdjustment adjustment) {
        if (adjustment != null) {
            this.adjustment = adjustment;
        } else {
            this.adjustment = new ExposureAdjustment();
        }
    }

    public String getValueText() {
        String text;
        if (adjustment.getUnit() == ExposureAdjustment.UNIT_SECONDS) {
            text = Converter.burnDodgeSecondsDisplayToString(adjustment.getSecondsValue());
        } else if (adjustment.getUnit() == ExposureAdjustment.UNIT_PERCENT) {
            NumberFormat f = NumberFormat.getInstance();
            f.setMinimumFractionDigits(0);
            f.setGroupingUsed(false);
            if (f instanceof DecimalFormat) {
                ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(false);
            }
            int value = adjustment.getPercentValue();
            if (value > 0) {
                text = '+' + f.format(value);
            } else {
                text = f.format(value);
            }
        } else if (adjustment.getUnit() == ExposureAdjustment.UNIT_STOPS) {
            text = Converter.stopsValueToString(adjustment.getStopsValue());
        } else {
            text = "";
        }
        return text;
    }

    public int getSuffixTextId() {
        if (adjustment.getUnit() == ExposureAdjustment.UNIT_SECONDS) {
            return R.string.unit_suffix_seconds;
        } else {
            return R.string.empty;
        }
    }

    public int getEndIconDrawableId() {
        if (adjustment.getUnit() == ExposureAdjustment.UNIT_SECONDS) {
            return R.drawable.ic_shutter_speed;
        } else if (adjustment.getUnit() == ExposureAdjustment.UNIT_PERCENT) {
            return R.drawable.ic_percent;
        } else if (adjustment.getUnit() == ExposureAdjustment.UNIT_STOPS) {
            return R.drawable.ic_exposure;
        } else {
            return R.drawable.ic_adjust;
        }
    }
}
