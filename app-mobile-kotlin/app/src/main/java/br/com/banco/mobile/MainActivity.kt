package br.com.banco.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send // <-- Correção do aviso aqui!
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import br.com.banco.mobile.ui.theme.BazaBankAppTheme

// ==========================================
// PALETA DE CORES PREMIUM (Design Principles)
// ==========================================
val BazaOffwhite = Color(0xFFF8F9FA) // Fundo suave para não cansar os olhos
val BazaNavy = Color(0xFF0F172A)     // Contraste forte: Transmite segurança e estabilidade
val BazaAccent = Color(0xFF3B82F6)   // Azul vivo para pequenos destaques e foco

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BazaBankAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BazaOffwhite
                ) {
                    BazaBankEcraPrincipal()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazaBankEcraPrincipal() {
    var saldoAtual by remember { mutableStateOf(5000.00) }
    var valorInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var mensagemFeedback by remember { mutableStateOf("") }

    val contaOrigemId = "11111111-1111-1111-1111-111111111111"
    val contaDestinoId = "22222222-2222-2222-2222-222222222222"
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. HEADER PREMIUM (Navy)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BazaNavy)
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Column {
                Text(
                    text = "Olá, Allan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Saldo em conta",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "R$ ${String.format("%.2f", saldoAtual)}",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // 2. CORPO COM CARTÃO FLUTUANTE
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20).dp)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp) // Elevação ligeiramente mais suave
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Transferência via PIX",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BazaNavy
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = contaDestinoId,
                        onValueChange = { },
                        label = { Text("Chave / Conta de Destino") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BazaAccent,
                            focusedLabelColor = BazaAccent,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = valorInput,
                        onValueChange = { valorInput = it },
                        label = { Text("Qual valor deseja transferir?") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BazaAccent,
                            focusedLabelColor = BazaAccent,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val valorDuplo = valorInput.toDoubleOrNull()
                            if (valorDuplo != null && valorDuplo > 0 && valorDuplo <= saldoAtual) {
                                coroutineScope.launch {
                                    isLoading = true
                                    mensagemFeedback = ""
                                    try {
                                        val pedido = TransferenciaRequest(contaOrigemId, contaDestinoId, valorDuplo)
                                        val resposta = RedeBazaBank.api.transferir(pedido)

                                        if (resposta.status == "SUCESSO") {
                                            saldoAtual -= valorDuplo
                                            valorInput = ""
                                            mensagemFeedback = "✅ Transferência enviada!"
                                        }
                                    } catch (e: Exception) {
                                        mensagemFeedback = "❌ Erro de conexão."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                mensagemFeedback = "⚠️ Saldo insuficiente ou valor inválido."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = BazaNavy), // Botão escuro de alta confiança
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            // <-- Novo ícone AutoMirrored em uso aqui
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Transferir Agora", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (mensagemFeedback.isNotEmpty()) {
                Text(
                    text = mensagemFeedback,
                    color = if (mensagemFeedback.contains("✅")) Color(0xFF059669) else Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BazaBankPreview() {
    BazaBankAppTheme {
        BazaBankEcraPrincipal()
    }
}