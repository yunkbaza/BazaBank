package br.com.banco.mobile

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// ==========================================
// 0. SESSÃO (O "Bolso" que guarda o Crachá do utilizador)
// ==========================================
object SessaoApp {
    var tokenJwt: String = ""
}

// ==========================================
// 1. DATA CLASSES (O formato dos dados)
// ==========================================

// NOVOS: Para o Login e Registo
data class AuthRequest(val cpf: String, val senha: String)
data class TokenResponse(val token: String, val cpf: String)

// ANTIGOS: Para as operações bancárias
data class TransferenciaRequest(
    val contaOrigemId: String,
    val contaDestinoId: String,
    val valor: Double
)

data class TransacaoResponse(
    val transacaoId: String,
    val status: String
)

data class ContaResponse(
    val id: String,
    val titular: String,
    val saldo: Double
)

data class TransacaoExtrato(
    val id: String,
    val contaOrigemId: String,
    val contaDestinoId: String,
    val valor: Double,
    val dataCriacao: String
)

// ==========================================
// 2. A INTERFACE DA API (Os Endpoints do Spring Boot)
// ==========================================
interface BazaBankApiService {

    // NOVAS ROTAS: Livres (Não exigem Token)
    @POST("/api/auth/registrar")
    suspend fun registrar(@Body request: AuthRequest): retrofit2.Response<String>

    @POST("/api/auth/login")
    suspend fun login(@Body request: AuthRequest): TokenResponse

    // ROTAS ANTIGAS: Agora protegidas pelo Spring Security!
    @POST("/api/transferencias")
    suspend fun transferir(@Body request: TransferenciaRequest): TransacaoResponse

    @GET("/api/contas/{id}")
    suspend fun buscarConta(@Path("id") id: String): ContaResponse

    @GET("/api/contas/{id}/extrato")
    suspend fun buscarExtrato(@Path("id") id: String): List<TransacaoExtrato>
}

// ==========================================
// 3. A CONFIGURAÇÃO DA LIGAÇÃO (Motor Retrofit)
// ==========================================
object RedeBazaBank {
    // 10.0.2.2 é o endereço que o emulador usa para chegar ao localhost do Windows
    private const val BASE_URL = "http://10.0.2.2:8080"

    // MÁGICA SÊNIOR: O Interceptor
    // Antes de qualquer pedido sair do telemóvel, ele passa por aqui.
    // Se existir um Token guardado, ele "cola" no cabeçalho do pedido automaticamente!
    private val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val original = chain.request()
        if (SessaoApp.tokenJwt.isNotEmpty()) {
            val requestComToken = original.newBuilder()
                .header("Authorization", "Bearer ${SessaoApp.tokenJwt}")
                .build()
            chain.proceed(requestComToken)
        } else {
            chain.proceed(original)
        }
    }.build()

    val api: BazaBankApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient) // Ligamos o nosso motor customizado aqui!
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BazaBankApiService::class.java)
    }
}