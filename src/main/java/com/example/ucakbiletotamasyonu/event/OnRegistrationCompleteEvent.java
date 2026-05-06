package com.example.ucakbiletotamasyonu.event;

import com.example.ucakbiletotamasyonu.model.User;
import lombok.Getter;

@Getter
public class OnRegistrationCompleteEvent {

    private final User user;

    public OnRegistrationCompleteEvent(User user) {
        this.user = user;
    }
}
