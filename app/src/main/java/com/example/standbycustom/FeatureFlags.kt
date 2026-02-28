package com.example.standbycustom

/**
 * Feature flags for build-time toggles. Change and rebuild to enable/disable.
 * Master branch: keep [FAST_FORWARD_BUTTON_ENABLED] false.
 */
object FeatureFlags {
    /** When true, shows the fast-forward FAB that moves the clock to the next segment. Useful for testing. */
    const val FAST_FORWARD_BUTTON_ENABLED = false
}
