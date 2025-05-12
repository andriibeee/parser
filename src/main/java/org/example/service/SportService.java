package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.exception.CannotLoadDataException;
import org.example.exception.UnknownSportError;
import org.example.helper.DataLoadHelper;
import org.example.model.SportKind;
import org.example.model.sports.Sports;
import org.example.helper.MappingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SportService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(SportService.class);
    private static final String SPORTS_URL_TEMPLATE = "%ssports?ctag=ru-UA&flags=urlv2";

    private final List<Sports> sportsList;

    public SportService() {
        this.sportsList = initSportsList();
    }

    private List<Sports> initSportsList() {
        try {
            String url = String.format(SPORTS_URL_TEMPLATE, BASE_URL);
            String json = DataLoadHelper.loadData(url);
            return MappingHelper.toObject(json, new TypeReference<>() {
            });
        } catch (IOException | CannotLoadDataException e) {
            logger.error("Error initializing sports list: {}", e.getMessage());
            return List.of();
        }
    }

    public List<Sports> getFilteredSportList() {
        return getSportsFromEnum();
    }

    private List<Sports> getSportsFromEnum() {
        return List.of(SportKind.values()).stream()
                .map(this::findSportBySportsEnum)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<Sports> findSportBySportsEnum(SportKind sportKind) {
        try {
            return Optional.of(findSportByFamily(sportKind.getValue()));
        } catch (UnknownSportError e) {
            logger.warn("Sport not found for family: {}", sportKind.getValue());
            return Optional.empty();
        }
    }

    private Sports findSportByFamily(String family) throws UnknownSportError {
        return sportsList.stream()
                .filter(sport -> sport.getFamily().equals(family))
                .findFirst()
                .orElseThrow(() -> new UnknownSportError(family));
    }
}