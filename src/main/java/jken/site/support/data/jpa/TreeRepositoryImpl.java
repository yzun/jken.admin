/*
 * Copyright (c) 2019.
 * @Link: http://jken.site
 * @Author: ken kong
 * @LastModified: 2019-12-21T21:19:36.170+08:00
 *
 */

package jken.site.support.data.jpa;

import jken.site.support.data.CorpDetection;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

public abstract class TreeRepositoryImpl<T extends TreeEntity<T, U, I>, U, I extends Serializable> extends EntityRepositoryImpl<T, I> implements TreeRepository<T, U, I> {

    public TreeRepositoryImpl(CorpDetection corpDetection, JpaEntityInformation<T, I> entityInformation, EntityManager entityManager) {
        super(corpDetection, entityInformation, entityManager);
    }

    @Override
    public List<T> findRoots() {
        return getQuery((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parent")), (Sort) null).getResultList();
    }

    @Override
    public List<T> findAllChildren(T parent) {
        return getQuery((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("treePath"), parent.getTreePath() + parent.getId() + "%"), (Sort) null).getResultList();
    }
}