package com.lunarbase24.lookintothesky;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Wada on 2016/08/17.
 */
public class SeekBarPreference extends DialogPreference {

    /**
     * The default value of the seek bar.
     */
    protected static final int DEFAULT_VALUE = 50;

    /**
     * The default minimum value of the seek bar.
     */
    protected static final int DEFAULT_MIN_VALUE = 0;

    /**
     * The default maximum value of the seek bar.
     */
    protected static final int DEFAULT_MAX_VALUE = 100;

    /**
     * The default, which are shown depending on the currently persisted value.
     */
    protected static final String[] DEFAULT_SUMMARIES = null;

    /**
     * The currently persisted value.
     */
    private int value;

    /**
     * The current value of the seek bar.
     */
    private int seekBarValue;

    /**
     * The maximum value of the seek bar.
     */
    private int minValue;

    /**
     * The minimum value of the seek bar.
     */
    private int maxValue;

    /**
     * The default value of the seek bar.
     */
    private int defaultValue;

    /**
     * A string array, which contains the summaries, which should be shown
     * depending on the currently persisted value.
     */
    private String[] summaries;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        obtainStyledAttributes(context, attrs);
        setValue(getPersistedInt(defaultValue));

        setDialogLayoutResource(R.layout.settings_seekbarpreference_layout);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    private void obtainStyledAttributes(final Context context, final AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet,
                R.styleable.com_lunarbase24_lookintothesky_SeekBarPreference);
        try {
            obtainMaxValue(typedArray);
            obtainMinValue(typedArray);
            obtainDefaultValue(typedArray);
            obtainSummaries(typedArray);
        } finally {
            typedArray.recycle();
        }
    }

    private void obtainMinValue(final TypedArray typedArray) {
        setMinValue(typedArray.getInteger(R.styleable.com_lunarbase24_lookintothesky_SeekBarPreference_minValue, DEFAULT_MIN_VALUE));
    }

    private void obtainMaxValue(final TypedArray typedArray) {
        setMaxValue(typedArray.getInteger(R.styleable.com_lunarbase24_lookintothesky_SeekBarPreference_maxValue, DEFAULT_MAX_VALUE));
    }

    private void obtainDefaultValue(final TypedArray typedArray) {
        defaultValue = typedArray.getInt(R.styleable.com_lunarbase24_lookintothesky_SeekBarPreference_android_defaultValue, DEFAULT_VALUE);
    }

    private void obtainSummaries(final TypedArray typedArray) {
        try {
            CharSequence[] charSequences = typedArray.getTextArray(R.styleable.com_lunarbase24_lookintothesky_SeekBarPreference_android_summary);

            String[] obtainedSummaries = new String[charSequences.length];
            for (int i = 0; i < charSequences.length; i++) {
                obtainedSummaries[i] = charSequences[i].toString();
            }
            setSummaries(obtainedSummaries);
        } catch (NullPointerException e) {
            setSummaries(DEFAULT_SUMMARIES);
        }
    }

    public final int getValue() {
        return value;
    }

    public final int getRange() {
        return maxValue - minValue;
    }

    public final int getMinValue() {
        return minValue;
    }

    public final int getMaxValue() {
        return maxValue;
    }

    private String getProgressText() {
        return String.format("%d", seekBarValue);
    }

    protected final int getSeekBarValue() {
        return seekBarValue;
    }

    public final String[] getSummaries() {
        return summaries;
    }

    public final void setValue(final int value) {
        if (this.value != value) {
            this.value = value;
            this.seekBarValue = value;
            persistInt(value);
            notifyChanged();
        }
    }

    public final void setMinValue(final int minValue) {
        this.minValue = minValue;
        setValue(Math.max(getValue(), minValue));
    }

    public final void setMaxValue(final int maxValue) {
        this.maxValue = maxValue;
        setValue(Math.min(getValue(), maxValue));
    }

    public final void setSummaries(final String[] summaries) {
        this.summaries = summaries;
    }

    @Override
    public final CharSequence getSummary() {
        return String.format("%s:%s", super.getSummary(), getProgressText());
    }

    @Override
    public final void setSummary(final CharSequence summary) {
        super.setSummary(summary);
        this.summaries = null;
    }

    @Override
    public final void setSummary(final int summaryResId) {
        try {
            setSummaries(getContext().getResources().getStringArray(summaryResId));
        } catch (Exception e) {
            super.setSummary(summaryResId);
        }
    }

    @Override
    protected final Object onGetDefaultValue(final TypedArray a, final int index) {
        return a.getInt(index, DEFAULT_VALUE);
    }

    @Override
    protected final void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
        if (restoreValue) {
            setValue(getPersistedInt(DEFAULT_VALUE));
        } else {
            setValue((int)defaultValue);
        }
    }

    @Override
    protected View onCreateDialogView() {
        View dialogView = super.onCreateDialogView();

        TextView progressTextView = (TextView) dialogView.findViewById(R.id.current_value);
        progressTextView.setText(getProgressText());

        TextView minView = (TextView) dialogView.findViewById(R.id.min_value);
        minView.setText(String.format("%d", minValue));
        TextView maxView = (TextView) dialogView.findViewById(R.id.max_value);
        maxView.setText(String.format("%d", maxValue));

        SeekBar seekBar = (SeekBar) dialogView.findViewById(R.id.seek_bar);
        seekBar.setMax(getRange());
        seekBar.setProgress(Math.round((getValue() - getMinValue())));
        seekBar.setOnSeekBarChangeListener(getSeekBarListener(progressTextView));

        return dialogView;
    }

    private SeekBar.OnSeekBarChangeListener getSeekBarListener(
            final TextView progressTextView) {
        return new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                return;
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
                return;
            }

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                seekBarValue = getMinValue() + progress;
                progressTextView.setText(getProgressText());
            }
        };
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && callChangeListener(seekBarValue)) {
            setValue(seekBarValue);
        } else {
            seekBarValue = getValue();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.value);
    }

    private static class SavedState extends BaseSavedState {
        int value;

        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

}
