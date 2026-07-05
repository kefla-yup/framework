package framework;

import java.util.Objects;

public class MappingKey {
    private final String url;
    private final String methodType;

    public MappingKey(String url, String methodType) {
        this.url = url;
        this.methodType = methodType.toUpperCase();
    }

    public String getUrl() { return url; }
    public String getMethodType() { return methodType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MappingKey that = (MappingKey) o;
        return Objects.equals(url, that.url) && 
               Objects.equals(methodType, that.methodType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, methodType);
    }
}