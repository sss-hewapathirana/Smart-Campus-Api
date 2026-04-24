package com.smartcampus;

import com.smartcampus.store.DataStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataStoreTest {

    @Test
    public void testSingletonInstance() {
        DataStore store1 = DataStore.getInstance();
        DataStore store2 = DataStore.getInstance();
        assertSame(store1, store2, "DataStore should be a singleton");
    }
}
