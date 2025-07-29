package com.dsp.main.Core.Event;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.Input;
import net.neoforged.bus.api.Event;

public class UpdateInputEvent extends Event {
    private final LocalPlayer player;
    private final Input input;
    private float leftImpulse;
    private float forwardImpulse;
    private int sprintTriggerTime;

    public UpdateInputEvent(LocalPlayer player, Input input, float leftImpulse, float forwardImpulse, int sprintTriggerTime) {
        this.player = player;
        this.input = input;
        this.leftImpulse = leftImpulse;
        this.forwardImpulse = forwardImpulse;
        this.sprintTriggerTime = sprintTriggerTime;
    }

    public LocalPlayer getPlayer() {
        return player;
    }

    public Input getInput() {
        return input;
    }

    public float getLeftImpulse() {
        return leftImpulse;
    }

    public void setLeftImpulse(float leftImpulse) {
        this.leftImpulse = leftImpulse;
    }

    public float getForwardImpulse() {
        return forwardImpulse;
    }

    public void setForwardImpulse(float forwardImpulse) {
        this.forwardImpulse = forwardImpulse;
    }

    public int getSprintTriggerTime() {
        return sprintTriggerTime;
    }

    public void setSprintTriggerTime(int sprintTriggerTime) {
        this.sprintTriggerTime = sprintTriggerTime;
    }
}