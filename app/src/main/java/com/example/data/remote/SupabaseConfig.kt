package com.example.data.remote

/**
 * Direct Supabase configuration and credentials for Dakshyam Innovations.
 * Injected for production cloud database synchronization.
 */
object SupabaseConfig {
    const val PROJECT_URL: String = "https://uttffudevdijhrpmewqx.supabase.co"
    const val PUBLISHABLE_KEY: String = "sb_publishable_UQoW7OC-1Kfo6EgYJf1ezQ_XiO5RpGt"

    const val REST_URL: String = "$PROJECT_URL/rest/v1"
    const val REALTIME_URL: String = "wss://uttffudevdijhrpmewqx.supabase.co/realtime/v1/websocket?apikey=$PUBLISHABLE_KEY&vsn=1.0.0"
}
