package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.Claim
import com.example.data.ClaimRepository
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val pendingClaims: List<Claim> = emptyList(),
    val isAnalyzing: Boolean = false,
    val analysisError: String? = null,
    val totalCount: Int = 0,
    val canUndo: Boolean = false,
    val useLocalLlm: Boolean = false,
    val localLlmUrl: String = "http://10.0.2.2:1234/v1/chat/completions"
)

class ClaimViewModel(private val repository: ClaimRepository) : ViewModel() {
    
    val pendingClaims: StateFlow<List<Claim>> = repository.pendingClaims
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allClaims.collect { claims ->
                _uiState.update { it.copy(totalCount = claims.size) }
            }
        }
    }

    private val _undoStack = mutableListOf<Claim>()

    private fun updateUndoState() {
        _uiState.update { it.copy(canUndo = _undoStack.isNotEmpty()) }
    }

    fun handleSwipe(claim: Claim, isApproved: Boolean) {
        viewModelScope.launch {
            _undoStack.add(claim)
            updateUndoState()
            val status = if (isApproved) "APPROVED" else "REJECTED"
            repository.update(claim.copy(status = status))
        }
    }

    fun undoLastSwipe() {
        val lastItem = _undoStack.removeLastOrNull() ?: return
        updateUndoState()
        viewModelScope.launch {
            repository.update(lastItem.copy(status = "PENDING", aiAnalysis = null))
        }
    }

    fun loadMockData() {
        viewModelScope.launch {
            if (repository.count() == 0) {
                val mockClaims = listOf(
                    Claim(claimId = "C001", content = "The user interface must be accessible via keyboard navigation.", sourceFile = "doc_ui_reqs.pdf"),
                    Claim(claimId = "C002", content = "All components must be accessible via mouse only.", sourceFile = "doc_ui_v2.pdf"),
                    Claim(claimId = "C003", content = "System must support offline mode for up to 3 days.", sourceFile = "doc_arch_spec.docx"),
                    Claim(claimId = "C004", content = "Offline mode requires manual sync triggered by user.", sourceFile = "doc_arch_spec.docx"),
                    Claim(claimId = "C005", content = "Syncing is fully automatic and hidden from the user.", sourceFile = "doc_sync_spec.docx")
                )
                repository.insertAll(mockClaims)
            }
        }
    }

    fun analyzeClaim(claim: Claim) {
        if (claim.aiAnalysis != null) return // Already analyzed
        
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, analysisError = null) }
            val useLocal = _uiState.value.useLocalLlm
            val localUrl = _uiState.value.localLlmUrl
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            if (!useLocal && (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY")) {
                _uiState.update { it.copy(isAnalyzing = false, analysisError = "API Key missing in Secrets") }
                return@launch
            }

            try {
                val analysis = withContext(Dispatchers.IO) {
                    val prompt = "Analyze the following system requirement claim. Identify if it might be contradictory to typical software standards, or cluster it into a specific category (e.g., UI, Networking, Offline). Keep the response under 2 sentences.\n\nClaim: ${claim.content}\nSource: ${claim.sourceFile}"
                    
                    if (useLocal) {
                        val request = com.example.network.ChatRequest(
                            messages = listOf(com.example.network.ChatMessage("user", prompt))
                        )
                        val response = com.example.network.RetrofitClient.localService.generateContent(localUrl, request)
                        response.choices?.firstOrNull()?.message?.content ?: "No insight found from Local LLM."
                    } else {
                        val request = GenerateContentRequest(
                            contents = listOf(Content(parts = listOf(Part(prompt))))
                        )
                        val response = RetrofitClient.service.generateContent(apiKey, request)
                        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No insight found."
                    }
                }
                
                repository.update(claim.copy(aiAnalysis = analysis))
                _uiState.update { it.copy(isAnalyzing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAnalyzing = false, analysisError = e.message) }
            }
        }
    }

    fun updateSettings(useLocal: Boolean, url: String) {
        _uiState.update { it.copy(useLocalLlm = useLocal, localLlmUrl = url) }
    }
}
