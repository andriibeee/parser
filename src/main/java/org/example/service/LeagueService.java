package org.example.service;

import org.example.exception.CannotLoadDataException;
import org.example.helper.DataLoadHelper;
import org.example.model.result.EventResult;
import org.example.model.league.LeagueInfo;
import org.example.model.sports.League;
import org.example.model.sports.Region;
import org.example.helper.MappingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class LeagueService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(LeagueService.class);
    private static final String LEAGUE_CHANGES_URL_TEMPLATE = "%schanges/all?ctag=ru-UA&vtag=9c2cd386-31e1-4ce9-a140-28e9b63a9300&league_id=%d&hideClosed=true&flags=reg,mm2,rrc,nodup,urlv2";

    private final EventService eventService;

    public LeagueService() {
        this.eventService = new EventService();
    }

    public LeagueService(EventService eventService) {
        this.eventService = eventService;
    }

    public List<EventResult> getPrintEventsInRegionTopLeagueResults(List<Region> regions) {
        return getTopLeaguesFromRegions(regions)
                .stream()
                .map(this::extractFirstEventData)
                .map(this::getEventDetails)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private LinkedHashMap<String, Object> extractFirstEventData(LeagueInfo leagueInfo) {
        List<LinkedHashMap<String, Object>> dataEventList =
                (List<LinkedHashMap<String, Object>>) leagueInfo.getAdditionalProperties().get("data");
        return dataEventList.get(0);
    }

    private Optional<EventResult> getEventDetails(LinkedHashMap<String, Object> dataEvent) {
        Long eventId = ((Number) dataEvent.get("id")).longValue();
        return eventService.getSportEventWithTopLeaguePrintResult(eventId);
    }

    private List<LeagueInfo> getTopLeaguesFromRegions(List<Region> regions) {
        return regions.stream()
                .flatMap(region -> region.getLeagues().stream()
                        .filter(League::getTop)
                        .map(league -> fetchLeagueInfo(league.getId())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<LeagueInfo> fetchLeagueInfo(Long leagueId) {
        try {
            String url = String.format(LEAGUE_CHANGES_URL_TEMPLATE, BASE_URL, leagueId);
            String json = DataLoadHelper.loadData(url);
            return Optional.of(MappingHelper.toObject(json, LeagueInfo.class));
        } catch (IOException | CannotLoadDataException e) {
            logger.error("Error fetching league info for league ID {}: {}", leagueId, e.getMessage());
            return Optional.empty();
        }
    }
}