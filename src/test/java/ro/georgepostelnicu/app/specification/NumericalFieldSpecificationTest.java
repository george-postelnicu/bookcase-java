package ro.georgepostelnicu.app.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NumericalFieldSpecificationTest {

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
    }

    @Test
    void toPredicate_usesBetween_whenBothBoundsProvided() {
        var spec = NumericalFieldSpecification.<Object>buildNumericalSpecification(10, 20, r -> fieldExpression);
        Predicate expected = mock(Predicate.class);
        when(cb.between(eq(fieldExpression), eq("10"), eq("20"))).thenReturn(expected);

        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertSame(expected, p);
        verify(cb, times(1)).between(eq(fieldExpression), eq("10"), eq("20"));
        verify(cb, never()).greaterThanOrEqualTo(any(Expression.class), any(String.class));
        verify(cb, never()).lessThanOrEqualTo(any(Expression.class), any(String.class));
        verify(cb, never()).conjunction();
    }

    @Test
    void toPredicate_usesGte_whenOnlyMinProvided() {
        var spec = NumericalFieldSpecification.<Object>buildNumericalSpecification(5, null, r -> fieldExpression);
        Predicate expected = mock(Predicate.class);
        when(cb.greaterThanOrEqualTo(eq(fieldExpression), eq("5"))).thenReturn(expected);

        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertSame(expected, p);
        verify(cb, times(1)).greaterThanOrEqualTo(eq(fieldExpression), eq("5"));
        verify(cb, never()).between(any(Expression.class), any(String.class), any(String.class));
        verify(cb, never()).lessThanOrEqualTo(any(Expression.class), any(String.class));
        verify(cb, never()).conjunction();
    }

    @Test
    void toPredicate_usesLte_whenOnlyMaxProvided() {
        var spec = NumericalFieldSpecification.<Object>buildNumericalSpecification(null, 42, r -> fieldExpression);
        Predicate expected = mock(Predicate.class);
        when(cb.lessThanOrEqualTo(eq(fieldExpression), eq("42"))).thenReturn(expected);

        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertSame(expected, p);
        verify(cb, times(1)).lessThanOrEqualTo(eq(fieldExpression), eq("42"));
        verify(cb, never()).between(any(Expression.class), any(String.class), any(String.class));
        verify(cb, never()).greaterThanOrEqualTo(any(Expression.class), any(String.class));
        verify(cb, never()).conjunction();
    }

    @Test
    void toPredicate_returnsConjunction_whenNoBoundsProvided() {
        var spec = NumericalFieldSpecification.<Object>buildNumericalSpecification(null, null, r -> fieldExpression);
        Predicate expected = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(expected);

        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertSame(expected, p);
        verify(cb, times(1)).conjunction();
        verify(cb, never()).between(any(Expression.class), any(String.class), any(String.class));
        verify(cb, never()).greaterThanOrEqualTo(any(Expression.class), any(String.class));
        verify(cb, never()).lessThanOrEqualTo(any(Expression.class), any(String.class));
    }
}
