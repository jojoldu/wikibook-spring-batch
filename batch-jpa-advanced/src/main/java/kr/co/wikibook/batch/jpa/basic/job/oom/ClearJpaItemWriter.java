package kr.co.wikibook.batch.jpa.basic.job.oom;

import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

public class ClearJpaItemWriter<T> extends JpaItemWriter<T> {

    private EntityManagerFactory entityManagerFactory;

    private boolean clearPersistenceContext = true;

    public void setClearPersistenceContext(boolean clearPersistenceContext) {
        this.clearPersistenceContext = clearPersistenceContext;
    }

    @Override
    public void write(List<? extends T> items) {
        super.write(items);
        EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);

        if (entityManager == null) {
            throw new DataAccessResourceFailureException("Unable to obtain a transactional EntityManager");
        }

        entityManager.flush();

        if (clearPersistenceContext) {
            entityManager.clear();
        }
    }
}
