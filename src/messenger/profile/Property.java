package messenger.profile;

public class Property<T> {
	private Scope propertyScope = StandardScope.Public;
	private final String name;
	private T value;
	private boolean invalid = false;

	public Property(Scope propertyScope, String name, T value) {
		super();

		if (value == null) {
			invalid = true;
		}

		this.propertyScope = propertyScope;
		this.name = name;
		this.value = value;
	}

	public Property(String name) {
		super();
		this.name = name;
	}

	public Property(String name, T value) {
		super();

		if (value == null) {
			invalid = true;
		}

		this.name = name;
		this.value = value;
	}

	/**
	 * @return the name of the property
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the scope of this property
	 */
	public Scope getPropertyScope() {
		return propertyScope;
	}

	/**
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	public boolean isInvalid() {
		return invalid;
	}

	/**
	 * @param propertyScope the new scope of this property to set
	 */
	public void setPropertyScope(Scope propertyScope) {
		if (invalid) {
			throw new IllegalStateException("Cannot set scope of an invalid value");
		}
		this.propertyScope = propertyScope;
	}

	/**
	 * @param value the new value
	 */
	public void setValue(T value) {
		if (invalid) {
			throw new IllegalStateException("Cannot set scope of an invalid value");
		}
		this.value = value;
	}

}
