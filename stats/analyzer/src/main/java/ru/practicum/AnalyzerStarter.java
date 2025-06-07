package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.processor.EventSimilarityProcessor;
import ru.practicum.processor.UserActionProcessor;


@Slf4j
@Component
public class AnalyzerStarter implements CommandLineRunner {
    private final EventSimilarityProcessor eventSimilarityProcessor;
    private final UserActionProcessor userActionProcessor;

    public AnalyzerStarter(EventSimilarityProcessor eventSimilarityProcessor, UserActionProcessor userActionProcessor) {
        this.eventSimilarityProcessor = eventSimilarityProcessor;
        this.userActionProcessor = userActionProcessor;
    }

    @Override
    public void run(String... args) throws Exception {
        Thread eventSimilarityThread = new Thread(eventSimilarityProcessor);
        eventSimilarityThread.setName("eventSimilarityHandlerThread");
        log.info("{}: Запускаем EventSimilarityProcessor в отдельном потоке", AnalyzerStarter.class.getSimpleName());
        eventSimilarityThread.start();

        log.info("{}: Запускаем UserActionProcessor", AnalyzerStarter.class.getSimpleName());
        userActionProcessor.start();
    }
}