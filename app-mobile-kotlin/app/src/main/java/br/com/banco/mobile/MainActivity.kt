package br.com.banco.mobile

import android.util.Log
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import br.com.banco.mobile.ui.theme.BazaBankAppTheme
import java.util.UUID

// ==========================================
// 1. O CÉREBRO DA APLICAÇÃO (VIEWMODEL)
// ==========================================
class BazaViewModel : ViewModel() {
    private val _saldo = MutableStateFlow(0.0)
    val saldo: StateFlow<Double> = _saldo.asStateFlow()

    private val _extrato = MutableStateFlow<List<TransacaoExtrato>>(emptyList())
    val extrato: StateFlow<List<TransacaoExtrato>> = _extrato.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _mensagemErro = MutableStateFlow("")
    val mensagemErro: StateFlow<String> = _mensagemErro.asStateFlow()

    fun limparErro() { _mensagemErro.value = "" }
    fun setErro(msg: String) { _mensagemErro.value = msg }

    fun login(cpf: String, senha: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _mensagemErro.value = ""
            try {
                val resposta = RedeBazaBank.api.login(AuthRequest(cpf, senha))
                SessaoApp.tokenJwt = resposta.token
                onSuccess()
            } catch (e: Exception) {
                _mensagemErro.value = "❌ CPF ou senha inválidos."
            } finally { _isLoading.value = false }
        }
    }

    fun registrar(cpf: String, senha: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _mensagemErro.value = ""
            try {
                val resposta = RedeBazaBank.api.registrar(AuthRequest(cpf, senha))
                if (resposta.isSuccessful) {
                    onSuccess()
                } else {
                    _mensagemErro.value = "⚠️ CPF já registado ou dados inválidos."
                }
            } catch (e: Exception) {
                _mensagemErro.value = "❌ Erro de conexão com o banco."
            } finally { _isLoading.value = false }
        }
    }

    fun atualizarHome() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("BAZABANK_REDE", "A tentar buscar saldo da conta: ${SessaoApp.contaIdAtual}")
                val conta = RedeBazaBank.api.buscarConta(SessaoApp.contaIdAtual)
                _saldo.value = conta.saldo
                Log.d("BAZABANK_REDE", "Saldo recebido com sucesso: ${conta.saldo}")
            } catch (e: retrofit2.HttpException) {
                Log.e("BAZABANK_REDE", "Erro HTTP do Backend: Código ${e.code()} - ${e.message()}")
                _mensagemErro.value = "Erro no servidor: ${e.code()}"
            } catch (e: Exception) {
                Log.e("BAZABANK_REDE", "Erro na app: ${e.message}")
                _mensagemErro.value = "Erro ao buscar dados."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun buscarExtrato() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val transacoes = RedeBazaBank.api.buscarExtrato(SessaoApp.contaIdAtual)
                _extrato.value = transacoes
            } catch (e: Exception) {
                _mensagemErro.value = "Erro ao carregar extrato."
            } finally { _isLoading.value = false }
        }
    }

    fun transferir(chaveDestino: String, valor: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _mensagemErro.value = ""
            try {
                val pedido = TransferenciaRequest(SessaoApp.contaIdAtual, chaveDestino, valor)
                val idempotencyKey = UUID.randomUUID().toString()

                val resposta = RedeBazaBank.api.transferir(idempotencyKey, pedido)

                if (resposta.isSuccessful) {
                    val corpo = resposta.body()
                    if (corpo?.status == "SUCESSO") {
                        atualizarHome()
                        onSuccess()
                    }
                } else if (resposta.code() == 409) {
                    _mensagemErro.value = "⚠️ Transação em andamento ou duplicada."
                } else {
                    _mensagemErro.value = "❌ Erro ao processar PIX (Código: ${resposta.code()})."
                }
            } catch (e: Exception) {
                _mensagemErro.value = "❌ Erro de conexão: Verifique a internet."
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ==========================================
// 2. TEMA E CORES
// ==========================================
val BazaBackground = Color(0xFFF8F9FA)
val BazaDark = Color(0xFF121212)
val BazaAccent = Color.Black
val BazaInputBg = Color(0xFFEBEBEB)

// ==========================================
// 3. NAVEGAÇÃO PRINCIPAL E MAIN ACTIVITY
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BazaBankAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BazaBackground) {
                    val viewModel: BazaViewModel = viewModel()
                    BazaBankNavegacao(viewModel)
                }
            }
        }
    }
}

@Composable
fun BazaBankNavegacao(viewModel: BazaViewModel) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        SessaoApp.eventoSessaoExpirada.collect { expirou ->
            if (expirou) {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { EcraLogin(navController, viewModel) }
        composable("registro") { EcraRegistro(navController, viewModel) }
        composable("home") { EcraHome(navController, viewModel) }
        composable("transferencia") { EcraTransferencia(navController, viewModel) }
        composable("extrato") { EcraExtrato(navController, viewModel) }
    }
}

// ==========================================
// 4. TELAS DA APLICAÇÃO
// ==========================================

@Composable
fun EcraLogin(navController: NavController, viewModel: BazaViewModel) {
    var cpf by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.mensagemErro.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { viewModel.limparErro() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(BazaAccent), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("BazaBank", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = BazaDark)
        Text("Exclusive", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium, letterSpacing = 4.sp)

        Spacer(modifier = Modifier.height(48.dp))

        BazaTextField(value = cpf, onValueChange = { cpf = it }, label = "CPF", icon = Icons.Default.Person)
        Spacer(modifier = Modifier.height(16.dp))
        BazaTextField(value = senha, onValueChange = { senha = it }, label = "Senha", icon = Icons.Default.Lock, isPassword = true)

        Spacer(modifier = Modifier.height(32.dp))

        if (erro.isNotEmpty()) {
            Text(erro, color = Color(0xFFDC2626), modifier = Modifier.padding(bottom = 16.dp), fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = {
                viewModel.login(cpf, senha) {
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BazaAccent),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading && cpf.isNotBlank() && senha.isNotBlank()
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
            else Text("Acessar a minha conta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("registro") }) {
            Text("Abrir uma conta BazaBank", color = BazaDark, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun EcraRegistro(navController: NavController, viewModel: BazaViewModel) {
    var cpf by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var sucessoMsg by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.mensagemErro.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { viewModel.limparErro() }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(32.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = BazaDark)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Criar Conta", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = BazaDark)
        Text("Simples, seguro e minimalista.", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))

        BazaTextField(value = cpf, onValueChange = { cpf = it }, label = "Seu CPF")
        Spacer(modifier = Modifier.height(16.dp))
        BazaTextField(value = senha, onValueChange = { senha = it }, label = "Crie uma senha forte", isPassword = true)

        Spacer(modifier = Modifier.height(40.dp))

        if (erro.isNotEmpty()) Text(erro, color = Color(0xFFDC2626), modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp))
        if (sucessoMsg.isNotEmpty()) Text(sucessoMsg, color = Color(0xFF059669), modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp), fontWeight = FontWeight.Bold)

        Button(
            onClick = {
                viewModel.registrar(cpf, senha) {
                    sucessoMsg = "✅ Sucesso! Pode fazer o Login."
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BazaAccent),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading && cpf.isNotBlank() && senha.isNotBlank()
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
            else Text("Finalizar Cadastro", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun EcraHome(navController: NavController, viewModel: BazaViewModel) {
    val saldo by viewModel.saldo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current // Usado para mostrar os Toasts

    LaunchedEffect(Unit) { viewModel.atualizarHome() }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = BazaDark, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Olá,", fontSize = 14.sp, color = Color.Gray)
                    Text("Investidor", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BazaDark)
                }
            }

            Row {
                IconButton(onClick = {
                    Toast.makeText(context, "🔔 Sem novas notificações", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notificações", tint = BazaDark)
                }
                IconButton(onClick = { SessaoApp.encerrarSessao() }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sair", tint = Color.Red)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(200.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF222222), Color.Black)))) {
                Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Saldo Disponível", color = Color.LightGray, fontSize = 14.sp)
                        Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = Color.LightGray)
                    }
                    if (isLoading && saldo == 0.0) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    } else {
                        Text("R$ ${String.format("%.2f", saldo)}", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
        Text("Ações Rápidas", modifier = Modifier.padding(horizontal = 24.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BazaDark)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(icon = Icons.AutoMirrored.Filled.Send, label = "Área PIX", modifier = Modifier.weight(1f), onClick = { navController.navigate("transferencia") })
            QuickActionButton(icon = Icons.Outlined.CreditCard, label = "Extrato", modifier = Modifier.weight(1f), onClick = { navController.navigate("extrato") })

            QuickActionButton(icon = Icons.Default.Refresh, label = "Atualizar", modifier = Modifier.weight(1f), onClick = { viewModel.atualizarHome() })

            QuickActionButton(icon = Icons.Default.Person, label = "Perfil", modifier = Modifier.weight(1f), onClick = {
                Toast.makeText(context, "👤 Perfil em construção!", Toast.LENGTH_SHORT).show()
            })
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcraTransferencia(navController: NavController, viewModel: BazaViewModel) {
    var valorInput by remember { mutableStateOf("") }
    var sucessoMsg by remember { mutableStateOf("") }

    val saldo by viewModel.saldo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val erro by viewModel.mensagemErro.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) { viewModel.limparErro() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Área PIX", fontWeight = FontWeight.Bold, color = BazaDark) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = BazaDark)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BazaBackground)
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text("Quanto deseja transferir?", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            TextField(
                value = valorInput,
                onValueChange = { valorInput = it },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = BazaDark),
                placeholder = { Text("R$ 0.00", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.LightGray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Seu saldo: R$ ${String.format("%.2f", saldo)}", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))

            BazaTextField(value = "22222222-2222-2222-2222-222222222222", onValueChange = {}, label = "Chave de Destino", readOnly = true)

            Spacer(modifier = Modifier.height(40.dp))

            if (erro.isNotEmpty()) Text(erro, color = Color(0xFFDC2626), modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp))
            if (sucessoMsg.isNotEmpty()) Text(sucessoMsg, color = Color(0xFF059669), modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp), fontWeight = FontWeight.Bold)

            Button(
                onClick = {
                    focusManager.clearFocus()
                    val valor = valorInput.toDoubleOrNull()
                    if (valor != null && valor > 0) {
                        if (valor <= saldo) {
                            viewModel.transferir("22222222-2222-2222-2222-222222222222", valor) {
                                sucessoMsg = "✅ PIX Enviado com Sucesso!"
                                valorInput = ""
                            }
                        } else {
                            viewModel.setErro("⚠️ Saldo insuficiente!")
                        }
                    } else {
                        viewModel.setErro("⚠️ Digite um valor válido!")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BazaAccent),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && valorInput.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                else Text("Confirmar Transferência", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcraExtrato(navController: NavController, viewModel: BazaViewModel) {
    val transacoes by viewModel.extrato.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.buscarExtrato() }

    Column(modifier = Modifier.fillMaxSize().background(BazaBackground)) {
        TopAppBar(
            title = { Text("O seu Extrato", fontWeight = FontWeight.Bold, color = BazaDark) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = BazaDark)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BazaBackground)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BazaDark)
            }
        } else if (transacoes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sem movimentos recentes.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(transacoes) { transacao ->
                    val isSaida = transacao.contaOrigemId == SessaoApp.contaIdAtual

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isSaida) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isSaida) Icons.AutoMirrored.Filled.Send else Icons.Outlined.Home,
                                        contentDescription = null,
                                        tint = if (isSaida) Color(0xFFDC2626) else Color(0xFF059669)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(if (isSaida) "PIX Enviado" else "PIX Recebido", fontWeight = FontWeight.Bold, color = BazaDark)
                                    Text(transacao.dataCriacao.take(10), fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            Text(
                                text = "${if (isSaida) "-" else "+"} R$ ${String.format("%.2f", transacao.valor)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (isSaida) BazaDark else Color(0xFF059669)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES AUXILIARES
// ==========================================
@Composable
fun BazaTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, isPassword: Boolean = false, isNumeric: Boolean = false, readOnly: Boolean = false) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = value, onValueChange = onValueChange, label = { Text(label, color = Color.Gray) }, readOnly = readOnly,
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = BazaDark) } },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isNumeric) KeyboardType.Decimal else KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White, unfocusedContainerColor = BazaInputBg,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = BazaDark, unfocusedTextColor = BazaDark
        ),
        singleLine = true
    )
}

@Composable
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 8.dp)
    ) {
        Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = BazaDark, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BazaDark)
    }
}