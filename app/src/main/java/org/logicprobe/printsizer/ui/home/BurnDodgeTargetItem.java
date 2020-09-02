package org.logicprobe.printsizer.ui.home;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import org.logicprobe.printsizer.ui.Converter;

import java.util.Objects;

public class BurnDodgeTargetItem implements Parcelable {
    private long itemId;
    private int index;
    private String name;
    private double secondsValue;

    public BurnDodgeTargetItem() {
        itemId = -1;
    }

    public BurnDodgeTargetItem(BurnDodgeTargetItem item) {
        if (item != null) {
            this.itemId = item.itemId;
            this.index = item.index;
            this.name = item.name;
            this.secondsValue = item.secondsValue;
        } else {
            this.itemId = -1;
        }
    }

    public static final Parcelable.Creator<BurnDodgeTargetItem> CREATOR = new Parcelable.Creator<BurnDodgeTargetItem>() {
        @Override
        public BurnDodgeTargetItem createFromParcel(Parcel in) {
            BurnDodgeTargetItem item = new BurnDodgeTargetItem();
            item.itemId = in.readLong();
            item.index = in.readInt();
            item.name = in.readString();
            item.secondsValue = in.readDouble();
            return item;
        }

        @Override
        public BurnDodgeTargetItem[] newArray(int size) {
            return new BurnDodgeTargetItem[size];
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
        out.writeDouble(secondsValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BurnDodgeTargetItem that = (BurnDodgeTargetItem) o;
        return itemId == that.itemId &&
                index == that.index &&
                Double.compare(that.secondsValue, secondsValue) == 0 &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, index, name, secondsValue);
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
            return BurnDodgeItem.getDefaultName(res, index);
        }
    }

    public double getSecondsValue() {
        return secondsValue;
    }

    public void setSecondsValue(double secondsValue) {
        this.secondsValue = secondsValue;
    }
}
