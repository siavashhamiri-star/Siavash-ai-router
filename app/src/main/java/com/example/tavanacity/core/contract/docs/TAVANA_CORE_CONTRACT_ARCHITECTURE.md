# معماری مرجع قرارداد مرکزی و مرز پلتفرم‌ها (TAVANA Core Contract)

## ۱. اصل محوری معماری (Core Architectural Principle)

در معماری معمار ارشد توانا (TAVANA Master Architect):
- **کلاینت‌های اندروید (Android)** و **وب‌اپلیکیشن پیش‌رونده (PWA/Web)** صرفاً **Client** هستند.
- **TAVANA Core** مغز واحد و پردازشگر مرکزی است.
- منطق تجاری سیستم (Orchestrator, Governor, Judge, Verification, Memory, AI Router) در کلاینت‌های مختلف تکثیر (**Duplicate**) نمی‌شود.

```
+--------------------------+          +--------------------------+
|      ANDROID CLIENT      |          |        PWA CLIENT        |
|  (Compose, Room Cache,   |          |    (Web UI, Local Cache, |
|   Myket Billing, UI)     |          |      IndexedDB, UI)      |
+--------------------------+          +--------------------------+
             \                                      /
              \                                    /
               v                                  v
    +--------------------------------------------------------+
    |               TAVANA CORE CONTRACT (DTOs)              |
    |  - Task & TaskRequest DTOs                             |
    |  - ExecutionPlan & ExecutionStep DTOs                  |
    |  - ExecutionResult & StepResult DTOs                   |
    |  - VerificationResult DTOs                             |
    |  - JudgeVerdict DTOs                                   |
    |  - WorkflowState & Snapshot DTOs                       |
    |  - MemoryRecord & MemoryQuery DTOs                     |
    |  - GovernorDecision & ApprovalRequest DTOs             |
    |  - ProviderRequest & ProviderResponse DTOs             |
    +--------------------------------------------------------+
                                |
                                v
    +--------------------------------------------------------+
    |                 FUTURE CENTRAL BACKEND                 |
    |  - TavanaMasterOrchestrator Engine                     |
    |  - Policy & Budget Governor                            |
    |  - Distributed Execution Gateway                       |
    |  - Multi-Provider AI Routing Pipeline                  |
    |  - Centralized Memory & Context Store                  |
    |  - Quantitative & Qualitative Judge                    |
    |  - Server-Side Billing / Entitlement / XP Authority   |
    +--------------------------------------------------------+
```

---

## ۲. تفکیک لایه‌ها و استقلال از وابستگی‌های پلتفرمی

قرارداد مرکزی (`com.example.tavanacity.core.contract.*`) دارای مشخصات زیر است:
- **عدم وابستگی مطلق**: بدون هیچ‌گونه واردسازی (`import`) از Android Context, Activity, Jetpack Compose, Room, Android ViewModel یا APIهای خاص دستگاه.
- **سریال‌پذیری کامل**: منطبق بر استانداردهای REST API / WebSocket / gRPC با DTOهای استاندارد.
- **تبدیل دوطرفه**: توسط `ContractMappers`، کلاینت اندروید می‌تواند مدل‌های Domain داخلی را به DTOهای قرارداد مرکزی نگاشت کند.

---

## ۳. نگاشت مدل‌های دامین به قرارداد مرکزی

| دامنه | مدل داخلی اندروید | DTO در قرارداد مرکزی |
| :--- | :--- | :--- |
| **وظایف** | `ArchitectTask` | `TaskDTO`, `TaskRequestDTO` |
| **برنامه‌ریزی** | `ExecutionPlan`, `ExecutionStep` | `ExecutionPlanDTO`, `ExecutionStepDTO` |
| **اجرا** | `ExecutionResult`, `StepExecutionResult` | `ExecutionResultDTO`, `StepExecutionResultDTO` |
| **صحت‌سنجی** | `VerificationResult` | `VerificationResultDTO`, `VerificationCheckDTO` |
| **داوری** | `JudgeVerdict` | `JudgeVerdictDTO`, `QualityMetricDTO` |
| **ماشین وضعیت** | `WorkflowState`, `WorkflowStateSnapshot` | `WorkflowStateDTO`, `WorkflowSnapshotDTO` |
| **حافظه** | `MemoryEntry` | `MemoryRecordDTO`, `MemoryQueryDTO` |
| **نظارت امنیتی** | `GovernorEvaluation`, `GovernorDecision` | `GovernorEvaluationDTO`, `GovernorDecisionDTO`, `ApprovalRequestDTO` |
| **روتر هوش مصنوعی** | `ModelTier`, `AIResponse` | `ModelTierDTO`, `ProviderRequestDTO`, `ProviderResponseDTO` |
