package com.lumina.notes.spen

import android.content.Context
import android.util.Log

/**
 * Bridges the Samsung **S Pen Remote** SDK (Air Actions) without a compile-time
 * dependency. The SDK ships only as an AAR from Samsung Developers and isn't on
 * Maven, so we bind to it reflectively: the app builds and runs everywhere, and
 * on a Galaxy device with S Pen the button/air gestures light up. On anything
 * else [connect] is a no-op and the app degrades gracefully.
 *
 * What it surfaces (when available):
 *  - [onButtonClick]  : single click of the S Pen side button
 *  - [onButtonDouble] : double click
 *
 * Mapping to app actions (e.g. toggle pen/eraser) is done by the caller.
 */
class SpenAirActions(private val appContext: Context) {

    var onButtonClick: () -> Unit = {}
    var onButtonDouble: () -> Unit = {}

    private var spenUnitManager: Any? = null
    private var buttonEvent: Any? = null
    private var available = false

    /** True if the Samsung S Pen Remote SDK is present on this device. */
    fun isSupported(): Boolean = runCatching {
        Class.forName("com.samsung.android.sdk.penremote.SpenRemote")
        true
    }.getOrDefault(false)

    /**
     * Connects to the S Pen Remote service. Safe to call on any device; returns
     * false if the SDK/feature isn't available.
     */
    fun connect(): Boolean {
        if (!isSupported()) return false
        return runCatching {
            val spenRemoteClass = Class.forName("com.samsung.android.sdk.penremote.SpenRemote")
            val instance = spenRemoteClass.getMethod("getInstance").invoke(null)

            // FEATURE_TYPE_BUTTON = 1 in the SDK; check support defensively.
            val isFeatureEnabled = runCatching {
                spenRemoteClass.getMethod("isFeatureEnabled", Int::class.javaPrimitiveType)
                    .invoke(instance, FEATURE_TYPE_BUTTON) as? Boolean
            }.getOrNull() ?: true

            if (isFeatureEnabled == false) return false

            // Build a ConnectionResultCallback proxy.
            val callbackClass = Class.forName(
                "com.samsung.android.sdk.penremote.SpenRemote\$ConnectionResultCallback"
            )
            val callback = java.lang.reflect.Proxy.newProxyInstance(
                callbackClass.classLoader,
                arrayOf(callbackClass),
            ) { _, method, args ->
                when (method.name) {
                    "onSuccess" -> {
                        spenUnitManager = args?.getOrNull(0)
                        registerButtonListener()
                    }
                    "onFailure" -> Log.w(TAG, "S Pen connect failed: ${args?.firstOrNull()}")
                }
                null
            }

            spenRemoteClass.getMethod(
                "connect",
                Context::class.java,
                callbackClass,
            ).invoke(instance, appContext, callback)

            available = true
            true
        }.getOrElse {
            Log.w(TAG, "S Pen Air Actions unavailable: ${it.message}")
            false
        }
    }

    private fun registerButtonListener() {
        val manager = spenUnitManager ?: return
        runCatching {
            val managerClass = manager.javaClass
            val buttonEventListenerClass = Class.forName(
                "com.samsung.android.sdk.penremote.SpenEventListener"
            )
            // Obtain the BUTTON unit.
            val getUnit = managerClass.getMethod("getUnit", Int::class.javaPrimitiveType)
            val buttonUnit = getUnit.invoke(manager, FEATURE_TYPE_BUTTON) ?: return

            val listener = java.lang.reflect.Proxy.newProxyInstance(
                buttonEventListenerClass.classLoader,
                arrayOf(buttonEventListenerClass),
            ) { _, method, args ->
                if (method.name == "onEvent") {
                    handleButtonEvent(args?.getOrNull(0))
                }
                null
            }

            managerClass.getMethod(
                "registerSpenEventListener",
                buttonEventListenerClass,
                Class.forName("com.samsung.android.sdk.penremote.SpenUnit"),
            ).invoke(manager, listener, buttonUnit)
        }.onFailure { Log.w(TAG, "Button listener registration failed: ${it.message}") }
    }

    private var lastEventAt = 0L

    private fun handleButtonEvent(event: Any?) {
        event ?: return
        // ButtonEvent carries press/release; rather than reflect into its
        // internal structure (which we can't verify here), debounce raw events
        // and treat a quick second one as a double-click.
        val now = System.currentTimeMillis()
        if (now - lastEventAt in 1..400) {
            onButtonDouble()
        } else {
            onButtonClick()
        }
        lastEventAt = now
    }

    fun disconnect() {
        if (!available) return
        runCatching {
            val spenRemoteClass = Class.forName("com.samsung.android.sdk.penremote.SpenRemote")
            val instance = spenRemoteClass.getMethod("getInstance").invoke(null)
            spenRemoteClass.getMethod("disconnect", Context::class.java)
                .invoke(instance, appContext)
        }
        spenUnitManager = null
        available = false
    }

    companion object {
        private const val TAG = "SpenAirActions"
        private const val FEATURE_TYPE_BUTTON = 1
    }
}
