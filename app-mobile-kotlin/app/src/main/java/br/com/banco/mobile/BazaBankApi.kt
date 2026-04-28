package br.com.banco.mobile

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.TimeUnit

// ==========================================
// 1. SESSÃO E SEGURANÇA (Gestão Reativa)
// ==========================================
object SessaoApp {
    var tokenJwt: String = ""
    var contaIdAtual: String = "11111111-1111-1111-1111-111111111111"

    // Canal de comunicação Sénior: Avisa a UI quando o token morre
    val eventoSessaoExpirada = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)

    fun encerrarSessao() {
        tokenJwt = ""
        eventoSessaoExpirada.tryEmit(true)
    }
}

// ==========================================
// 2. DTOs (Data Transfer Objects)
// ==========================================
data class AuthRequest(val cpf: String, val senha: String)
data class TokenResponse(val token: String, val cpf: String)
data class TransferenciaRequest(val contaOrigemId: String, val contaDestinoId: String, val valor: Double)
data class TransacaoResponse(val transacaoId: String, val status: String)
data class ContaResponse(val id: String, val titular: String, val saldo: Double)
data class TransacaoExtrato(val id: String, val contaOrigemId: String, val contaDestinoId: String, val valor: Double, val dataCriacao: String)

// ==========================================
// 3. INTERFACE DA API
// ==========================================
interface BazaBankApiService {
    @POST("/api/transferencias")
    suspend fun transferir(
        @Header("Idempotency-Key") idempotencyKey: String, // <-- NOVO CÓDIGO
        @Body request: TransferenciaRequest
    ): TransacaoResponse

    @POST("/api/auth/registrar")
    suspend fun registrar(@Body request: AuthRequest): Response<Void>

    @POST("/api/auth/login")
    suspend fun login(@Body request: AuthRequest): TokenResponse

    @POST("/api/transferencias")
    suspend fun transferir(@Body request: TransferenciaRequest): TransacaoResponse

    @GET("/api/contas/{id}")
    suspend fun buscarConta(@Path("id") id: String): ContaResponse

    @GET("/api/contas/{id}/extrato")
    suspend fun buscarExtrato(@Path("id") id: String): List<TransacaoExtrato>
}

// ==========================================
// 4. CLIENTE RETROFIT CONFIGURADO
// ==========================================
object RedeBazaBank {
    private const val BASE_URL = "http://10.0.2.2"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()

            // 1. Cola o Crachá (Token) se existir
            val requestBuilder = original.newBuilder()
            if (SessaoApp.tokenJwt.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer ${SessaoApp.tokenJwt}")
            }

            // 2. Faz o pedido ao Spring Boot
            val response = chain.proceed(requestBuilder.build())

            // 3. A CORREÇÃO ESTÁ AQUI: Usar .code() com parênteses!
            if (response.code() == 401 && SessaoApp.tokenJwt.isNotEmpty()) {
                SessaoApp.encerrarSessao()
            }

            response
        }.build()

    val api: BazaBankApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BazaBankApiService::class.java)
    }
}
