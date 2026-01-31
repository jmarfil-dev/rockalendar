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

    @Query("""
                select a from Artist a
                where lower(a.name) like lower(concat('%', :qRaw, '%'))
                   or a.slug like concat('%', :qSlug, '%')
                order by a.name asc
            """)
    List<Artist> findForAutocomplete(@Param("qRaw") String qRaw, @Param("qSlug") String qSlug, Pageable pageable);
}
