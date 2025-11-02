package com.dsp.main.Utils.Render.AnimFromRockstarClient;


import org.joml.Vector2f;

/**
 * @author ConeTin
 * @since 6 дек. 2023 г.
 */

public class RotationAnimation {
	   
	private final InfinityAnimation yaw = new InfinityAnimation();
	private final InfinityAnimation pitch = new InfinityAnimation();
	
    public Vector2f animate(Vector2f rotation, int yawSpeed, int pitchSpeed) {
        return new Vector2f(
        		yaw.animate(rotation.x, yawSpeed),
        		pitch.animate(rotation.y, pitchSpeed)
        );
    }
    
    public float getYaw() {
    	return this.yaw.get();
    }
    
    public float getPitch() {
    	return this.pitch.get();
    }
    
    public RotationAnimation easing(Easing easing) {
    	yaw.easing(easing);
    	yaw.easing(easing);
		
		return this;
	}
    

}
