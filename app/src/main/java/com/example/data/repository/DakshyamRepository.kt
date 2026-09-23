package com.example.data.repository

import android.util.Log
import com.example.data.local.ActivityAlertEntity
import com.example.data.local.CashFlowEntity
import com.example.data.local.CompanyProfileEntity
import com.example.data.local.DailyReportEntity
import com.example.data.local.PartnerEntity
import com.example.data.local.ProjectComponentEntity
import com.example.data.local.ProjectEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * DakshyamRepository coordinates real-time business operations exclusively
 * via Supabase PostgreSQL backend. SQLite and Room database are completely eliminated.
 */
class DakshyamRepository(
    private val supabaseSyncRepository: SupabaseSyncRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // In-memory real-time state caches synchronized directly with Supabase
    private val _partners = MutableStateFlow<List<PartnerEntity>>(emptyList())
    val allPartners: Flow<List<PartnerEntity>> = _partners.asStateFlow()

    private val _transactions = MutableStateFlow<List<CashFlowEntity>>(emptyList())
    val allTransactions: Flow<List<CashFlowEntity>> = _transactions.asStateFlow()

    private val _projects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val allProjects: Flow<List<ProjectEntity>> = _projects.asStateFlow()

    private val _components = MutableStateFlow<List<ProjectComponentEntity>>(emptyList())
    val allComponents: Flow<List<ProjectComponentEntity>> = _components.asStateFlow()

    private val _dailyReports = MutableStateFlow<List<DailyReportEntity>>(emptyList())
    val allDailyReports: Flow<List<DailyReportEntity>> = _dailyReports.asStateFlow()

    private val _alerts = MutableStateFlow<List<ActivityAlertEntity>>(emptyList())
    val allAlerts: Flow<List<ActivityAlertEntity>> = _alerts.asStateFlow()

    private val _companyProfile = MutableStateFlow<CompanyProfileEntity?>(null)
    val companyProfile: Flow<CompanyProfileEntity?> = _companyProfile.asStateFlow()

    init {
        // Collect real-time streams from Supabase
        repositoryScope.launch {
            supabaseSyncRepository.streamRemotePartners().collect {
                if (it.isNotEmpty()) _partners.value = it
            }
        }
        repositoryScope.launch {
            supabaseSyncRepository.streamRemoteProjects().collect {
                _projects.value = it
            }
        }
        repositoryScope.launch {
            supabaseSyncRepository.streamRemoteComponents().collect {
                _components.value = it
            }
        }
        repositoryScope.launch {
            supabaseSyncRepository.streamRemoteDailyReports().collect {
                _dailyReports.value = it
            }
        }
        repositoryScope.launch {
            supabaseSyncRepository.streamRemoteCashFlow().collect {
                _transactions.value = it
            }
        }
        repositoryScope.launch {
            supabaseSyncRepository.streamRemoteAlerts().collect {
                _alerts.value = it
            }
        }
        repositoryScope.launch {
            supabaseSyncRepository.streamRemoteCompanyProfile().collect {
                if (it != null) _companyProfile.value = it
            }
        }
    }

    // -------------------------------------------------------------
    // PARTNERS
    // -------------------------------------------------------------
    val totalCapital: Flow<Double?> = _partners.map { list ->
        list.sumOf { it.capitalContributed }
    }

    suspend fun savePartner(partner: PartnerEntity): Long {
        val targetId = if (partner.id > 0) partner.id else System.currentTimeMillis()
        val toSave = partner.copy(id = targetId)
        val result = supabaseSyncRepository.syncPartnerToRemote(toSave)
        check(result.isSuccess) { result.exceptionOrNull()?.message ?: "Cloud save failed for partner." }

        val current = _partners.value.toMutableList()
        val idx = current.indexOfFirst { it.id == targetId }
        if (idx >= 0) current[idx] = toSave else current.add(toSave)
        _partners.value = current

        addAlert(ActivityAlertEntity(
            id = System.currentTimeMillis() + 1,
            title = if (partner.id == 0L) "New Partner Registered" else "Partner Profile Updated",
            description = "${toSave.name} (${toSave.role}) - Capital ₹${toSave.capitalContributed}",
            category = "PARTNER"
        ))
        return targetId
    }

    suspend fun updatePartner(partner: PartnerEntity) {
        savePartner(partner)
    }

    suspend fun deletePartner(partner: PartnerEntity) {
        _partners.value = _partners.value.filter { it.id != partner.id }
        supabaseSyncRepository.deletePartnerFromRemote(partner.id)
        addAlert(
            ActivityAlertEntity(
                id = System.currentTimeMillis() + 1,
                title = "Partner Deactivated",
                description = "${partner.name} was removed from registry.",
                category = "PARTNER"
            )
        )
    }

    // -------------------------------------------------------------
    // CASH FLOW & TREASURY
    // -------------------------------------------------------------
    val totalExpenses: Flow<Double?> = _transactions.map { list ->
        list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }

    val totalInflow: Flow<Double?> = _transactions.map { list ->
        list.filter { it.type != "EXPENSE" }.sumOf { it.amount }
    }

    suspend fun addTransaction(tx: CashFlowEntity): Long {
        val targetId = if (tx.id > 0) tx.id else System.currentTimeMillis()
        val toSave = tx.copy(id = targetId)
        val result = supabaseSyncRepository.syncCashFlowToRemote(toSave)
        check(result.isSuccess) { result.exceptionOrNull()?.message ?: "Cloud save failed for transaction." }
        _transactions.value = listOf(toSave) + _transactions.value.filter { it.id != targetId }

        val typeLabel = when (tx.type) {
            "EXPENSE" -> "Expense Recorded"
            "CAPITAL_INJECTION" -> "Capital Injected"
            else -> "Payment Received"
        }
        addAlert(ActivityAlertEntity(
            id = System.currentTimeMillis() + 2,
            title = typeLabel,
            description = "₹${tx.amount} for '${tx.description}' by ${tx.paidByPartnerName}",
            category = "FINANCE"
        ))
        return targetId
    }

    suspend fun saveTransactionDirect(tx: CashFlowEntity): Long {
        val targetId = if (tx.id > 0) tx.id else System.currentTimeMillis()
        val toSave = tx.copy(id = targetId)
        val result = supabaseSyncRepository.syncCashFlowToRemote(toSave)
        check(result.isSuccess) { result.exceptionOrNull()?.message ?: "Cloud save failed for transaction." }
        _transactions.value = listOf(toSave) + _transactions.value.filter { it.id != targetId }
        return targetId
    }



    suspend fun deleteTransaction(tx: CashFlowEntity) {
        _transactions.value = _transactions.value.filter { it.id != tx.id }
        supabaseSyncRepository.deleteCashFlowFromRemote(tx.id)
    }

    // -------------------------------------------------------------
    // PROJECTS & COMPONENTS
    // -------------------------------------------------------------
    suspend fun saveProject(project: ProjectEntity): Long {
        val targetId = if (project.id > 0) project.id else System.currentTimeMillis()
        val toSave = project.copy(id = targetId)
        val result = supabaseSyncRepository.syncProjectToRemote(toSave)
        check(result.isSuccess) { result.exceptionOrNull()?.message ?: "Cloud save failed for project." }
        val current = _projects.value.toMutableList()
        val idx = current.indexOfFirst { it.id == targetId }
        if (idx >= 0) current[idx] = toSave else current.add(toSave)
        _projects.value = current
        return targetId
    }

    

    suspend fun createProjectWithComponents(
        project: ProjectEntity,
        components: List<ProjectComponentEntity>
    ): Long {
        val projectId = saveProject(project)
        val adjustedComponents = components.mapIndexed { index, comp ->
            val compId = if (comp.id > 0) comp.id else (System.currentTimeMillis() + index)
            comp.copy(id = compId, projectId = projectId)
        }
        for (comp in adjustedComponents) {
            addComponent(comp)
        }
        addAlert(
            ActivityAlertEntity(
                id = System.currentTimeMillis() + 3,
                title = "Project Launched",
                description = "${project.title} (${project.status}) with ${components.size} components.",
                category = "PROJECT"
            )
        )
        return projectId
    }

    suspend fun updateProject(project: ProjectEntity) {
        saveProject(project)
    }

    suspend fun deleteProject(project: ProjectEntity) {
        _projects.value = _projects.value.filter { it.id != project.id }
        _components.value = _components.value.filter { it.projectId != project.id }
        _dailyReports.value = _dailyReports.value.filter { it.projectId != project.id }
        supabaseSyncRepository.deleteProjectFromRemote(project.id)
        addAlert(
            ActivityAlertEntity(
                id = System.currentTimeMillis() + 4,
                title = "Project Deleted",
                description = "${project.title} has been archived/removed.",
                category = "PROJECT"
            )
        )
    }

    fun getComponentsForProject(projectId: Long): Flow<List<ProjectComponentEntity>> {
        return _components.map { list -> list.filter { it.projectId == projectId } }
    }

    suspend fun addComponent(component: ProjectComponentEntity): Long {
        val compId = if (component.id > 0) component.id else System.currentTimeMillis()
        val toSave = component.copy(id = compId)
        val result = supabaseSyncRepository.syncComponentToRemote(toSave)
        check(result.isSuccess) { result.exceptionOrNull()?.message ?: "Cloud save failed for component." }
        val current = _components.value.toMutableList()
        val idx = current.indexOfFirst { it.id == compId }
        if (idx >= 0) current[idx] = toSave else current.add(toSave)
        _components.value = current
        return compId
    }



    suspend fun saveComponent(component: ProjectComponentEntity): Long {
        return addComponent(component)
    }

    suspend fun deleteComponent(component: ProjectComponentEntity) {
        _components.value = _components.value.filter { it.id != component.id }
        supabaseSyncRepository.deleteComponentFromRemote(component.id)
    }

    // -------------------------------------------------------------
    // DAILY REPORTS (DPR)
    // -------------------------------------------------------------
    fun getReportsForProject(projectId: Long): Flow<List<DailyReportEntity>> {
        return _dailyReports.map { list -> list.filter { it.projectId == projectId } }
    }

    suspend fun addDailyReport(report: DailyReportEntity): Long {
        val repId = if (report.id > 0) report.id else System.currentTimeMillis()
        val toSave = report.copy(id = repId)
        val result = supabaseSyncRepository.syncDailyReportToRemote(toSave)
        check(result.isSuccess) { result.exceptionOrNull()?.message ?: "Cloud save failed for daily report." }
        _dailyReports.value = listOf(toSave) + _dailyReports.value.filter { it.id != repId }
        addAlert(ActivityAlertEntity(
            id = System.currentTimeMillis() + 5,
            title = "Daily Progress Logged",
            description = "${report.reportedByPartner} logged report for ${report.projectTitle}",
            category = "REPORT"
        ))
        return repId
    }



    suspend fun saveDailyReportDirect(report: DailyReportEntity): Long {
        val repId = if (report.id > 0) report.id else System.currentTimeMillis()
        val toSave = report.copy(id = repId)
        val result = supabaseSyncRepository.syncDailyReportToRemote(toSave)
        check(result.isSuccess) { result.exceptionOrNull()?.message ?: "Cloud save failed for daily report." }
        _dailyReports.value = listOf(toSave) + _dailyReports.value.filter { it.id != repId }
        return repId
    }



    suspend fun deleteDailyReport(report: DailyReportEntity) {
        _dailyReports.value = _dailyReports.value.filter { it.id != report.id }
        supabaseSyncRepository.deleteDailyReportFromRemote(report.id)
    }

    // -------------------------------------------------------------
    // ALERTS & NOTIFICATIONS
    // -------------------------------------------------------------
    val unreadAlertCount: Flow<Int> = _alerts.map { list -> list.count { !it.isRead } }

    suspend fun addAlert(alert: ActivityAlertEntity): Long {
        val alertId = if (alert.id > 0) alert.id else System.currentTimeMillis()
        val toSave = alert.copy(id = alertId)
        val result = supabaseSyncRepository.syncAlertToRemote(toSave)
        check(result.isSuccess) { result.exceptionOrNull()?.message ?: "Cloud save failed for alert." }
        _alerts.value = listOf(toSave) + _alerts.value.filter { it.id != alertId }
        return alertId
    }



    suspend fun markAlertRead(alertId: Long) {
        _alerts.value = _alerts.value.map { if (it.id == alertId) it.copy(isRead = true) else it }
        supabaseSyncRepository.markAlertReadInRemote(alertId)
    }

    suspend fun markAllAlertsRead() {
        _alerts.value = _alerts.value.map { it.copy(isRead = true) }
        for (a in _alerts.value) {
            supabaseSyncRepository.markAlertReadInRemote(a.id)
        }
    }

    suspend fun clearAlerts() {
        _alerts.value = emptyList()
        supabaseSyncRepository.clearAllAlertsFromRemote()
    }

    // -------------------------------------------------------------
    // COMPANY PROFILE & BUSINESS IDENTITY
    // -------------------------------------------------------------
    suspend fun saveCompanyProfile(profile: CompanyProfileEntity) {
        val result = supabaseSyncRepository.syncCompanyProfileToRemote(profile)
        check(result.isSuccess) { result.exceptionOrNull()?.message ?: "Cloud save failed for company profile." }
        _companyProfile.value = profile
        addAlert(ActivityAlertEntity(
            id = System.currentTimeMillis() + 6,
            title = "Business Profile Updated",
            description = "${profile.companyName} details modified by ${profile.updatedByPartner}",
            category = "PARTNER"
        ))
    }



    suspend fun refreshFromCloud() = withContext(Dispatchers.IO) {
        try {
            val partners = supabaseSyncRepository.fetchPartnersDirect()
            if (partners.isNotEmpty()) _partners.value = partners

            val projects = supabaseSyncRepository.fetchProjectsDirect()
            _projects.value = projects

            val components = supabaseSyncRepository.fetchComponentsDirect()
            _components.value = components

            val reports = supabaseSyncRepository.fetchDailyReportsDirect()
            _dailyReports.value = reports

            val txs = supabaseSyncRepository.fetchCashFlowDirect()
            _transactions.value = txs

            val alerts = supabaseSyncRepository.fetchAlertsDirect()
            _alerts.value = alerts

            val profile = supabaseSyncRepository.fetchCompanyProfileDirect()
            if (profile != null) _companyProfile.value = profile
        } catch (e: Exception) {
            Log.w("DakshyamRepository", "Manual refresh error: ${e.message}")
        }
    }

    fun purgeAllLocalData() {
        _projects.value = emptyList()
        _components.value = emptyList()
        _dailyReports.value = emptyList()
        _transactions.value = emptyList()
        _alerts.value = emptyList()
        _partners.value = _partners.value.map { it.copy(capitalContributed = 0.0) }
    }
}
