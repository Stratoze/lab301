package com.laptrinhjavaweb.orm.transaction;

public interface Transaction {
    void commit();

    void rollback();
}
