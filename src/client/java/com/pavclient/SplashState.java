package com.pavclient;

/**
 * Splash overlay durumunu tutan yardimci sinif.
 * Mixin icerisinde public static field kullanilamadigi icin
 * bu sinif uzerinden state paylasimi yapilir.
 */
public final class SplashState {

    private SplashState() {}

    /** Splash overlay hala aktif mi? */
    public static volatile boolean splashActive = true;
}
