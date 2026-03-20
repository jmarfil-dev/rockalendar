package com.jmarfildev.rockalendar.artists.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jmarfildev.rockalendar.artists.domain.Artist;
import com.jmarfildev.rockalendar.common.dto.ComboItemDto;

/**
 * @author jmarfil
 *
 */
public interface ArtistRepository extends JpaRepository<Artist, UUID> {
    Optional<Artist> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM event_artists WHERE artist_id = :id)", nativeQuery = true)
    boolean hasEvents(@Param("id") UUID id);

    @Query(value = """
            SELECT * FROM artists
            WHERE NOT EXISTS (SELECT 1 FROM event_artists WHERE artist_id = artists.id)
              AND (:query = '' OR lower(name) LIKE lower(concat('%', :query, '%')))
            ORDER BY name ASC
            """,
            countQuery = """
            SELECT COUNT(*) FROM artists
            WHERE NOT EXISTS (SELECT 1 FROM event_artists WHERE artist_id = artists.id)
              AND (:query = '' OR lower(name) LIKE lower(concat('%', :query, '%')))
            """,
            nativeQuery = true)
    Page<Artist> findOrphans(@Param("query") String query, Pageable pageable);

    /**
     * Busca artistas por nombre o slug.
     *
     * @param qRaw nombre normalizado
     * @param qSlug slug normalizado
     * @param pageable
     * @return lista de artistas o vacía
     */
    @Query("""
           SELECT new com.jmarfildev.rockalendar.common.dto.ComboItemDto(a.id, a.name)
           FROM Artist a
           WHERE lower(a.name) LIKE lower(concat('%', :qRaw, '%'))
               OR a.slug LIKE concat('%', :qSlug, '%')
           ORDER BY a.name asc
           """)
    List<ComboItemDto> findForAutocomplete(@Param("qRaw") String qRaw, @Param("qSlug") String qSlug, Pageable pageable);
}
