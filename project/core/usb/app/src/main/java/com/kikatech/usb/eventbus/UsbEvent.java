package com.kikatech.usb.eventbus;

/**
 * @author SkeeterWang Created on 2018/6/25.
 */

public class UsbEvent extends BaseEvent {
    public static final String ACTION_USB_DEVICE_ATTACHED = "action_usb_device_attached";
    public static final String ACTION_USB_DEVICE_DETACHED = "action_usb_device_detached";
    public static final String ACTION_USB_DEVICE_PERMISSION_GRANTED = "action_usb_device_permission_granted";
    public static final String ACTION_USB_ACCESSORY_ATTACHED = "action_usb_accessory_attached";
    public static final String ACTION_USB_ACCESSORY_DETACHED = "action_usb_accessory_detached";

    public static final String PARAM_USB_DEVICE = "param_usb_device";
    public static final String PARAM_USB_ACCESSORY = "param_usb_accessory";

    public UsbEvent(String action) {
        super(action);
    }
}