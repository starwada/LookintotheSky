package com.lunarbase24.lookintothesky;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Wada on 2016/09/29.
 * 指定色のビュー シェイプは円
 */

public class ColorView extends View {

    private RectF mOvalRect;
    private Paint mColorPaint;
    private int mColor;

    public ColorView(Context context){
        super(context);
        init();
    }

    public ColorView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public ColorView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mOvalRect = new RectF();
        mColorPaint = new Paint();
        mColor = 0;
    }

    public void setColor(int nColor){
        mColor = nColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        mOvalRect.set( (float)paddingLeft, (float)paddingTop, (float)(getWidth()-paddingRight), (float)(getHeight()-paddingBottom));
        mColorPaint.setColor(mColor);

        canvas.drawOval(mOvalRect, mColorPaint);
    }
}
