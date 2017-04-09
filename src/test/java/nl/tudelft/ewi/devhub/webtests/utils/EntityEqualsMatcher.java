package nl.tudelft.ewi.devhub.webtests.utils;

import com.mysema.util.ReflectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hibernate.Hibernate;

import java.lang.reflect.Field;

/**
 * JPA Entities have a low weight {@link Object#equals(Object)} method based on their
 * {@link javax.persistence.Id} value. For tests it is however sometimes useful to be
 * able to determine wether two value objects are completely equal. This matcher checks
 * if both the actual and expected object share the same type and whether their field
 * values are equal.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RequiredArgsConstructor
public class EntityEqualsMatcher<T> extends TypeSafeMatcher<T> {

    private final T expectedValue;

    @Override
    @SneakyThrows
    protected boolean matchesSafely(T actualValue) {
        if (expectedValue == actualValue) {
            return true;
        }

        Class<?> clasz = Hibernate.getClass(expectedValue);
        Class<?> otherClasz = Hibernate.getClass(actualValue);

        if (!clasz.equals(otherClasz)) {
            return false;
        }

        for (Field field : ReflectionUtils.getFields(clasz)) {
            field.setAccessible(true);
            Object expectedFieldValue = field.get(expectedValue);
            Object actualFieldValue = field.get(actualValue);

            if (expectedFieldValue == null) {
                if (actualFieldValue != null) {
                    return false;
                }
            }
            else if (!expectedFieldValue.equals(actualFieldValue)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Expected entity ").appendValue(expectedValue);
    }

    @Override
    @SneakyThrows
    protected void describeMismatchSafely(T actualValue, Description mismatchDescription) {
        super.describeMismatchSafely(actualValue, mismatchDescription);

        Class<?> clasz = Hibernate.getClass(expectedValue);
        Class<?> otherClasz = Hibernate.getClass(actualValue);

        if (clasz.equals(otherClasz)) {
            for (Field field : ReflectionUtils.getFields(clasz)) {
                field.setAccessible(true);
                Object expectedFieldValue = field.get(expectedValue);
                Object actualFieldValue = field.get(actualValue);

                if (expectedFieldValue == null) {
                    if (actualFieldValue != null) {
                        mismatchDescription.appendText("Field ")
                            .appendText(field.getName())
                            .appendText(" was expected to be null, but was ")
                            .appendValue(actualFieldValue);
                    }
                }
                else if (!expectedFieldValue.equals(actualFieldValue)) {
                    mismatchDescription.appendText("Field ")
                        .appendText(field.getName())
                        .appendText(" was expected to be ")
                        .appendValue(expectedFieldValue)
                        .appendText(" but was ")
                        .appendValue(actualFieldValue);
                }
            }
        }
    }

    /**
     * JPA Entities have a low weight {@link Object#equals(Object)} method based on their
     * {@link javax.persistence.Id} value. For tests it is however sometimes useful to be
     * able to determine wether two value objects are completely equal. This matcher checks
     * if both the actual and expected object share the same type and whether their field
     * values are equal.
     *
     * @param expectedEntity Expected entity.
     * @param <T> Entity Type.
     * @return A matcher to check for.
     */
    public static <T> EntityEqualsMatcher<T> isEntity(T expectedEntity) {
        return new EntityEqualsMatcher<>(expectedEntity);
    }

}
