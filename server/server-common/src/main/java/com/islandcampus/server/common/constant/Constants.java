package com.islandcampus.server.common.constant;

public class Constants {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    public static final long TOKEN_EXPIRE_MS = 2 * 60 * 60 * 1000L; // 2小时
    public static final long REFRESH_TOKEN_EXPIRE_DAYS = 7;
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final String REDIS_TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    public static final String REDIS_WS_SESSION_PREFIX = "ws:session:";
    public static final String REDIS_ISLAND_CONFIG_PREFIX = "island:config:";
    public static final String REDIS_WEATHER_CACHE_PREFIX = "weather:cache:";
    public static final String REDIS_EXAM_STATE_PREFIX = "exam:state:";
    public static final String REDIS_BROADCAST_CHANNEL = "island:broadcast";
    public static final String REDIS_CONFIG_UPDATE_CHANNEL = "island:config_update";
    public static final int LOGIN_MAX_RETRY = 5;
    public static final long LOGIN_LOCK_MINUTES = 15;

    private Constants() {}
}
