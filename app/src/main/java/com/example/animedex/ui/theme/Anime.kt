package com.example.animedex.ui.theme


import android.adservices.adid.AdId
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

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

@Entity(tableName = "company")
data class CompanyEntity(
    @PrimaryKey @ColumnInfo(name = "company_id") val companyId: Int,
    val name: String
)

@Entity(tableName = "character")
data class CharacterEntity(
    @PrimaryKey @ColumnInfo(name = "character_id") val characterId: Int,
    val name: String,
    @ColumnInfo(name = "image_url") val imageUrl: String?
)
@Entity(tableName = "genre")
data class GenreEntity(
    @PrimaryKey @ColumnInfo(name = "genre_id") val genreId: Int,
    val name:String,
    val category: String

)
@Entity(
    tableName = "anime_genre",
    primaryKeys = ["anime_id", "genre_id"],
    foreignKeys = [
        ForeignKey(
            entity = AnimeEntity::class,
            parentColumns = ["anime_id"],
            childColumns = ["anime_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GenreEntity::class,
            parentColumns = ["genre_id"],
            childColumns = ["genre_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("genre_id")]
)
data class AnimeGenreCrossRef(
    @ColumnInfo(name = "anime_id") val animeId: Int,
    @ColumnInfo(name = "genre_id") val genreId: Int
)
data class AnimeWithGenres(
    @Embedded val anime: AnimeEntity,
    @Relation(
        parentColumn = "anime_id",
        entityColumn = "genre_id",
        associateBy = Junction(
            value = AnimeGenreCrossRef::class,
            parentColumn = "anime:id",
            entityColumn = "genre_id"
        )
    )
    val genres: List<GenreEntity>
)
@Dao
interface AnimeDao{
    @Query("select * from anime order by rank Limit :limit")
    fun topAnime(limit: Int): Flow<List<AnimeEntity>>
    @Query("select * from anime where anime_id = :id")
    fun byId(id:Int): Flow<AnimeEntity?>

    @Transaction
    @Query("select * from anime where anime_id =:id")
    fun detail(id:Int): Flow<AnimeWithGenres?>

    @Query("""
        select* from anime
        where title like '%' || :text || '%'
        order by rank
    """)
    fun buscarTitulo(text:String): Flow<List<AnimeEntity>>

    @Query("""
        select anime.*
        from anime
        join anime_genre
        on anime.anime_id = anime_genre.anime_id
        join genre 
        on anime_genre.genre_id = genre.genre_id
        where genre.name = :genero
        order by anime.rank
    """)
    fun buscargenero(genero:String): Flow<List<AnimeEntity>>

    @Query("""
        select* from anime
        where score >= :minimo
        order by rank
    """)
    fun puntuacionMaxima(minimo: Double): Flow<List<AnimeEntity>>

    @Query("""
        select * from anime
        where type = :tipo
        order by rank
    """)
    fun buscarTipo(tipo:String): Flow<List<AnimeEntity>>

    @Query("""
        select * from anime
        where episodes is null
        order by rank
    """)
    fun sinEpisodios(): Flow<List<AnimeEntity>>

    @Query("select * from anime order by members desc limit 3")
    fun masMiembros(): Flow<List<AnimeEntity>>
}
