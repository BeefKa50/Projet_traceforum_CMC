package fr.traceforum.traceforum_app.data_classes;

import java.util.List;

public class Indicators {
    private List<String> indicators;

    public Indicators(){}

    public Indicators(List<String> indicators) {
        this.indicators = indicators;
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<String> indicators) {
        this.indicators = indicators;
    }
}
