package com.mahao.ioc_api;

import android.content.Context;
import android.view.View;

/**
 * Created by mahao on 17-3-9.
 */

public class ViewProvider implements Provider {

    @Override
    public View findView(Object source, int id) {
        return  ((View) source).findViewById(id);
    }
}
