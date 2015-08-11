/*
 * Copyright (c) 2015. 2Lines Software,Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.twolinessoftware.smarterlist.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.twolinessoftware.smarterlist.R;

public class TextDrawable extends Drawable {

    private final String text;
    private final Paint paint;
    private final Paint backgroundPaint;

    public TextDrawable(Context context, String text) {

        this.text = text.toUpperCase();

        this.paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.pal_grey_2));
        paint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.letter_icon_view_text));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.LEFT);


        backgroundPaint = new Paint();
        backgroundPaint.setColor(context.getResources().getColor(R.color.pal_grey_5));
        backgroundPaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {


        final Rect bounds = getBounds();
        final int count = canvas.save();

            canvas.translate(bounds.left, bounds.top);
            canvas.drawCircle(bounds.centerX(),bounds.centerY(),bounds.height()/2, backgroundPaint);

            Rect textBounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawText(text, bounds.centerX()-textBounds.centerX(),bounds.centerY()-textBounds.centerY(), paint);


        canvas.restoreToCount(count);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
