package experimentation;

public final class TimedResult<T> {
    private final T result;
    private final double duration;

    public T getResult() {
        return result;
    }

    double getDuration() {
        return duration / 1_000_000_000.0;
    }

    private TimedResult(T result, double duration) {
        this.result = result;
        this.duration = duration;
    }

    public static <T> TimedResult<T> time(java.util.function.Supplier<T> supplier) {
        var now = System.nanoTime();
        T result = supplier.get();
        var duration = System.nanoTime() - now;

        return new TimedResult<>(result, duration);
    }
}