package org.openmarkov.java.cloneUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnreacheableException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class CloneUtils {
    
    public static <ToClone extends Cloneable> ToClone safeClone(ToClone toClone) {
        if (toClone == null) {
            return null;
        }
        try {
            Class<? extends ToClone> cloneableClass = (Class<? extends ToClone>) toClone.getClass();
            var specialHandleCase = CloneUtils.SPECIAL_HANDLE_CASES
                    .stream()
                    .filter(specificCase -> specificCase.isApplicableFor(toClone))
                    .findFirst();
            if (specialHandleCase.isPresent()) {
                return (ToClone) specialHandleCase.get().uncheckedApply(toClone);
            }
            var cloneMethod = cloneableClass.getMethod("clone");
            ToClone cloned = cloneableClass.cast(cloneMethod.invoke(toClone));
            if (cloned == toClone) {
                throw new UnreacheableException("The clone method of " + cloneableClass.getName() + " is badly implemented, as it returns the original object");
            }
            return cloned;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw new UnreacheableException(e);
        }
    }
    
    private static final Collection<SpecificCase<?>> SPECIAL_HANDLE_CASES = new ArrayList<>();
    
    static {
        CloneUtils.addSpecialClase(
                Map.class, (mapClass, instance) -> {
                    try {
                        mapClass.getDeclaredConstructor();
                        return true;
                    } catch (NoSuchMethodException e) {
                        return false;
                    }
                },
                map -> {
                    Map newMap;
                    try {
                        newMap = map.getClass().getDeclaredConstructor().newInstance();
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                             InvocationTargetException e) {
                        throw new UnreacheableException(e);
                    }
                    map.forEach((key, value) -> {
                        if (key instanceof Cloneable) {
                            key = CloneUtils.safeClone((Cloneable) key);
                        }
                        if (value instanceof Cloneable) {
                            value = CloneUtils.safeClone((Cloneable) value);
                        }
                        newMap.put(key, value);
                    });
                    return newMap;
                });
        
        CloneUtils.addSpecialClase(
                Collection.class, (collectionClass, instance) -> {
                    try {
                        collectionClass.getDeclaredConstructor();
                        return true;
                    } catch (NoSuchMethodException e) {
                        return false;
                    }
                },
                collection -> {
                    Collection newCollection;
                    try {
                        newCollection = collection.getClass().getDeclaredConstructor().newInstance();
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                             InvocationTargetException e) {
                        throw new UnreacheableException(e);
                    }
                    collection.forEach(value -> {
                        if (value instanceof Cloneable) {
                            value = CloneUtils.safeClone((Cloneable) value);
                        }
                        newCollection.add(value);
                    });
                    return newCollection;
                });
    }
    
    private static <SpecialClass> void addSpecialClase(
            @NotNull Class<SpecialClass> theClass,
            @Nullable BiPredicate<Class<SpecialClass>, SpecialClass> filter,
            @NotNull Function<SpecialClass, SpecialClass> handleCase) {
        CloneUtils.SPECIAL_HANDLE_CASES.add(new SpecificCase<>(theClass, filter, handleCase));
    }
    
    static class SpecificCase<T> {
        final Class<T> theClass;
        final BiPredicate<Class<T>, T> filter;
        final Function<T, T> handleCase;
        
        public SpecificCase(Class<T> theClass, BiPredicate<Class<T>, T> filter, Function<T, T> handleCase) {
            this.theClass = theClass;
            this.filter = filter;
            this.handleCase = handleCase;
        }
        
        public boolean isApplicableFor(Object object) {
            if (object == null || !this.theClass.isInstance(object)) {
                return false;
            }
            T specificObject = this.theClass.cast(object);
            Class<T> specificClass = (Class<T>) specificObject.getClass();
            return this.filter.test(specificClass, specificObject);
        }
        
        public T uncheckedApply(Object object) {
            return this.handleCase.apply(this.theClass.cast(object));
        }
    }
    
}
