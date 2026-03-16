package org.openmarkov.java.initialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Lazy<T> {
    private boolean isInitialized;
    private @NotNull Supplier<? extends T> initializer;
    private @Nullable T value;
    
    private Lazy(@NotNull Supplier<? extends T> initializer) {
        this.initializer = initializer;
        this.isInitialized = false;
        this.value = null;
    }
    
    public static <T> Lazy<T> of(@NotNull Supplier<? extends T> initializer) {
        return new Lazy<>(initializer);
    }
    
    public @NotNull T get() {
        synchronized (this) {
            if (!this.isInitialized) {
                this.value = this.initializer.get();
                this.isInitialized = true;
            }
            return this.value;
        }
    }
    
    public void reset() {
        synchronized (this) {
            this.isInitialized = false;
            this.value = null;
        }
    }
    
}
