package com.commercetools.sync.commons.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class StreamUtils {

    /**
     * Applies the supplied {@code mapper} function on every non null element in the supplied {@link Stream} of
     * {@code elements}.
     *
     * @param elements the stream of elements.
     * @param mapper   the mapper function to apply on every element.
     * @param <T>      the type of the elements in the stream.
     * @param <S>      the resulting type after applying the mapper function on an element.
     * @return a stream of the resulting mapped elements.
     * TODO: CHANGE elements to nullable
     */
    @Nonnull
    public static <T, S> Stream<S> filterNullAndMap(
        @Nonnull final Stream<T> elements,
        @Nonnull final Function<T, S> mapper) {

        return elements.filter(Objects::nonNull).map(mapper);
    }

    public static <T> Stream<T> emptyIfNull(@Nullable final Stream<T> stream) {
        return stream == null ? Stream.empty() : stream;
    }

    @Nonnull
    public static <T> Stream<T> unpackPresentOptionals(@Nullable final Stream<Optional<T>> stream) {

        return emptyIfNull(stream).filter(Optional::isPresent)
                                  .map(Optional::get);
    }

    @Nonnull
    public static <T> Set<T> unpackPresentOptionalsToSet(@Nullable final Stream<Optional<T>> stream) {
        return unpackPresentOptionalsToCollection(stream, toSet());
    }

    @Nonnull
    public static <T> List<T> unpackPresentOptionalsToList(@Nullable final Stream<Optional<T>> stream) {
        return unpackPresentOptionalsToCollection(stream, toList());
    }

    @Nonnull
    public static <T, S extends Collection<T>> S unpackPresentOptionalsToCollection(
        @Nullable final Stream<Optional<T>> stream,
        @Nonnull final Collector<T, ?, S> collector) {

        return emptyIfNull(stream).filter(Optional::isPresent)
                                  .map(Optional::get)
                                  .collect(collector);
    }



    private StreamUtils() {
    }
}
