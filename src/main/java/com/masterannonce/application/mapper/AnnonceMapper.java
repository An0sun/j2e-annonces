package com.masterannonce.application.mapper;

import com.masterannonce.application.dto.AnnonceCreateDTO;
import com.masterannonce.application.dto.AnnonceDTO;
import com.masterannonce.application.dto.AnnoncePatchDTO;
import com.masterannonce.application.dto.AnnonceUpdateDTO;
import com.masterannonce.domain.model.Annonce;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper MapStruct pour Annonce ↔ DTO.
 * Pas de mapping "à la main" : tout est généré par MapStruct.
 */
@Mapper(componentModel = "spring")
public interface AnnonceMapper {

    // ===== Entity → DTO =====

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.username", target = "authorUsername")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.label", target = "categoryLabel")
    @Mapping(source = "status", target = "status")
    AnnonceDTO toDTO(Annonce entity);

    List<AnnonceDTO> toDTOList(List<Annonce> entities);

    // ===== DTO → Entity (création) =====

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "version", ignore = true)
    Annonce toEntity(AnnonceCreateDTO dto);

    // ===== DTO → Entity (mise à jour complète) =====

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    Annonce toEntity(AnnonceUpdateDTO dto);

    // ===== Mise à jour partielle (PATCH) via @MappingTarget =====

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "version", ignore = true)
    void patchEntity(AnnoncePatchDTO dto, @MappingTarget Annonce entity);

    // ===== Conversion AnnonceStatus enum → String =====

    default String statusToString(com.masterannonce.domain.model.AnnonceStatus status) {
        return status != null ? status.name() : null;
    }
}
