package ro.georgepostelnicu.app.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CollectionFieldSpecificationTest {

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
    void toPredicate_returnsConjunction_whenNamesIsNull() {
        var spec = CollectionFieldSpecification.<Object>buildCollectionsSpecification(null, r -> fieldExpression);
        Predicate expected = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(expected);

        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertSame(expected, p);
        verify(cb, times(1)).conjunction();
        verify(cb, never()).and(any());
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void toPredicate_returnsConjunction_whenNamesIsEmpty() {
        var spec = CollectionFieldSpecification.<Object>buildCollectionsSpecification(Collections.emptySet(), r -> fieldExpression);
        Predicate expected = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(expected);

        Predicate p = spec.toPredicate((Root) root, cq, cb);
        assertSame(expected, p);
        verify(cb, times(1)).conjunction();
        verify(cb, never()).and(any());
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void toPredicate_buildsAndOfEquals_forEachProvidedName() {
        Set<String> names = new LinkedHashSet<>();
        names.add("Alice");
        names.add("Bob");

        var spec = CollectionFieldSpecification.<Object>buildCollectionsSpecification(names, r -> fieldExpression);

        Predicate pAlice = mock(Predicate.class);
        Predicate pBob = mock(Predicate.class);
        when(cb.equal(eq(fieldExpression), eq("Alice"))).thenReturn(pAlice);
        when(cb.equal(eq(fieldExpression), eq("Bob"))).thenReturn(pBob);

        Predicate combined = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(combined);

        Predicate result = spec.toPredicate((Root) root, cq, cb);
        assertSame(combined, result);

        // verify equals called for each name
        verify(cb, times(1)).equal(eq(fieldExpression), eq("Alice"));
        verify(cb, times(1)).equal(eq(fieldExpression), eq("Bob"));
        // verify and called once with both predicates
        verify(cb, times(1)).and(any(Predicate[].class));
        verify(cb, never()).conjunction();
    }
}
