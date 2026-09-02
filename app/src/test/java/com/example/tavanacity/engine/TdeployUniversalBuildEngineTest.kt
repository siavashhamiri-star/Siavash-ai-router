package com.example.tavanacity.engine

import com.example.tavanacity.domain.architect.engine.TdeployUniversalBuildEngine
import com.example.tavanacity.domain.architect.model.ArtifactKind
import com.example.tavanacity.domain.architect.model.ProjectSigningProfile
import com.example.tavanacity.domain.architect.model.ProjectSigningStatus
import com.example.tavanacity.domain.architect.model.TenantProject
import com.example.tavanacity.domain.architect.model.UniversalBuildRequest
import com.example.tavanacity.domain.architect.model.UniversalBuildStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TdeployUniversalBuildEngineTest {

    private lateinit var engine: TdeployUniversalBuildEngine

    @Before
    fun setUp() {
        engine = TdeployUniversalBuildEngine()
    }

    @Test
    fun testIndependentProjectBuildRequests() = runBlocking {
        // Project A
        val reqA = UniversalBuildRequest(
            userId = "user_alpha",
            projectId = "project_a",
            repositoryUrl = "https://github.com/tenant/app-alpha.git",
            branch = "develop"
        )
        val runA = engine.submitBuildRequest(reqA)

        // Project B
        val reqB = UniversalBuildRequest(
            userId = "user_beta",
            projectId = "project_b",
            repositoryUrl = "https://github.com/tenant/app-beta.git",
            branch = "release-v2"
        )
        val runB = engine.submitBuildRequest(reqB)

        // Verify independent IDs and configurations
        assertNotEquals(runA.runId, runB.runId)
        assertEquals("project_a", runA.projectId)
        assertEquals("project_b", runB.projectId)
        assertEquals("develop", runA.branch)
        assertEquals("release-v2", runB.branch)
        assertEquals(UniversalBuildStatus.PENDING, runA.status)
        assertEquals(UniversalBuildStatus.PENDING, runB.status)
    }

    @Test
    fun testBuildExecutionAndArtifactIsolation() = runBlocking {
        // Register Projects
        val projectA = TenantProject(
            projectId = "project_a",
            userId = "user_101",
            projectName = "Project Alpha",
            repositoryUrl = "https://github.com/alpha/repo.git",
            branch = "main",
            applicationId = "com.alpha.app",
            hasDedicatedKeystore = false
        )
        val projectB = TenantProject(
            projectId = "project_b",
            userId = "user_202",
            projectName = "Project Beta",
            repositoryUrl = "https://github.com/beta/repo.git",
            branch = "master",
            applicationId = "com.beta.app",
            hasDedicatedKeystore = true
        )
        engine.registerProject(projectA)
        engine.registerProject(projectB)

        // Build Project A
        val runA = engine.submitBuildRequest(
            UniversalBuildRequest(userId = "user_101", projectId = "project_a", repositoryUrl = projectA.repositoryUrl)
        )
        assertEquals(ProjectSigningStatus.SIGNING_REQUIRED, runA.signingStatus)
        val executedA = engine.executeBuildRun(runA.runId)
        assertEquals(UniversalBuildStatus.SUCCESS, executedA.status)

        // Build Project B
        val runB = engine.submitBuildRequest(
            UniversalBuildRequest(userId = "user_202", projectId = "project_b", repositoryUrl = projectB.repositoryUrl)
        )
        assertEquals(ProjectSigningStatus.PRODUCTION_READY, runB.signingStatus)
        val executedB = engine.executeBuildRun(runB.runId)
        assertEquals(UniversalBuildStatus.SUCCESS, executedB.status)

        // Artifact Isolation Checks
        val artifactsA = engine.getArtifactsForUserAndProject("user_101", "project_a")
        val artifactsB = engine.getArtifactsForUserAndProject("user_202", "project_b")

        assertEquals(3, artifactsA.size) // Debug APK, Release APK, Release AAB
        assertEquals(3, artifactsB.size)

        // Verify Artifact kinds exist
        assertTrue(artifactsA.any { it.kind == ArtifactKind.APK_DEBUG })
        assertTrue(artifactsA.any { it.kind == ArtifactKind.APK_RELEASE })
        assertTrue(artifactsA.any { it.kind == ArtifactKind.AAB_RELEASE })

        // Checksums and names must not mix
        val apkA = artifactsA.first { it.kind == ArtifactKind.APK_RELEASE }
        val apkB = artifactsB.first { it.kind == ArtifactKind.APK_RELEASE }
        assertEquals("project_a-release.apk", apkA.filename)
        assertEquals("project_b-release.apk", apkB.filename)
        assertNotEquals(apkA.sha256Checksum, apkB.sha256Checksum)

        // User Isolation: User 101 cannot download User 202's artifact
        val downloadAllowed = engine.getDownloadableArtifact("user_202", apkB.artifactId)
        assertNotNull(downloadAllowed)
        val downloadForbidden = engine.getDownloadableArtifact("user_101", apkB.artifactId)
        assertNull(downloadForbidden)
    }

    @Test
    fun testProjectScopedSigningNoFakeProduction() = runBlocking {
        val reqWithoutKeystore = UniversalBuildRequest(
            userId = "user_test",
            projectId = "unsigned_proj",
            repositoryUrl = "https://github.com/test/repo.git",
            customSigningProfile = ProjectSigningProfile(projectId = "unsigned_proj", isConfigured = false)
        )
        val run = engine.submitBuildRequest(reqWithoutKeystore)
        assertEquals(ProjectSigningStatus.SIGNING_REQUIRED, run.signingStatus)

        val executed = engine.executeBuildRun(run.runId)
        assertTrue(executed.logs.any { it.contains("SIGNING REQUIRED (No fake credentials generated)") })
    }
}
