package ac.ghost.anticheat.util;

import ac.ghost.anticheat.player.GhostPlayer;


public final class ClientDeviceUtil {
    
    public static final int DEVICE_OS_ANDROID = 1;
    public static final int DEVICE_OS_IOS = 2;
    public static final int DEVICE_OS_FIRE_OS = 4;
    public static final int DEVICE_OS_WINDOWS_PHONE = 14;

    private ClientDeviceUtil() {
    }

    
    public static int deviceOS(final GhostPlayer player) {
        if (player == null || player.getSession() == null) {
            return -1;
        }
        try {
            if (player.getSession().getLoginChainData() == null) {
                return -1;
            }
            return player.getSession().getLoginChainData().getDeviceOS();
        } catch (RuntimeException ignored) {
            return -1;
        }
    }

    public static boolean isMobileTouchOS(final int deviceOS) {
        return deviceOS == DEVICE_OS_ANDROID
                || deviceOS == DEVICE_OS_IOS
                || deviceOS == DEVICE_OS_FIRE_OS
                || deviceOS == DEVICE_OS_WINDOWS_PHONE;
    }

    




    public static boolean canTrustMouseAsDigital(final int deviceOS) {
        return deviceOS > 0 && !isMobileTouchOS(deviceOS);
    }

    public static boolean canTrustMouseAsDigital(
            final GhostPlayer player) {
        return canTrustMouseAsDigital(deviceOS(player));
    }
}
