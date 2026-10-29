package com.sani.ai;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class PhoneControlService extends AccessibilityService {
    private static PhoneControlService instance;

    @Override public void onServiceConnected() { super.onServiceConnected(); instance = this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { if (instance == this) instance = null; super.onDestroy(); }

    public static void performBack() { if (instance != null) instance.performGlobalAction(GLOBAL_ACTION_BACK); }
    public static void performHome() { if (instance != null) instance.performGlobalAction(GLOBAL_ACTION_HOME); }

    public static boolean typeText(String text) {
        if (instance == null) return false;
        AccessibilityNodeInfo root = instance.getRootInActiveWindow();
        if (root == null) return false;
        AccessibilityNodeInfo focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focused == null) return false;
        Bundle args = new Bundle();
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args);
    }

    public static void performScroll(boolean down) {
        if (instance == null) return;
        AccessibilityNodeInfo root = instance.getRootInActiveWindow();
        if (root != null) {
            AccessibilityNodeInfo node = root;
            if (down) node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            else node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
    }
}
