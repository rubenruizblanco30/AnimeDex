package com.example.animedex.ui.theme

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// Tablas principales

@Entity(tableName = "anime", indices = [Index("rank"), Index("type")])
data class AnimeEntity(
    @PrimaryKey @ColumnInfo(name = "anime_id") val animeId: Int,
    val title: String,
    val score: Double,
    val rank: Int,
    val popularity: Int,
    val members: Int,
    val synopsis: String?,
    @ColumnInfo(name = "start_date") val startDate: String?,
    @ColumnInfo(name = "end_date") val endDate: String?,
    val type: String,
    val episodes: Int?,
    @ColumnInfo(name = "image_url") val imageUrl: String
)

@Entity(tableName = "genre")
data class GenreEntity(
    @PrimaryKey @ColumnInfo(name = "genre_id") val genreId: Int,
    val name: String,
    val category: String
)

@Entity(tableName = "company")
data class CompanyEntity(
    @PrimaryKey @ColumnInfo(name = "company_id") val companyId: Int,
    val name: String?,
    @ColumnInfo(name = "company_type") val companyType: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?
)

@Entity(tableName = "character")
data class CharacterEntity(
    @PrimaryKey @ColumnInfo(name = "character_id") val characterId: Int,
    val name: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?
)

@Entity(tableName = "person", indices = [Index("person_type")])
data class PersonEntity(
    @PrimaryKey @ColumnInfo(name = "person_id") val personId: Int,
    val name: String?,
    @ColumnInfo(name = "person_type") val personType: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?
)

// Tablas intermedias

@Entity(
    tableName = "anime_genre",
    primaryKeys = ["anime_id", "genre_id"],
    foreignKeys = [
        ForeignKey(AnimeEntity::class, ["anime_id"], ["anime_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(GenreEntity::class, ["genre_id"], ["genre_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("genre_id")]
)
data class AnimeGenreCrossRef(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "genre_id") val genreId: Int
)

@Entity(
    tableName = "anime_company",
    primaryKeys = ["anime_id", "company_id"],
    foreignKeys = [
        ForeignKey(AnimeEntity::class, ["anime_id"], ["anime_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(CompanyEntity::class, ["company_id"], ["company_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("company_id")]
)
data class AnimeCompany(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "company_id") val companyId: Int,
    val role: String
)

@Entity(
    tableName = "anime_character",
    primaryKeys = ["anime_id", "character_id"],
    foreignKeys = [
        ForeignKey(AnimeEntity::class, ["anime_id"], ["anime_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(CharacterEntity::class, ["character_id"], ["character_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("character_id")]
)
data class AnimeCharacter(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "character_id") val characterId: Int,
    val role: String
)

@Entity(
    tableName = "anime_staff",
    primaryKeys = ["anime_id", "person_id"],
    foreignKeys = [
        ForeignKey(AnimeEntity::class, ["anime_id"], ["anime_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(PersonEntity::class, ["person_id"], ["person_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("person_id")]
)
data class AnimeStaff(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "person_id") val personId: Int,
    val role: String
)

@Entity(
    tableName = "character_voice_actor",
    primaryKeys = ["character_id", "person_id", "language"],
    foreignKeys = [
        ForeignKey(CharacterEntity::class, ["character_id"], ["character_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(PersonEntity::class, ["person_id"], ["person_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("person_id")]
)
data class CharacterVoiceActor(
    @ColumnInfo(name = "character_id") val characterId: Int,
    @ColumnInfo(name = "person_id") val personId: Int,
    val language: String
)

// Resultados de las consultas

data class AnimeWithGenres(
    @Embedded val anime: AnimeEntity,
    @Relation(
        parentColumn = "anime_id",
        entityColumn = "genre_id",
        associateBy = Junction(
            value = AnimeGenreCrossRef::class,
            parentColumn = "anime_id",
            entityColumn = "genre_id"
        )
    )
    val genres: List<GenreEntity>
)

data class EstudioConTotal(
    val nombre: String?,
    val total: Int
)

// Consultas

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime ORDER BY rank LIMIT :limit")
    fun topAnime(limit: Int): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE anime_id = :id")
    fun byId(id: Int): Flow<AnimeEntity?>

    @Transaction
    @Query("SELECT * FROM anime WHERE anime_id = :id")
    fun detail(id: Int): Flow<AnimeWithGenres?>

    @Query("""
        SELECT * FROM anime
        WHERE title LIKE '%' || :text || '%'
        ORDER BY rank
    """)
    fun buscarTitulo(text: String): Flow<List<AnimeEntity>>

    @Query("""
        SELECT anime.*
        FROM anime
        JOIN anime_genre ON anime.anime_id = anime_genre.anime_id
        JOIN genre ON anime_genre.genre_id = genre.genre_id
        WHERE genre.name = :genero
        ORDER BY anime.rank
    """)
    fun buscargenero(genero: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE score >= :minimo ORDER BY rank")
    fun puntuacionMaxima(minimo: Double): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE type = :tipo ORDER BY rank")
    fun buscarTipo(tipo: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE episodes IS NULL ORDER BY rank")
    fun sinEpisodios(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime ORDER BY members DESC LIMIT 3")
    fun masMiembros(): Flow<List<AnimeEntity>>

    @Query("""
        SELECT company.name AS nombre, COUNT(*) AS total
        FROM anime_company
        JOIN company ON company.company_id = anime_company.company_id
        WHERE anime_company.role = 'Studio'
        GROUP BY company.company_id, company.name
        ORDER BY total DESC
        LIMIT 1
    """)
    fun estudioConMasAnime(): Flow<EstudioConTotal?>
}

// Base de datos

@Database(
    entities = [
        AnimeEntity::class,
        GenreEntity::class,
        CompanyEntity::class,
        CharacterEntity::class,
        PersonEntity::class,
        AnimeGenreCrossRef::class,
        AnimeCompany::class,
        AnimeCharacter::class,
        AnimeStaff::class,
        CharacterVoiceActor::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AnimeDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao

    companion object {
        private var instancia: AnimeDatabase? = null

        @Synchronized
        fun get(context: Context): AnimeDatabase {
            val baseActual = instancia
            if (baseActual != null) return baseActual

            val baseNueva = Room.databaseBuilder(
                context.applicationContext,
                AnimeDatabase::class.java,
                "anime.db"
            )
                .createFromAsset("databases/anime.db")
                .build()

            instancia = baseNueva
            return baseNueva
        }
    }
}