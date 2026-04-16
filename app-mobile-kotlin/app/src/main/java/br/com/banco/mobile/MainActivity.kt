package br.com.banco.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import br.com.banco.mobile.ui.theme.BazaBankAppTheme

// ==========================================
// PALETA PREMIUM MONOCROMÁTICA (Black Card)
// ==========================================
val BazaBackground = Color(0xFFF8F9FA) // Off-white muito limpo e sofisticado
val BazaDark = Color(0xFF121212)       // Preto "fosco" (reduz o cansaço visual)
val BazaAccent = Color.Black           // Preto puro para botões e contrastes absolutos
val BazaInputBg = Color(0xFFEBEBEB)    // Cinza quase branco para os campos de texto

var saldoGlobal by mutableStateOf(5000.00) // Vai ser atualizado dinamicamente!

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BazaBankAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BazaBackground) {
                    BazaBankNavegacao()
                }
            }
        }
    }
}

@Composable
fun BazaBankNavegacao() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { EcraLogin(navController) }
        composable("registro") { EcraRegistro(navController) }
        composable("home") { EcraHome(navController) }
        composable("transferencia") { EcraTransferencia(navController) }
        composable("extrato") { EcraExtrato(navController) } // Nova rota do Extrato!
    }
}

// ==========================================
// COMPONENTES REUTILIZÁVEIS
// ==========================================
@Composable
fun BazaTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, isPassword: Boolean = false, isNumeric: Boolean = false, readOnly: Boolean = false) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        readOnly = readOnly,
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = BazaDark) } },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = if (isNumeric) KeyboardOptions(keyboardType = KeyboardType.Decimal) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = BazaInputBg,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = BazaDark,
            unfocusedTextColor = BazaDark
        ),
        singleLine = true
    )
}

// ==========================================
// 1. PÁGINA DE LOGIN
// ==========================================
@Composable
fun EcraLogin(navController: NavController) {
    var cpf by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ... (Mantenha o Logo e os Textos Iniciais) ...
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

        if (mensagemErro.isNotEmpty()) {
            Text(mensagemErro, color = Color(0xFFDC2626), modifier = Modifier.padding(bottom = 16.dp))
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    mensagemErro = ""
                    try {
                        // 1. Faz o Login Real
                        val resposta = RedeBazaBank.api.login(AuthRequest(cpf, senha))
                        // 2. Guarda o Crachá na Sessão!
                        SessaoApp.tokenJwt = resposta.token
                        // 3. Libera o acesso
                        navController.navigate("home")
                    } catch (e: Exception) {
                        mensagemErro = "❌ CPF ou senha inválidos."
                    } finally { isLoading = false }
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BazaAccent),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
            else Text("Acessar a minha conta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate("registro") }) {
            Text("Abrir uma conta BazaBank", color = BazaDark, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 2. PÁGINA DE REGISTRO
// ==========================================
@Composable
fun EcraRegistro(navController: NavController) {
    var nome by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var mensagem by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        // ... (Mantenha os cabeçalhos visuais iguais) ...
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = BazaDark)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Criar Conta", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = BazaDark)
        Text("Simples, seguro e minimalista.", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))

        BazaTextField(value = nome, onValueChange = { nome = it }, label = "Como quer ser chamado?")
        Spacer(modifier = Modifier.height(16.dp))
        BazaTextField(value = cpf, onValueChange = { cpf = it }, label = "Seu CPF")
        Spacer(modifier = Modifier.height(16.dp))
        BazaTextField(value = senha, onValueChange = { senha = it }, label = "Crie uma senha forte", isPassword = true)

        Spacer(modifier = Modifier.weight(1f))

        if (mensagem.isNotEmpty()) {
            Text(mensagem, color = if (mensagem.contains("Sucesso")) Color(0xFF059669) else Color(0xFFDC2626), modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp))
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    try {
                        val resposta = RedeBazaBank.api.registrar(AuthRequest(cpf, senha))
                        if (resposta.isSuccessful) {
                            mensagem = "✅ Sucesso! Voltando para o login..."
                            delay(1500)
                            navController.popBackStack()
                        } else {
                            mensagem = "⚠️ CPF já registado ou erro nos dados."
                        }
                    } catch (e: Exception) {
                        mensagem = "❌ Erro de conexão com o banco."
                    } finally { isLoading = false }
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BazaAccent),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
            else Text("Finalizar Cadastro", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// 3. PÁGINA HOME (Agora Sincronizada com o Banco!)
// ==========================================
@Composable
fun EcraHome(navController: NavController) {
    var isLoadingSaldo by remember { mutableStateOf(true) }

    // Vai buscar o saldo real ao Spring Boot quando a página abre
    LaunchedEffect(Unit) {
        try {
            val respostaConta = RedeBazaBank.api.buscarConta("11111111-1111-1111-1111-111111111111")
            saldoGlobal = respostaConta.saldo
        } catch (e: Exception) {
            println("Erro ao buscar saldo: ${e.message}")
        } finally {
            isLoadingSaldo = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

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
                    Text("Allan Silva", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BazaDark)
                }
            }
            IconButton(onClick = { navController.navigate("login") }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Sair", tint = BazaDark)
            }
        }

        // Cartão de Saldo
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(200.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF222222), Color.Black)))
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Saldo Disponível", color = Color.LightGray, fontSize = 14.sp)
                        Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = Color.LightGray)
                    }

                    if (isLoadingSaldo) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    } else {
                        Text("R$ ${String.format("%.2f", saldoGlobal)}", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text("Ações Rápidas", modifier = Modifier.padding(horizontal = 24.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BazaDark)
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(icon = Icons.AutoMirrored.Filled.Send, label = "Área PIX", onClick = { navController.navigate("transferencia") })
            // O botão de Cartões agora vai para o EXTRATO
            QuickActionButton(icon = Icons.Outlined.CreditCard, label = "Extrato", onClick = { navController.navigate("extrato") })
            QuickActionButton(icon = Icons.Outlined.Home, label = "Investir", onClick = { })
            QuickActionButton(icon = Icons.Default.Person, label = "Perfil", onClick = { })
        }
    }
}

@Composable
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = BazaDark, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BazaDark)
    }
}

// ==========================================
// 4. PÁGINA DE TRANSFERÊNCIA
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcraTransferencia(navController: NavController) {
    var valorInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var mensagemFeedback by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

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

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {

            Text("Quanto deseja transferir?", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            TextField(
                value = valorInput,
                onValueChange = { valorInput = it },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = BazaDark),
                placeholder = { Text("R$ 0.00", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.LightGray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Seu saldo: R$ ${String.format("%.2f", saldoGlobal)}", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))

            BazaTextField(value = "22222222-2222-2222-2222-222222222222", onValueChange = {}, label = "Chave de Destino", readOnly = true)

            Spacer(modifier = Modifier.weight(1f))

            if (mensagemFeedback.isNotEmpty()) {
                Text(
                    text = mensagemFeedback,
                    color = if (mensagemFeedback.contains("✅")) Color(0xFF059669) else Color(0xFFDC2626),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    val valor = valorInput.toDoubleOrNull()
                    if (valor != null && valor > 0 && valor <= saldoGlobal) {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val pedido = TransferenciaRequest("11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222", valor)
                                val resposta = RedeBazaBank.api.transferir(pedido)
                                if (resposta.status == "SUCESSO") {
                                    saldoGlobal -= valor
                                    mensagemFeedback = "✅ PIX Enviado!"
                                    delay(1000)
                                    navController.popBackStack() // Volta para a Home
                                }
                            } catch (e: Exception) {
                                mensagemFeedback = "❌ Erro de rede"
                            } finally { isLoading = false }
                        }
                    } else {
                        mensagemFeedback = "⚠️ Valor inválido."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BazaAccent),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp)) else Text("Confirmar Transferência", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 5. PÁGINA DE EXTRATO (LazyColumn de Alta Performance)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcraExtrato(navController: NavController) {
    var transacoes by remember { mutableStateOf<List<TransacaoExtrato>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val minhaContaId = "11111111-1111-1111-1111-111111111111"

    LaunchedEffect(Unit) {
        try {
            transacoes = RedeBazaBank.api.buscarExtrato(minhaContaId)
        } catch (e: Exception) {
            println("Erro ao carregar extrato: ${e.message}")
        } finally {
            isLoading = false
        }
    }

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
            // A Mágica da Performance: LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(transacoes) { transacao ->
                    val isSaida = transacao.contaOrigemId == minhaContaId

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
// PREVIEWS
// ==========================================
@Preview(showBackground = true, showSystemUi = true, name = "1. Login")
@Composable
fun LoginPreview() { BazaBankAppTheme { EcraLogin(rememberNavController()) } }

@Preview(showBackground = true, showSystemUi = true, name = "2. Registro")
@Composable
fun RegistroPreview() { BazaBankAppTheme { EcraRegistro(rememberNavController()) } }

@Preview(showBackground = true, showSystemUi = true, name = "3. Home")
@Composable
fun HomePreview() { BazaBankAppTheme { EcraHome(rememberNavController()) } }

@Preview(showBackground = true, showSystemUi = true, name = "4. Transferência")
@Composable
fun TransferenciaPreview() { BazaBankAppTheme { EcraTransferencia(rememberNavController()) } }

@Preview(showBackground = true, showSystemUi = true, name = "5. Extrato")
@Composable
fun ExtratoPreview() { BazaBankAppTheme { EcraExtrato(rememberNavController()) } }