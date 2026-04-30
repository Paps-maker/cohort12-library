package app.util;

import jakarta.persistence.Entity;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityScanner {

    /**
     * Scans the package for classes annotated with @Entity.
     * This ensures only intended database models are processed.
     */
    public static Class<?>[] getEntities(String pkg) {
        try {
            // Initialize reflections to scan for Type Annotations
            Reflections reflections = new Reflections(pkg, Scanners.TypesAnnotated);

            // 🎯 Only get classes marked with @jakarta.persistence.Entity
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Entity.class);

            // Filter to ensure we only get classes specifically in the requested package
            Set<Class<?>> entities = annotatedClasses.stream()
                    .filter(c -> c.getPackageName().equals(pkg))
                    .collect(Collectors.toSet());

            System.out.println("🔍 [SCANNER] Detected " + entities.size() + " annotated entities: "
                    + entities.stream().map(Class::getSimpleName).collect(Collectors.toList()));

            return entities.toArray(new Class<?>[0]);

        } catch (Exception e) {
            System.err.println("❌ [SCANNER] Error scanning package " + pkg + " for @Entity notations");
            e.printStackTrace();
            return new Class<?>[0];
        }
    }
}