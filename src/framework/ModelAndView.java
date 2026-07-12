package framework;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String view;
    private final Map<String, Object> model = new HashMap<>();

    public ModelAndView() {}

    public ModelAndView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void setAttribut(String name, Object value) {
        this.model.put(name, value);
    }

    public Map<String, Object> getModel() {
        return model;
    }
}