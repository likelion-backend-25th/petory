package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.MissingPetListPageResponse;

public interface MissingPetService {

    MissingPetListPageResponse getMissingPetList(
            Long cursor,
            int size
    );
}