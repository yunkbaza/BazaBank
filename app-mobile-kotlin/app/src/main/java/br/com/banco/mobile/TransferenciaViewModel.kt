package br.com.banco.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.banco.mobile.RedeBazaBank
import br.com.banco.mobile.SessaoApp
import br.com.banco.mobile.TransferenciaRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TransferenciaViewModel : ViewModel() {

    // Gerencia o estado do botão na UI (A carregar, Sucesso, Erro)
    private val _uiState = MutableStateFlow<TransferenciaUiState>(TransferenciaUiState.Idle)
    val uiState: StateFlow<TransferenciaUiState> = _uiState.asStateFlow()

    fun fazerPix(contaDestino: String, valor: Double) {
        viewModelScope.launch {
            _uiState.value = TransferenciaUiState.Loading

            try {
                // 1. Gera a chave de Idempotência Única para este clique!
                val idempotencyKey = UUID.randomUUID().toString()

                // 2. Prepara o JSON da requisição (usando a conta logada como origem)
                val request = TransferenciaRequest(
                    contaOrigemId = SessaoApp.contaIdAtual,
                    contaDestinoId = contaDestino,
                    valor = valor
                )

                // 3. Chama o backend
                val response = RedeBazaBank.api.transferir(idempotencyKey, request)

                // 4. Analisa a resposta do Spring Boot
                if (response.isSuccessful) {
                    _uiState.value = TransferenciaUiState.Success
                } else if (response.code() == 409) { // Ajuste para o código de erro que o seu backend retorna na idempotência
                    _uiState.value = TransferenciaUiState.Error("Transação em andamento ou duplicada!")
                } else {
                    _uiState.value = TransferenciaUiState.Error("Erro na transferência: ${response.code()}")
                }

            } catch (e: Exception) {
                _uiState.value = TransferenciaUiState.Error("Falha na rede: ${e.message}")
            }
        }
    }

    // Reseta o estado (por exemplo, após mostrar o alerta de erro)
    fun resetState() {
        _uiState.value = TransferenciaUiState.Idle
    }
}

// Classe auxiliar para gerir o estado da tela
sealed class TransferenciaUiState {
    object Idle : TransferenciaUiState()
    object Loading : TransferenciaUiState()
    object Success : TransferenciaUiState()
    data class Error(val message: String) : TransferenciaUiState()
}