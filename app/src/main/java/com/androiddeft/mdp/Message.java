package com.androiddeft.mdp;

import android.content.res.Resources;

public class Message {

    private int color;
    private String details;

    Message(boolean isRead, String d, Resources r) {
        color = isRead ? r.getColor(R.color.light_sky_blue) : r.getColor(R.color.red);
        details = d;
    }

    public int getColor() {
        return color;
    }

    public String getDetails() {
        return details;
    }
}
