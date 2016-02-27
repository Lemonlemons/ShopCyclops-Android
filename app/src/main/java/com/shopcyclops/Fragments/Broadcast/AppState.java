package com.shopcyclops.Fragments.Broadcast;

public enum AppState {
    PUBLISH(0),
    SUBSCRIBE(1),
    SECONDSCREEN(2);

    final int value;

    private AppState(int num) {
        this.value = num;
    }

    public int getValue() {
        return this.value;
    }

    public static AppState fromValue(int value) {
        for(AppState state : values()) {
            if(state.getValue() == value) {
                return state;
            }
        }
        return null;
    }
}