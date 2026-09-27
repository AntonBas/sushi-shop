package com.sushishop.architecture;

import com.sushishop.shared.web.SortableFields;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

class SortableFieldsTest {

    private static final ArchCondition<JavaMethod> ANNOTATE_SORTABLE_PARAMETERS =
            new ArchCondition<>("annotate every Pageable/Sort parameter with @SortableFields") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    method.getParameters().stream()
                            .filter(parameter -> parameter.getRawType().isAssignableTo(Pageable.class)
                                    || parameter.getRawType().isAssignableTo(Sort.class))
                            .filter(parameter -> !parameter.isAnnotatedWith(SortableFields.class))
                            .forEach(parameter -> events.add(SimpleConditionEvent.violated(method,
                                    method.getFullName() + " accepts client sort without @SortableFields")));
                }
            };

    @Test
    void controllerSortParametersShouldDeclareAllowedFields() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.sushishop");

        methods().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                .should(ANNOTATE_SORTABLE_PARAMETERS)
                .check(classes);
    }
}
