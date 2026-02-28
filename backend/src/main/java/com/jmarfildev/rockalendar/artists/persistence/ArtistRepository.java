package com.jmarfildev.rockalendar.artists.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jmarfildev.rockalendar.artists.domain.Artist;

/**
 * @author jmarfil
 *
 */
public interface ArtistRepository extends JpaRepository<Artist, UUID> {
    Optional<Artist> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /**
     * Busca artistas por nombre o slug.
     *
     * @param qRaw nombre normalizado
     * @param qSlug slug normalizado
     * @param pageable
     * @return lista de artistas o vacía
     */
    @Query("""
           SELECT a FROM Artist a
           WHERE lower(a.name) LIKE lower(concat('%', :qRaw, '%'))
               OR a.slug LIKE concat('%', :qSlug, '%')
           ORDER BY a.name asc
           """)
    List<Artist> findForAutocomplete(@Param("qRaw") String qRaw, @Param("qSlug") String qSlug, Pageable pageable);
}
