package com.android.chavah.effects;

import android.view.View;

import com.android.chavah.CellLayout;
import com.android.chavah.PagedView;

/**
 * Created by lcg on 16-5-3.
 */
public class Cross extends BaseEffectAnimation {

    public Cross(PagedView pagedView) {
        super(Effect.CROSS, pagedView);
    }

    @Override
    public void screenScrolled(View v, float progress) {
        float rotation = 90.0f * progress;
        float alpha = 0.2f + (1 - Math.abs(progress)) * 0.8f;

        v.setTranslationX(v.getMeasuredWidth() * progress);
        v.setPivotX(0.5f * v.getMeasuredWidth());
        v.setPivotY(0.5f * v.getMeasuredHeight());
        v.setRotationY(-rotation);

        if (v instanceof CellLayout) {
            ((CellLayout) v).getShortcutsAndWidgets().setAlpha(alpha);
        } else {
            v.setAlpha(alpha);
        }
    }
}
