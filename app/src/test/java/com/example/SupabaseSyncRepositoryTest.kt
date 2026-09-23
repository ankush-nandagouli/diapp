package com.example

import com.example.data.local.DailyReportEntity
import com.example.data.local.ProjectEntity
import com.example.data.remote.SupabaseConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseSyncRepositoryTest {

    @Test
    fun supabaseConfig_hasValidDirectAndRestEndpoints() {
        assertEquals("https://uttffudevdijhrpmewqx.supabase.co", SupabaseConfig.PROJECT_URL)
        assertEquals("sb_publishable_UQoW7OC-1Kfo6EgYJf1ezQ_XiO5RpGt", SupabaseConfig.PUBLISHABLE_KEY)
        assertTrue(SupabaseConfig.DIRECT_CONNECTION_STRING.contains("uttffudevdijhrpmewqx.supabase.co:5432"))
        assertEquals("https://uttffudevdijhrpmewqx.supabase.co/rest/v1", SupabaseConfig.REST_URL)
    }

    @Test
    fun projectEntity_preservesCoreTrackingAttributes() {
        val project = ProjectEntity(
            id = 101L,
            title = "Autonomous Inspection Rover",
            client = "Ministry of Agriculture",
            status = "In Progress",
            budget = 450000.0,
            assignedPartners = "Ankush Nandagouli, Himanshu Patle",
            createdAt = 1700000000000L
        )

        assertEquals("Autonomous Inspection Rover", project.title)
        assertEquals("Ministry of Agriculture", project.client)
        assertEquals(450000.0, project.budget, 0.001)
        assertEquals("In Progress", project.status)
        assertNotNull(project.assignedPartners)
    }

    @Test
    fun dailyReportEntity_preservesReportingAttributes() {
        val report = DailyReportEntity(
            id = 201L,
            projectId = 101L,
            projectTitle = "Autonomous Inspection Rover",
            reportedByPartner = "Shikhar Bisen",
            summary = "Calibrated LiDAR telemetry and executed obstacle avoidance field test.",
            mediaUri = "content://media/photos/lidar_test.png",
            mediaType = "PHOTO",
            timestamp = 1700001000000L
        )

        assertEquals(101L, report.projectId)
        assertEquals("Shikhar Bisen", report.reportedByPartner)
        assertEquals("PHOTO", report.mediaType)
        assertNotNull(report.summary)
    }
}
