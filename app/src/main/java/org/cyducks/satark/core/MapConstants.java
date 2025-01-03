package org.cyducks.satark.core;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

//    private static final class MapConstants {
//        static final LatLngBounds NAGPUR_BOUNDS = new LatLngBounds(
//                new LatLng(21.076, 78.926),
//                new LatLng(21.214, 79.186)
//        );
////        static final LatLngBounds NAGPUR_BOUNDS = new LatLngBounds(
////                new LatLng(18.4139, 73.7399), // Southwest corner (approx.)
////                new LatLng(18.6340, 73.9330)  // Northeast corner (approx.)
////        );
//
////        static final LatLng INITIAL_CENTER = new LatLng(18.5316, 73.8367);
//        static final LatLng INITIAL_CENTER = new LatLng(21.1535, 79.0548);
//        static final float INITIAL_ZOOM = 15f;
//        static final float MIN_ZOOM = 15f;
//    }
public final class MapConstants {
    public static final LatLngBounds NAGPUR_BOUNDS = new LatLngBounds(
            new LatLng(21.076, 78.926),
            new LatLng(21.214, 79.186)
    );

    public static final LatLngBounds PUNE_BOUNDS = new LatLngBounds(
            new LatLng(18.4139, 73.7399), // Southwest corner (approx.)
            new LatLng(18.6340, 73.9330)  // Northeast corner (approx.)
    );

    public static final LatLngBounds DELHI_BOUNDS = new LatLngBounds(
            new LatLng(28.4040, 76.8394), // Southwest corner (approx.)
            new LatLng(28.8810, 77.3210)  // Northeast corner (approx.)
    );

    public static final LatLngBounds MUMBAI_BOUNDS = new LatLngBounds(
            new LatLng(18.8923, 72.7758),
            new LatLng(19.3338, 73.0078)
    );

    public static final LatLng NAGPUR_CENTER = new LatLng(21.14158, 79.0882);
    public static final LatLng DELHI_CENTER = new LatLng(28.565956952916466, 77.17951713856367);
    public static final LatLng PUNE_CENTER = new LatLng(18.52140645425171, 73.82947305649449);
    public static final LatLng MUMBAI_CENTER = new LatLng(18.942030117706327, 72.82985195041178);
    public static final float INITIAL_ZOOM = 15f;
    public static final float MIN_ZOOM = 13f;
}
