package org.eigenbase.reltype;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.hydromatic.linq4j.function.Function1;
import org.eigenbase.util.IdentityHashSet;

import java.util.*;

/** Handles cyclic types. */
public class RelDataTypeResolver {
  private static final ThreadLocal<RelDataTypeResolver> THREAD_RESOLVERS =
      new ThreadLocal<RelDataTypeResolver>();

  private final IdentityHashSet<Object> active = new IdentityHashSet<Object>();

  private final Map<Object, RelDataType> map =
      new HashMap<Object, RelDataType>();
  private int resolveCount;

  public RelDataType unresolved(Object key) {
    final UnresolvedType
        unresolvedType = new UnresolvedType(key);
    map.put(key, unresolvedType);
    return unresolvedType;
  }

  public RelDataType resolve(RelDataType type) {
    if (type instanceof UnresolvedType) {
      UnresolvedType
          unresolvedType = (UnresolvedType) type;
      final RelDataType type2 = map.get(unresolvedType.key);
      if (type2 != null) {
        return type2;
      }
    }
    return type;
  }

  static List<? extends RelDataTypeField> resolveList(
      List<? extends RelDataTypeField> fields) {
    final RelDataTypeResolver resolver = THREAD_RESOLVERS.get();
    if (resolver == null) {
      return fields;
    }
    return Lists.transform(
        fields, new Function<RelDataTypeField, RelDataTypeField>() {
          public RelDataTypeField apply(RelDataTypeField field) {
            return new RelDataTypeFieldImpl(
                field.getName(),
                field.getIndex(),
                resolver.resolve(field.getType()));
          }
        }
    );
  }

  public boolean enter(Object key) {
    return active.add(key);
  }

  public boolean leave(Object key) {
    return active.remove(key);
  }

  /** Calls a given action with a thread-local instance of {@link RelDataTypeResolver},
   * creating a new one if necessary.
   *
   * @param function Action
   * @param <T> Return type
   * @return Result of applying action
   */
  public static <T> T withThreadInstance(Function1<RelDataTypeResolver, T> function) {
    final RelDataTypeResolver resolver = THREAD_RESOLVERS.get();
    if (resolver == null) {
      THREAD_RESOLVERS.set(new RelDataTypeResolver());
      try {
        return withThreadInstance(function);
      } finally {
        THREAD_RESOLVERS.remove();
      }
    } else {
      return function.apply(resolver);
    }
  }

  public void setResolution(Class type, RelDataType result) {
    if (map.get(type) instanceof UnresolvedType) {
      map.put(type, result);
      ++resolveCount;
    }
  }

  public int getResolveCount() {
    return resolveCount;
  }

  /** Created temporarily during type-resolution when we hit a cycle. */
  public static class UnresolvedType extends RelDataTypeImpl {
    private final Object key;

    private UnresolvedType(Object key) {
      this.key = key;
      computeDigest();
    }

    protected void generateTypeString(StringBuilder sb, boolean withDetail) {
      sb.append("Unresolved(").append(key).append(")");
    }
  }
}
