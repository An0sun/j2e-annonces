package org.j2e.rest.mapper;

import org.j2e.bean.Annonce;
import org.j2e.rest.dto.AnnonceDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utilitaire pour convertir Entity ↔ DTO.
 * Utilise le pattern Builder de AnnonceDTO.
 */
public class AnnonceMapper {

    private AnnonceMapper() {
    } // Classe utilitaire, pas d'instanciation

    /**
     * Convertir une entité Annonce en DTO de réponse.
     */
    public static AnnonceDTO toDTO(Annonce entity) {
        if (entity == null)
            return null;

        AnnonceDTO.Builder builder = AnnonceDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .adress(entity.getAdress())
                .mail(entity.getMail())
                .date(entity.getDate())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .version(entity.getVersion());

        if (entity.getAuthor() != null) {
            builder.authorUsername(entity.getAuthor().getUsername())
                    .authorId(entity.getAuthor().getId());
        }

        if (entity.getCategory() != null) {
            builder.categoryLabel(entity.getCategory().getLabel())
                    .categoryId(entity.getCategory().getId());
        }

        return builder.build();
    }

    /**
     * Convertir une liste d'entités en liste de DTOs.
     */
    public static List<AnnonceDTO> toDTOList(List<Annonce> entities) {
        return entities.stream()
                .map(AnnonceMapper::toDTO)
                .collect(Collectors.toList());
    }
}
