# Tips & Tricks

- **Avoid Removing Public Methods**: Customers may configure the plugin programmatically. Removing a public method is a breaking change and can cause issues. Instead, deprecate the method and create an overload if necessary.

- **Use `readResolve` for Defaults**: Utilize the `readResolve` method to handle meaningful defaults when deserializing objects.

- **Be Mindful of Object Creation**: When saving or applying configuration (e.g., clicking "Save" in the UI), Jenkins creates a new object instead of updating the existing one. This can cause issues if not considered. For example, in the UI for [OrkaComputer](../src/main/resources/io/jenkins/plugins/orka/OrkaComputer/configure.jelly), hidden entries are used to pass data to the new object without losing existing data.
