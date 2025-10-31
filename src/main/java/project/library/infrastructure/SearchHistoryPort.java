package project.library.infrastructure;

import java.util.List;

public interface SearchHistoryPort {

    void addHistory(String sessionId, String book);


    List<String> getHistory(String sessionId);
}
