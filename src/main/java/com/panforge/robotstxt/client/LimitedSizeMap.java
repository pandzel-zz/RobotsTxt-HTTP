/*
 * Copyright 2018 Piotr Andzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.panforge.robotstxt.client;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Limited size map.
 */
class LimitedSizeMap<TI,TD> extends AbstractMap<TI,TD> {
  private final HashMap<TI,TD> data = new HashMap<>();
  private int maxSize;
  private final Predicate<TD> predicate;
  private final Comparator<TD> comparator;
  private final UnaryOperator<TD> finalizer;

  public LimitedSizeMap(int maxSize, Predicate<TD> predicate, Comparator<TD> comparator, UnaryOperator<TD> finalizer) {
    this.maxSize = maxSize;
    this.predicate = predicate;
    this.comparator = comparator;
    this.finalizer = finalizer;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public synchronized void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
    resize();
  }

  @Override
  public Set<Entry<TI, TD>> entrySet() {
    return data.entrySet();
  }

  @Override
  public synchronized TD put(TI key, TD value) {
    resize();
    return data.put(key, value);
  }

  @Override
  public synchronized TD get(Object key) {
    return data.get(key);
  }
  
  private void resize() {
    if (size() >= maxSize) {
      List<Map.Entry<TI, TD>> toDelete = entrySet().stream()
              .filter(e -> predicate.test(e.getValue()))
              .sorted((e1, e2) -> comparator.compare(e1.getValue(), e2.getValue()))
              .limit(Math.round(maxSize * 0.1))
              .collect(Collectors.toList());
      toDelete.forEach(e -> remove(e.getKey()));
      values().forEach(e -> finalizer.apply(e));
    }
  }
}
