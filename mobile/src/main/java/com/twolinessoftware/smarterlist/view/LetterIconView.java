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
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.twolinessoftware.smarterlist.R;

/**
 * @todo probably add attributes one day
 */
public class LetterIconView extends ImageView {

    private char m_letter = ' ';

    private int m_backgroundColor;
    private int m_textColor;
    private Paint m_paint;
    private Paint m_paintText;

    public LetterIconView(Context context) {
        super(context);
        init();
    }

    public LetterIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LetterIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        m_backgroundColor = getResources().getColor(R.color.pal_grey_5);
        m_textColor = getResources().getColor(R.color.pal_white);

        m_paint = new Paint();

        m_paintText = new Paint();
    }

    public void setLetter(char letter){
        this.m_letter = letter;
        invalidate();
    }

    public void setTextColor(int color){
        this.m_textColor = color;
    }

    public void setBackgroundColor(int color){
        this.m_backgroundColor = color;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {



        m_paint.setAntiAlias(true);
        m_paint.setColor(m_backgroundColor);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2,getWidth()/2, m_paint);

        m_paintText.setTextSize(getResources().getDimensionPixelSize(R.dimen.letter_icon_view_text));
        m_paintText.setColor(m_textColor);
        canvas.drawText(String.valueOf(m_letter),getWidth()/2,getHeight()/2,m_paintText);


    }
}
