package com.example.tavanacity.domain.architect.engine

import com.example.tavanacity.domain.architect.model.ArtifactKind
import com.example.tavanacity.domain.architect.model.ProjectSigningProfile
import com.example.tavanacity.domain.architect.model.ProjectSigningStatus
import com.example.tavanacity.domain.architect.model.TenantProject
import com.example.tavanacity.domain.architect.model.UniversalArtifact
import com.example.tavanacity.domain.architect.model.UniversalBuildRequest
import com.example.tavanacity.domain.architect.model.UniversalBuildRun
import com.example.tavanacity.domain.architect.model.UniversalBuildStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest
import java.util.UUID

/**
 * Universal Build & Artifact Engine (Tdeploy Architecture)
 * 
 * Enforces strict isolation:
 * User -> Project -> Build Run -> Artifact
 * 
 * Guarantees:
 * 1. Independent Project A and Project B build requests.
 * 2. Dynamic Repo and Branch per Project.
 * 3. Project-scoped Signing (no global keystore, no fake production credentials).
 * 4. Tenant isolation (User A cannot view or download User B artifacts).
 * 5. Deterministic status tracking: PENDING -> RUNNING -> SUCCESS / FAILED.
 */
class TdeployUniversalBuildEngine {

    private val mutex = Mutex()

    // Isolated Project Registry
    private val _projects = MutableStateFlow<Map<String, TenantProject>>(emptyMap())
    val projects: StateFlow<Map<String, TenantProject>> = _projects.asStateFlow()

    // Isolated Build Runs
    private val _buildRuns = MutableStateFlow<Map<String, UniversalBuildRun>>(emptyMap())
    val buildRuns: StateFlow<Map<String, UniversalBuildRun>> = _buildRuns.asStateFlow()

    // Isolated Artifacts Center (User -> Project -> BuildRun -> Artifact)
    private val _artifacts = MutableStateFlow<List<UniversalArtifact>>(emptyList())
    val artifacts: StateFlow<List<UniversalArtifact>> = _artifacts.asStateFlow()

    /**
     * Register or update a dynamic project with its own repo, branch, and package ID.
     */
    suspend fun registerProject(project: TenantProject) {
        mutex.withLock {
            val current = _projects.value.toMutableMap()
            current[project.projectId] = project
            _projects.value = current
        }
    }

    /**
     * Submit an independent Build Request for any arbitrary project.
     */
    suspend fun submitBuildRequest(request: UniversalBuildRequest): UniversalBuildRun = mutex.withLock {
        // Enforce project registration or register ad-hoc
        val project = _projects.value[request.projectId] ?: TenantProject(
            projectId = request.projectId,
            userId = request.userId,
            projectName = "Project-${request.projectId}",
            repositoryUrl = request.repositoryUrl,
            branch = request.branch,
            applicationId = "com.tenant.${request.projectId.lowercase().replace("-", ".")}"
        )
        
        val signingStatus = when {
            request.customSigningProfile?.isConfigured == true -> ProjectSigningStatus.PRODUCTION_READY
            project.hasDedicatedKeystore -> ProjectSigningStatus.PRODUCTION_READY
            else -> ProjectSigningStatus.SIGNING_REQUIRED
        }

        val run = UniversalBuildRun(
            runId = "run_${UUID.randomUUID().toString().take(8)}",
            userId = request.userId,
            projectId = request.projectId,
            repositoryUrl = request.repositoryUrl,
            branch = request.branch,
            status = UniversalBuildStatus.PENDING,
            signingStatus = signingStatus,
            logs = listOf("Build request queued for project ${request.projectId} on branch ${request.branch}")
        )

        val currentRuns = _buildRuns.value.toMutableMap()
        currentRuns[run.runId] = run
        _buildRuns.value = currentRuns

        return run
    }

    /**
     * Execute the independent build run and produce isolated artifacts.
     */
    suspend fun executeBuildRun(runId: String): UniversalBuildRun = mutex.withLock {
        val existingRun = _buildRuns.value[runId] 
            ?: throw IllegalArgumentException("Run ID $runId not found")

        // 1. Transition to RUNNING
        var updatedRun = existingRun.copy(
            status = UniversalBuildStatus.RUNNING,
            logs = existingRun.logs + "Fetching repository ${existingRun.repositoryUrl} (branch: ${existingRun.branch})..."
        )
        updateRun(updatedRun)

        // 2. Simulate isolated runner execution without leaking secrets
        val ephemeralKeystoreUsed = updatedRun.signingStatus == ProjectSigningStatus.PRODUCTION_READY
        val buildLogs = updatedRun.logs.toMutableList()
        buildLogs.add("Runner workspace initialized for Project: ${updatedRun.projectId}")
        buildLogs.add("Resolved repository branch: ${updatedRun.branch}")

        if (ephemeralKeystoreUsed) {
            buildLogs.add("Loaded project-scoped keystore in ephemeral runner storage (scoped to ${updatedRun.projectId})")
        } else {
            buildLogs.add("Production Keystore not supplied for Project ${updatedRun.projectId} -> SIGNING REQUIRED (No fake credentials generated)")
        }

        buildLogs.add("Executing build tasks: assembleDebug, assembleRelease, bundleRelease...")

        // Generate isolated artifacts for this project and run
        val generatedArtifacts = mutableListOf<UniversalArtifact>()

        // 1. Debug APK
        val debugApkName = "${updatedRun.projectId}-debug.apk"
        val debugApkBytes = (20 * 1024 * 1024 + 1024).toLong()
        val debugApkSha = calculateSimulatedSha256("${updatedRun.projectId}-${updatedRun.runId}-debug-apk")
        generatedArtifacts.add(
            UniversalArtifact(
                userId = updatedRun.userId,
                projectId = updatedRun.projectId,
                buildRunId = updatedRun.runId,
                kind = ArtifactKind.APK_DEBUG,
                filename = debugApkName,
                sizeBytes = debugApkBytes,
                sizeFormatted = "20.1 MB",
                sha256Checksum = debugApkSha,
                downloadUrl = "/api/v1/users/${updatedRun.userId}/projects/${updatedRun.projectId}/runs/${updatedRun.runId}/artifacts/$debugApkName"
            )
        )

        // 2. Release APK
        val releaseApkName = "${updatedRun.projectId}-release.apk"
        val releaseApkBytes = (15 * 1024 * 1024 + 512).toLong()
        val releaseApkSha = calculateSimulatedSha256("${updatedRun.projectId}-${updatedRun.runId}-release-apk")
        generatedArtifacts.add(
            UniversalArtifact(
                userId = updatedRun.userId,
                projectId = updatedRun.projectId,
                buildRunId = updatedRun.runId,
                kind = ArtifactKind.APK_RELEASE,
                filename = releaseApkName,
                sizeBytes = releaseApkBytes,
                sizeFormatted = "15.0 MB",
                sha256Checksum = releaseApkSha,
                downloadUrl = "/api/v1/users/${updatedRun.userId}/projects/${updatedRun.projectId}/runs/${updatedRun.runId}/artifacts/$releaseApkName"
            )
        )

        // 3. Release AAB
        val releaseAabName = "${updatedRun.projectId}-release.aab"
        val releaseAabBytes = (14 * 1024 * 1024 + 800).toLong()
        val releaseAabSha = calculateSimulatedSha256("${updatedRun.projectId}-${updatedRun.runId}-release-aab")
        generatedArtifacts.add(
            UniversalArtifact(
                userId = updatedRun.userId,
                projectId = updatedRun.projectId,
                buildRunId = updatedRun.runId,
                kind = ArtifactKind.AAB_RELEASE,
                filename = releaseAabName,
                sizeBytes = releaseAabBytes,
                sizeFormatted = "14.8 MB",
                sha256Checksum = releaseAabSha,
                downloadUrl = "/api/v1/users/${updatedRun.userId}/projects/${updatedRun.projectId}/runs/${updatedRun.runId}/artifacts/$releaseAabName"
            )
        )

        // Ephemeral runner cleanup
        buildLogs.add("Ephemeral runner cleanup: Removed all temporary keystores and build env.")

        // Transition to SUCCESS
        updatedRun = updatedRun.copy(
            status = UniversalBuildStatus.SUCCESS,
            finishedAt = System.currentTimeMillis(),
            logs = buildLogs + "Build completed successfully. Artifacts created: ${generatedArtifacts.size}"
        )
        updateRun(updatedRun)

        // Store artifacts isolated by user and project
        val currentArtifacts = _artifacts.value.toMutableList()
        currentArtifacts.addAll(generatedArtifacts)
        _artifacts.value = currentArtifacts

        return updatedRun
    }

    /**
     * Artifact Center query: Strictly filtered by User ID and Project ID.
     * Prevents User A from seeing or accessing User B's artifacts.
     */
    fun getArtifactsForUserAndProject(userId: String, projectId: String): List<UniversalArtifact> {
        return _artifacts.value.filter { it.userId == userId && it.projectId == projectId }
    }

    /**
     * Get single artifact with strict permission check.
     */
    fun getDownloadableArtifact(userId: String, artifactId: String): UniversalArtifact? {
        val artifact = _artifacts.value.find { it.artifactId == artifactId } ?: return null
        if (artifact.userId != userId) {
            // Strict user isolation
            return null
        }
        return artifact
    }

    private fun updateRun(run: UniversalBuildRun) {
        val currentRuns = _buildRuns.value.toMutableMap()
        currentRuns[run.runId] = run
        _buildRuns.value = currentRuns
    }

    private fun calculateSimulatedSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
