package app.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class GenericDao<T, ID> {

    @PersistenceContext(unitName = "TrainingAppPU")
    private EntityManager em;

    private Class<T> entityClass;

    // Default constructor for CDI
    public GenericDao() {}

    /**
     * Enhanced type resolution to handle Proxy classes and avoid ClassCastException.
     */
    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        if (entityClass == null) {
            Type type = getClass().getGenericSuperclass();

            // Handle cases where the class is wrapped by a proxy (common in WildFly/JBoss)
            while (!(type instanceof ParameterizedType)) {
                if (type instanceof Class) {
                    type = ((Class<?>) type).getGenericSuperclass();
                } else {
                    throw new RuntimeException("Could not resolve entity type for " + getClass().getName());
                }
            }

            ParameterizedType paramType = (ParameterizedType) type;
            this.entityClass = (Class<T>) paramType.getActualTypeArguments()[0];
        }
        return entityClass;
    }

    public void save(T entity) {
        // merge is safer for JTA as it handles both new and existing entities
        em.merge(entity);
    }

    public T findById(ID id) {
        return em.find(getType(), id);
    }

    public List<T> findAll() {
        return em.createQuery("SELECT e FROM "
                + getType().getSimpleName() + " e", getType()).getResultList();
    }

    public void delete(ID id) {
        T entity = findById(id);
        if (entity != null) {
            // Ensure entity is managed before removal
            em.remove(em.contains(entity) ? entity : em.merge(entity));
        }
    }

    public EntityManager getEm() {
        return em;
    }
}