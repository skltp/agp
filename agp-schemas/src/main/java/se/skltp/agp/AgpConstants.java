package se.skltp.agp;

public interface AgpConstants {
	
	/*
	 * Http header x-vp-sender-id, for AGP to use when acting consumer towards VP. 
	 * Http heaeder x-vp-instance-id, for AGP to use when acting consumer towards VP.
	 * 
	 * These two headers are dependent on each other in a way that when using x-vp-sender-id
	 * against VP, VP will check for a valid x-vp-instance-id.
	 */
    public static final String X_VP_INSTANCE_ID = "x-vp-instance-id";
	public static final String X_VP_SENDER_ID = "x-vp-sender-id";
	
	public static final String X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID = "x-rivta-original-serviceconsumer-hsaid";
    public static final String X_SKLTP_CORRELATION_ID = "x-skltp-correlation-id";
}
