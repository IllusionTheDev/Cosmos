package me.illusion.cosmos.utilities.text;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
/*
 * placeholder -> namespace (%player_name% -> player)
 * replacement -> function (player -> "illusion")
 * expensiveLookup: recursive-like, it allows you to have placeholders returning other placeholders
 * cacheValue: if true, the value will be cached once and never updated
 * cachedValue: the cached value
 */
public class Placeholder<T> {

    private final Function<T, String> replacement;
    private String placeholder;
    @Setter
    private boolean expensiveLookup = false;
    @Setter
    private boolean cacheValue = false;

    private String cachedValue;

    private char openChar = '%';
    private char closeChar = '%';

    public Placeholder(String placeholder, Function<T, String> replacement) {
        this.placeholder = placeholder;
        this.replacement = replacement;
    }

    public Placeholder(String placeholder, String replacement) {
        this(placeholder, (object) -> replacement);
        this.cachedValue = replacement;
    }

    public Placeholder(String placeholder, CompletableFuture<String> replacement) {
        AtomicReference<String> ref = new AtomicReference<>("Loading...");

        replacement.thenAccept(ref::set);

        this.placeholder = placeholder;
        this.replacement = (object) -> ref.get();
    }

    public Placeholder(String placeholder, Supplier<String> replacement) {
        this(placeholder, (object) -> replacement.get());
    }

    public static List<Placeholder<Player>> asPlaceholderList(Map<String, Object> map) {
        List<Placeholder<Player>> list = new ArrayList<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();

            if (value instanceof CompletableFuture<?> future) {
                list.add(new Placeholder<>(key, future.thenApply(Object::toString)));
                continue;
            }

            if (value instanceof Supplier<?> supplier) {
                list.add(new Placeholder<>(key, supplier.get().toString()));
                continue;
            }

            list.add(new Placeholder<>(key, (__) -> map.get(key).toString()));
        }

        return list;
    }


    public String replace(String text, T object) {
        if (text == null)
            return null;

        if (!placeholder.startsWith(openChar + "")) {
            placeholder = openChar + placeholder;
        }

        if (!placeholder.endsWith(closeChar + "")) {
            placeholder = placeholder + closeChar;
        }

        if (cacheValue && cachedValue != null) {
            return text.replace(placeholder, cachedValue);
        }

        if (expensiveLookup) {
            while (text.contains(placeholder)) {
                text = text.replace(placeholder, replace(object));
            }

            tryCache(text);

            return text;
        }

        String value = replace(object);

        tryCache(value);

        return text.replace(placeholder, value);
    }

    private String replace(T object) {
        return replacement.apply(object);
    }

    private void tryCache(String value) {
        if (cacheValue) {
            cachedValue = value;
        }
    }

    @Override
    public int hashCode() {
        return placeholder.hashCode();
    }

    public void setOpenChar(char openChar) {
        this.openChar = openChar;
    }

    public void setCloseChar(char closeChar) {
        this.closeChar = closeChar;
    }
}
