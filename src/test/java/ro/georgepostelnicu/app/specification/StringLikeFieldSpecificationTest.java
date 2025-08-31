package ro.georgepostelnicu.app.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StringLikeFieldSpecificationTest {

    private CriteriaBuilder cb;
    private CriteriaQuery<?> cq;
    private Root<Object> root;
    private Expression<String> fieldExpression;

    @BeforeEach
    void setUp() {
        cb = mock(CriteriaBuilder.class);
        cq = mock(CriteriaQuery.class);
        root = mock(Root.class);
        fieldExpression = mock(Expression.class);

        // Default stubs for methods used
        when(cb.conjunction()).thenReturn(mock(Predicate.class));
        when(cb.lower(any())).thenReturn(fieldExpression);
        when(cb.like(any(Expression.class), any(String.class))).thenReturn(mock(Predicate.class));
        when(cb.equal(any(Expression.class), any())).thenReturn(mock(Predicate.class));
    }

    private <T> StringLikeFieldSpecification<T> specWithField(Function<Root<T>, Expression<String>> provider, Object value) {
        return StringLikeFieldSpecification.buildSpecification(value, provider);
    }

    @Test
    void toPredicate_returnsConjunction_whenValueIsBlank() {
        var spec = specWithField(r -> (Expression<String>) fieldExpression, "   ");
        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertNotNull(p);
        verify(cb, times(1)).conjunction();
        verify(cb, never()).like(any(Expression.class), any(String.class));
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void toPredicate_returnsConjunction_whenOnlyWildcard() {
        var spec = specWithField(r -> (Expression<String>) fieldExpression, "*");
        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertNotNull(p);
        verify(cb, times(1)).conjunction();
        verify(cb, never()).like(any(Expression.class), any(String.class));
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void toPredicate_returnsConjunction_whenWildcardTooShort() {
        var spec = specWithField(r -> (Expression<String>) fieldExpression, "ZZ*"); // length < 4
        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertNotNull(p);
        verify(cb, times(1)).conjunction();
        verify(cb, never()).like(any(Expression.class), any(String.class));
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void toPredicate_returnsConjunction_whenMoreThanTwoWildcards() {
        var spec = specWithField(r -> (Expression<String>) fieldExpression, "*A*B*C*");
        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertNotNull(p);
        verify(cb, times(1)).conjunction();
        verify(cb, never()).like(any(Expression.class), any(String.class));
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void toPredicate_usesEqual_whenNoWildcardPresent() {
        var spec = specWithField(r -> (Expression<String>) fieldExpression, "Exactly");
        // Prepare a specific predicate to be returned by equal
        Predicate expected = mock(Predicate.class);
        when(cb.equal(eq(fieldExpression), eq("exactly"))).thenReturn(expected);

        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertSame(expected, p);

        // ensure equal(lower(expr), valueLowercased)
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(cb, times(1)).equal(eq(fieldExpression), captor.capture());
        assertEquals("exactly", captor.getValue());

        verify(cb, never()).like(any(Expression.class), any(String.class));
        verify(cb, times(1)).lower(any());
    }

    @Test
    void toPredicate_usesLike_whenWildcardPresent() {
        var spec = specWithField(r -> (Expression<String>) fieldExpression, "A*BC"); // becomes a%bc
        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertNotNull(p);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(cb, times(1)).like(eq(fieldExpression), captor.capture());
        assertEquals("a%bc", captor.getValue());

        verify(cb, never()).equal(any(), any());
        verify(cb, times(1)).lower(any());
    }
}
