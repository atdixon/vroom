/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.vroom;

import com.github.atdixon.vroom.TypeReference;
import com.github.atdixon.vroom.V;
import com.github.atdixon.vroom.VMap;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test
public class SimpleVMapTest {

    @SuppressWarnings("unchecked")
    public void testSimple() {
        VMap e = VMap.create(ImmutableMap.<String, Object>builder()
            .put("name", "my entity")
            .put("age", Integer.MAX_VALUE)
            .put("ttl", Long.MIN_VALUE)
            .put("creator", getClass().getSimpleName())
            .put("details.key1", "val1")
            .put("details", asList(ImmutableMap.of(
                "key2", "val2",
                "key3", asList("val3"),
                "key4", asList("val4a", "val4b"),
                "key5", asList(),
                "key.6", asList(
                    ImmutableMap.of("key.7", "val7"),
                    ImmutableMap.of("key.8", "val8"))))).build());

        assertTrue(e.knows("age", int.class));
        assertFalse(e.knows("ttl", boolean.class)); // not as boolean

        assertEquals(e.one("age", Integer.class), (Integer) Integer.MAX_VALUE);
        assertEquals(e.one("age", Integer.class), (Integer) Integer.MAX_VALUE);
        assertEquals(e.one("age", Integer.class, (Integer) null), (Integer) Integer.MAX_VALUE);
        assertEquals(e.one("age", new TypeReference<Optional<Integer>>() {})
            .orElse(null), (Integer) Integer.MAX_VALUE);
        assertTrue(e.one("age", int.class, 0) == Integer.MAX_VALUE);

        assertEquals(e.one("ttl", long.class), (Long) Long.MIN_VALUE);
        assertEquals((long) e.one("ttl", long.class), Long.MIN_VALUE);
        assertEquals((long) e.one("ttl", long.class, (Long) null), Long.MIN_VALUE);
        assertEquals((long) e.one("ttl", new TypeReference<Optional<Long>>() {})
            .orElse(null), Long.MIN_VALUE);
        assertTrue(e.one("ttl", long.class, 0L) == Long.MIN_VALUE);

        assertEquals(e.one("no-such-key", new TypeReference<Optional<Integer>>() {}).orElse(null), null);

        assertEquals(e.one("details.key1", String.class), "val1");
        assertEquals(e.one("details.key2", String.class), "val2");
        assertEquals(e.one("details.key3", String.class), "val3");
        assertEquals(e.one("details.key4", String.class), "val4a");
        assertEquals(e.one("details.key5", String.class, (String) null), null);
        assertEquals(e.one("details.key.6.key.7", String.class), "val7");

        assertNull(e.one("details.missing.rating", int.class, (Integer) null));
        try {
            assertEquals((int) e.one("details.missing.rating", int.class), 4);
            fail();
        } catch (Exception ex) { /* ok */ }

        try {
            e.one("isMissing", boolean.class);
            fail();
        } catch (Exception ex) { /* ok */ }
    }

    public void testPropertyAccess() {
        Map<String, Object> map = ImmutableMap.<String, Object>of(
            "foo", ImmutableMap.of(
                "bar.cat", asList(ImmutableMap.of("dog", "bark"))));

        assertEquals(V.one(V.get(map, "foo.bar.cat.dog"), String.class), "bark");
    }

    public void testArraySupport() {
        Map<String, Object> map = ImmutableMap.<String, Object>of(
            "foo", ImmutableMap.of(
                "bar.cat", new String[] { "1", "2", "3" }));
        assertEquals(V.one(V.get(map, "foo.bar.cat"), String.class), "1");
        assertEquals(V.many(V.get(map, "foo.bar.cat"), int.class), asList(1, 2, 3));
    }

}
