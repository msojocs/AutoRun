package org.runrun.utils.HTTP;


import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieIdentityComparator;
import org.apache.hc.client5.http.cookie.CookieStore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 自定义Cookie存储类
 * 让httpClient无法读取cookie，但开发者可以[达到禁用Cookie的目的]
 *
 */
public class CustomCookieStore implements CookieStore {
    private List<Cookie> emptyCookieStore = new ArrayList();
    private final TreeSet<Cookie> cookies = new TreeSet(new CookieIdentityComparator());
    private transient ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void addCookie(Cookie cookie) {
        if (cookie != null) {
            this.lock.writeLock().lock();

            try {
                this.cookies.remove(cookie);
                if (!cookie.isExpired(new Date())) {
                    this.cookies.add(cookie);
                }
            } finally {
                this.lock.writeLock().unlock();
            }
        }
    }

    public void addCookies(Cookie[] cookies) {
        if (cookies != null) {
            Cookie[] arr$ = cookies;
            int len$ = cookies.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                Cookie cookie = arr$[i$];
                this.addCookie(cookie);
            }
        }

    }

    @Override
    public List<Cookie> getCookies() {
        return emptyCookieStore;
    }


    /**
     * 自定义Cookie读取策略，读完清空 o(*￣▽￣*)ブ
     *
     * @return
     */
    public List<Cookie> getCookiesCustom() {
        this.lock.readLock().lock();

        ArrayList var1;
        try {
            var1 = new ArrayList(this.cookies);
        } finally {
            this.cookies.clear();
            this.lock.readLock().unlock();
        }

        return var1;
    }

    @Override
    public boolean clearExpired(Date date) {
        return true;
    }

    @Override
    public void clear() {

    }
}