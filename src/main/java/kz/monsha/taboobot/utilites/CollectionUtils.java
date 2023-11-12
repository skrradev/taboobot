package kz.monsha.taboobot.utilites;

import java.util.Collection;
import java.util.function.Function;

public class CollectionUtils {

    public static <T, V> boolean contains(Collection<T> collection, V val, Function<T, V> function) {
        return collection.stream().anyMatch((itm) -> function.apply(itm).equals(val));
    }
}
