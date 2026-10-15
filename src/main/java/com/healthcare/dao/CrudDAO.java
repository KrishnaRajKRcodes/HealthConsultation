package com.healthcare.dao;

import com.healthcare.util.DAOException;
import java.util.List;

/** Generic CRUD contract implemented by the DAOs (interface + generics). */
public interface CrudDAO<T> {
    int insert(T entity) throws DAOException;
    T findById(int id) throws DAOException;
    List<T> findAll() throws DAOException;
    boolean update(T entity) throws DAOException;
    boolean delete(int id) throws DAOException;
}
