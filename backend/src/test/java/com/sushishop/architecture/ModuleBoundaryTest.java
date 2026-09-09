package com.sushishop.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ModuleBoundaryTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.sushishop");
    }

    @Test
    void topLevelPackagesShouldBeFreeOfCycles() {
        // product <-> promotion and product <-> review are genuine, actively used bidirectional
        // JPA associations (Product.promotions / Product.reviews are read in real business logic),
        // not accidental coupling - accepted exceptions rather than erosion to prevent.
        ArchRule rule = SlicesRuleDefinition.slices()
                .matching("com.sushishop.(*)..")
                .should().beFreeOfCycles()
                .ignoreDependency(resideInAPackage("com.sushishop.product.."), resideInAPackage("com.sushishop.promotion.."))
                .ignoreDependency(resideInAPackage("com.sushishop.promotion.."), resideInAPackage("com.sushishop.product.."))
                .ignoreDependency(resideInAPackage("com.sushishop.product.."), resideInAPackage("com.sushishop.review.."))
                .ignoreDependency(resideInAPackage("com.sushishop.review.."), resideInAPackage("com.sushishop.product.."));

        rule.check(classes);
    }

    @Test
    void controllersShouldNotBeUsedAsDependencies() {
        ArchRule rule = noClasses()
                .that().haveSimpleNameNotEndingWith("Controller")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Controller");

        rule.check(classes);
    }

    @Test
    void repositoriesShouldNotDependOnServicesOrControllers() {
        ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("Repository")
                .should().dependOnClassesThat()
                .haveSimpleNameEndingWith("Service")
                .orShould().dependOnClassesThat().haveSimpleNameEndingWith("Controller");

        rule.check(classes);
    }
}
