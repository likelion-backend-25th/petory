package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.MissingPetListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MissingPetMapper {

    List<MissingPetListResponse> findByCursor(
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );

    Long countAll();
}