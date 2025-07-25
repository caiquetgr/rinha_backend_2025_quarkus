package br.com.caiqueborges.rinha.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record ProcessorsHealthCheck(
        boolean defaultFailing,
        int defaultMinResponseTime,
        boolean fallbackFailing,
        int fallbackMinResponseTime
) {
    public static ProcessorsHealthCheck buildEmpty() {
        return new ProcessorsHealthCheck(true, 100, true, 100);
    }

    @JsonIgnore
    public boolean isDefaultOnlineAndFaster() {
        return !defaultFailing() && (defaultMinResponseTime() <= fallbackMinResponseTime());
    }

    @JsonIgnore
    public boolean areBothFailing() {
        return defaultFailing && fallbackFailing;
    }

    @JsonIgnore
    public boolean isDefaultFailingButFallbackNotFailing() {
        return defaultFailing() && !fallbackFailing();
    }

    @JsonIgnore
    public boolean isFallbackAtLeast300msFaster() {
        return !fallbackFailing() && defaultMinResponseTime() - fallbackMinResponseTime() >= 300;
    }
}
