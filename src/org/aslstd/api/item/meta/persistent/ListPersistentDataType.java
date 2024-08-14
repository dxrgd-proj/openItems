package org.aslstd.api.item.meta.persistent;

import java.util.List;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;

/**
 * Paper implementation from newer versions
 */
interface ListPersistentDataType<P, C> extends PersistentDataType<List<P>, List<C>> {

	@NotNull
	PersistentDataType<P, C> elementType();

	static class ListPersistentDataTypeImpl<P, C> implements ListPersistentDataType<P, C> {

		@NotNull
		private final PersistentDataType<P, C> innerType;

		ListPersistentDataTypeImpl(@NotNull final PersistentDataType<P, C> innerType) {
			this.innerType = innerType;
		}

		@NotNull
		@Override
		@SuppressWarnings("unchecked")
		public Class<List<P>> getPrimitiveType() {
			return (Class<List<P>>) (Object) List.class;
		}

		@NotNull
		@Override
		@SuppressWarnings("unchecked")
		public Class<List<C>> getComplexType() {
			return (Class<List<C>>) (Object) List.class;
		}

		@NotNull
		@Override
		public List<P> toPrimitive(@NotNull final List<C> complex, @NotNull final PersistentDataAdapterContext context) {
			return Lists.transform(complex, s -> innerType.toPrimitive(s, context));
		}

		@NotNull
		@Override
		public List<C> fromPrimitive(@NotNull final List<P> primitive, @NotNull final PersistentDataAdapterContext context) {
			return Lists.transform(primitive, s -> innerType.fromPrimitive(s, context));
		}

		@NotNull
		@Override
		public PersistentDataType<P, C> elementType() {
			return innerType;
		}
	}

}
