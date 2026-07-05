package framework;

import java.lang.reflect.Method;

public class MappingValue {
    private final Class<?> controllerClass;
    private final Method method;

    public MappingValue(Class<?> controllerClass, Method method) {
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public Class<?> getControllerClass() { return controllerClass; }
    public Method getMethod() { return method; }
}