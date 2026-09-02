package com.example.tavanacity.domain.architect.model

import java.util.UUID

/**
 * Universal Build Engine Models (Tdeploy Architecture)
 * Supports: User -> Project -> Build Run -> Artifact
 * Decoupled from TAVANA City domain.
 */

enum class UniversalBuildStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED
}

enum class ArtifactKind {
    APK_DEBUG,
    APK_RELEASE,
    AAB_RELEASE,
    DISTRIBUTION_MANIFEST
}

enum class ProjectSigningStatus {
    PRODUCTION_READY,
    SIGNING_REQUIRED,
    DEBUG_ONLY
}

/**
 * Isolated tenant project configuration.
 */
data class TenantProject(
    val projectId: String,
    val userId: String,
    val projectName: String,
    val repositoryUrl: String,
    val branch: String = "main",
    val applicationId: String,
    val hasDedicatedKeystore: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Project-scoped secure signing configuration.
 * Never stores raw secrets in logs, files, or frontend.
 */
data class ProjectSigningProfile(
    val projectId: String,
    val keyAlias: String? = null,
    val isConfigured: Boolean = false,
    val signingStatus: ProjectSigningStatus = if (isConfigured) ProjectSigningStatus.PRODUCTION_READY else ProjectSigningStatus.SIGNING_REQUIRED
)

/**
 * Independent Build Run with unique run ID.
 */
data class UniversalBuildRun(
    val runId: String = UUID.randomUUID().toString(),
    val userId: String,
    val projectId: String,
    val repositoryUrl: String,
    val branch: String,
    val targetArtifacts: Set<ArtifactKind> = setOf(ArtifactKind.APK_DEBUG, ArtifactKind.APK_RELEASE, ArtifactKind.AAB_RELEASE),
    val status: UniversalBuildStatus = UniversalBuildStatus.PENDING,
    val signingStatus: ProjectSigningStatus = ProjectSigningStatus.SIGNING_REQUIRED,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val logs: List<String> = emptyList(),
    val errorMessage: String? = null
)

/**
 * Secure isolated Artifact strictly mapped to User -> Project -> Build Run.
 */
data class UniversalArtifact(
    val artifactId: String = UUID.randomUUID().toString(),
    val userId: String,
    val projectId: String,
    val buildRunId: String,
    val kind: ArtifactKind,
    val filename: String,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val sha256Checksum: String,
    val downloadUrl: String,
    val isVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Build Request payload for dispatching arbitrary projects.
 */
data class UniversalBuildRequest(
    val userId: String,
    val projectId: String,
    val repositoryUrl: String,
    val branch: String = "main",
    val buildApk: Boolean = true,
    val buildAab: Boolean = true,
    val customSigningProfile: ProjectSigningProfile? = null
)
