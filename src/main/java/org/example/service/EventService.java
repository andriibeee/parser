package org.example.service;

import org.example.exception.CannotLoadDataException;
import org.example.helper.DataLoadHelper;
import org.example.model.event.Event;
import org.example.model.event.Market;
import org.example.model.result.EventResult;
import org.example.helper.MappingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class EventService extends BaseService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private static final String EVENT_URL_TEMPLATE = "%sevent/all?ctag=ru-UA&eventId=%d&flags=reg,mm2,rrc,nodup,urlv2,smg,outv2";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Optional<EventResult> getSportEventWithTopLeaguePrintResult(Long eventId) {
        try {
            String url = String.format(EVENT_URL_TEMPLATE, BASE_URL, eventId);
            String json = DataLoadHelper.loadData(url);
            Event event = MappingHelper.toObject(json, Event.class);

            return Optional.of(createSportEventResult(event, eventId));
        } catch (IOException | CannotLoadDataException e) {
            logger.error("Error retrieving sport event for ID {}: {}", eventId, e.getMessage());
            return Optional.empty();
        }
    }

    private EventResult createSportEventResult(Event event, Long eventId) {
        EventResult result = new EventResult(
                event.getLeague().getSport().getName(),
                event.getLeague().getRegion().getName(),
                event.getLeague().getName()
        );

        result.createEvent(event.getName(), formatDate(event.getKickoff()), eventId);

        processMarkets(event.getMarkets(), result);

        return result;
    }

    private void processMarkets(List<Market> markets, EventResult result) {
        markets.forEach(market ->
                market.getRunners().forEach(runner ->
                        result.addMarketWithRunnersValue(
                                market.getName(),
                                runner.getName(),
                                runner.getPriceStr(),
                                runner.getId()
                        )
                )
        );
    }

    private String formatDate(Long kickoff) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(kickoff),
                ZoneOffset.UTC
        );
        return dateTime.format(DATE_FORMATTER) + " UTC";
    }
}