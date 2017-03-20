package com.mahao.ioc_api;

/**
 * Created by mahao on 17-3-9.
 */

public interface Finder<T> {
    void inject(T host, Object source, Provider provider);
}
