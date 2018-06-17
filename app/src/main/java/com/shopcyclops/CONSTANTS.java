package com.shopcyclops;

/**
 * Created by Andrew on 7/14/2015.
 */
public class CONSTANTS {
    public static final String BASE_URL = "http://4daf86f3.ngrok.com";

    public static final String KICKFLIP_CLIENT_KEY = "B_wi=z4n8GnqsfE4CP!pMpFa19hvCVVD_0cpOAxD";
    public static final String KICKFLIP_CLIENT_SECRET = "evUnof;OX_7=1Kwv7s;2LPu=m:RPiu6rM!V5n3WHi2sj2kR-82.lzP=Wn1SmN4P1e!ec:6.baJKJwgqbTEiwUNgmn4M:Uj6!q9ixLN7BbQrdGpD483FqO4Oy6@o.@sJf";

    public static final String TOKEN_KEY = "TOKEN";
    public static final String EMAIL_KEY = "USER_EMAIL";
    public static final String IS_ADMIN_KEY = "IS_ADMIN";
    public static final String IS_CYCLOPS_KEY = "IS_CYCLOPS";
    public static final String USER_ID_KEY = "USER_ID";
    public static final String FIRST_NAME_KEY = "FIRST_NAME";
    public static final String LAST_NAME_KEY = "LAST_NAME";
    public static final String DISPLAY_NAME_KEY = "DISPLAY_NAME";
    public static final String PHONE_NUMBER_KEY = "PHONE_NUMBER";
    public static final String SHARED_PREFS_KEY = "com.shopcyclops";
    public static final String CREDIT_CHECK = "credit check";
    public static final String INTENT_CREDIT_CHECK = "intent credit check";
    public static final String STREAM_PROGRESS = "stream_progress";
    public static final String ALL_STREAMS = "all_streams";
    public static final String CURRENT_DELIVERY_LAT = "current delivery lat";
    public static final String CURRENT_DELIVERY_LNG = "current delivery lng";
    public static final String CURRENT_CARDCODE = "current credit code";
    public static final String IS_COMPLETE_STREAM = "is the stream complete?";

    public static final String STRIPE_CLIENT_ID = "ca_5qamk5Dt98nk4LcYpzrgDgYk6ARmn4rw";
    public static final String STRIPE_SECRET_KEY = "sk_test_FQN03AnepMDNcZaFFduIsVJS";
    public static final String STRIPE_PUBLIC_KEY = "pk_test_7CMIjTOkDl8S6fkOImYJ8Qz5";
    public static final String STRIPE_CALLBACK_URL = BASE_URL+"/users/auth/stripe_connect/callback";
    public static final String STRIPE_TOKEN_KEY = "stripe_token_key";

    public static final String PUSHER_KEY = "efb3250cf7fd30c78d49";
    public static final String PUSHER_SECRET = "2d9da406190b5c9dd3ed";
    public static final String PUSHER_APP_ID = "133847";
    public static final String PUSHER_AUTH_ENDPOINT = BASE_URL+"/auth";

    public static final int RED_PORT = 8554;
    public static final String RED_HOST= "52.26.15.217";
    public static final String RED_SECONDSCREEN_PORT = "secondscreen_port";
    public static final String RED_APP_NAME = "live";
    public static final String RED_SECONDSCREEN_APP_NAME = "secondscreen_app_name";
    public static final int RED_BITRATE = 128;
    public static final boolean RED_AUDIO = true;
    public static final boolean RED_VIDEO = true;

    public static final String CURRENT_STREAM_ID = "current_stream";
    public static final String CURRENT_STREAM_TITLE = "stream_title";
    public static final String CURRENT_STREAM_DESCRIPTION = "stream_description";
    public static final String CURRENT_STREAM_STORE = "stream_store";
    public static final String CURRENT_STREAM_HOME_POINT_LAT = "latitude of the final waypoint";
    public static final String CURRENT_STREAM_HOME_POINT_LNG = "longitude of the final waypoint";

    public static final String AWS_ACCESS_KEY = "AKIAJIYKCLZ3HC5MVDNQ";
    public static final String AWS_SECRET_KEY = "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";
    public static final String AWS_BUCKET_NAME = "shopcyclops";
    public static final String AWS_IMAGE_URL = "https://s3-us-west-2.amazonaws.com/shopcyclops/";

    public static final String GOOGLE_MAPS_KEY = "AIzaSyA8P6lCZ8eRjE1Hilp94HhHyHTiG6Kk-k4";
}
