package br.com.banco.mobile

import android.content.Context
import androidx.room.*

// 1. A Tabela (Entity)
@Entity(tableName = "extrato_local")
data class TransacaoLocal(
    @PrimaryKey val id: String,
    val contaOrigemId: String,
    val contaDestinoId: String,
    val valor: Double,
    val dataCriacao: String,
    val donoDaContaId: String // Para não misturar extratos se outra pessoa fizer login no mesmo telemóvel
)

// 2. Os Comandos SQL (DAO)
@Dao
@JvmSuppressWildcards
interface ExtratoDao {
    @Query("SELECT * FROM extrato_local WHERE donoDaContaId = :donoId ORDER BY dataCriacao DESC")
    suspend fun buscarExtratoOffline(donoId: String): List<TransacaoLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTransacoes(transacoes: List<TransacaoLocal>): List<Long>

    @Query("DELETE FROM extrato_local WHERE donoDaContaId = :donoId")
    suspend fun limparExtrato(donoId: String): Int
}

// 3. A Base de Dados
@Database(entities = [TransacaoLocal::class], version = 1, exportSchema = false)
abstract class BazaRoomDatabase : RoomDatabase() {
    abstract fun extratoDao(): ExtratoDao

    companion object {
        @Volatile
        private var INSTANCE: BazaRoomDatabase? = null

        fun getDatabase(context: Context): BazaRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BazaRoomDatabase::class.java,
                    "baza_bank_offline_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}