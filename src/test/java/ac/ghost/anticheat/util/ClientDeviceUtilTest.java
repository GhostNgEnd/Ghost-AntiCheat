package ac.ghost.anticheat.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClientDeviceUtilTest {
    @Test
    void mobileMouseRemainsAnalogCapable() {
        assertFalse(ClientDeviceUtil.canTrustMouseAsDigital(
                ClientDeviceUtil.DEVICE_OS_ANDROID));
        assertFalse(ClientDeviceUtil.canTrustMouseAsDigital(
                ClientDeviceUtil.DEVICE_OS_IOS));
        assertFalse(ClientDeviceUtil.canTrustMouseAsDigital(
                ClientDeviceUtil.DEVICE_OS_FIRE_OS));
        assertFalse(ClientDeviceUtil.canTrustMouseAsDigital(
                ClientDeviceUtil.DEVICE_OS_WINDOWS_PHONE));
    }

    @Test
    void missingDeviceClaimIsConservative() {
        assertFalse(ClientDeviceUtil.canTrustMouseAsDigital(-1));
        assertFalse(ClientDeviceUtil.canTrustMouseAsDigital(0));
    }

    @Test
    void knownNonMobileMouseCanUseDigitalDirections() {
        assertTrue(ClientDeviceUtil.canTrustMouseAsDigital(7)); 
        assertTrue(ClientDeviceUtil.canTrustMouseAsDigital(3)); 
        assertTrue(ClientDeviceUtil.canTrustMouseAsDigital(11)); 
    }
}
