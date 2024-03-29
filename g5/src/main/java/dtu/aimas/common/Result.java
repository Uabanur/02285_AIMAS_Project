// Borrowed from repo: https://github.com/MrKloan/result-type

package dtu.aimas.common;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import dtu.aimas.errors.AggregateError;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.Collection;

public interface Result<T> {
    static <T> Result<T> ok(final T value) {
        requireNonNull(value, "The value of a Result cannot be null");
        return new Ok<>(value);
    }

    static <T> Result<T> empty(){
        return Result.error(new Exception("Empty result"));
    }

    static <T, E extends Throwable> Result<T> error(final E throwable) {
        requireNonNull(throwable, "The error of a Result cannot be null");
        return new Error<>(throwable);
    }

    static <T, T2> Result<T> passError(final Result<T2> result) {
        requireNonNull(result.getError(), "The error of a Result cannot be null");
        return Result.error(result.getError());
    }

    static <T> Result<T> of(final Supplier<T> supplier) {
        requireNonNull(supplier, "The value supplier cannot be null");

        try {
            return ok(supplier.get());
        } catch (final Exception error) {
            return error(error);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> Result<T> of(final Optional<T> optional) {
        requireNonNull(optional, "The optional value cannot be null");

        return optional
                .map(Result::ok)
                .orElseGet(() -> error(new NoSuchElementException("No value present when unwrapping the optional")));
    }

    static <T> Result<T> ofNullable(final T value) {
        return ofNullable(value, () -> new NullPointerException("The result was initialized with a null value"));
    }

    static <T> Result<T> ofNullable(final T value, final Supplier<? extends Throwable> errorSupplier) {
        requireNonNull(errorSupplier, "The error supplier cannot be null");

        return nonNull(value)
                ? ok(value)
                : error(errorSupplier.get());
    }

    static <T> Result<List<T>> collapse(final Collection<Result<T>> collection) {
        if(collection.stream().allMatch(Result::isOk))
            return Result.ok(collection.stream()
                .map(Result::get)
                .collect(Collectors.toList())
            );

        var errors = collection.stream()
            .filter(Result::isError)
            .map(Result::getError)
            .toArray(Throwable[]::new);
        return Result.error(new AggregateError(errors));
    }

    boolean isOk();

    void ifOk(final Consumer<T> consumer);

    boolean isError();

    void ifError(final Consumer<Throwable> consumer);

    Result<T> switchIfError(final Function<Throwable, Result<T>> fallbackMethod);

    <U> Result<U> map(final Function<? super T, ? extends U> mapper);

    <U> Result<U> flatMap(final Function<? super T, Result<U>> mapper);

    Result<T> mapError(final Function<Throwable, ? extends Throwable> mapper);

    T get();

    T getOrElse(final Supplier<T> supplier);

    Throwable getError();

    String getErrorMessageOrEmpty();
}
