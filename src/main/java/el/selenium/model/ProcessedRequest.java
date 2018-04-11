package el.selenium.model;

import java.time.LocalDateTime;

public class ProcessedRequest {
    private String originalURL;
    private String finalURL;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long latencyBySeconds;

    public ProcessedRequest(String originalURL) {
        this.originalURL = originalURL;
    }

    public ProcessedRequest(String originalURL, LocalDateTime startTime) {
        this.originalURL = originalURL;
        this.startTime = startTime;
    }

    public void setFinalURL(String finalURL) {
        this.finalURL = finalURL;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setLatencyBySeconds(Long requestTime) {
        this.latencyBySeconds = requestTime;
    }

    public String getOriginalURL() {
        return originalURL;
    }

    public String getFinalURL() {
        return finalURL;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Long getLatencyBySeconds() {
        return latencyBySeconds;
    }

    @Override
    public String toString() {
        return "RequestProcessData{" +
                "originalURL='" + originalURL + '\'' +
                ", finalURL='" + finalURL + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", requestTime=" + latencyBySeconds +
                '}';
    }
}
