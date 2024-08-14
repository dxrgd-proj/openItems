package org.aslstd.api.item.meta.persistent;

import org.aslstd.api.item.meta.persistent.ListPersistentDataType.ListPersistentDataTypeImpl;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public abstract class MetaPersistentDataType<P,C> implements PersistentDataType<P,C> {

	public static final ListPersistentDataType<String,String> STRING_LIST = new ListPersistentDataTypeImpl<>(PersistentDataType.STRING);
	public static final MetaPersistentDataType<Byte, Boolean> BOOLEAN = new MetaPersistentDataType<Byte,Boolean>(Byte.class, Boolean.class) {

		@NotNull
		@Override
		public Byte toPrimitive(@NotNull Boolean complex, @NotNull PersistentDataAdapterContext context) {
			return (byte) (complex ? 1 : 0);
		}

		@NotNull
		@Override
		public Boolean fromPrimitive(@NotNull Byte primitive, @NotNull PersistentDataAdapterContext context) {
			return primitive != 0;
		}

	};

	private Class<P> primitive;
	private Class<C> complex;

	private MetaPersistentDataType(Class<P> primitive, Class<C> complex) {
		this.primitive = primitive;
		this.complex = complex;
	}

	@Override
	public @NotNull Class<P> getPrimitiveType() { return primitive; }

	@Override
	public @NotNull Class<C> getComplexType() { return complex; }

}
