package br.com.banco.mobile // Verifique se este é exatamente o seu package

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 1. As Data Classes do Kotlin (Equivalentes aos "records" do Java)
data class TransferenciaRequest(
    val contaOrigemId: String,
    val contaDestinoId: String,
    val valor: Double
)

data class TransacaoResponse(
    val id: String,
    val status: String
)

// 2. A Interface da nossa API
interface BazaBankApiService {
    @POST("/api/transferencias")
    suspend fun transferir(@Body request: TransferenciaRequest): TransacaoResponse
}

// 3. A Configuração da Ligação (Apontando para o Mac)
object RedeBazaBank {
    // 10.0.2.2 é o endereço que o emulador usa para chegar ao localhost do Mac
    private const val BASE_URL = "http://10.0.2.2:8080"

    val api: BazaBankApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BazaBankApiService::class.java)
    }
}